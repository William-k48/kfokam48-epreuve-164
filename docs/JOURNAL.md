# Journal de bord — Épreuve KFOKAM48 (matricule KF48-164)

Journal tenu **au fil de l'eau**. Une entrée par étape du sujet.
Format : **Fait** / **Bloqué (durée)** / **IA (demandé + vérifié)**.

## Étape 1 — Analyser, spécifier, concevoir

**Fait** : cahier des charges complet (10 sections, 21 EF, 17 RG, 8 zones d'ombre tranchées), contrat `api/contrat.yaml` complété (5 opérations imposées + 7 opérations libres + code `429`), les 4 diagrammes Mermaid (D1 cas d'usage, D2 classes, D3 séquence présence, D4 états-transitions en bonus).

**Bloqué** : environ 25 min au total sur (a) la contradiction Q10 / Q15 — tranchée en faveur de Q15 parce que le contrat impose `409` sur une relecture déjà rendue ; (b) le conflit contrat / Q13 sur le remplacement de lien — tranché en distinguant `POST` (création) et `PATCH` (remplacement) ; (c) le trou sur l'ajout manuel de présence — tranché en créant une opération dédiée `POST /api/sessions/{id}/presences` qui force `source = FORMATEUR`.

**IA** : m'a proposé la structure du cahier des charges et un squelette de diagrammes Mermaid. J'ai vérifié chaque décision contre le contrat imposé (`api/contrat.yaml`) et contre les 16 Q/R de `CLIENT.md`. Les codes d'erreur ont été alignés manuellement : `AUTO_RELECTURE` (et non `AUTO_RELECTURE_INTERDITE`), `RELECTURE_DEJA_RENDUE`, `DEJA_PRESENT`, `CODE_EXPIRE`, `PROMOTION_INCONNUE`. J'ai aussi ajouté deux zones d'ombre que l'IA n'avait pas listées : la notification du relecteur (décision A6) et la moyenne nulle (décision A7).

**Jalon** : commit vide `[JALON] analyse` poussé après validation des 4 livrables (CDC, contrat, diagrammes, journal), et **avant** tout commit de code.
### Ticket #14 — Données de démo + 2 tests B6 + README

**Fait** : `DataInitializer` (charge 1 promotion, 10 étudiants, 1 session ouverte avec code `DEMO01`, des présences et des exercices déjà relus — uniquement si la base est vide, donc idempotent), test unitaire `PresenceServiceTest` (3 tests Mockito sur RG1 : code expiré, code inconnu, cas nominal), test d'intégration `PresenceControllerTest` (2 tests sur `POST /api/presences` avec H2 et profil `test`), README d'installation mis à jour (frontend React + Vite justifié en une ligne, `docker compose up`, alternative 3 commandes, endpoints principaux, tests B6, procédure testée depuis un clone vierge).

**Bloqué** : environ 10 min sur le nom du test d'intégration — le ticket disait `PresenceControllerIT`, mais Surefire ignore `*IT` avec `./mvnw test` et modifier le `pom.xml` était interdit. Renommé `PresenceControllerTest` (écart justifié, signalé). Vérifié : `./mvnw test` → 5 tests verts (3 unitaires + 2 d'intégration).

**IA** : m'a généré les 3 nouveaux fichiers et modifié le README. J'ai testé : `./mvnw test` → `BUILD SUCCESS` (5/5), `docker compose down -v` puis `up` → données de démo chargées (1 promo, 10 étudiants, session `DEMO01` active), flux réel (marquer une présence, consulter le tableau), idempotence (redémarrage backend → pas de doublon). Tout conforme.

**Commit** : `feat(demo): donnees de demo, tests B6 et README (Closes #14)`

**Jalon** : commit vide `[JALON] v0.1` posé après le merge de la dernière issue et stabilisation de `main`.

---