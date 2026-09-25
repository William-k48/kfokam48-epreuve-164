# Journal de bord — Épreuve KFOKAM48 (matricule KF48-164)

Journal tenu **au fil de l'eau**. Une entrée par étape du sujet.
Format : **Fait** / **Bloqué (durée)** / **IA (demandé + vérifié)**.

## Étape 1 — Analyser, spécifier, concevoir

**Fait** : cahier des charges complet (10 sections, 21 EF, 17 RG, 8 zones d'ombre tranchées), contrat `api/contrat.yaml` complété (5 opérations imposées + 7 opérations libres + code `429`), les 4 diagrammes Mermaid (D1 cas d'usage, D2 classes, D3 séquence présence, D4 états-transitions en bonus).

**Bloqué** : environ 25 min au total sur (a) la contradiction Q10 / Q15 — tranchée en faveur de Q15 parce que le contrat impose `409` sur une relecture déjà rendue ; (b) le conflit contrat / Q13 sur le remplacement de lien — tranché en distinguant `POST` (création) et `PATCH` (remplacement) ; (c) le trou sur l'ajout manuel de présence — tranché en créant une opération dédiée `POST /api/sessions/{id}/presences` qui force `source = FORMATEUR`.

**IA** : m'a proposé la structure du cahier des charges et un squelette de diagrammes Mermaid. J'ai vérifié chaque décision contre le contrat imposé (`api/contrat.yaml`) et contre les 16 Q/R de `CLIENT.md`. Les codes d'erreur ont été alignés manuellement : `AUTO_RELECTURE` (et non `AUTO_RELECTURE_INTERDITE`), `RELECTURE_DEJA_RENDUE`, `DEJA_PRESENT`, `CODE_EXPIRE`, `PROMOTION_INCONNUE`. J'ai aussi ajouté deux zones d'ombre que l'IA n'avait pas listées : la notification du relecteur (décision A6) et la moyenne nulle (décision A7).

**Jalon** : commit vide `[JALON] analyse` poussé après validation des 4 livrables (CDC, contrat, diagrammes, journal), et **avant** tout commit de code.
### Ticket #10 — `GET /api/tableau?promotionId=`

**Fait** : DTO `TableauLigneResponse` (record : `etudiantId`, `nom`, `presences`, `exercicesDeposes`, `moyenne` nullable, `relecturesEnAttente`), `TableauService` (agrégation par étudiant : comptage présences, comptage exercices déposés, moyenne nullable si aucune note — décision A7, comptage relectures en attente), `TableauController` (`@GetMapping` avec `@RequestParam Long promotionId`). Ajouts dans `EtudiantRepository` (`findByPromotionId`), `PresenceRepository` (`countByEtudiantId`), `ExerciceRepository` (`countByEtudiantId`, `countByRelecteurIdAndStatut`), `RelectureRepository` (`findByExerciceEtudiantId`).

**Bloqué** : environ 10 min sur le cas « paramètre `promotionId` manquant » — Spring renvoyait `500` au lieu de `400`. Résolu par un handler local au `TableauController` (fichier créé par ce ticket) qui mappe `MissingServletRequestParameterException` vers `400 VALIDATION_ECHOUEE`, sans toucher au `GlobalExceptionHandler`.

**IA** : m'a généré les 3 nouveaux fichiers et modifié 4 repositories (ajouts seuls). J'ai testé : nominal (`200` + liste triée par nom, moyenne nullable, `relecturesEnAttente` correct), promotion inconnue (`404 PROMOTION_INCONNUE`), paramètre manquant (`400 VALIDATION_ECHOUEE`). Tout conforme.

**Commit** : `feat(tableau): implemente GET /api/tableau (Closes #10)`

---