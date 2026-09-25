# Journal de bord — Épreuve KFOKAM48 (matricule KF48-164)

Journal tenu **au fil de l'eau**. Une entrée par étape du sujet.
Format : **Fait** / **Bloqué (durée)** / **IA (demandé + vérifié)**.

## Étape 1 — Analyser, spécifier, concevoir

**Fait** : cahier des charges complet (10 sections, 21 EF, 17 RG, 8 zones d'ombre tranchées), contrat `api/contrat.yaml` complété (5 opérations imposées + 7 opérations libres + code `429`), les 4 diagrammes Mermaid (D1 cas d'usage, D2 classes, D3 séquence présence, D4 états-transitions en bonus).

**Bloqué** : environ 25 min au total sur (a) la contradiction Q10 / Q15 — tranchée en faveur de Q15 parce que le contrat impose `409` sur une relecture déjà rendue ; (b) le conflit contrat / Q13 sur le remplacement de lien — tranché en distinguant `POST` (création) et `PATCH` (remplacement) ; (c) le trou sur l'ajout manuel de présence — tranché en créant une opération dédiée `POST /api/sessions/{id}/presences` qui force `source = FORMATEUR`.

**IA** : m'a proposé la structure du cahier des charges et un squelette de diagrammes Mermaid. J'ai vérifié chaque décision contre le contrat imposé (`api/contrat.yaml`) et contre les 16 Q/R de `CLIENT.md`. Les codes d'erreur ont été alignés manuellement : `AUTO_RELECTURE` (et non `AUTO_RELECTURE_INTERDITE`), `RELECTURE_DEJA_RENDUE`, `DEJA_PRESENT`, `CODE_EXPIRE`, `PROMOTION_INCONNUE`. J'ai aussi ajouté deux zones d'ombre que l'IA n'avait pas listées : la notification du relecteur (décision A6) et la moyenne nulle (décision A7).

**Jalon** : commit vide `[JALON] analyse` poussé après validation des 4 livrables (CDC, contrat, diagrammes, journal), et **avant** tout commit de code.
### Ticket #9 — `POST /api/relectures/{id}`

**Fait** : entité `Relecture` (mappée sur `V1__init.sql` : `@OneToOne` sur `exercice_id` avec `UNIQUE`, `relecteur_id` NOT NULL, `note` CHECK 0–20, `commentaire` TEXT), `RelectureRepository`, DTO records (`RelectureRequest`, `RelectureResponse`), 3 exceptions (`NoteInvalideException` 400, `AutoRelectureException` 403, `RelectureDejaRendueException` 409), `RelectureService` (transactionnel : création de la relecture + passage de l'exercice à `RELUE`), `RelectureController` (`@PostMapping("/{id}")` → `200 OK`, pas `201`).

**Bloqué** : environ 15 min sur le mapping `NOTE_INVALIDE` vs `VALIDATION_ECHOUEE` — le `@Min/@Max` du DTO déclenchait le handler global (intouchable) au lieu de renvoyer `NOTE_INVALIDE` comme exigé par le contrat. Résolu par un handler local au `RelectureController` (fichier créé par ce ticket), qui mappe les erreurs de validation sur `note` vers `NOTE_INVALIDE`, sans toucher au `GlobalExceptionHandler`. Également signalé : Jackson convertit silencieusement `15.5` en `15` (coercition float→int par défaut), impossible à corriger sans toucher à la config Jackson (interdit).

**IA** : m'a généré les 9 nouveaux fichiers. J'ai testé : nominal (`200` + `RELUE`), note 25 (`400 NOTE_INVALIDE`), note non entière (`400 NOTE_INVALIDE` — via handler local), exercice inconnu (`404 EXERCICE_INCONNU`), relecture déjà rendue (`409 RELECTURE_DEJA_RENDUE`), auto-relecture forcée en BDD (`403 AUTO_RELECTURE`). Tout conforme (sauf la coercition Jackson, signalée).

**Commit** : `feat(relectures): implemente POST /api/relectures/{id} (Closes #9)`

---