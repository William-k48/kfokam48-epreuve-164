# kfokam48-epreuve-164 — Application de présence et de relecture par les pairs

Épreuve finale fullstack KFOKAM48 (matricule KF48-164).
Le formateur ouvre une session et obtient un code à 6 caractères ; les étudiants
marquent leur présence avec ce code, déposent le lien de leur exercice et se
relisent entre eux ; le formateur suit tout depuis un tableau récapitulatif.

- **Backend** : Spring Boot 3.3.5 (Java 17, Maven, PostgreSQL 16, Flyway) — port **8085**
- **Frontend** : **React (avec Vite)** — port **4200**. Choix retenu (F1) : Vite offre le démarrage et le build les plus rapides pour une SPA légère, et l'écosystème React est le mieux maîtrisé pour livrer l'interface complète dans le temps imparti.
- **Contrat d'API** : `api/contrat.yaml` (OpenAPI, respecté à la lettre — B2)
- **Documentation** : `docs/` (cahier des charges, diagrammes, journal)

## Prérequis

- **Docker** (avec Docker Compose v2) — recommandé
- Java 17 et Node 20+ — seulement pour l'alternative sans Docker

## Démarrage (Docker Compose)

```bash
docker compose up --build
```

Puis :

- Frontend : http://localhost:4200
- Backend (API) : http://localhost:8085

Au premier démarrage, la base est créée par Flyway (`V1__init.sql`) et un jeu de
**données de démonstration** est chargé (10 étudiants, une session ouverte avec
des présences et des exercices déjà relus) si la base est vide.

## Alternative sans Docker (3 commandes)

```bash
# 1. PostgreSQL local (base epreuve / epreuve / epreuve) en écoute sur 5432
# 2. Backend
cd backend && ./mvnw spring-boot:run
# 3. Frontend
cd frontend && npm install && npm run dev
```

## Endpoints principaux (contrat `api/contrat.yaml`)

| Méthode | Chemin | Rôle |
|---|---|---|
| POST | `/api/sessions` | Le formateur ouvre une session (EF1) |
| POST | `/api/presences` | L'étudiant marque sa présence avec le code (EF2) |
| POST | `/api/exercices` | L'étudiant dépose le lien de son exercice (EF7) |
| POST | `/api/relectures/{id}` | Le relecteur rend note et commentaire (EF12) |
| GET | `/api/tableau?promotionId=` | Tableau récapitulatif du formateur (EF16) |

Toutes les erreurs suivent le format imposé : `{ "code": "...", "message": "..." }` (B4).

## Tests (B6)

```bash
cd backend && ./mvnw test
```

Les tests tournent sur H2 en mémoire (profil `test`) : aucun PostgreSQL requis,
ils passent sur un poste vierge.

## Vérifié depuis un clone vierge

Le parcours `git clone` → `docker compose up --build` → ouverture de
http://localhost:4200 a été testé ; l'application démarre avec les données de
démonstration et l'API répond sur http://localhost:8085.
