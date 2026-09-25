# Journal de bord — Épreuve KFOKAM48 (matricule KF48-164)

Journal tenu **au fil de l'eau**. Une entrée par étape du sujet.
Format : **Fait** / **Bloqué (durée)** / **IA (demandé + vérifié)**.

## Étape 1 — Analyser, spécifier, concevoir

**Fait** : cahier des charges complet (10 sections, 21 EF, 17 RG, 8 zones d'ombre tranchées), contrat `api/contrat.yaml` complété (5 opérations imposées + 7 opérations libres + code `429`), les 4 diagrammes Mermaid (D1 cas d'usage, D2 classes, D3 séquence présence, D4 états-transitions en bonus).

**Bloqué** : environ 25 min au total sur (a) la contradiction Q10 / Q15 — tranchée en faveur de Q15 parce que le contrat impose `409` sur une relecture déjà rendue ; (b) le conflit contrat / Q13 sur le remplacement de lien — tranché en distinguant `POST` (création) et `PATCH` (remplacement) ; (c) le trou sur l'ajout manuel de présence — tranché en créant une opération dédiée `POST /api/sessions/{id}/presences` qui force `source = FORMATEUR`.

**IA** : m'a proposé la structure du cahier des charges et un squelette de diagrammes Mermaid. J'ai vérifié chaque décision contre le contrat imposé (`api/contrat.yaml`) et contre les 16 Q/R de `CLIENT.md`. Les codes d'erreur ont été alignés manuellement : `AUTO_RELECTURE` (et non `AUTO_RELECTURE_INTERDITE`), `RELECTURE_DEJA_RENDUE`, `DEJA_PRESENT`, `CODE_EXPIRE`, `PROMOTION_INCONNUE`. J'ai aussi ajouté deux zones d'ombre que l'IA n'avait pas listées : la notification du relecteur (décision A6) et la moyenne nulle (décision A7).

**Jalon** : commit vide `[JALON] analyse` poussé après validation des 4 livrables (CDC, contrat, diagrammes, journal), et **avant** tout commit de code.
### Ticket #11 — `GET /api/etudiants/{id}/exercices`

**Fait** : DTO `ExerciceEtudiantResponse` (record : `id`, `sessionId`, `lien`, `statut`, `note` nullable, `commentaire` nullable), `EtudiantService` (liste les exercices de l'étudiant, joint la `Relecture` si elle existe, **n'expose jamais le relecteur** — RG7), `EtudiantController` (`@GetMapping("/{id}/exercices")`). Ajout dans `ExerciceRepository` (`findByEtudiantId`).

**Bloqué** : aucun blocage significatif. Point d'attention : vérifier que la réponse ne contient **aucune** mention du relecteur (anonymat RG7).

**IA** : m'a généré les 3 nouveaux fichiers et modifié `ExerciceRepository`. J'ai testé : nominal (`200` + `note`/`commentaire` `null` si non relu), étudiant inconnu (`404 ETUDIANT_INCONNU`), et vérifié l'**anonymat** (0 occurrence de « relecteur » dans la réponse JSON). Tout conforme.

**Commit** : `feat(etudiants): implemente GET /api/etudiants/{id}/exercices (Closes #11)`

---

### Ticket #12 — `GET /api/etudiants/{id}/relectures`

**Fait** : DTO `RelectureEnAttenteResponse` (record : `exerciceId`, `lien`, `statut`), `RelectureEnAttenteService` (liste les exercices assignés à l'étudiant comme relecteur avec `statut = EN_ATTENTE` uniquement, **n'expose jamais l'auteur**), méthode `@GetMapping("/{id}/relectures")` ajoutée à `EtudiantController` (chemin `/api/etudiants` unique, comme demandé). Ajout dans `ExerciceRepository` (`findByRelecteurIdAndStatut`).

**Bloqué** : aucun blocage significatif. Point d'attention : ne retourner que les exercices `EN_ATTENTE` (pas ceux déjà `RELUE`), et ne pas exposer l'auteur.

**IA** : m'a généré les 2 nouveaux fichiers et modifié 2 fichiers existants (ajouts seuls). J'ai testé : nominal (`200` + uniquement `EN_ATTENTE`), étudiant inconnu (`404 ETUDIANT_INCONNU`), anonymat vérifié (0 occurrence de `auteur`/`etudiantId`/`nom`), non-régression de `GET /{id}/exercices` (#11). Tout conforme.

**Commit** : `feat(etudiants): implemente GET /api/etudiants/{id}/relectures (Closes #12)`

---