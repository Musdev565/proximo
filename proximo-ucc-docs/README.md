# Proximo UCC — Documentation du projet (à lire en premier)

> Nom provisoire : **Proximo UCC** (à revoir plus tard).

Ce dossier explique le projet à un assistant IA (ou à un développeur) afin qu'il puisse
aider à le concevoir et à le coder correctement. Lis les fichiers dans cet ordre :

| Fichier | Contenu |
|---|---|
| `01-vision-et-contexte.md` | Le problème, la solution, les utilisateurs |
| `02-regles-metier.md` | Règles de gestion : catalogue, stock, commandes, statuts |
| `03-architecture-technique.md` | Stack, architecture, concurrence, hébergement gratuit |
| `04-modele-de-donnees.md` | Entités et relations (version de départ) |
| `05-ui-ux.md` | Principes d'interface (style Uber, minimaliste) |
| `06-carte-auditoires.md` | Comment décrire les auditoires + plan 2D |
| `data/auditoires.yaml` | Données des auditoires (à remplir par l'auteur) |
| `07-mvp-et-feuille-de-route.md` | Périmètre du MVP et étapes |
| `08-questions-ouvertes.md` | Questions tranchées et points encore ouverts |
| `09-vision-future-services.md` | Vision future (vitrine de services) — hors MVP |

## Instructions pour l'IA
- Langue de travail : **français** (code et identifiants techniques en anglais ou français, mais cohérents).
- Respecte les sections **[DÉCIDÉ]** : ce sont des choix de l'auteur. Ne les remets pas en cause sans raison forte.
- Les sections **[RECOMMANDÉ]** sont des conseils : applique-les par défaut, mais l'auteur peut les changer.
- Les sections **[À DÉFINIR]** ne sont pas tranchées : pose la question à l'auteur avant d'inventer.
- Le projet doit rester **simple**, **gratuit à héberger** et **réaliste pour un étudiant seul**.
- L'auteur est étudiant en informatique (génie logiciel), développeur Java back-end, et vise un poste
  en banque : le projet doit montrer de la rigueur transactionnelle (concurrence, états, intégrité).
