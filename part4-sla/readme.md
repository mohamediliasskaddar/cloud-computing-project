# Part 4: Service Level Agreement (SLA) and availability monitoring

**Student:** Mohamed Iliass KADDAR · **Student ID:** imk25
**Module:** Cloud Intelligence · **Prof.:** C. EL AMRANI

---

## 0. Environment

| Item | Value |
|---|---|
| Host OS | Ubuntu (laptop) |
| DevStack VM | Ubuntu Server 26.04.1 LTS in VirtualBox, 12 GB RAM, DevStack `stable/2026.2` |
| Monitored instances | `cirros-mon-1`, `cirros-mon-2`, `cirros-mon-3` (CirrOS 0.6.3, flavor `cirros256`, network `net-tp`, one floating IP each) |
| Floating IPs | `172.24.4.2`, `172.24.4.26`, `172.24.4.216` |
| Monitoring program | `monitor.py` (Python, `openstacksdk`, cloud `devstack-admin` from `clouds.yaml`, Keystone authentication) |
| Python environment | DevStack's own venv: `/opt/stack/data/venv/bin/python3` |
| Session manager | `tmux` (session `monitor`), so the run survives an SSH disconnect |

Why CirrOS: the instances are tiny (almost no memory), so three of them can run for hours without stressing the DevStack VM.

---

## 1. Part 4.1: The SLA (`sla.json`)

### Content of the SLA

| Item | Value |
|---|---|
| Objective | **Availability >= 99.5%** per daily evaluation period |
| Instances concerned | `cirros-mon-1`, `cirros-mon-2`, `cirros-mon-3` |
| Definition of "available" | At a measurement, the instance is available if **Nova reports `ACTIVE` AND TCP port 22 (SSH) answers within 3 seconds**. Otherwise it is unavailable |
| Measurement frequency | Every 5 minutes, which gives **288 measurements per day** |
| Formula | `availability (%) = available measurements / total measurements x 100` |
| If the objective is not met | 99.0% to below 99.5%: incident report within 24 h and 10% credit for the day. Below 99.0%: incident report within 24 h and 25% credit. Three days in a row missed: infrastructure review |
| Exclusions | Maintenance announced 24 h ahead, customer-caused failures (except the controlled test), failures of the customer's own network or host machine |

The full file is `sla.json` (valid JSON, checked with `python3 -m json.tool`). Its `monitoring_report` field is filled at the end (section 4).

### Command

```bash
python3 -m json.tool sla.json > /dev/null && echo "sla.json is valid"
```

### Points to explain orally

- An SLA is a **contract with measurable objectives and a defined way to measure them**. Without the measurement method, a percentage means nothing.
- I defined "available" with **two conditions** (Nova status and an SSH test): Nova can say `ACTIVE` while the guest is still booting or frozen. The final run showed exactly this case (see section 3).
- The consequence clause (credits) is what makes the SLA a contract and not just a monitoring report.

---

## 2. Part 4.2 and 4.3: The monitoring program

### How `monitor.py` works

1. Connects with `openstack.connect(cloud="devstack-admin")` and calls `conn.authorize()`, which performs the **Keystone authentication** and prints a confirmation.
2. Every 5 minutes it lists the instances whose name starts with `cirros-mon-` through the **Nova API** (`conn.compute.servers`).
3. For each instance it takes the Nova status, finds the floating IP, and tests **TCP port 22** with a 3-second timeout.
4. The state is `UP` only if the status is `ACTIVE` **and** SSH answers. Otherwise it is `DOWN`.
5. Each measurement is appended to `measurements.csv` (columns: `date, instance, nova_status, ip, ssh_port_open, state`) and flushed immediately, so nothing is lost if the program stops.
6. At the end it computes `available measurements / total measurements x 100` per instance and compares it with 99.5%.
7. If the OpenStack API itself fails during a cycle, the cycle is skipped and a message is printed. An API error is not counted as an instance failure.

Options: `--interval 300` (seconds between measurements), `--duration 7200` (total seconds), `--report` (print the report from an existing CSV).

### Commands

```bash
# Prerequisite: working DevStack, instances ACTIVE
~/fix-net.sh
cd ~/devstack && source openrc admin admin

# Quick test (4 cycles)
PY=/opt/stack/data/venv/bin/python3
cd ~/part4
$PY monitor.py --interval 10 --duration 40 --csv test.csv
```

Quick test result: 3 instances, 4 cycles, all `UP`, 100% each.



---

## 3. Part 4.4: Controlled outage during the monitoring

### Setup

Two things ran at the same time, started together at about 21:15:

```bash
# Window 1 (tmux session "monitor"): the monitor, 2 hours, one measurement every 5 minutes
nohup ./outage.sh > outage-run.log 2>&1 &
$PY monitor.py --interval 300 --duration 7200 --csv measurements.csv | tee monitor-output.txt
```

`outage.sh` (automatic, so the outage is reproducible and logged):

```bash
#!/bin/bash
source ~/devstack/openrc admin admin 2>/dev/null
sleep 2400                                   # wait 40 minutes
echo "$(date '+%F %T') STOP  cirros-mon-2" | tee -a ~/part4/outage-events.log
openstack server stop cirros-mon-2
sleep 900                                    # outage lasts 15 minutes
echo "$(date '+%F %T') START cirros-mon-2" | tee -a ~/part4/outage-events.log
openstack server start cirros-mon-2
```

Exact times from `outage-events.log`: STOP at `2026-11-07 21:55:13`, START at `2026-11-07 22:10:15`.

### What the monitor detected (from `monitor-output.txt`)

| Cycle | Time | `cirros-mon-2` | Comment |
|---|---|---|---|
| 8 | 21:50:29 | UP (ACTIVE) | normal |
| **9** | **21:55:28** | **DOWN (SHUTOFF)** | outage detected |
| 10 | 22:00:28 | DOWN (SHUTOFF) | |
| 11 | 22:05:27 | DOWN (SHUTOFF) | |
| **12** | **22:10:30** | **DOWN (ACTIVE)** | instance restarted, Nova says ACTIVE but SSH not answering yet (guest still booting) |
| 13 | 22:15:28 | UP (ACTIVE) | recovered |

`cirros-mon-1` and `cirros-mon-3` stayed `UP` in all 24 cycles.


## 4. Part 4.5: Monitoring report

### Final report (output of the program)

```
=== SLA MONITORING REPORT ===
Period      : 2026-11-07 21:15:27 -> 2026-11-07 23:10:28
Objective   : 99.5% availability
instance         measures available  failed  availability  objective
cirros-mon-1           24        24       0       100.00%  MET
cirros-mon-2           24        20       4        83.33%  NOT MET
cirros-mon-3           24        24       0       100.00%  MET
```

| Instance | Measurements | Available | Failed | Availability | Objective 99.5% |
|---|---|---|---|---|---|
| cirros-mon-1 | 24 | 24 | 0 | 100.00% | **Met** |
| cirros-mon-2 | 24 | 20 | 4 | 83.33% | **Not met** |
| cirros-mon-3 | 24 | 24 | 0 | 100.00% | **Met** |

- **Period:** 2026-11-07, 21:15:27 to 23:10:28 (2-hour evaluation window, 24 measurements per instance, 72 in total in `measurements.csv`).
- **Why 4 failed measurements for `cirros-mon-2`:** the 15-minute outage plus the guest boot time (about 2 minutes) covers four 5-minute measurements (21:55, 22:00, 22:05, 22:10).
- The monitoring report was also added to `sla.json` (field `monitoring_report`).

### Commands used for the report

```bash
cd ~/part4
$PY monitor.py --report --csv measurements.csv | tee final-report.txt
cat outage-events.log
```

### How the calculation extends to one day

The program applies the same formula at any scale. A day has 288 measurements per instance (24 x 60 / 5).

- **If the same outage (4 failed measurements) happened in a day:** (288 - 4) / 288 x 100 = **98.61%**, which is still **below 99.5%**, so the objective would **not** be met.
- **For the 2-hour test window itself:** 0.5% of 24 measurements is 0.12, so **no failed measurement at all** is tolerated in such a short window (23/24 = 95.83%). The daily window is more forgiving: it tolerates 1 failure.
- A 2-hour test therefore cannot prove 99.5% over a day. It only shows that the measurement and the calculation work, and that the program reacts to a failure.

---

## 5. Question of the assignment

> Over one day, with one measurement every 5 minutes, how many failed measurements can be tolerated to stay above 99.5%? What outage duration does that represent?

1. **Measurements per day:** 24 h x 60 min / 5 min = **288**.
2. **Tolerated failures:** the target allows 100% - 99.5% = 0.5% of failures, so 0.5% x 288 = **1.44**. Only whole measurements can fail, so at most **1 failed measurement**.
   - 1 failure: 287/288 = **99.65%** (objective met).
   - 2 failures: 286/288 = **99.31%** (objective not met).
3. **Outage duration:**
   - 1 failed measurement represents about **5 minutes** of downtime (one measurement interval).
   - In strict time terms, the allowed downtime is 0.5% x 24 h = 0.005 x 1440 min = **7.2 minutes per day**.
   - The measured value (5 min) is lower than 7.2 min because sampling is discrete: a failure is only seen at the moment of a measurement.

---

## 6. Deliverables checklist

- [x] `sla.json` with the `monitoring_report` field filled in
- [x] `monitor.py`
- [x] `measurements.csv` (the measurement log, 72 rows plus header)
- [x] `monitor-output.txt` and `final-report.txt`
- [x] `outage-events.log` and `outage.sh`
- [x] Screenshot of the detected failure (`DOWN`), plus the recovery and Horizon `Shutoff` screenshots
- [x] Exact STOP and START times recorded in section 3
- [x] Section 5 reworded in my own words

---

## 7. Final state

- The three `cirros-mon-*` instances are still running (stop them with `openstack server stop cirros-mon-1 cirros-mon-2 cirros-mon-3` before shutting the VM down).
- After every reboot: `~/fix-net.sh`, then `source ~/devstack/openrc admin admin`.