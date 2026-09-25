# D1 — Diagramme de cas d'utilisation

**Acteurs** : Formateur · Étudiant (spécialisé en Relecteur)
**Source** : `docs/CAHIER_DES_CHARGES.md`, sections 2 (Acteurs et rôles) et 4 (Exigences fonctionnelles).

```mermaid
flowchart LR
    %% Acteurs
    F["👨‍🏫 Formateur"]
    E["🎓 Étudiant"]
    R["📝 Relecteur"]

    %% Héritage : le relecteur est un étudiant
    R --|> E

    %% Cas d'usage — Formateur
    UC1(("Ouvrir une session\net obtenir un code"))
    UC2(("Consulter le tableau\nde synthèse"))
    UC3(("Ajouter manuellement\nune présence"))
    UC4(("Réassigner\nle relecteur"))
    UC5(("Consulter la liste\ndes promotions"))

    %% Cas d'usage — Étudiant
    UC6(("Choisir son nom\ndans une liste"))
    UC7(("Marquer sa présence\navec un code"))
    UC8(("Déposer le lien\nd'un exercice"))
    UC9(("Remplacer le lien\nd'un exercice"))
    UC10(("Consulter sa note\net le commentaire"))

    %% Cas d'usage — Relecteur
    UC11(("Consulter ses relectures\nen attente"))
    UC12(("Rendre une relecture\nnote + commentaire"))

    %% Cas d'usage inclus (include)
    UC13(("Vérifier la validité\ndu code de présence"))
    UC14(("Vérifier que l'étudiant\nn'est pas l'auteur"))
    UC15(("Vérifier que la relecture\nn'est pas déjà rendue"))

    %% Relations Formateur
    F --> UC1
    F --> UC2
    F --> UC3
    F --> UC4
    F --> UC5

    %% Relations Étudiant
    E --> UC6
    E --> UC7
    E --> UC8
    E --> UC9
    E --> UC10

    %% Relations Relecteur (hérite de Étudiant mais a ses propres cas)
    R --> UC11
    R --> UC12

    %% Includes
    UC7 -. "<<include>>" .-> UC13
    UC12 -. "<<include>>" .-> UC14
    UC12 -. "<<include>>" .-> UC15
```

## Légende

| Élément | Signification |
|---|---|
| `--\|>` | Héritage : le Relecteur **est un** Étudiant (il cumule les deux rôles) |
| `-. "<<include>>" .->` | Le cas d'usage source **inclut** systématiquement le cas cible |
| Ellipses `(( ))` | Cas d'usage |
| Rectangles avec emoji | Acteurs |

## Correspondance avec les exigences fonctionnelles

| Cas d'usage | Exigence liée |
|---|---|
| Ouvrir une session et obtenir un code | EF1 |
| Consulter le tableau de synthèse | EF16, EF17 |
| Ajouter manuellement une présence | EF6 |
| Réassigner le relecteur | EF11 |
| Consulter la liste des promotions | EF21 |
| Choisir son nom dans une liste | EF19 |
| Marquer sa présence avec un code | EF2, EF3, EF4, EF5 |
| Déposer le lien d'un exercice | EF7 |
| Remplacer le lien d'un exercice | EF8 |
| Consulter sa note et le commentaire | EF15 |
| Consulter ses relectures en attente | EF18 |
| Rendre une relecture (note + commentaire) | EF12, EF13, EF14 |