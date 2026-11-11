### 1. Décisions Architecturales Clés

#### A. Modèle de Déploiement : **Cloud Hybride**

* **Cloud Privé (On-Premise) :** Héberge les données sensibles (dossiers étudiants, bases de données académiques, examens). Il est dimensionné pour absorber la **charge normale** à coût maîtrisé.
* **Cloud Public (Extérieur / Bursting) :** Utilisé en **période de pointe** (inscriptions, examens) via un mécanisme d'*Auto-Scaling / Cloud Bursting* pour absorber le surcroît de trafic sans surdimensionner inutilement le matériel local.

#### B. Modèle de Services (SPI Framework)

* **SaaS (Software as a Service) :** Plateforme Moodle (E-learning), système de messagerie institutionnelle, portail d'inscription, bibliothèque numérique.
* **PaaS (Platform as a Service) :** Environnements d'exécution pour les chercheurs (containers Docker/Kubernetes, bases de données MySQL/PostgreSQL gérées).
* **IaaS (Infrastructure as a Service) :** Pools de ressources virtuelles (CPU, RAM, Stockage Ceph) gérés via OpenStack.

#### C. Topologie Géographique & Data Centers

Pour couvrir les 6 provinces/préfectures et respecter la répartition 55% / 45% :

1. **Data Center Principal (DC-1 Tanger - Pôle Ouest) :**
* **Localisation :** Cité de l'Innovation / FST Tanger.
* **Couverture :** Tanger, Larache, Ksar El Kebir (55% de la charge).
* **Rôle :** Datacenter Master (héberge le cluster Kubernetes/OpenStack principal).


2. **Data Center Secondaire / DRP (DC-2 Tétouan - Pôle Est) :**
* **Localisation :** Techno-Centre Tétouan.
* **Couverture :** Tétouan, Al Hoceima (45% de la charge).
* **Rôle :** Datacenter Réplique & Plan de Reprise d'Activité (Disaster Recovery Plan).



---

### 2. Dimensionnement du Matériel et des VMs (Sizing)

#### A. Hôtes Physiques (Serveurs Lames / Blade Servers)

* **DC-1 (Tanger) :** 8 Hôtes physiques.
* **DC-2 (Tétouan) :** 6 Hôtes physiques.
* **Spécification type par hôte :** 16 Coeurs CPU (MIPS élevé), 64 GB RAM, 1 TB NVMe, carte réseau 10 Gbps.

#### B. Profils des Machines Virtuelles (VMs)

* **VM-Web/App (Moodle & Services Web) :** 4 vCPU, 8 GB RAM, 50 GB SSD.
* **VM-DB (Base de données master/replica) :** 8 vCPU, 16 GB RAM, 200 GB SSD.

---

### 3. Schéma de l'Architecture (À inclure dans ton rapport)

Voici l'architecture sous forme de diagramme explicatif :

```
                                  [ UTILISATEURS (132,700) ]
                                               |
                                     [ DNS / CDN Global ]
                                               |
                                   [ Load Balancer Global ]
                                               |
                     +-------------------------+-------------------------+
                     | (55% du trafic)                                   | (45% du trafic)
                     v                                                   v
      +-----------------------------+                     +-----------------------------+
      |   DATA CENTER 1 : TANGER    |                     |   DATA CENTER 2 : TÉTOUAN   |
      |-----------------------------|  Lien Dédié Inter-DC|-----------------------------|
      | - 8 Hôtes Physiques         | <-----------------> | - 6 Hôtes Physiques         |
      | - Cluster VMs Web (SaaS)    |      10 Gbps        | - Cluster VMs Web (SaaS)    |
      | - Cluster DB Master (Ceph)  |  (Synchronisation)  | - Cluster DB Replica        |
      +-----------------------------+                     +-----------------------------+
                     |                                                   |
                     +-------------------------+-------------------------+
                                               | (Débordement en période de pointe)
                                               v
                                 [ CLOUD PUBLIC (Cloud Bursting) ]
                                 - Auto-scaling dynamique de VMs

```
