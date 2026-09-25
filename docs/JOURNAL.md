### Ticket #1 — Init backend Spring Boot + frontend React Vite + Docker Compose

**Fait** : création du squelette backend Spring Boot 3.3.5 (Java 17, Maven, wrapper `mvnw` commité) et frontend React 18 + Vite (port 4200). `docker-compose.yml` avec 3 services (`postgres` interne, `backend` sur 8085, `frontend` sur 4200). `application.properties` (port 8085, PostgreSQL, Flyway, `ddl-auto=validate`), `application-dev.properties` (SQL visible), `application-test.properties` (H2 en mémoire). `GlobalExceptionHandler` + `ErrorResponse` au format `{code, message}`. `Dockerfile` multi-stage pour chaque service.

**Bloqué** : environ 20 min sur le build Docker du backend (téléchargement complet des dépendances Maven dans le conteneur, ~10 min) — résolu en relançant le build une fois les couches en cache.

**IA** : m'a généré le `pom.xml`, le `Dockerfile`, le `docker-compose.yml` et les classes d'exception. J'ai vérifié que le port 8085 était bien pris en compte, que Flyway était activé (`ddl-auto=validate` et non `update`), et que le format d'erreur `{code, message}` était respecté. Testé avec `mvnw clean compile` → `BUILD SUCCESS`, `docker compose up` → les 3 conteneurs démarrent.

**Commit** : `feat: init backend Spring Boot + frontend React Vite + Docker Compose (Closes #1)`
### Ticket #2 — Migration Flyway V1 (`V1__init.sql`)

**Fait** : création de `backend/src/main/resources/db/migration/V1__init.sql` avec les 6 tables du diagramme D2 : `promotion`, `etudiant`, `session`, `presence`, `exercice`, `relecture`. Contraintes `UNIQUE` (présence unique par session/étudiant, un seul exercice par couple, une seule relecture par exercice), `CHECK` (source `ETUDIANT`/`FORMATEUR`, statut `EN_ATTENTE`/`RELUE`, note 0–20), index sur les clés étrangères, `relecteur_id` nullable.


**Jalon** : commit vide `[JALON] analyse` poussé après validation des 4 livrables (CDC, contrat, diagrammes, journal), et **avant** tout commit de code.
### Ticket #10 — `GET /api/tableau?promotionId=`

**Fait** : DTO `TableauLigneResponse` (record : `etudiantId`, `nom`, `presences`, `exercicesDeposes`, `moyenne` nullable, `relecturesEnAttente`), `TableauService` (agrégation par étudiant : comptage présences, comptage exercices déposés, moyenne nullable si aucune note — décision A7, comptage relectures en attente), `TableauController` (`@GetMapping` avec `@RequestParam Long promotionId`). Ajouts dans `EtudiantRepository` (`findByPromotionId`), `PresenceRepository` (`countByEtudiantId`), `ExerciceRepository` (`countByEtudiantId`, `countByRelecteurIdAndStatut`), `RelectureRepository` (`findByExerciceEtudiantId`).

**Bloqué** : environ 10 min sur le cas « paramètre `promotionId` manquant » — Spring renvoyait `500` au lieu de `400`. Résolu par un handler local au `TableauController` (fichier créé par ce ticket) qui mappe `MissingServletRequestParameterException` vers `400 VALIDATION_ECHOUEE`, sans toucher au `GlobalExceptionHandler`.

**IA** : m'a généré les 3 nouveaux fichiers et modifié 4 repositories (ajouts seuls). J'ai testé : nominal (`200` + liste triée par nom, moyenne nullable, `relecturesEnAttente` correct), promotion inconnue (`404 PROMOTION_INCONNUE`), paramètre manquant (`400 VALIDATION_ECHOUEE`). Tout conforme.

**Commit** : `feat(tableau): implemente GET /api/tableau (Closes #10)`


**Jalon** : commit vide `[JALON] analyse` poussé après validation des 4 livrables (CDC, contrat, diagrammes, journal), et **avant** tout commit de code.
### Ticket #9 — `POST /api/relectures/{id}`

**Fait** : entité `Relecture` (mappée sur `V1__init.sql` : `@OneToOne` sur `exercice_id` avec `UNIQUE`, `relecteur_id` NOT NULL, `note` CHECK 0–20, `commentaire` TEXT), `RelectureRepository`, DTO records (`RelectureRequest`, `RelectureResponse`), 3 exceptions (`NoteInvalideException` 400, `AutoRelectureException` 403, `RelectureDejaRendueException` 409), `RelectureService` (transactionnel : création de la relecture + passage de l'exercice à `RELUE`), `RelectureController` (`@PostMapping("/{id}")` → `200 OK`, pas `201`).

**Bloqué** : environ 15 min sur le mapping `NOTE_INVALIDE` vs `VALIDATION_ECHOUEE` — le `@Min/@Max` du DTO déclenchait le handler global (intouchable) au lieu de renvoyer `NOTE_INVALIDE` comme exigé par le contrat. Résolu par un handler local au `RelectureController` (fichier créé par ce ticket), qui mappe les erreurs de validation sur `note` vers `NOTE_INVALIDE`, sans toucher au `GlobalExceptionHandler`. Également signalé : Jackson convertit silencieusement `15.5` en `15` (coercition float→int par défaut), impossible à corriger sans toucher à la config Jackson (interdit).

**IA** : m'a généré les 9 nouveaux fichiers. J'ai testé : nominal (`200` + `RELUE`), note 25 (`400 NOTE_INVALIDE`), note non entière (`400 NOTE_INVALIDE` — via handler local), exercice inconnu (`404 EXERCICE_INCONNU`), relecture déjà rendue (`409 RELECTURE_DEJA_RENDUE`), auto-relecture forcée en BDD (`403 AUTO_RELECTURE`). Tout conforme (sauf la coercition Jackson, signalée).

**Commit** : `feat(relectures): implemente POST /api/relectures/{id} (Closes #9)
**Bloqué** : environ 15 min sur le cache Docker — la première exécution de `docker compose up` ne voyait pas le fichier de migration. Résolu avec `docker compose up -d --build backend` (reconstruction de l'image). Flyway a ensuite appliqué la migration : `flyway_schema_history.version = 1, success = t`, 6 tables + la table d'historique.

**IA** : m'a généré le SQL initial à partir du diagramme D2. J'ai vérifié manuellement la correspondance exacte entre les colonnes du SQL et les attributs du diagramme, et j'ai testé que Flyway appliquait bien la migration dans un conteneur neuf (`docker compose down -v` puis `up`).

**Commit** : `feat(db): ajoute la migration Flyway V1 — schéma initial (Closes #2)`
### Ticket #3 — `POST /api/sessions`

**Fait** : entités `Promotion` et `Session` (alignées sur `V1__init.sql`), repositories, DTO records (`SessionRequest`, `SessionResponse`), `PromotionNotFoundException` (404), `SessionService` (génération d'un code 6 caractères `A-Z0-9` via `SecureRandom`, unicité vérifiée, `expirationAt = ouvertureAt + 15 min` — RG1), `SessionController` (`@PostMapping`, `201 Created`).

**Bloqué** : aucun blocage significatif. Le `GlobalExceptionHandler` gérait déjà `MethodArgumentNotValidException` → `400 VALIDATION_ECHOUEE` et `ResourceNotFoundException` → `404` avec code propagé, donc `PromotionNotFoundException` a été couverte par polymorphisme sans modifier le handler.

**IA** : m'a généré les 9 fichiers. J'ai testé les 3 cas du contrat avec `curl` : nominal (`201` + code 6 car. + écart exact 15 min), promotion inconnue (`404 PROMOTION_INCONNUE`), titre vide (`400 VALIDATION_ECHOUEE`). Tout conforme.

**Commit** : `feat(sessions): implemente POST /api/sessions (Closes #3)`
### Ticket #4 — `POST /api/presences`

**Fait** : entités `Etudiant`, `Presence`, enum `SourcePresence` (`ETUDIANT`/`FORMATEUR`), repositories, DTO records, 4 exceptions métier (`CodeInconnuException` 400, `CodeExpireException` 410, `DejaPresentException` 409, `TropDeTentativesException` 429), `CompteurTentativesService` (`ConcurrentHashMap`, seuil 5 erreurs → blocage 2 min), `PresenceService` (ordre strict : blocage → code → expiration → doublon → étudiant → création), `PresenceController`.

**Bloqué** : environ 10 min sur l'ordre des vérifications — il fallait absolument vérifier le blocage **avant** de chercher le code (Q4), et ne **pas** incrémenter le compteur sur un doublon (sinon un étudiant présent 6 fois serait bloqué injustement).

**IA** : m'a généré les 14 fichiers. J'ai testé les 5 cas : nominal (`201` + `source = ETUDIANT`), doublon (`409`), code inconnu (`400` ×5), 6ᵉ tentative (`429`), code expiré (`410`, testé en insérant une session avec `expiration_at` passée). Tout conforme. Non-régression vérifiée.

**Commit** : `feat(presences): implemente POST /api/presences (Closes #4)`
### Ticket #5 — `POST /api/sessions/{id}/presences` (ajout manuel)

**Fait** : `PresenceManuelleRequest` (record), `SessionNotFoundException` (404), méthode `ajouterPresenceManuelle` ajoutée à `PresenceService` (sans toucher à `marquerPresence`), méthode `@PostMapping("/{id}/presences")` ajoutée à `SessionController` (injection de `PresenceService`). L'ajout manuel force `source = FORMATEUR` (RG13, décision A5).

**Bloqué** : aucun blocage significatif. Décision : pas de vérification d'expiration (le formateur agit en admin, Q14), pas de compteur de tentatives (RG3 ne s'applique qu'au flux étudiant avec code).

**IA** : m'a généré les 2 nouveaux fichiers et modifié 2 fichiers existants (ajouts seuls). J'ai testé les 5 cas : nominal (`201 FORMATEUR`), doublon (`409 DEJA_PRESENT`), session inconnue (`404 SESSION_INCONNUE`), étudiant inconnu (`404 ETUDIANT_INCONNU`), body vide (`400 VALIDATION_ECHOUEE`). Non-régression des endpoints #3 et #4 vérifiée.

**Commit** : `feat(presences): implemente POST /api/sessions/{id}/presences (Closes #5)`
### Ticket #6 — `POST /api/exercices`

**Fait** : entités `Exercice` et enum `StatutExercice` (`EN_ATTENTE`/`RELUE`), `ExerciceRepository`, DTO records (`ExerciceRequest`, `ExerciceResponse`), 2 exceptions (`LienInvalideException` 400, `ExerciceDejaDeposeException` 409), `ExerciceService` (ordre : session → étudiant → doublon → lien → assignation), `ExerciceController`. Ajout de `findBySessionId` dans `PresenceRepository`. Logique d'assignation automatique d'un relecteur (RG6) : présents à la session, exclusion de l'auteur, `Collections.shuffle`, `relecteur_id = null` si aucun présent (décision A2).

**Bloqué** : environ 30 min sur la logique d'assignation — il fallait croiser `Presence` et `Etudiant`, puis gérer le cas « aucun présent disponible ». Résolu en ajoutant `findBySessionId` dans `PresenceRepository`. Également environ 10 min sur la validation du lien : `java.net.URI` + vérification du schéma `http`/`https`, avec capture de `URISyntaxException`.

**IA** : m'a généré les 9 nouveaux fichiers et modifié `PresenceRepository`. J'ai testé les 9 cas : nominal (`201 EN_ATTENTE` + `relecteur_id` ≠ auteur vérifié en BDD), doublon (`409`), lien `ftp://` (`400`), lien `pasuneurl` (`400`), session inconnue (`404`), étudiant inconnu (`404`), session sans présence (`201` + `relecteur_id NULL`), non-régression des endpoints #4 et #5. Tout conforme.

**Commit** : `feat(exercices): implemente POST /api/exercices (Closes #6)`
### Ticket #7 — `PATCH /api/exercices/{id}` (Should)

**Fait** : DTO records (`RemplacementLienRequest`, `ExerciceLienResponse`), 2 exceptions (`ExerciceNotFoundException` 404, `RelectureDejaCommenceeException` 409), méthode `remplacerLien` ajoutée à `ExerciceService` (réutilise `validerLien` de l'issue #6), méthode `@PatchMapping("/{id}")` ajoutée à `ExerciceController` (retourne `200 OK`, pas `201`). L'exercice garde son statut `EN_ATTENTE` et son relecteur.

**Bloqué** : aucun blocage significatif. Décision : pas de vérification de l'auteur (pas d'auth dans le projet), pas de vérification de clôture (pas dans le contrat).

**IA** : m'a généré les 4 nouveaux fichiers et modifié 2 fichiers existants (ajouts seuls). J'ai testé : nominal (`200` + nouveau lien), lien invalide (`400 LIEN_INVALIDE`), exercice inconnu (`404 EXERCICE_INCONNU`), body vide (`400 VALIDATION_ECHOUEE`), exercice déjà `RELUE` (`409 RELECTURE_DEJA_COMMENCEE` + lien inchangé en BDD). Non-régression de `POST /api/exercices` vérifiée.

**Commit** : `feat(exercices): implemente PATCH /api/exercices/{id} (Closes #7)`
### Ticket #8 — `PATCH /api/exercices/{id}/relecteur` (Should)

**Fait** : DTO records (`ReassignationRelecteurRequest`, `ExerciceRelecteurResponse`), exception `RelecteurInvalideException` (400), méthode `reassignerRelecteur` ajoutée à `ExerciceService` (ordre : exercice → statut → relecteur → auteur), méthode `@PatchMapping("/{id}/relecteur")` ajoutée à `ExerciceController` (retourne `200 OK`). Le relecteur ne peut pas être l'auteur, l'exercice doit être `EN_ATTENTE`, le statut reste `EN_ATTENTE`.

**Bloqué** : aucun blocage significatif. Décision : pas de vérification que le relecteur est présent à la session (le formateur agit en admin), pas de filtre « relecteur déjà assigné ailleurs » (Q6 = un seul relecteur par exercice, pas l'inverse).

**IA** : m'a généré les 3 nouveaux fichiers et modifié 2 fichiers existants (ajouts seuls). J'ai testé : nominal (`200` + nouveau relecteur), relecteur = auteur (`400 RELECTEUR_INVALIDE`), exercice inconnu (`404 EXERCICE_INCONNU`), relecteur inconnu (`404 ETUDIANT_INCONNU`), exercice déjà `RELUE` (`409 RELECTURE_DEJA_COMMENCEE`), non-régression de `PATCH /api/exercices/{id}` (#7). Tout conforme.

**Commit** : `feat(exercices): implemente PATCH /api/exercices/{id}/relecteur (Closes #8)

---