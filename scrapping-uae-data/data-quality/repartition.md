# Répartition géographique et hypothèses de charge

Cette fiche décrit les valeurs de référence transmises à la simulation CloudSim Plus. Les chiffres ci-dessous sont des hypothèses de modélisation, pas des mesures de trafic réel.

## 1. Population de référence

Le relevé du site officiel dans [`data/uae_research.md`](../data/uae_research.md) indique :

| Population | Nombre |
|---|---:|
| Étudiants, doctorants inclus | 130 750 |
| Enseignants-chercheurs permanents | 1 357 |
| Personnel administratif et technique | 598 |
| **Utilisateurs potentiels estimés** | **132 705** |

Formule : `130 750 + 1 357 + 598 = 132 705`. On conserve **132 705** dans les calculs, sans le confondre avec le seul nombre d'étudiants. Les données n'ont pas de date de référence affichée et le scraping est signalé comme effectué sans vérification du certificat TLS ; elles doivent donc être confirmées sur la source avant d'être présentées comme des chiffres certifiés.

## 2. Répartition proposée entre les deux pôles

La répartition **55 % Ouest / 45 % Est** est une hypothèse de conception issue des données d'établissements incomplètes ; elle n'est pas publiée comme telle par l'université.

| Pôle | Villes retenues | Part hypothétique | Utilisateurs calculés |
|---|---|---:|---:|
| Ouest | Tanger, Larache, Ksar El Kebir | 55 % | 72 988 |
| Est | Tétouan, Al Hoceima | 45 % | 59 717 |
| **Total** | | **100 %** | **132 705** |

Les résultats sont arrondis à l'utilisateur le plus proche. Cette répartition sert à placer les utilisateurs et les VM dans le modèle, pas à affirmer une répartition officielle de l'effectif.

## 3. Niveaux d'activité estimés

| Période | Activité simultanée supposée | Utilisateurs actifs estimés |
|---|---:|---:|
| Normale | 5 % | 6 635 |
| Pointe | 30 % | 39 812 |

Formule : `utilisateurs actifs = 132 705 × taux de simultanéité`.

Les taux de 5 % et 30 % sont des hypothèses de scénario. Le site ne publie pas de télémétrie Moodle, de taux de requêtes par utilisateur, ni de débit de requêtes/seconde. **Les nombres d'utilisateurs actifs ne doivent donc pas être présentés comme des requêtes/seconde.**

## 4. Échantillon de simulation

CloudSim utilise un échantillon de **50 Cloudlets en période normale** et **300 Cloudlets en pointe**. Le rapport 1:6 entre les deux échantillons suit le rapport entre les activités supposées de 5 % et 30 %. Il sert à comparer les scénarios avec un volume gérable ; ce n'est pas une conversion mesurée d'un utilisateur en Cloudlet.

Les résultats indiquent comment l'infrastructure simulée traite ces charges synthétiques. Ils ne constituent pas une prévision de production pour 6 635 ou 39 812 utilisateurs tant qu'une charge réelle par utilisateur n'a pas été mesurée et intégrée.
