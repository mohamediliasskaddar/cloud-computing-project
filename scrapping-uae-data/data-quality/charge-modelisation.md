# Modélisation de la charge pour CloudSim Plus

Cette fiche décrit le workload implémenté dans [`part1-cloudsim`](../../part1-cloudsim/). Les durées et tailles sont des hypothèses de laboratoire, faute de mesures de production publiées par l'UAE.

## 1. Types de requêtes synthétiques

Chaque Cloudlet modélise une tâche CPU mono-cœur. Sa longueur est en MI (*Million Instructions*) et ses tailles d'entrée/sortie en KB.

| Profil | Exemple | Longueur | Entrée / sortie | Période normale | Période de pointe |
|---|---|---:|---:|---:|---:|
| LIGHT | Consultation de cours, notes ou planning | 5 000 MI | 50 / 150 KB | 70 % | 50 % |
| MEDIUM | Formulaire, quiz, soumission d'un devoir | 20 000 MI | 300 / 300 KB | 20 % | 30 % |
| HEAVY | Examen ou génération de document | 50 000 MI | 1 000 / 2 000 KB | 10 % | 20 % |

La concentration plus forte de requêtes MEDIUM et HEAVY en pointe est un scénario choisi pour le projet, pas une statistique mesurée. Chaque Cloudlet demande 1 PE ; chaque VM offre 4 PE à 2 500 MIPS par PE. Sans partage ni attente, les durées CPU théoriques sont donc 2, 8 et 20 secondes respectivement.

## 2. Volumes et arrivées

| Scénario | Population active estimée | Cloudlets simulées | LIGHT / MEDIUM / HEAVY |
|---|---:|---:|---:|
| Normal | 6 635 (5 %) | 50 | 35 / 10 / 5 |
| Pointe | 39 812 (30 %) | 300 | 150 / 90 / 60 |

Les nombres de Cloudlets sont des échantillons de benchmark (50 puis 300), et **pas** des nombres de requêtes par seconde ou des tâches par utilisateur. Aucun taux de requêtes par utilisateur n'est publié ; le facteur d'échelle entre utilisateurs et Cloudlets ne peut donc pas servir à extrapoler la performance réelle.

Chaque échantillon est soumis en 10 lots répartis toutes les 5 secondes : le premier lot démarre au début, les neuf suivants arrivent pendant la simulation. Ce profil représente une montée en charge synthétique. Tous les scénarios de pointe utilisent les mêmes 300 tâches, la même répartition géographique et les mêmes arrivées.

## 3. Placement des tâches et VM

- Deux datacenters sont simulés : Tanger (8 hôtes) et Tétouan (6 hôtes).
- Les Cloudlets sont réparties entre les brokers de site en proportion 55/45. Pour 300 tâches, cela donne 165 à Tanger et 135 à Tétouan.
- Les 10 VM de départ sont réparties au plus proche de 55/45 : 6 à Tanger, 4 à Tétouan (arrondi nécessaire, donc 60/40 pour les VM de départ).
- Les VM Web/app ont 4 vCPU, 8 GiB de RAM et 50 GB de stockage. La capacité maximale de l'essai autoscalé est de 20 VM au total.

## 4. Comparaisons contrôlées

1. **Normal / TimeShared :** 50 tâches, 10 VM, sans autoscaling.
2. **Pointe / SpaceShared :** 300 tâches, 10 VM, sans autoscaling.
3. **Pointe / TimeShared :** mêmes 300 tâches et 10 VM, sans autoscaling.
4. **Pointe / TimeShared + HorizontalVmScaling :** même workload, 10 VM initiales ; une VM horizontale supplémentaire est demandée quand une VM atteint 70 % de CPU, jusqu'à 20 VM.

Les essais 2 et 3 comparent les ordonnanceurs en gardant workload et capacité constants. Les essais 3 et 4 mesurent séparément l'effet de l'autoscaling. Le scheduler de chaque VM est instancié séparément.

## 5. Portée et limites d'interprétation

Les résultats permettent de comparer la réponse du modèle au workload synthétique. Ils ne suffisent pas à certifier que l'infrastructure réelle de l'UAE absorbera 39 812 utilisateurs : il faudrait mesurer la charge par utilisateur (requêtes, intensité CPU et fréquence), vérifier les données d'effectifs et calibrer les Cloudlets sur une application réelle.
