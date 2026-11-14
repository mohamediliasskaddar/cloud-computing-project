# Final deliverables audit

Based on [`projet_cloud_intelligence.md`](./projet_cloud_intelligence.md) and the files currently in this workspace. The evidence inventory is substantially complete, but the two required final submission files have not been assembled.

## Part 1 — CloudSim Plus

- [x] Simulation source and Maven project in [`part1-cloudsim/`](./part1-cloudsim/).
- [x] UAE source data and data-quality/workload notes in [`scrapping-uae-data/`](./scrapping-uae-data/).
- [x] Editable architecture diagram: [`architecture.drawio`](./part1-cloudsim/architecture.drawio); explanatory guide: [`guide.md`](./part1-cloudsim/guide.md).
- [x] Results and analysis basis: [`benchmark_results.csv`](./part1-cloudsim/benchmark_results.csv). Latest verified run has four scenarios; every Cloudlet completed; autoscaling reached 20 VMs.
- [x] Include the diagram, sources, assumptions, result table, and careful peak-capacity conclusion in [`Rapport_Global_Cloud_Intelligence.md`](./Rapport_Global_Cloud_Intelligence.md). The report identifies 50/300 Cloudlets as synthetic samples, not measured production request rates.

## Part 2 — OpenStack IaaS and SaaS

- [x] Commands, service roles, environment notes, and IaaS/SaaS walkthrough in [`part2-openstack/readme.md`](./part2-openstack/readme.md).
- [x] OpenStack command outputs and supporting logs are present in [`scripts/part2/`](./scripts/part2/) and [`part2-openstack/`](./part2-openstack/).
- [x] Screenshots for Horizon, services/network, CirrOS ping/SSH, and Flask browser are present and reviewed in [`screenshots/part2-openstack/`](./screenshots/part2-openstack/). Some service/agent captures are composite and would benefit from a clean recapture.
- [x] Commands, outcomes, screenshots, environment, and SSH/tunnel access are documented in the global report.

## Part 3 — Terraform and Ansible

- [x] Terraform, Ansible, templates, inventories, and apply/destroy/playbook outputs are present in [`scripts/part3/`](./scripts/part3/).
- [x] Screenshots include Terraform version/apply, Ansible run and idempotent rerun, deployed page, and destroy evidence in [`screenshots/part3-iac/`](./screenshots/part3-iac/). The web screenshot shows name, student ID, instance, URL, and date.
- [x] Record Terraform/Ansible versions and exact run 2/run 3 play recaps in [`part3-iac/readme.md`](./part3-iac/readme.md).
- [x] Terraform/Ansible explanation, apply evidence, and deployed-page screenshot are incorporated in the global report.

## Part 4 — SLA and monitoring

- [x] SLA, monitoring script, measurement CSV (72 data rows), monitor output, and final report are present in [`scripts/part4/`](./scripts/part4/).
- [x] The SLA states 99.5%, five-minute sampling, and the 288-measurement daily calculation. It correctly distinguishes the actual two-hour test from a full-day evaluation.
- [x] Screenshots for active instances, outage detection, recovery, Horizon, report, and outage event log are present in [`screenshots/part4-sla/`](./screenshots/part4-sla/).
- [x] Record the STOP/START times in [`part4-sla/readme.md`](./part4-sla/readme.md): STOP `2026-11-07 21:55:13`, START `2026-11-07 22:10:15`.
- [x] SLA terms, monitoring results, outage evidence, and daily-evaluation limitation are incorporated in the global report.

## Final submission files — still required

- [ ] Create the final PDF named exactly `Nom_Prenom_Rapport.pdf`, with student details and all four parts, commands, explanations, screenshots, citations, and conclusions.
- [ ] Create the required ZIP named exactly `Nom_Prenom_Code.zip`.
- [ ] Put the CloudSim Plus Java project, Terraform/Ansible source files, SLA file, and Python monitoring script in the ZIP. Include useful configuration/docs as appropriate.
- [ ] Exclude credentials (`clouds.yaml`, private keys), Terraform state files, and unrelated build/cache files from the ZIP. Check the ZIP contents before uploading.
- [ ] Submit both named files to Google Classroom.

[`scripts/livrables-tp-cloud.tar.gz`](./scripts/livrables-tp-cloud.tar.gz) is not a substitute for the required ZIP: its current contents include Parts 2–4 but omit the Part 1 CloudSim project, and it contains Terraform state files. Do not upload it as-is.

## Bottom line

The global Markdown report and project evidence are now assembled. You are **not ready to submit yet**: the required PDF and complete, clean code ZIP do not exist. Recapture the noted composite screenshots if possible, then export the report and build/check the ZIP before uploading.
