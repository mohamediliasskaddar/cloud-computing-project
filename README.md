# Cloud Intelligence — Projet Cloud Computing

Projet universitaire consacré à la conception et à l'expérimentation d'une
architecture cloud pour l'Université Abdelmalek Essaâdi (UAE). Il combine une
simulation CloudSim Plus et un laboratoire OpenStack local, automatisé avec
Terraform et Ansible, puis surveillé avec un SLA.

> DevStack et OpenStack doivent être installés dans une machine virtuelle
> locale (par exemple VirtualBox), et non chez un fournisseur cloud externe.

## Architecture générale

Le schéma ci-dessous présente les composants du projet et leurs interactions :

![Architecture globale du projet](<screenshots/other/globale architecture of the project.png>)

CloudSim Plus fonctionne indépendamment sur l'hôte. Les parties OpenStack,
Terraform, Ansible et SLA utilisent la même VM locale DevStack.

## Contenu du projet

### Partie 1 — Simulation avec CloudSim Plus

**Objectif :** modéliser un cloud universitaire réparti entre Tanger et
Tétouan, puis étudier son comportement en charge normale et en période de pointe.

**À réaliser :**
1. Rassembler les données de référence sur l'UAE et préciser leurs limites.
2. Décrire l'architecture proposée : datacenters, hôtes, VM et services.
3. Créer des workloads synthétiques pour les périodes normale et de pointe.
4. Simuler les scénarios, comparer des ordonnanceurs et tester l'autoscaling.
5. Présenter les résultats de performance, d'utilisation et de coût, puis discuter
   des limites du modèle.

**Livrables :** schéma d'architecture, code Java, résultats et analyse.

### Partie 2 — OpenStack : IaaS et SaaS

**Objectif :** vérifier DevStack et déployer des ressources et applications dans
OpenStack.

**À réaliser :**
1. Vérifier les services OpenStack dans Horizon et avec la CLI.
2. Créer un réseau privé, un routeur, un groupe de sécurité et une paire de clés.
3. Lancer une instance CirrOS, lui associer une IP flottante, puis tester ping et SSH.
4. Lancer une instance Ubuntu et y déployer une application Web Flask accessible
   depuis un navigateur.

**Livrables :** commandes utilisées, captures d'écran et explications des services.

### Partie 3 — Infrastructure as Code : Terraform et Ansible

**Objectif :** automatiser la création d'une VM OpenStack et sa configuration Web.

**À réaliser :**
1. Décrire l'instance Ubuntu, son réseau, son groupe de sécurité et son IP flottante
   dans Terraform ; exécuter `init`, `plan`, `apply`, puis `destroy`.
2. Utiliser Ansible par SSH pour installer et démarrer Nginx et déployer une page Web.
3. Vérifier l'accès à la page et démontrer l'idempotence du playbook.
4. Expliquer la différence : Terraform provisionne l'infrastructure ; Ansible
   configure le système d'exploitation.

**Livrables :** fichiers Terraform, inventaire et playbook Ansible, sorties de
commandes et capture de la page Web.

### Partie 4 — SLA et surveillance

**Objectif :** définir un objectif de disponibilité et vérifier son respect sur des
instances OpenStack.

**À réaliser :**
1. Définir un SLA quotidien de 99,5 %, les instances suivies et la règle permettant
   de décider si une instance est disponible.
2. Écrire un programme Python avec `openstacksdk` qui mesure l'état Nova et la
   connectivité SSH toutes les cinq minutes.
3. Enregistrer les mesures et calculer la disponibilité de chaque instance.
4. Provoquer un arrêt contrôlé, vérifier sa détection et présenter le rapport.
5. Calculer le nombre d'échecs tolérables sur une journée et expliquer l'effet de
   l'échantillonnage.

**Livrables :** définition du SLA, script de surveillance, journal des mesures,
rapport final et captures de la panne et de la récupération.

## Organisation des principaux dossiers

| Chemin | Contenu |
|---|---|
| `part1-cloudsim/` | Projet Java CloudSim Plus, résultats et schéma |
| `part2-openstack/` | Notes, commandes et résultats du laboratoire OpenStack |
| `part3-iac/` | Instructions et éléments Terraform / Ansible |
| `part4-sla/` | Instructions et éléments du suivi SLA |
| `scripts/` | Scripts Terraform, Ansible et de surveillance |
| `screenshots/` | Captures du rapport, classées par partie |
| `scrapping-uae-data/` | Données et notes de recherche sur l'UAE |
| `Rapport_Global_Cloud_Intelligence.md` | Rapport détaillé en Markdown |


