# Répartition géographique et profils de charge

Cette fiche transforme les données brutes en paramètres exploitables pour la simulation CloudSim Plus.

## 1. Répartition géographique pour les datacenters

Le scraping ne fournit pas un total exact par établissement : environ 40 % des détails individuels manquent encore. Pour cette raison, on utilise les données nettoyées uniquement pour estimer la répartition entre les villes et les pôles géographiques.

### Répartition estimée

| Pôle | Villes concernées | Part estimée | Utilisateurs estimés |
|---|---|---:|---:|
| Ouest | Tanger, Larache, Ksar El Kebir | ~55 % | ~73 000 |
| Est | Tétouan, Al Hoceima | ~45 % | ~59 700 |

### Formule de calcul

- Nombre total d'utilisateurs estimé : $N \approx 132\ 700$
- Pôle Ouest : $0.55 \times N \approx 72\ 985$
- Pôle Est : $0.45 \times N \approx 59\ 715$

Cette répartition justifie un déploiement en deux datacenters principaux : un datacenter ouest et un datacenter est.

## 2. Définition des profils de charge pour CloudSim Plus

À partir du nombre total d'utilisateurs, on définit deux scénarios de charge :

### Période normale

- 5 % des utilisateurs actifs simultanément
- Charge estimée : environ 6 600 requêtes / seconde

### Période de pointe

- 30 % des utilisateurs actifs simultanément
- Charge estimée : environ 40 000 requêtes / seconde

### Formule de calcul

- Utilisateurs actifs = $N \times \%$ d'utilisateurs simultanés
- Requêtes estimées ≈ utilisateurs actifs

Exemples :

- Période normale : $132\ 700 \times 0.05 \approx 6\ 635$
- Période de pointe : $132\ 700 \times 0.30 \approx 39\ 810$

## 3. Usage dans la simulation

Ces deux scénarios permettent de comparer le comportement du système dans des conditions ordinaires et en surcharge, notamment lors :

- des inscriptions;
- des examens en ligne;
- des périodes de forte utilisation des plateformes pédagogiques.

Le but est de vérifier si l'architecture proposée reste fonctionnelle et rentable en période de pointe.
