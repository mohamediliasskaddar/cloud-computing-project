# Part 2: OpenStack, IaaS and SaaS

**Student:** <YOUR NAME> · **Student ID:** <YOUR ID>
**Module:** Cloud Intelligence · **Prof.:** C. EL AMRANI

---

## 0. Environment (say this first to the teacher)

| Item | Value |
|---|---|
| Host OS | Ubuntu (my laptop), VS Code |
| Hypervisor | Oracle VirtualBox (local VM, as required) |
| Guest OS (DevStack VM) | Ubuntu Server 26.04.1 LTS |
| VM resources | 4 vCPU, about 10 GB RAM, 68 GB disk |
| DevStack branch | `stable/2026.2` |
| Network | Adapter 1: NAT (internet). Adapter 2: Host-only, static IP `192.168.56.101` |
| Horizon | http://192.168.56.101/dashboard (user `admin`) |
| Neutron backend | OVN (default in recent DevStack) |

`local.conf` used:

```ini
[[local|localrc]]
ADMIN_PASSWORD=secret
DATABASE_PASSWORD=$ADMIN_PASSWORD
RABBIT_PASSWORD=$ADMIN_PASSWORD
SERVICE_PASSWORD=$ADMIN_PASSWORD
HOST_IP=192.168.56.101
IP_VERSION=4
LOGFILE=$DEST/logs/stack.sh.log
```

Install command: `./stack.sh` (about 1 hour).

**Why `stable/2026.2`:** my VM runs Ubuntu 26.04 (`resolute`). I checked `SUPPORTED_DISTROS` in `stack.sh`: `stable/2026.1` does not list `resolute`, but `stable/2026.2` does.

---

## 1. Part 2.1: Verify the DevStack installation

### Commands

```bash
cd ~/devstack
source openrc admin admin
openstack service list
openstack compute service list
openstack network agent list
```

### Screenshots to include

| # | File name | What it must show |
|---|---|---|
| 1 | `01-horizon-overview.png` | Horizon dashboard after login (URL bar visible) |
| 2 | `02-service-list.png` | Output of `openstack service list` |
| 3 | `03-compute-service-list.png` | Output of `openstack compute service list` (all `up`) |
| 4 | `04-network-agent-list.png` | Output of `openstack network agent list` (`:-)` / `UP`) |

### Role of each service (reword in your own words)

| Service | Role |
|---|---|
| Keystone | Identity: authenticates users and issues tokens and the service catalog |
| Nova | Compute: creates and manages VM instances |
| Glance | Image service: stores the OS images instances boot from |
| Neutron | Networking: networks, subnets, routers, floating IPs, security groups |
| Placement | Tracks resource inventory and usage so Nova can pick a suitable host |
| Cinder | Block storage: persistent volumes for instances |

### Points to explain orally

- `nova_legacy` is a compatibility entry that DevStack registers. It is not a separate service I installed.
- `network agent list` shows only OVN agents (`ovn-controller` and `neutron-ovn-agent`). With OVN, the DHCP, L3 and metadata agents of older Neutron are no longer needed.
- Nova shows two `nova-conductor` entries because one serves the main cell and the other serves `cell0`.

---

## 2. Part 2.2: IaaS with CirrOS

### Resources created

| Resource | Name | Details |
|---|---|---|
| Private network | `net-tp` | |
| Subnet | `subnet-tp` | `10.10.10.0/24`, DNS 8.8.8.8 |
| Router | `router-tp` | gateway on `public`, interface on `subnet-tp` |
| Security group | `sg-tp` | ICMP + TCP/22 (later also TCP/5000) |
| Key pair | `key-tp` | private key saved in `~/key-tp.pem` |
| Instance | `vm-cirros` | image `cirros-0.6.3-x86_64-disk`, flavor `cirros256` |
| Floating IP | `172.24.4.201` | private IP `10.10.10.101` |

### Commands

```bash
# Existing images and flavors
openstack image list
openstack flavor list

# Private network + subnet
openstack network create net-tp
openstack subnet create subnet-tp --network net-tp \
  --subnet-range 10.10.10.0/24 --dns-nameserver 8.8.8.8

# Router
openstack router create router-tp
openstack router set router-tp --external-gateway public
openstack router add subnet router-tp subnet-tp

# Security group (SSH + ICMP)
openstack security group create sg-tp --description "SSH and ICMP"
openstack security group rule create --proto icmp sg-tp
openstack security group rule create --proto tcp --dst-port 22 sg-tp

# Key pair
openstack keypair create key-tp > ~/key-tp.pem
chmod 600 ~/key-tp.pem

# Instance
openstack server create --image cirros-0.6.3-x86_64-disk --flavor cirros256 \
  --network net-tp --security-group sg-tp --key-name key-tp vm-cirros
openstack server list

# Floating IP
FLOATING_IP=$(openstack floating ip create public -f value -c floating_ip_address)
openstack server add floating ip vm-cirros $FLOATING_IP

# Tests
ping -c 3 $FLOATING_IP
ssh -i ~/key-tp.pem cirros@$FLOATING_IP
```

### Result obtained

- `ping 172.24.4.201`: 3 packets sent, 3 received, **0% loss**
- SSH as `cirros`: `hostname` printed `vm-cirros`, `eth0` had `10.10.10.101/24`

### Screenshots to include

| # | File name | What it must show |
|---|---|---|
| 5 | `05-network-topology.png` | Horizon: **Project → Network → Network Topology** (`net-tp`, `router-tp`, `public`, instance) |
| 6 | `06-instances.png` | Horizon: **Compute → Instances** with floating IPs |
| 7 | `07-ping-ssh-cirros.png` | Terminal: ping 0% loss + SSH session with `hostname` and `ip a` |
| 8 | `08-security-group.png` | Horizon: **Network → Security Groups → sg-tp** rules (SSH, ICMP) |

### Points to explain orally

- **Why a router:** it connects the private network `10.10.10.0/24` to the external `public` network (`172.24.4.0/24`) and does NAT.
- **Why a floating IP:** the instance only has a private IP. The floating IP is a public-side address mapped (NAT) to it, so it can be reached from outside.
- **Why a security group:** instance firewall. By default everything incoming is denied, so I opened ICMP (ping) and TCP/22 (SSH).
- **Why a key pair:** CirrOS and cloud images use key-based SSH. Nova injects my public key through cloud-init.
- **PuTTY vs ssh:** my host is Ubuntu, so I used the native `ssh` client. It does the same job as PuTTY.
- **Where I ran ping and SSH:** from inside the DevStack VM, because `172.24.4.0/24` is the DevStack-internal external network.

---

## 3. Part 2.3: SaaS (Flask web application on Ubuntu Server)

### Resources created

| Resource | Name | Details |
|---|---|---|
| Flavor | `m1.saas` | 1536 MB RAM, 10 GB disk, 1 vCPU |
| Image | `ubuntu-noble` | Ubuntu 24.04 cloud image (qcow2) |
| Instance | `vm-saas` | on `net-tp`, group `sg-tp`, key `key-tp` |
| Floating IP | `172.24.4.40` | private IP `10.10.10.62` |
| Application | Flask app on port 5000 | |

### Commands

```bash
# Flavor
openstack flavor create --ram 1536 --disk 10 --vcpus 1 m1.saas

# Ubuntu cloud image -> Glance
wget https://cloud-images.ubuntu.com/noble/current/noble-server-cloudimg-amd64.img
openstack image create ubuntu-noble --file noble-server-cloudimg-amd64.img \
  --disk-format qcow2 --container-format bare --public

# Open port 5000 for the app
openstack security group rule create --proto tcp --dst-port 5000 sg-tp

# Instance + floating IP
openstack server create --image ubuntu-noble --flavor m1.saas \
  --network net-tp --security-group sg-tp --key-name key-tp vm-saas
SAAS_IP=$(openstack floating ip create public -f value -c floating_ip_address)
openstack server add floating ip vm-saas $SAAS_IP

# Wait for SSH, then connect (user is "ubuntu" on this image)
until timeout 3 bash -c "</dev/tcp/$SAAS_IP/22" 2>/dev/null; do echo waiting; sleep 10; done
ssh -i ~/key-tp.pem ubuntu@$SAAS_IP
```

Flask installed **offline** (see section 4, problem 5):

```bash
# On the DevStack VM: download the 9 .deb packages Flask needs
mkdir -p ~/flask-debs && cd ~/flask-debs
U=http://archive.ubuntu.com/ubuntu/pool/main
S=http://security.ubuntu.com/ubuntu/pool/main
wget -c $U/j/javascript-common/javascript-common_11%2bnmu1_all.deb
wget -c "$U/n/node-jquery/libjs-jquery_3.6.1%2bdfsg%2b%7e3.5.14-1_all.deb"
wget -c $U/p/python-asgiref/python3-asgiref_3.7.2-1_all.deb
wget -c $U/p/python-itsdangerous/python3-itsdangerous_2.1.2-4_all.deb
wget -c $S/p/python-werkzeug/python3-werkzeug_3.0.1-3ubuntu0.2_all.deb
wget -c $S/f/flask/python3-flask_3.0.2-1ubuntu1.1_all.deb
wget -c $U/p/python-pyasyncore/python3-pyasyncore_1.0.2-2_all.deb
wget -c $U/p/pyinotify/python3-pyinotify_0.9.6-2ubuntu1_all.deb
wget -c $U/s/simplejson/python3-simplejson_3.19.2-1build2_amd64.deb

# Copy to the instance
scp -i ~/key-tp.pem *.deb ubuntu@172.24.4.40:~

# Inside the instance
sudo dpkg -i ~/*.deb
python3 -c "import flask; print('Flask OK', flask.__version__)"
```

Application (inside the instance):

```bash
mkdir -p ~/app && cd ~/app
cat > app.py <<'EOF'
from flask import Flask
import socket
app = Flask(__name__)

@app.route("/")
def home():
    return f"<h1>Hello from my OpenStack SaaS app</h1><p><YOUR NAME> - Cloud Intelligence project</p><p>Served by instance: {socket.gethostname()}</p>"

app.run(host="0.0.0.0", port=5000)
EOF
nohup python3 app.py > app.log 2>&1 &
curl http://localhost:5000
```

Access from the host browser (SSH tunnel, run on the **host**):

```bash
ssh -L 8080:172.24.4.40:5000 imk@192.168.56.101
# then open http://localhost:8080
```

### Screenshots to include

| # | File name | What it must show |
|---|---|---|
| 9 | `09-flavor-image.png` | `openstack flavor list` (m1.saas) and `openstack image list` (ubuntu-noble active) |
| 10 | `10-vm-saas-active.png` | `openstack server list` with `vm-saas` ACTIVE + floating IP |
| 11 | `11-ssh-vm-saas.png` | SSH session into `vm-saas` (Ubuntu 24.04 welcome banner) |
| 12 | `12-flask-browser.png` | **Browser at `http://localhost:8080`** showing the app (URL bar visible) |
| 13 | `13-flask-curl.png` | `curl http://localhost:5000` inside the instance (optional) |

### Points to explain orally

- **Why this is SaaS:** the end user only uses the web application through a browser. The OS, Python and the app are managed on my side. The IaaS layer (OpenStack) underneath is what lets me provision it.
- **Why `m1.saas`:** the Ubuntu image needs more than CirrOS. 1.5 GB RAM is enough for Flask, and it saves memory on my laptop.
- **Why the SSH tunnel:** the floating-IP network is only reachable from inside the DevStack VM, so I forwarded local port 8080 to port 5000 of the instance.

---

## 4. Problems encountered and how I solved them (shows real understanding)

| # | Problem | Cause | Fix |
|---|---|---|---|
| 1 | `stack.sh` failed at the very end (`sysctl: cannot stat /proc/sys/net/ipv6/conf/fe80::2/accept_ra`) | DevStack misread the IPv6 route table and used `fe80::2` as an interface name | Disabled IPv6 setup: added `IP_VERSION=4` to `local.conf`, then `./unstack.sh` and `./stack.sh` again |
| 2 | Ubuntu 26.04 not accepted by `stable/2026.1` | `resolute` was missing from `SUPPORTED_DISTROS` | Switched to `stable/2026.2`, which lists it |
| 3 | After a reboot the floating IPs did not answer (`br-ex` DOWN, no IP) | DevStack sets the `br-ex` address by hand during install, and Linux does not keep it after a reboot | Script `~/fix-net.sh`: `ip addr replace 172.24.4.1/24 dev br-ex`, `ip link set br-ex up`, enable IP forwarding, add an iptables MASQUERADE rule |
| 4 | VirtualBox "Guru Meditation" crash while `vm-saas` was booting; instance stuck at the first seconds of boot | Nested virtualization (KVM inside VirtualBox) is fragile with a heavier guest | Restored my snapshot, then switched Nova to software emulation: `virt_type = qemu` in `/etc/nova/nova-cpu.conf`, restarted `devstack@n-cpu` (slower first boot, about 9 min, but stable) |
| 5 | `apt update` / `apt install` inside `vm-saas` kept timing out, while ping to 8.8.8.8 worked | Small packets pass but large downloads stall on the path instance, OVN router, DevStack VM, VirtualBox NAT | Downloaded the 9 Flask `.deb` files on the DevStack VM (working internet), copied them with `scp` and installed with `dpkg -i` |
| 6 | `ssh ubuntu@$172.24.4.40` gave "connection refused" to `72.24.4.40` | Typo: bash read `$1` as a variable | Used `$SAAS_IP`. "Refused" in the first attempts was just cloud-init still booting |

---

## 5. Final state and snapshots

- Instances: `vm-cirros` (CirrOS) and `vm-saas` (Ubuntu + Flask), both on `net-tp`
- VirtualBox snapshots taken: `before-devstack`, `devstack-working`, `part2-iaas-done`, `part2-done`
- After every reboot I run `~/fix-net.sh` and `source ~/devstack/openrc admin admin`

---

## 6. Quick checklist before submitting

- [ ] Replace `<YOUR NAME>` and `<YOUR ID>` everywhere
- [ ] 13 screenshots saved with the file names above
- [ ] The Flask page screenshot shows the URL bar
- [ ] Service roles reworded in my own words
- [ ] Commands copied into the final report (this file is the source)