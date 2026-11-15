# Curated project screenshots

These are selected captures for the project report and demos.

The capture dates and evidence are retained. Some captures are not ideal for the
final PDF: the Part 2 service/agent-list images are composited or partly
overlapped, and `p4-02-monitor-test.png` includes additional later terminal
activity. Recapture those cleanly if there is time before submission.

## Part 2 — OpenStack

| File | Evidence |
|---|---|
| `part2-openstack/p2-01-horizon-overview.png` | Horizon dashboard |
| `part2-openstack/p2-02-service-list.png` | OpenStack service catalog |
| `part2-openstack/p2-03-compute-service-list.png` | Nova compute services |
| `part2-openstack/p2-04-network-topology.png` | Network topology |
| `part2-openstack/p2-05-instances.png` | CirrOS and SaaS instance state and addresses |
| `part2-openstack/p2-06-ping-ssh-cirros.png` | CirrOS floating IP and connectivity test |
| `part2-openstack/p2-07-security-group-rule.png` | `sg-tp` ICMP rule creation |
| `part2-openstack/p2-08-ubuntu-image.png` | Active Ubuntu image |
| `part2-openstack/p2-09-ssh-vm-saas.png` | SSH session on the SaaS instance |
| `part2-openstack/p2-10-flask-browser.png` | Flask app in a browser |

`part2-openstack/p2-11-network-agent-list.png` shows the `openstack network agent list`
output.

## Part 3 — Terraform and Ansible

| File | Evidence |
|---|---|
| `part3-iac/p3-01-terraform-version.png` | Terraform and Ansible versions |
| `part3-iac/p3-02-terraform-plan.png` | Terraform plan |
| `part3-iac/p3-03-terraform-apply.png` | Apply/rebuild output and floating IP |
| `part3-iac/p3-04-horizon-instances.png` | Deployed instance in Horizon |
| `part3-iac/p3-05-horizon-secgroup.png` | SSH and HTTP rules on the instance port |
| `part3-iac/p3-06-terraform-destroy.png` | Terraform destroy completion |
| `part3-iac/p3-07-ansible-ping.png` | Ansible ping (`pong`) and connectivity |
| `part3-iac/p3-08-playbook-run.png` | Ansible playbook run and idempotent rerun |
| `part3-iac/p3-09-playbook-idempotent.png` | Ansible rerun; final run has `changed=0` |
| `part3-iac/p3-10-deployed-webpage.png` | Ansible-deployed page with student identity |

## Part 1 — CloudSim Plus

| File | Evidence |
|---|---|
| `part1-cloudsim/p1-01-architecture.jpg` | Exported architecture diagram (editable source: `part1-cloudsim/architecture.drawio`) |

## Part 4 — SLA

| File | Evidence |
|---|---|
| `part4-sla/p4-01-instances-active.png` | Monitored instances active |
| `part4-sla/p4-02-monitor-test.png` | Successful quick monitoring test |
| `part4-sla/p4-03-outage-detected.png` | Monitor detects the stopped instance |
| `part4-sla/p4-04-horizon-shutoff.png` | Instance shown as shut off in Horizon |
| `part4-sla/p4-05-instances-recovered.png` | Instances active after recovery |
| `part4-sla/p4-06-final-report.png` | Final availability report |
| `part4-sla/p4-07-outage-events.png` | Outage stop/start event times |
