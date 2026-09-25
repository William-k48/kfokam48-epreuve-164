# Journal de bord — Épreuve KFOKAM48 (matricule KF48-164)

Journal tenu **au fil de l'eau**. Une entrée par étape du sujet.
Format : **Fait** / **Bloqué (durée)** / **IA (demandé + vérifié)**.

## Étape 1 — Analyser, spécifier, concevoir

**Fait** : cahier des charges complet (10 sections, 21 EF, 17 RG, 8 zones d'ombre tranchées), contrat `api/contrat.yaml` complété (5 opérations imposées + 7 opérations libres + code `429`), les 4 diagrammes Mermaid (D1 cas d'usage, D2 classes, D3 séquence présence, D4 états-transitions en bonus).

**Bloqué** : environ 25 min au total sur (a) la contradiction Q10 / Q15 — tranchée en faveur de Q15 parce que le contrat impose `409` sur une relecture déjà rendue ; (b) le conflit contrat / Q13 sur le remplacement de lien — tranché en distinguant `POST` (création) et `PATCH` (remplacement) ; (c) le trou sur l'ajout manuel de présence — tranché en créant une opération dédiée `POST /api/sessions/{id}/presences` qui force `source = FORMATEUR`.

**IA** : m'a proposé la structure du cahier des charges et un squelette de diagrammes Mermaid. J'ai vérifié chaque décision contre le contrat imposé (`api/contrat.yaml`) et contre les 16 Q/R de `CLIENT.md`. Les codes d'erreur ont été alignés manuellement : `AUTO_RELECTURE` (et non `AUTO_RELECTURE_INTERDITE`), `RELECTURE_DEJA_RENDUE`, `DEJA_PRESENT`, `CODE_EXPIRE`, `PROMOTION_INCONNUE`. J'ai aussi ajouté deux zones d'ombre que l'IA n'avait pas listées : la notification du relecteur (décision A6) et la moyenne nulle (décision A7).

**Jalon** : commit vide `[JALON] analyse` poussé après validation des 4 livrables (CDC, contrat, diagrammes, journal), et **avant** tout commit de code.

### Ticket #13 — `GET /api/promotions` + `GET /api/promotions/{id}/etudiants`

**Fait** : DTO records (`PromotionResponse`, `EtudiantResponse`), `PromotionService` (liste des promotions triées par nom, liste des étudiants d'une promotion triés par nom, `PromotionNotFoundException` si la promotion n'existe pas), `PromotionController` (`@GetMapping` et `@GetMapping("/{id}/etudiants")`).

**Bloqué** : environ 5 min sur une erreur de compilation (`Promotion::getNom` utilisé par erreur dans le stream des étudiants). Résolu en utilisant `Etudiant::getNom` avec un import propre.

**IA** : m'a généré les 4 nouveaux fichiers. J'ai testé : `GET /api/promotions` (`200` + liste triée), `GET /api/promotions/1/etudiants` (`200` + liste triée), promotion inconnue (`404 PROMOTION_INCONNUE`). Tout conforme.

**Commit** : `feat(promotions): implemente GET /api/promotions et /{id}/etudiants (Closes #13)`

---