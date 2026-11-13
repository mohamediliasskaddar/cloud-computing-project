#!/usr/bin/env python3
"""SLA availability monitor for OpenStack instances (openstacksdk + Keystone)."""
import argparse, csv, os, signal, socket, sys, time
from datetime import datetime
import openstack

CLOUD = "devstack-admin"      # defined in clouds.yaml
TARGET = 99.5                 # SLA objective in percent
HEADER = ["date", "instance", "nova_status", "ip", "ssh_port_open", "state"]


def now():
    return datetime.now().strftime("%Y-%m-%d %H:%M:%S")


def get_ip(server):
    """Prefer the floating IP, fall back to the fixed IP."""
    fixed = None
    for addrs in (server.addresses or {}).values():
        for a in addrs:
            if a.get("OS-EXT-IPS:type") == "floating":
                return a["addr"]
            fixed = fixed or a["addr"]
    return fixed


def port_open(ip, port, timeout=3):
    try:
        with socket.create_connection((ip, port), timeout=timeout):
            return True
    except OSError:
        return False


def measure(conn, prefix, port):
    rows = []
    servers = [s for s in conn.compute.servers(details=True) if s.name.startswith(prefix)]
    for s in sorted(servers, key=lambda x: x.name):
        ip = get_ip(s)
        ssh_ok = s.status == "ACTIVE" and bool(ip) and port_open(ip, port)
        up = s.status == "ACTIVE" and ssh_ok
        rows.append([now(), s.name, s.status, ip or "", "yes" if ssh_ok else "no",
                     "UP" if up else "DOWN"])
    return rows


def report(csv_path):
    stats = {}
    first = last = None
    with open(csv_path, newline="") as f:
        for r in csv.DictReader(f):
            d = stats.setdefault(r["instance"], [0, 0])
            d[0] += 1
            d[1] += r["state"] == "UP"
            first = first or r["date"]
            last = r["date"]
    print("=== SLA MONITORING REPORT ===")
    print(f"Period      : {first} -> {last}")
    print(f"Objective   : {TARGET}% availability")
    print(f"{'instance':<16}{'measures':>9}{'available':>10}{'failed':>8}{'availability':>14}  objective")
    for name, (total, up) in sorted(stats.items()):
        pct = up / total * 100
        print(f"{name:<16}{total:>9}{up:>10}{total - up:>8}{pct:>13.2f}%  "
              f"{'MET' if pct >= TARGET else 'NOT MET'}")


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--prefix", default="cirros-mon-", help="monitor instances whose name starts with this")
    ap.add_argument("--interval", type=int, default=300, help="seconds between measurements")
    ap.add_argument("--duration", type=int, default=0, help="total seconds, 0 = until Ctrl+C")
    ap.add_argument("--csv", default="measurements.csv")
    ap.add_argument("--port", type=int, default=22)
    ap.add_argument("--report", action="store_true", help="only print the report from the CSV")
    args = ap.parse_args()

    if args.report:
        report(args.csv)
        return

    conn = openstack.connect(cloud=CLOUD)
    conn.authorize()                      # forces Keystone authentication now
    print(f"[{now()}] Authenticated with Keystone, cloud={CLOUD}", flush=True)

    stop = {"v": False}
    def handler(sig, frm):
        stop["v"] = True
    signal.signal(signal.SIGINT, handler)
    signal.signal(signal.SIGTERM, handler)

    max_cycles = args.duration // args.interval if args.duration else 0
    new_file = not os.path.exists(args.csv) or os.path.getsize(args.csv) == 0
    next_t = time.time()
    n = 0
    with open(args.csv, "a", newline="") as f:
        w = csv.writer(f)
        if new_file:
            w.writerow(HEADER)
            f.flush()
        while not stop["v"]:
            try:
                rows = measure(conn, args.prefix, args.port)
            except Exception as e:        # API problem: not an instance failure, skip this cycle
                print(f"[{now()}] API error, cycle skipped: {e}", flush=True)
                rows = []
            if not rows:
                print(f"[{now()}] warning: no instance found with prefix {args.prefix!r}", flush=True)
            for r in rows:
                w.writerow(r)
            f.flush()
            n += 1
            print(f"[{now()}] cycle {n}: " + ", ".join(f"{r[1]}={r[5]}({r[2]})" for r in rows), flush=True)
            if max_cycles and n >= max_cycles:
                break
            next_t += args.interval
            while not stop["v"] and time.time() < next_t:
                time.sleep(1)
    print()
    report(args.csv)


if __name__ == "__main__":
    main()
