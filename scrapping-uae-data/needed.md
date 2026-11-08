# What’s still missing, and where to find it

| Priority | Data | Why we need it | Where to look |
| :---: | :--- | :--- | :--- |
| **1** | Current student and staff numbers, total and per establishment | Sizes the hosts and the workload. Our only figures (126,000 students, 1,318 teachers, 588 administrators) are from 2022-2023 | Each page under `/etablissements`, `/histoire`, `/chronologie`, `/communiques-presse` (rentrée 2023, 2024, 2025), the sustainability report, and the Ministry of Higher Education statistics |
| **1** | Academic calendar: registration windows and exam sessions | Defines when the peak happens and how long it lasts | `/calendriers-universitaires` |
| **1** | Registration volume (applications, new students) | Defines the size of the registration peak | Press releases. We already have 27,000 new baccalaureate holders for 2022-2023 |
| **2** | How Moodle and the other platforms are deployed (one central instance, or one per establishment) | Decides the number of VMs per service | `/plateformes pédagogiques`, `/centre-de-linnovation-pedagogique`, the student-space guide |
| **2** | Digital strategy | Justifies the architecture choices | `/strategie-pacte-esri`, `/projet-de-developpement` |
| **3** | Existing IT infrastructure (servers, bandwidth, datacenter) | Only for realism | Probably not public. Try the “Infrastructures” page and public tenders (`marchespublics.gov.ma`, searching “serveurs” or “virtualisation”) |


If item 3 isn’t public, we state our hardware sizes as assumptions and justify them (typical server specs from a vendor datasheet). That is acceptable for a simulation, as long as the report says so.

Outside the university, I can also help you find three things with sources later: a cloud price list for the cost metric, a server datasheet for the host specs, and Moodle sizing guidance for the share of users online at the same time.