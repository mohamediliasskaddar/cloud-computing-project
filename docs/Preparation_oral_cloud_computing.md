# Préparation à l’oral — Projet Cloud Computing

Ce document propose des questions probables et des réponses à adapter à l’oral. Les réponses sont basées sur le rapport du projet ; elles distinguent les résultats réellement observés des hypothèses et des pistes d’architecture. Il est préférable de comprendre les idées et de répondre naturellement plutôt que de réciter mot à mot.

## Présentation d’ouverture (environ 1 minute)

> Mon projet de fin de module porte sur la conception d’une architecture cloud universitaire et sur sa mise en œuvre dans un laboratoire local. Il comprend quatre parties. D’abord, j’ai simulé avec CloudSim Plus une architecture proposée pour l’Université Abdelmalek Essaâdi et comparé des charges normales et de pointe, plusieurs ordonnanceurs et l’autoscaling. Ensuite, j’ai utilisé DevStack, installé dans une machine virtuelle locale, pour créer une infrastructure OpenStack : réseau, routeur, instance CirrOS et application Flask sur Ubuntu. Dans la troisième partie, Terraform provisionne une instance et Ansible y installe Nginx et déploie une page Web. Enfin, j’ai défini un objectif de disponibilité de 99,5 % et écrit un moniteur Python qui vérifie Nova et l’accès SSH. Les résultats montrent le fonctionnement de ces mécanismes dans un laboratoire, mais ne constituent pas une certification de capacité ou de disponibilité en production.

## 1. Vue d’ensemble et environnement

### Questions de cadrage

**1. Quel est l’objectif général du projet ?**  
L’objectif est de relier les concepts du cloud à des expériences concrètes : concevoir une architecture, simuler ses performances, déployer des ressources IaaS/SaaS, automatiser leur création et leur configuration, puis mesurer un engagement de disponibilité.

**2. Pourquoi le projet est-il composé de quatre parties ?**  
Elles couvrent des étapes complémentaires du cycle de vie cloud : l’étude de capacité par simulation, l’exploitation d’une plateforme IaaS, l’automatisation avec Infrastructure as Code, et la supervision contractuelle à travers un SLA.

**3. Quelle partie est indépendante des autres ?**  
La simulation CloudSim Plus est indépendante de l’installation OpenStack. Les parties OpenStack, Terraform/Ansible et SLA réutilisent le même environnement DevStack local.

**4. Où avez-vous installé OpenStack et pourquoi ?**  
DevStack est installé dans une VM Ubuntu Server sous VirtualBox sur ma machine personnelle. Le sujet impose une installation locale, pas une VM hébergée sur AWS, Azure ou un autre cloud.

**5. Quelle est la configuration de la VM de laboratoire ?**  
Elle dispose de 4 vCPU, 12 Go de RAM et environ 68 Go de disque. Elle exécute Ubuntu Server 26.04.1 LTS et DevStack, branche `stable/2026.2`, avec Neutron et OVN.

**6. Pourquoi deux interfaces réseau dans la VM ?**  
L’interface NAT donne à la VM un accès Internet sortant. L’interface Host-only fournit une liaison privée et stable entre l’hôte et la VM, notamment pour SSH et Horizon.

**7. Quelle différence entre `192.168.56.101` et `172.24.4.x` ?**  
`192.168.56.101` est l’adresse Host-only de gestion de la VM DevStack. `172.24.4.x` appartient au réseau externe/floating d’OpenStack et sert aux instances. Ce sont deux réseaux différents.

**8. Pourquoi utiliser un tunnel SSH pour afficher les applications ?**  
Les adresses flottantes sont accessibles dans le réseau du laboratoire, mais pas directement depuis le navigateur de l’hôte dans cette configuration. Le tunnel SSH relaie localement un port, par exemple `localhost:8080`, vers le port applicatif de l’instance.

**9. Qu’est-ce que DevStack ? Est-ce une distribution de production ?**  
DevStack est un outil destiné à installer rapidement un environnement OpenStack de développement et de test. Je l’utilise comme laboratoire pédagogique, pas comme une architecture de production haute disponibilité.

**10. Quelle est la principale limite générale de vos résultats ?**  
Ils démontrent des mécanismes dans un environnement de laboratoire et/ou des modèles synthétiques. Ils ne prouvent ni la capacité réelle de l’université à supporter une charge de production, ni un SLA de 24 heures.

## 2. Conception et simulation avec CloudSim Plus

### Besoin, architecture et hypothèses

**11. Quel besoin universitaire avez-vous modélisé ?**  
Un ensemble de services numériques possibles ou existants — par exemple Moodle, les inscriptions, la messagerie, les examens en ligne et la bibliothèque numérique — avec une charge susceptible d’augmenter lors des périodes d’inscription ou d’examens.

**12. Quelles données universitaires avez-vous utilisées ?**  
Le site officiel relevé dans le rapport indiquait 130 750 étudiants, doctorants inclus, 1 357 enseignants-chercheurs permanents, 598 personnels administratifs et techniques, 10 facultés, 7 écoles et 6 provinces/préfectures couvertes.

**13. Que signifie le total de 132 705 utilisateurs potentiels ?**  
C’est une estimation obtenue par addition des trois catégories d’effectifs : étudiants, enseignants et personnels administratifs et techniques. Ce n’est ni le nombre de comptes réellement créés, ni celui des utilisateurs simultanés.

**14. Pourquoi précisez-vous que les chiffres sont à confirmer ?**  
Le relevé ne donnait pas de date de référence affichée et le scrape avait désactivé la vérification TLS. Je rapporte donc les valeurs publiées comme base de travail, pas comme une source parfaitement validée ou une mesure opérationnelle.

**15. Pourquoi avoir choisi Tanger et Tétouan ?**  
Ce sont deux pôles géographiques proposés pour répartir les ressources et rapprocher les services des établissements. Ce sont des choix de conception pour le modèle, pas des datacenters existants de l’université.

**16. La répartition 55 % / 45 % correspond-elle à du trafic observé ?**  
Non. C’est une hypothèse de dimensionnement appliquée aux deux pôles, faute de statistiques réelles de trafic par site.

**17. Décrivez l’architecture simulée.**  
Elle comprend deux datacenters logiques. Tanger dispose de 8 hôtes et de 6 VM applicatives initiales ; Tétouan de 6 hôtes et de 4 VM. Chaque hôte a 16 PE à 10 000 MIPS par PE, 64 GiB de RAM et 1 TB de stockage. Chaque VM applicative a 4 vCPU à 2 500 MIPS par PE, 8 GiB de RAM et 50 GB de stockage.

**18. Pourquoi dites-vous « datacenters logiques » ?**  
CloudSim modélise des ressources et leur comportement, mais ne crée pas de sites physiques. La simulation ne représente pas les salles, l’alimentation, les liens réseau réels ni la localisation effective des équipements.

**19. Quels modèles de service avez-vous proposés ?**  
IaaS pour fournir les VM, réseaux et volumes ; PaaS pour des environnements applicatifs ou bases de données gérés ; SaaS pour les applications consommées par navigateur, comme Moodle ou un portail étudiant.

**20. Quel modèle de déploiement proposez-vous ?**  
Un cloud hybride : un cloud privé pour les données sensibles et la charge de base, avec une extension potentielle vers un cloud public lors des pics, après analyse de la sécurité, des coûts, du réseau et de la conformité. Cette extension n’a pas été déployée ni simulée.

**21. Quels éléments d’architecture ne sont pas représentés par votre simulation ?**  
Le cloud public et le bursting, les latences réseau, la réplication réelle, les bases de données, les sauvegardes, le plan de reprise d’activité et les mécanismes réels de bascule entre sites.

**22. Pourquoi ne pas utiliser simplement une architecture à un seul datacenter ?**  
Deux pôles permettent d’étudier une répartition géographique et de discuter de résilience et de proximité. Mais la simulation ne prouve pas cette résilience : elle faudrait la mettre en œuvre et tester les défaillances et la réplication.

### Charge et CloudSim

**23. Qu’est-ce qu’un Cloudlet ?**  
Dans CloudSim Plus, un Cloudlet représente une tâche soumise au cloud simulé. Ici, il s’agit d’une tâche CPU synthétique mono-cœur, avec une longueur exprimée en millions d’instructions (MI) ainsi que des tailles d’entrée et de sortie.

**24. Quels profils de Cloudlet avez-vous définis ?**  
LIGHT : 5 000 MI et environ 2 secondes de CPU à 2 500 MIPS ; MEDIUM : 20 000 MI et environ 8 secondes ; HEAVY : 50 000 MI et environ 20 secondes. Les tailles d’entrée/sortie sont respectivement 50/150, 300/300 et 1 000/2 000 KiB.

**25. Quelle charge représente la période normale ?**  
Une hypothèse de simultanéité de 5 % des 132 705 utilisateurs potentiels, soit environ 6 635 personnes, est accompagnée d’un échantillon de 50 Cloudlets : 35 LIGHT, 10 MEDIUM et 5 HEAVY.

**26. Quelle charge représente la période de pointe ?**  
Une hypothèse de simultanéité de 30 %, soit environ 39 812 personnes, est accompagnée d’un échantillon de 300 Cloudlets : 150 LIGHT, 90 MEDIUM et 60 HEAVY.

**27. Pourquoi simuler 50 ou 300 Cloudlets si des milliers d’utilisateurs sont estimés ?**  
Ce sont des échantillons de comparaison du modèle. Je ne les assimile pas à tous les utilisateurs, à des requêtes par seconde ou à une requête par utilisateur. Les utilisateurs simultanés estimés ne sont pas convertis directement en Cloudlets.

**28. Comment les tâches arrivent-elles ?**  
Chaque lot est divisé en dix arrivées espacées de cinq secondes. Cela donne une arrivée étalée dans le temps plutôt qu’un seul envoi instantané de toutes les tâches.

**29. Qu’est-ce que MIPS et MI ?**  
MIPS signifie millions d’instructions par seconde : c’est une capacité de traitement. MI est une quantité de millions d’instructions : c’est la longueur de la tâche. Dans un modèle idéal mono-cœur, le temps CPU isolé est approximativement `MI / MIPS`.

**30. Pourquoi les résultats ne garantissent-ils pas le support de 39 812 utilisateurs simultanés ?**  
La charge est synthétique et n’a pas été calibrée sur des requêtes de production. Il manque notamment le taux réel d’arrivée des requêtes, leur coût CPU, les accès base de données, les échanges réseau et les comportements des utilisateurs.

### Ordonnancement, autoscaling et résultats

**31. Quelle différence entre `CloudletSchedulerSpaceShared` et `CloudletSchedulerTimeShared` ?**  
SpaceShared affecte une capacité à des tâches et peut les laisser attendre qu’une ressource se libère. TimeShared partage le temps CPU entre les tâches actives. Le choix crée un compromis entre attente en file et partage de la capacité ; il ne signifie pas qu’un ordonnanceur est toujours meilleur.

**32. Quelle différence entre autoscaling horizontal et vertical ?**  
L’autoscaling horizontal ajoute ou retire des VM. Le scaling vertical augmente ou diminue les ressources d’une VM existante. Le scénario du rapport ajoute horizontalement des VM.

**33. Quel est le seuil et la limite du scaling simulé ?**  
Le seuil CPU est de 70 %, avec un maximum de 20 VM. Le scénario passe de 10 VM initiales à 20 VM dans la période de pointe avec scaling.

**34. Résumez les principaux résultats.**  
En période normale, 50/50 Cloudlets finissent avec 10 VM, un temps de réponse moyen de 5,10 s et un makespan de 56,76 s. En pointe sans scaling, 300/300 finissent ; le temps moyen est de 11,36 s en SpaceShared et 11,73 s en TimeShared. Avec TimeShared et scaling, le temps moyen tombe à 7,54 s.

**35. Que gagne-t-on avec l’autoscaling dans le scénario de pointe ?**  
Par rapport au TimeShared fixe, le temps de réponse moyen diminue d’environ 36 % (11,73 à 7,54 s) et le makespan d’environ 14 % (76,11 à 65,40 s). Le modèle crée 10 VM supplémentaires.

**36. Quel est le coût de ce gain ?**  
Le coût estimé passe de 0,043376 $ en TimeShared sans scaling à 0,073594 $ avec scaling, soit environ 70 % de plus. C’est une estimation basée sur les tarifs configurés dans le simulateur, pas un devis fournisseur.

**37. Comment expliquer que SpaceShared ait une attente moyenne de file mais un makespan plus long ?**  
Les tâches peuvent attendre qu’une ressource soit libre dans SpaceShared ; le rapport relève une attente moyenne de 3,87 s, contre zéro dans ce run TimeShared. Le makespan dépend de la fin de l’ensemble des tâches et de leur ordonnancement, donc les métriques ne suivent pas nécessairement la même tendance. Les résultats sont ceux de la configuration et du run testés.

**38. Que signifie « toutes les Cloudlets sont terminées » ?**  
Cela signifie que le simulateur a exécuté toutes les tâches synthétiques dans ces scénarios. Cela ne démontre ni l’absence de problème en production, ni un niveau de service utilisateur, ni que la capacité est suffisante pour l’université réelle.

**39. Pourquoi l’utilisation moyenne des hôtes est-elle faible ?**  
La moyenne est calculée sur les 14 hôtes, y compris ceux qui sont peu sollicités. Elle ne représente donc pas uniquement l’utilisation des VM actives. De plus, le volume de Cloudlets est un échantillon synthétique.

**40. Les coûts simulés sont-ils des prix OpenStack ?**  
Non. Ce sont des estimations illustratives appliquées à la durée de vie simulée des VM selon les tarifs paramétrés. OpenStack est une plateforme et ne détermine pas à lui seul un tarif commercial.

**41. Quelle conclusion tirez-vous sur la capacité en pointe ?**  
Pour le workload synthétique testé, les tâches se terminent et le scaling améliore les métriques de temps au prix d’un coût supérieur. Je ne conclus pas que l’architecture supporte réellement 39 812 utilisateurs simultanés.

**42. Que faudrait-il faire pour rendre l’étude plus réaliste ?**  
Mesurer ou obtenir une charge représentative, calibrer les profils de tâches, tester plusieurs niveaux et distributions de charge, intégrer les bases de données et le réseau, puis comparer avec des tests de charge sur une application réelle.

## 3. OpenStack : IaaS et SaaS

### Services et concepts OpenStack

**43. À quoi sert Keystone ?**  
Keystone gère l’identité, l’authentification, l’autorisation et le catalogue des services OpenStack.

**44. À quoi sert Nova ?**  
Nova orchestre le calcul : création, gestion et cycle de vie des instances.

**45. À quoi sert Glance ?**  
Glance gère le catalogue d’images utilisées pour lancer des instances.

**46. À quoi sert Neutron ?**  
Neutron fournit les services réseau : réseaux, sous-réseaux, routeurs, IP flottantes et groupes de sécurité.

**47. À quoi sert Placement ?**  
Placement inventorie et alloue les ressources de calcul disponibles pour les demandes de ressources.

**48. À quoi sert Cinder ?**  
Cinder fournit du stockage bloc persistant, notamment sous forme de volumes que l’on peut attacher à des instances.

**49. Que montrent les commandes de vérification ?**  
`openstack service list` montre le catalogue des services ; `openstack compute service list` présente les services de calcul et leur état ; `openstack network agent list` montre les agents réseau. L’installation utilise OVN, donc les agents observés diffèrent de ceux du backend Neutron classique.

**50. Pourquoi ne voyez-vous pas forcément les agents DHCP/L3 classiques ?**  
Le backend réseau est OVN. La sortie montre notamment `ovn-controller` et `neutron-ovn-agent`. Les agents DHCP et L3 historiques ne sont pas attendus de la même façon dans cette configuration.

### IaaS avec CirrOS

**51. Qu’avez-vous créé pour le test IaaS ?**  
Un réseau privé `net-tp`, un sous-réseau `10.10.10.0/24`, un routeur relié au réseau externe `public`, un groupe de sécurité, une paire de clés et une instance CirrOS à laquelle une IP flottante a été associée.

**52. Quel est le rôle du routeur OpenStack ?**  
Il permet le routage entre le sous-réseau privé et le réseau externe OpenStack. Il ne faut pas le confondre avec une IP flottante : ce sont des ressources différentes.

**53. Qu’est-ce qu’une IP flottante ?**  
C’est une adresse du réseau externe associée à une interface/instance pour permettre une connectivité depuis ce réseau, selon le routage et les règles de sécurité configurés.

**54. À quoi sert le groupe de sécurité ?**  
C’est un filtrage réseau au niveau des ports d’instance. Le groupe de test autorise ICMP et TCP/22 pour le ping et SSH.

**55. À quoi sert une paire de clés ?**  
Elle permet une authentification SSH par clé. La clé privée doit rester protégée, ici avec des permissions restrictives (`chmod 600`).

**56. Pourquoi CirrOS ?**  
CirrOS est une image très légère, adaptée aux tests de démarrage, de réseau et de connectivité dans un environnement aux ressources limitées. Elle n’est pas destinée à héberger une application de production.

**57. Comment avez-vous vérifié le fonctionnement de CirrOS ?**  
Le ping a obtenu trois réponses sur trois depuis la VM DevStack. La connexion SSH a permis de confirmer le nom d’hôte `vm-cirros` et l’adresse privée `10.10.10.101/24`.

**58. Le ping seul prouve-t-il que l’application est opérationnelle ?**  
Non. Il valide une connectivité ICMP de base, pas le fonctionnement d’un service applicatif. Pour un service, il faut aussi tester le port ou envoyer une requête applicative.

**59. Pourquoi une instance peut-elle être créée mais ne pas être joignable ?**  
Plusieurs éléments doivent être corrects : état de l’instance, attachement au réseau, routeur et réseau externe, IP flottante, règles de sécurité, service à l’écoute et chemin réseau.

### SaaS Flask

**60. Décrivez l’instance `vm-saas`.**  
Elle utilise Ubuntu 24.04, le flavor `m1.saas` (1 vCPU, 1 536 MB de RAM, disque de 10 GB), le réseau `net-tp`, l’adresse privée `10.10.10.62` et l’IP flottante `172.24.4.40`.

**61. Quelle application avez-vous déployée ?**  
Une application Web Flask accessible sur le port TCP/5000. Depuis le navigateur hôte, le tunnel SSH expose le service sur `http://localhost:8080`.

**62. Pourquoi avez-vous transféré des paquets `.deb` pour installer Flask ?**  
Les téléchargements depuis l’instance Ubuntu expiraient par le chemin réseau utilisé. J’ai téléchargé les paquets depuis la VM DevStack puis je les ai transférés avec `scp`.

**63. Pourquoi Flask écoute-t-il sur `0.0.0.0:5000` ?**  
Pour écouter sur toutes les interfaces réseau de l’instance, et pas seulement sur l’interface de boucle locale. Il faut alors contrôler l’accès avec le pare-feu/groupe de sécurité et éviter d’exposer le service sans nécessité.

**64. En quoi est-ce du SaaS ?**  
Du point de vue de l’utilisateur, l’application est consommée directement via un navigateur. Dans cette démonstration, l’application reste très simple et son infrastructure sous-jacente est gérée dans l’instance Ubuntu.

**65. Est-ce un SaaS complet prêt pour la production ?**  
Non. C’est une démonstration du modèle de consommation applicative. Un SaaS de production demanderait notamment durcissement, gestion des comptes, supervision, sauvegarde, disponibilité, mises à jour et sécurisation du trafic.

## 4. Infrastructure as Code : Terraform et Ansible

**66. Qu’est-ce que l’Infrastructure as Code ?**  
C’est le fait de décrire l’infrastructure et/ou sa configuration sous forme de code versionnable et reproductible, au lieu de réaliser chaque action manuellement.

**67. Quelle est la différence principale entre Terraform et Ansible ?**  
Terraform provisionne les ressources cloud via l’API OpenStack et suit leur état. Ansible configure le système d’exploitation via SSH, par exemple en installant Nginx et en déployant des fichiers.

**68. Pourquoi utiliser les deux outils ?**  
Ils couvrent deux niveaux distincts : Terraform crée l’instance, le groupe de sécurité et l’IP flottante ; Ansible configure l’instance et le service qui s’exécute dessus.

**69. Quelles ressources Terraform a-t-il décrites ?**  
Le groupe `sg-terraform` avec ingress SSH et HTTP, un port sur le réseau existant `net-tp`, l’instance Ubuntu `vm-terraform`, une IP flottante attachée et des outputs pour le nom et l’adresse.

**70. Pourquoi le réseau existant est-il une data source ?**  
Le réseau `net-tp` est déjà géré par le laboratoire. Terraform le consulte au lieu de le recréer et de risquer de modifier une ressource partagée.

**71. Que font `terraform init`, `plan`, `apply` et `destroy` ?**  
`init` initialise le répertoire et récupère le provider ; `plan` prévisualise les changements ; `apply` applique le plan et crée/modifie les ressources ; `destroy` supprime les ressources gérées par cette configuration.

**72. Qu’est-ce que l’état Terraform ?**  
C’est le suivi des ressources que Terraform gère, utilisé pour faire correspondre la configuration déclarative aux ressources existantes et planifier les changements.

**73. Combien de ressources Terraform ont été créées ?**  
Le plan initial annonçait sept ressources et l’application en a ajouté sept. Après validation, `terraform destroy` les a supprimées.

**74. Pourquoi avoir utilisé `-replace` sur l’instance ?**  
Après un problème de disque/boot, j’ai demandé le remplacement ciblé de l’instance avec `terraform apply -replace=...`, plutôt que de détruire toutes les ressources. Terraform a remplacé uniquement l’instance concernée.

**75. Où se trouvent les identifiants OpenStack ?**  
Ils sont lus depuis la configuration du cloud `devstack-admin` et ne sont pas inscrits en clair dans les fichiers Terraform. Il faut protéger ces fichiers de configuration et ne jamais publier les secrets.

**76. Que fait Ansible dans votre projet ?**  
Il se connecte en SSH à l’instance Ubuntu, installe Nginx, active et démarre le service, puis déploie un template HTML avec le nom de l’étudiant, l’identifiant `imk25` et le nom de l’instance.

**77. Qu’est-ce que l’idempotence et comment l’avez-vous vérifiée ?**  
Une opération idempotente converge vers l’état voulu et peut être relancée sans changements inutiles. La troisième exécution du playbook a affiché `changed=0` et `failed=0`, ce qui montre qu’aucune modification supplémentaire n’était nécessaire à ce moment-là.

**78. Pourquoi l’installation APT a-t-elle nécessité un proxy ?**  
Le chemin de téléchargement direct depuis l’instance posait problème. Un proxy `apt-cacher-ng` installé sur DevStack a permis à l’instance d’obtenir les paquets.

**79. Quelles règles de sécurité sont perfectibles dans la démonstration Terraform ?**  
SSH et HTTP étaient ouverts à `0.0.0.0/0`. Pour la production, SSH devrait être limité au réseau d’administration et HTTP/HTTPS aux clients nécessaires ; il faut aussi privilégier HTTPS.

**80. Pourquoi exécuter Ansible depuis la VM DevStack ?**  
La VM a l’accès réseau requis aux ressources du laboratoire et dispose de l’environnement Linux nécessaire. Le sujet conseille Linux ou WSL, car Ansible ne s’exécute pas directement sous Windows de la même manière.

**81. À quoi sert le template HTML Ansible ?**  
Il permet de générer la page en injectant des variables comme le nom de l’étudiant et celui de l’instance, plutôt que de coder une page différente à la main.

**82. Quelles sont les limites du déploiement Terraform/Ansible ?**  
Il s’agit d’une seule instance dans un laboratoire. Le projet ne met pas en place de cluster, d’équilibrage de charge, de déploiement sans interruption, de sauvegardes ou de pipeline CI/CD.

## 5. SLA et surveillance

**83. Qu’est-ce qu’un SLA ?**  
Un Service Level Agreement est un accord qui définit un niveau de service mesurable, la méthode de mesure, la période d’évaluation et les conséquences si l’objectif n’est pas respecté.

**84. Quel objectif de disponibilité avez-vous défini ?**  
99,5 % par journée pour `cirros-mon-1`, `cirros-mon-2` et `cirros-mon-3`.

**85. Comment définissez-vous une instance « disponible » ?**  
À une mesure, Nova doit indiquer `ACTIVE` et le port TCP/22 doit répondre en moins de trois secondes. Les deux conditions sont nécessaires.

**86. Pourquoi ne pas se contenter de l’état `ACTIVE` de Nova ?**  
`ACTIVE` indique l’état vu par Nova, mais ne garantit pas que le service est joignable. Pendant le test, Nova affichait `ACTIVE` alors que SSH échouait encore ; le moniteur a donc gardé l’état `DOWN`.

**87. Comment le moniteur s’authentifie-t-il et quelles API utilise-t-il ?**  
Il utilise `openstacksdk`, s’authentifie auprès de Keystone, consulte Nova pour l’état des instances et teste ensuite leur connectivité TCP/SSH.

**88. Quelle donnée enregistrez-vous ?**  
Chaque mesure consigne notamment la date, l’instance et son état. Le journal CSV permet de calculer le nombre de mesures disponibles et le taux de disponibilité par instance.

**89. Quelle est la formule de disponibilité ?**  
`Disponibilité (%) = mesures disponibles / mesures totales × 100`.

**90. Que se passe-t-il si l’API OpenStack ne répond pas ?**  
Le cycle est signalé avec un message et sauté. Il n’est pas compté comme une panne d’instance, parce que l’état de celle-ci n’a pas pu être mesuré. C’est une limite méthodologique : l’indisponibilité du système de supervision/API devrait être suivie séparément.

**91. Comment avez-vous provoqué la panne ?**  
J’ai arrêté `cirros-mon-2` puis demandé son redémarrage avec les commandes OpenStack `server stop` et `server start`. Le moniteur a détecté les mesures en échec avant le retour de l’accès SSH.

**92. Quels sont les résultats du test long ?**  
Sur environ deux heures, 24 mesures ont été enregistrées pour chaque instance, soit 72 lignes. `cirros-mon-1` et `cirros-mon-3` ont eu 24/24 mesures disponibles (100 %). `cirros-mon-2` a eu 20/24 disponibles, soit 83,33 %, et l’objectif n’a pas été atteint sur cet échantillon.

**93. Ce test prouve-t-il le respect ou le non-respect d’un SLA journalier ?**  
Non. Il démontre le fonctionnement du moniteur et la détection d’une panne sur une période courte. Ce n’est pas une évaluation complète sur 24 heures.

**94. Combien y a-t-il de mesures par jour avec une fréquence de cinq minutes ?**  
`24 × 60 / 5 = 288` mesures par jour et par instance.

**95. Combien de mesures en échec peut-on tolérer pour rester au-dessus de 99,5 % ?**  
Au plus une. Avec un échec, `287/288 × 100 ≈ 99,65 %`. Avec deux échecs, `286/288 × 100 ≈ 99,31 %`, ce qui est inférieur à l’objectif.

**96. Quelle durée d’indisponibilité représente 0,5 % d’une journée ?**  
`24 h × 0,005 = 0,12 h`, soit 7,2 minutes. Une mesure toutes les cinq minutes rend cependant le résultat discret : une mesure échouée correspond à environ un intervalle de cinq minutes, sans localiser précisément le début et la fin de la panne.

**97. Une panne entre deux mesures peut-elle être manquée ?**  
Oui. Une surveillance par sondage toutes les cinq minutes peut manquer une panne brève survenue entre deux sondages. Une fréquence plus élevée ou une collecte d’événements continue permettrait de mieux caractériser les interruptions.

**98. Pourquoi le résultat `cirros-mon-2` est-il de 83,33 % ?**  
Il y a 20 mesures disponibles sur 24 : `20/24 × 100 = 83,33 %`. Les quatre mesures non disponibles reflètent l’arrêt et la période de récupération observés.

**99. Un port SSH disponible signifie-t-il que toute l’application est disponible ?**  
Pas nécessairement. SSH teste l’accès à l’instance, pas la disponibilité fonctionnelle de l’application. Un SLA applicatif devrait sonder le bon port HTTP/HTTPS et vérifier une réponse attendue ou une transaction représentative.

**100. Comment améliorer ce SLA pour un système réel ?**  
Définir un endpoint applicatif et une fenêtre de mesure précise, surveiller la disponibilité du moniteur lui-même, journaliser les erreurs d’API séparément, utiliser des sondes indépendantes et établir clairement les exclusions, notifications, responsabilités et compensations.

## 6. Questions de recul et de défense

**101. Quelle décision technique vous semble la plus importante dans le projet ?**  
La séparation des responsabilités : CloudSim sert à comparer des scénarios, OpenStack fournit l’infrastructure, Terraform la décrit et la provisionne, Ansible configure l’OS, et le moniteur vérifie un indicateur défini. Chaque outil répond à une couche différente.

**102. Quel compromis illustre le mieux la simulation ?**  
L’autoscaling en pointe : il améliore le temps de réponse et le makespan, mais augmente le nombre de VM et le coût estimé. La décision dépend donc de la valeur accordée à la performance par rapport au coût.

**103. Si vous deviez continuer le projet, que feriez-vous en premier ?**  
Je commencerais par collecter des données réalistes de charge et définir des scénarios applicatifs mesurables. Cela améliorerait la validité de la simulation et permettrait de dimensionner les ressources à partir d’éléments observés plutôt que d’hypothèses.

**104. Quelle partie pourrait être industrialisée le plus facilement ?**  
Les fichiers Terraform et le playbook Ansible peuvent servir de base à une automatisation reproductible, après sécurisation des secrets, restriction des règles réseau, tests et adaptation aux environnements cibles.

**105. Quelle différence entre une simulation, un test de laboratoire et un système de production ?**  
La simulation exécute un modèle abstrait ; le laboratoire utilise des services et ressources réels mais à petite échelle ; la production doit répondre aux exigences réelles de charge, sécurité, disponibilité, exploitation, sauvegarde et conformité.

**106. Quel résultat faut-il éviter de surinterpréter ?**  
Le fait que 300 Cloudlets soient toutes terminées ne doit pas être présenté comme la preuve que 39 812 utilisateurs réels peuvent travailler simultanément. De même, les 24 mesures SLA ne constituent pas un bilan de 24 heures.

**107. Quelles améliorations de sécurité recommanderiez-vous ?**  
Restreindre les groupes de sécurité aux adresses nécessaires, préférer HTTPS, protéger les clés privées et les fichiers de credentials, appliquer le moindre privilège, maintenir les images à jour et éviter d’exposer directement les ports d’administration.

**108. Comment expliquer votre méthode si un chiffre est remis en question ?**  
Je précise d’abord s’il s’agit d’une donnée publiée, d’une hypothèse ou d’un résultat calculé. Ensuite, je donne la formule ou la procédure, puis la limite. Par exemple, 39 812 est une estimation de simultanéité, alors que 300 est le nombre de Cloudlets du scénario simulé.

**109. Que répondez-vous si le professeur demande si vous referiez un choix différemment ?**  
Je conserverais la structure en quatre parties, mais je renforcerais la calibration de la charge avec des données d’application réelles et je définirais le SLA autour d’un test applicatif plutôt que du seul port SSH. Cela rendrait les conclusions plus proches d’un service utilisateur.

**110. Quelle est votre conclusion en une phrase ?**  
Le projet démontre, dans un laboratoire local, une chaîne cohérente allant de la simulation et du déploiement cloud à l’automatisation et à la supervision, tout en explicitant les limites nécessaires avant toute extrapolation à la production.

## 7. Fiche de révision rapide

| Sujet | À retenir |
|---|---|
| Effectifs cités | 130 750 étudiants + 1 357 enseignants + 598 personnels = 132 705 utilisateurs potentiels estimés |
| Architecture simulée | 2 datacenters logiques ; 14 hôtes ; 10 VM au départ ; jusqu’à 20 VM avec scaling |
| Hôte simulé | 16 PE × 10 000 MIPS/PE, 64 GiB RAM, 1 TB stockage |
| VM applicative simulée | 4 vCPU × 2 500 MIPS/PE, 8 GiB RAM, 50 GB stockage |
| Workload normal | 50 Cloudlets ; hypothèse de 5 % de simultanéité |
| Workload de pointe | 300 Cloudlets ; hypothèse de 30 % de simultanéité |
| Scaling | Horizontal ; seuil CPU 70 % ; maximum 20 VM |
| Gain du scaling vs TimeShared fixe | Réponse moyenne : 11,73 s → 7,54 s (~36 % de moins) ; makespan : 76,11 s → 65,40 s (~14 % de moins) |
| Coût estimé en pointe | TimeShared fixe : 0,043376 $ ; avec scaling : 0,073594 $ (~70 % de plus) |
| DevStack | VM locale VirtualBox ; Ubuntu Server 26.04.1 ; 4 vCPU, 12 Go RAM ; DevStack `stable/2026.2` |
| IaaS | CirrOS ; réseau `10.10.10.0/24` ; ICMP et TCP/22 ; IP flottante `172.24.4.201` |
| Flask | `vm-saas` ; IP flottante `172.24.4.40` ; port 5000 ; tunnel hôte `localhost:8080` |
| Terraform/Ansible | Terraform crée les ressources ; Ansible configure Nginx ; Ansible relancé sans changement : `changed=0` |
| Instance Terraform | `vm-terraform` ; IP flottante `172.24.4.84` ; port 80 et SSH pour la démo |
| SLA | Objectif 99,5 % par jour ; Nova `ACTIVE` et TCP/22 joignable en moins de 3 s |
| Échantillon SLA | 24 mesures par instance sur ~2 h ; 20/24 pour `cirros-mon-2` = 83,33 % |
| Seuil journalier | 288 mesures/jour ; au plus 1 échec donne 99,65 % ; 2 donnent 99,31 % |
| Budget d’indisponibilité théorique | 0,5 % × 24 h = 7,2 minutes ; mesure toutes les 5 minutes = précision limitée |

## Conseils pour répondre à l’oral

- Commencer par répondre directement à la question, puis donner une justification ou un chiffre.
- Dire explicitement « mesuré », « simulé », « hypothèse » ou « proposé » : ces catégories ne sont pas interchangeables.
- Ne pas présenter DevStack comme une installation de production, ni le test de deux heures comme une preuve de SLA journalier.
- Si un détail n’a pas été évalué, l’indiquer honnêtement et proposer comment le vérifier.
- Pour expliquer une commande, décrire d’abord son rôle dans le résultat recherché, puis citer les options importantes.
- Pour les chiffres, retenir la formule et l’ordre de grandeur plutôt que seulement le résultat final.
