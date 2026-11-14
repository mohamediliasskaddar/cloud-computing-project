# Qualité des données et règles de nettoyage

Cette fiche résume les incohérences observées dans les données publiques de l'Université Abdelmalek Essaâdi et les règles appliquées pour obtenir un jeu de données exploitable dans la simulation CloudSim Plus.

> Objectif : ne pas copier les chiffres bruts sans validation, mais construire un modèle cohérent à partir d'hypothèses documentées.

## 1. Données globales

| Indicateur | Valeur brute | Problème détecté | Règle appliquée | Valeur retenue |
|---|---:|---|---|---:|
| Nombre total d'étudiants | 130 750 | Chiffre de la page d'accueil, sans date de référence affichée | À utiliser comme valeur publiée, à confirmer auprès de la source | 130 750 étudiants |
| Enseignants et personnel | 1 357 enseignants / 598 administratifs | Aucune incohérence majeure | Utilisé comme estimation des utilisateurs de la plateforme (personnel + enseignants) | 1 955 utilisateurs staff |

### Calcul du volume total d'utilisateurs

On estime le nombre total d'utilisateurs potentiels comme suit :

- étudiants : 130 750
- enseignants : 1 357
- administratifs : 598
- total estimé : 132 705 utilisateurs potentiels

Ce total sert de base pour la modélisation de la charge et des scénarios de simulation.

## 2. Problèmes détectés par établissement

| Établissement / indicateur | Valeur brute | Problème détecté | Règle appliquée | Valeur retenue |
|---|---:|---|---|---:|
| ENCG Tanger | 27 269 étudiants / 65 enseignants | Valeur d'étudiants étonnamment élevée et difficile à réconcilier avec les autres indicateurs de la page | Ne pas transformer cette valeur en effectif annuel ou inscrit sans confirmation indépendante | Valeur exclue des totaux par établissement |
| EST & ENS Tétouan | 1 580 étudiants (valeur identique) | Copie-collage de template HTML sur le site de l'UAE | Les chiffres sont marqués comme "faible confiance" et lissés par pôle géographique | Ventilation par pôle géographique |
| FSJES vs FSJP / FEG (Tanger / Tétouan) | Doublons d'anciens intitulés de facultés | Anciennes pages non supprimées sur le site officiel | Exclusion des doublons pour éviter de compter deux fois les mêmes étudiants | Données nettoyées |

## 3. Règles de correction conservées

1. Ne pas inventer de données manquantes : on documente les hypothèses.
2. Ne pas utiliser les chiffres d'établissement comme total global : ils sont incomplets et parfois redondants.
3. Utiliser les données d'établissement uniquement pour estimer la répartition géographique entre les pôles (ouest / est).
4. Marquer les valeurs peu fiables comme indicatives et non comme nombres certifiés.

## 4. Conclusion

Les données de l'UAE sont exploitables pour la simulation si elles sont traitées avec une logique de modélisation, pas comme une base exacte sans nettoyage. La méthode retenue consiste à :

- conserver le total global officiel ;
- corriger les anomalies structurelles ;
- répartir les utilisateurs entre deux pôles selon des estimations documentées ;
- tester la robustesse du modèle avec plusieurs scénarios de charge.

