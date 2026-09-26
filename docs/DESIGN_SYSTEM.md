# Design System — KFOKAM 48

**Source** : identité visuelle KFOKAM 48 (logo) et mission de refonte UI/UX (issue #48).
**Implémentation** : `frontend/src/index.css` (variables), `frontend/src/App.css` (styles), `frontend/src/components/` (composants).
**Règle d'or** : le visuel ne porte **aucune logique métier** — appels API, DTO, routes, statuts et codes d'erreur restent inchangés (voir `JOURNAL.md`, « Analyse préalable »).

---

## 1. Palette de couleurs

| Jeton CSS | Valeur | Rôle | Usage |
|---|---|---|---|
| `--primaire` | `#0077B6` | Primaire | Boutons, liens, focus, item de nav actif |
| `--primaire-fonce` | `#005F8E` | Primaire foncé | Hover des boutons, textes colorés sur fond clair |
| `--primaire-clair` | `rgba(0,119,182,.1)` | Primaire 10 % | Fonds de nav active, hover discrets, pastilles d'icônes |
| `--secondaire` | `#E63946` | Secondaire (corail) | Accents, badges de note < 10, erreurs |
| `--secondaire-fonce` | `#C1121F` | Corail foncé | Texte d'erreur (contraste AA sur fond clair) |
| `--tertiaire` | `#D4A373` | Tertiaire (ocre) | Badges positifs, succès « doux », rôle relecteur |
| `--tertiaire-fonce` | `#8A5A2B` | Ocre foncé | Texte ocre contrasté |
| `--succes` / `--succes-fonce` | `#2A9D8F` / `#1F776D` | Succès | Notes > 15, statut « Relu », confirmations |
| `--alerte` / `--alerte-fonce` | `#F4A261` / `#9A5B21` | Avertissement | Notes 10–15 |
| `--fond` | `#F8F9FA` | Fond de page | Zone de contenu, lignes alternées |
| `--fond-carte` | `#FFFFFF` | Fond de carte | Cartes, sidebar, header |
| `--texte` | `#212529` | Texte principal | **Jamais de noir pur** |
| `--texte-secondaire` | `#495057` | Texte secondaire | Descriptions, labels secondaires |
| `--texte-metadonnee` | `#6C757D` | Métadonnées | Dates, mentions, légendes (AA sur blanc à 13 px+) |
| `--bordure` | `#DEE2E6` | Bordures | Séparateurs, contours de champs |

**Interdits** : violet (héritage template Vite — supprimé), texte gris clair sur blanc, noir pur `#000`.
**Contraste** : chaque paire texte/fond du système respecte WCAG 2.1 AA ≥ 4.5:1 (les variantes `-fonce` existent précisément pour les textes colorés).

> Le logo est **très chargé** : uniquement en petite taille (hauteur 40 px), coin supérieur gauche de la sidebar, jamais agrandi, jamais recréé en CSS/SVG. Fichier : `src/assets/logo-kfokam48.png`.

## 2. Typographie

Police unique : **Inter** (Google Fonts, `display=swap`, fallback `Segoe UI`/`system-ui`). La police du logo n'est jamais utilisée pour le texte.

| Élément | Taille / poids | Jeton |
|---|---|---|
| `h1` | 32 px / 700, `letter-spacing: -0.5px` | base `index.css` |
| `h2` | 24 px / 600 | base |
| `h3` | 18 px / 600 | base |
| Corps | 16 px / 400, `line-height: 1.6` | `html` |
| Labels | 14 px / 600 | `.champ label` |
| Métadonnées | 13 px / 400 | `.session-dates`, `.sidebar-utilisateur`… |
| Code de session | 28 px / 700 mono, `letter-spacing: 4px` | `.code-session` |

## 3. Layout global

```
┌──────────┬──────────────────────────────┐
│ Sidebar  │ Header (64 px, sticky)       │
│ 250 px   ├──────────────────────────────┤
│ (sticky) │ Contenu (fond #F8F9FA,       │
│          │ padding 32 px, max 1100 px)  │
│ logo 40px│                              │
│ nav      │                              │
│ « Invité»│                              │
└──────────┴──────────────────────────────┘
```

- **Sidebar** (`components/Sidebar.jsx`) : logo, navigation (Tableau de bord `/`, Formateur `/formateur`, Étudiant `/etudiant`, Relecteur `/relecteur`), bas = « Invité » + Déconnexion **désactivé** (aucune auth dans le projet — ne pas inventer).
- **Header** (`components/Header.jsx`) : fil d'Ariane `Accueil › Écran` (dérivé de la route), cloche décorative désactivée, hamburger < 768 px.
- **AppLayout** (`layouts/AppLayout.jsx`) : assemble le tout et expose `<Outlet/>`. Les routes ne changent pas.
- **Responsive** : < 768 px sidebar = drawer overlay (voile + Échap + clic hors zone) ; 769–1024 px contenu padding 24 px ; > 1024 px complet.

## 4. Composants (`frontend/src/components/`)

| Composant | API | Usage |
|---|---|---|
| `Card` | `titre`, `idTitre`, `children` | Bloc blanc ombré (`.carte-bloc`), titre relié par `aria-labelledby` |
| `Badge` | `variante`: neutre\|succes\|alerte\|erreur | Pill de statut ou de note |
| `varianteNote(note)` | export nommé | > 15 → `succes`, 10–15 → `alerte`, < 10 → `erreur` (seuils centralisés) |
| `Toast` | `message`, `onFerme`, `duree=3000` | Notification fixe en bas, `role="status"`, disparition auto |
| `EmptyState` | `illustration`, `titre`, `texte` | État vide (illustration SVG + message bienveillant) |

Icônes : **SVG inline** (stroke 2, 20–28 px, `aria-hidden="true"`), aucune dépendance ajoutée.

## 5. Formulaire et boutons

- **Champs** : bordure `#DEE2E6`, focus = bordure primaire + anneau `--ombre-focus` (3 px rgba primaire), disabled = opacité .5 + `not-allowed`, labels toujours liés (`htmlFor`/`id`), placeholders concrets (« Ex. TP 4 — Design patterns »).
- **Boutons** : `.bouton` (primaire) et `.bouton.secondaire` (contour). États hover (foncé), active (1 px vers le bas), `:focus-visible` (anneau), disabled (opacité .5). Icône à gauche du texte quand l'action est porteuse (Lancer, Marquer, Déposer, Copier).

## 6. Feedback et micro-interactions

- Toute action utilisateur a une réaction : confirmation (`.message.succes` / Toast), erreur (`.message.erreur`, `role="alert"`), bouton « Copier » → « Copié ! » 2 s, étapes estompées tant que l'identification n'est pas faite, cartes de l'accueil qui s'élèvent au survol.
- **Animation unique** `apparait` (opacité + translation 4 px, 200–250 ms ease-out), appliquée aux cartes/messages/toast.
- **`prefers-reduced-motion`** : toutes les animations/transitions sont neutralisées (bloc global dans `index.css`).

## 7. Accessibilité (WCAG 2.1 AA)

- Navigation clavier complète : focus visible partout (`:focus-visible` uniforme), drawer fermable à **Échap**, liens de nav réels (`NavLink`).
- `role="alert"` sur toutes les erreurs, `role="status"` sur les succès/états vides/toast, `aria-label` sur les icônes seules (hamburger, cloche), `aria-hidden` sur les icônes décoratives, `aria-current` implicite via `NavLink` actif.
- Texte : jamais `#000`, jamais gris clair sur blanc, hiérarchie de titres sans saut.

## 8. Décisions notables (et pourquoi)

1. **Thème clair unique** : le `prefers-color-scheme: dark` du template a été retiré — l'outil a une identité de marque fixe (bleu/ocre/corail sur fond clair) ; un thème sombre aurait doublé la surface de vérification du contraste.
2. **SVG inline plutôt que `lucide-react`** : zéro dépendance, zéro impact sur `package.json`, rendu maîtrisé — la mission autorisait la dépendance mais ne l'exigeait pas.
3. **« (provisoire) » en italique grisé** : la mention suit le badge de note (issue #47), jamais appliquée quand `noteProvisoire` est `null`.
4. **RG7** : aucune interface n'affiche l'identité de l'auteur/du relecteur — l'API ne l'expose pas et le design n'invente rien.
5. **Logo placeholder** : l'image commitée est un placeholder fidèle généré localement ; déposer le fichier officiel au même chemin (`src/assets/logo-kfokam48.png`) suffit — aucun code à changer.
