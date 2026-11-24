# Proximo UCC 🎓🥪

> **Proximo UCC** est une plateforme web/mobile progressive (PWA) de mise en relation directe entre étudiants vendeurs et acheteurs au sein du campus de l'Université Catholique du Congo (UCC).
> Elle résout le problème de l'accès aux collations et repas pendant les pauses courtes, en permettant la commande à l'avance et la livraison directement dans les auditoires par les vendeurs.

---

## 📌 Présentation du Projet

À l'UCC, les pauses entre les cours sont courtes (10h, 11h, midi) et les points de vente extérieurs sont éloignés. De nombreux étudiants préparent et vendent des collations sur le campus, mais manquent de visibilité et d'organisation logistique. 

**Proximo UCC** structure cette économie étudiante :
1. **Modèle de compte unique** : Chaque étudiant s'inscrit avec un compte standard. En un clic (« Je veux vendre »), il peut ouvrir sa boutique sans inscription distincte.
2. **Disponibilité quotidienne** : Chaque matin, les vendeurs confirment leur présence (« Je suis là aujourd'hui ») et saisissent les stocks apportés.
3. **Commandes et livraison** : Les étudiants commandent le matin et jusqu'à 15h30. Les commandes sont regroupées par auditoire pour optimiser le parcours de livraison du vendeur.
4. **Paiement Cash & Gestion rigoureuse du stock** : Paiement en espèces à la livraison. Le stock réel ne diminue que lorsque le vendeur confirme le paiement, avec réservation temporaire pour empêcher toute survente (concurrence gérée en base).
5. **Remise à zéro quotidienne** : À 16h30 (heure de Kinshasa), les disponibilités sont réinitialisées pour le lendemain.

---

## 🛠️ Stack Technique

- **Back-end** : Java 21+ / **Spring Boot** (Architecture en couches `Controller -> Service -> Repository`, DTOs, Spring Security + JWT).
- **Base de données** : **PostgreSQL** avec gestion des migrations via **Flyway**.
- **Gestion de la concurrence** : Verrouillage optimiste (`@Version`) ou pessimiste (`@Lock(PESSIMISTIC_WRITE)`) garantissant `quantite_reservee <= quantite_stock`.
- **Front-end** : **React** (Approche Mobile-First, Tailwind CSS, PWA installable sur smartphone).
- **Stockage Médias** : Bucket S3 compatible (photos produits compressées côté client).
- **Hébergement** : 100% offres gratuites (Render / Supabase / Neon / S3 free-tier).

---

## 📂 Documentation Complète

Le dossier [`proximo-ucc-docs/`](./proximo-ucc-docs/) contient l'ensemble des spécifications fonctionnelles et techniques :

- 📄 [`01-vision-et-contexte.md`](./proximo-ucc-docs/01-vision-et-contexte.md) — Le problème, la solution, le persona et le positionnement.
- 📄 [`02-regles-metier.md`](./proximo-ucc-docs/02-regles-metier.md) — Règles de gestion : catalogue, stock, horaires (15h30 / 16h30), cycle de vie d'une commande.
- 📄 [`03-architecture-technique.md`](./proximo-ucc-docs/03-architecture-technique.md) — Stack, gestion de la concurrence transactionnelle, tâches planifiées.
- 📄 [`04-modele-de-donnees.md`](./proximo-ucc-docs/04-modele-de-donnees.md) — Schéma relationnel et règles d'intégrité.
- 📄 [`05-ui-ux.md`](./proximo-ucc-docs/05-ui-ux.md) — Ergonomie sobre, mobile-first inspirée d'Uber.
- 📄 [`06-carte-auditoires.md`](./proximo-ucc-docs/06-carte-auditoires.md) — Structuration spatiale du campus UCC et plan 2D.
- 📄 [`07-mvp-et-feuille-de-route.md`](./proximo-ucc-docs/07-mvp-et-feuille-de-route.md) — Découpage du MVP et étapes du projet.
- 📄 [`08-questions-ouvertes.md`](./proximo-ucc-docs/08-questions-ouvertes.md) — Points tranchés et questions à affiner.
- 📄 [`09-vision-future-services.md`](./proximo-ucc-docs/09-vision-future-services.md) — Extensions futures (services étudiants, monétisation).

---

## 🗺️ Feuille de Route MVP

1. [x] Documentation et cadrage métier complets
2. [x] Initialisation du dépôt Git
3. [ ] Configuration du repository distant GitHub
4. [ ] Modélisation et initialisation du socle Spring Boot (PostgreSQL + Flyway + tests de concurrence)
5. [ ] Mise en place de l'API REST sécurisée (Auth JWT, Rôles)
6. [ ] Initialisation de l'application front-end React (Mobile-First / PWA)
7. [ ] Intégration du plan 2D des auditoires et du regroupement des commandes
8. [ ] Déploiement gratuit et phase de test terrain
