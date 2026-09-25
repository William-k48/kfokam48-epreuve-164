# D3 — Séquence : « marquer sa présence »

**Source** : `api/contrat.yaml` (opération `POST /api/presences`) et `docs/CAHIER_DES_CHARGES.md` (EF2, EF3, EF4, EF5 ; RG1, RG2, RG3).
**Contrainte** : ce diagramme doit correspondre aux codes HTTP du contrat : `201`, `400`, `409`, `410`, `429`.

```mermaid
sequenceDiagram
    autonumber
    actor E as Étudiant
    participant F as Frontend (React)
    participant C as PresenceController
    participant S as PresenceService
    participant R as PresenceRepository
    participant DB as Base de données

    E->>F: saisit le code de présence
    F->>C: POST /api/presences { code, etudiantId }
    C->>C: @Valid (code non vide, etudiantId non nul)
    C->>S: enregistrerPresence(code, etudiantId)
    S->>R: rechercherSessionParCode(code)

    alt code inconnu (400)
        R-->>S: Optional.empty()
        S-->>C: CodeInconnuException
        C-->>F: 400 { code: "CODE_INCONNU", message: "..." }
        F-->>E: affiche l'erreur

    else étudiant bloqué (429, RG3)
        S->>S: vérifier compteur d'erreurs de l'étudiant
        S-->>C: TropDeTentativesException
        C-->>F: 429 { code: "TROP_DE_TENTATIVES", message: "..." }
        F-->>E: affiche le blocage

    else code expiré (410, RG1)
        S->>S: comparer expirationAt et maintenant
        S-->>C: CodeExpireException
        C-->>F: 410 { code: "CODE_EXPIRE", message: "..." }
        F-->>E: affiche l'expiration

    else étudiant déjà présent (409, RG2)
        S->>R: rechercherPresence(sessionId, etudiantId)
        R-->>S: Optional.of(presence)
        S-->>C: DejaPresentException
        C-->>F: 409 { code: "DEJA_PRESENT", message: "..." }
        F-->>E: affiche le doublon

    else cas nominal (201)
        S->>R: save(Presence)
        R->>DB: INSERT INTO presence ...
        DB-->>R: OK
        R-->>S: Presence enregistrée
        S->>S: remettre compteur d'erreurs à zéro
        S-->>C: PresenceResponse
        C-->>F: 201 { id, sessionId, etudiantId, source: "ETUDIANT" }
        F-->>E: confirme la présence
    end
```

## Légende

| Élément | Signification |
|---|---|
| `->>` | Message synchrone (appel) |
| `-->>` | Message de retour |
| `alt / else / end` | Branchement conditionnel (cas alternatifs) |
| `autonumber` | Numérote automatiquement les étapes |

## Correspondance avec le contrat d'API

| Cas | Code HTTP | Champ `code` (erreur) | Source |
|---|---|---|---|
| Cas nominal | `201` | — (retourne `{ id, sessionId, etudiantId, source }`) | Contrat imposé |
| Code inconnu | `400` | `CODE_INCONNU` | Contrat imposé (commentaire) |
| Étudiant bloqué | `429` | `TROP_DE_TENTATIVES` | Décision A4, Q4 |
| Code expiré | `410` | `CODE_EXPIRE` | Contrat imposé |
| Étudiant déjà présent | `409` | `DEJA_PRESENT` | Contrat imposé |
| Champ manquant | `400` | (validation) | Contrat imposé |

## Correspondance avec les règles de gestion

| Règle | Traduction dans la séquence |
|---|---|
| **RG1** (code expire 15 min) | Branche `code expiré` → `410 CODE_EXPIRE` |
| **RG2** (présence unique) | Branche `étudiant déjà présent` → `409 DEJA_PRESENT` |
| **RG3** (blocage 5 erreurs → 2 min) | Branche `étudiant bloqué` → `429 TROP_DE_TENTATIVES` |

## Correspondance avec les exigences fonctionnelles

| Exigence | Vérification dans la séquence |
|---|---|
| **EF2** (présence avec code valide) | Cas nominal, `201` + `source: ETUDIANT` |
| **EF3** (code expiré → 410) | Branche `code expiré` |
| **EF4** (déjà présent → 409) | Branche `étudiant déjà présent` |
| **EF5** (blocage → 429) | Branche `étudiant bloqué` |