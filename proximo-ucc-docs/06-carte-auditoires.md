# 06 — Carte des auditoires (plan 2D)

## Ce que l'on sait [DÉCIDÉ]
- Le bâtiment a **3 niveaux avec auditoires** (+ un sous-sol sans auditoire, ignoré).
- Les auditoires sont **alignés sur une même colonne**, les uns à la suite des autres.
- Chaque auditoire correspond à une promotion/filière (ex. « L2 Sciences informatiques »).

## Objectif du plan
Permettre au vendeur de voir **à quel niveau** et **à quel endroit** se trouve l'auditoire d'une commande,
sans connaître tous les auditoires.

## Comment l'auteur doit décrire les auditoires (modèle à remplir)
Pour **chaque niveau**, décrire les auditoires **dans l'ordre, du début à la fin de la colonne**
(par exemple depuis l'entrée ou l'escalier principal). Pour chaque auditoire, donner :

1. `code` : un identifiant court et unique (ex. `N1-A01`)
2. `nom` : le nom réel utilisé par les étudiants (ex. « Auditoire Mulumba »)
3. `promotion` : promotion(s) qui y ont cours (peut varier ; mettre les plus fréquentes)
4. `position` : numéro d'ordre dans la colonne (1 = le plus proche de l'entrée/escalier)
5. `cote` : si pertinent, de quel côté du couloir il se trouve (gauche/droite en regardant depuis l'escalier)
6. `repere` : un repère visuel facile (escalier, toilettes, secrétariat, porte vitrée…)
7. `capacite` : optionnel

Aussi indiquer, **pour chaque niveau** : où se trouvent l'**entrée**, les **escaliers** / ascenseurs.

Si possible, joindre une **photo** ou un **croquis** à la main du plan de chaque niveau :
l'IA peut s'en servir pour générer le plan 2D.

## Format de données
Voir `data/auditoires.yaml` (modèle avec exemples fictifs à **remplacer** par les vrais auditoires).

## Rendu du plan 2D [RECOMMANDÉ]
- Vue **par niveau** (onglets ou sélecteur 1 / 2 / 3), chaque niveau dessiné comme une **colonne de
  rectangles** étiquetés (SVG généré à partir des données).
- L'auditoire de la commande est **mis en surbrillance** ; les escaliers et l'entrée sont marqués.
- Le plan est généré depuis `auditoires.yaml` / la table `Auditoire` : **pas de dessin codé en dur**.
- À côté du plan, toujours afficher un **texte** : « Niveau 2, 3e auditoire depuis l'escalier, côté gauche ».
