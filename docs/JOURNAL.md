### Issue #48 — Commit 5 : écran étudiant

**Fait** : `EtudiantPage.jsx` réorganisé en **stepper de 3 étapes** — chaque section devient une `.carte-bloc` avec pastille numérotée (`.etape-numero`, fond primaire) : **1 — Qui êtes-vous ?** (les deux selects ; une fois l'étudiant choisi, le formulaire se **masque et se réduit à un résumé** badge vert ✓ + nom + promotion, `role="status"`), **2 — Marquer ma présence** (code en grand style mono/letter-spacing, bouton avec icône ✓, confirmation « ✓ Présence marquée, merci ! » animée), **3 — Déposer un exercice** (bouton avec icône upload). Les étapes 2 et 3 sont **estompées** (`.etape-verrouillee`, opacité 0.6) tant que l'étudiant n'est pas identifié — les champs restent accessibles (aucune fonctionnalité retirée, seulement une guidance visuelle). Section « Mes exercices et notes » : le tableau devient des **cartes individuelles** (`.notes-cartes`, grille auto-fill) avec lien cliquable, **badge de statut** (« En attente » neutre / « Relu » vert — libellés lisibles, valeurs API inchangées), **note en gros en badge coloré** (>15 vert, 10–15 orange, <10 rouge, « — » neutre) avec **mention « (provisoire) » conservée** si `noteProvisoire === true`, commentaire en dessous. Mention RG7 : aucun relecteur affiché.

**Pas touché (logique métier)** : `marquerPresence`, `deposerExercice`, `getExercicesEtudiant`, `getPromotions`, `getEtudiants`, tous les états et effets, les 6 codes d'erreur mappés à l'identique, `noteProvisoire` (même condition stricte `=== true`), RG7, les `role="alert"/"status"`, la coercion `Number()` des ids.

**Adaptations de tests (autorisées, vérifications inchangées)** : (1) `findByText('Présence marquée, merci !')` → `findByText(/Présence marquée, merci !/)` car le « ✓ » décoratif casse le nœud texte exact ; (2) `getByText('RELUE')` → `getByText('Relu')` (libellé lisible du badge) ; (3) le test provisoire ciblait la carte via le commentaire du 2ᵈ exercice — ambigu depuis les cartes (le « — » du commentaire) → ciblage par le lien unique `https://exercice.example/7` puis `.closest('.note-carte')`.

**Bloqué** : encore l'instabilité workers Vitest (un timeout 5 s sur le test de présence en run complet, passe seul et au run suivant 21/21) — mêmes symptômes que #34 ; signalé, aucun changement de code.

**IA** : a refondu la page en stepper, les cartes de notes et adapté les 3 tests. Vérifié : `npm run build` → ✓ built in 1.50s, `npx vitest run` → 21/21 (fichier seul 4/4 aussi), `npm run lint` → 0 erreur.

**Commit** : `feat(ui): refait l'ecran etudiant`

---

### Issue #48 — Commit 4 : écran formateur

**Fait** : `FormateurPage.jsx` réorganisé en **layout 2 colonnes** (`.formateur-grille` 1fr/2fr, empilé sous 1024 px), chaque section dans une `.carte-bloc` ombrée. Colonne gauche « Ouvrir une session » : bouton renommé **« Lancer la session »** (libellé imposé par la mission) avec icône lecture ; après création, le **code s'affiche en gros** (`.code-session` : mono 28 px, letter-spacing 4 px) avec bouton **« Copier »** (presse-papier `navigator.clipboard`, retour « Copié ! » 2 s, repli silencieux si le presse-papier est indisponible — le code reste affiché), dates en méta 13 px. Colonne droite « Tableau de synthèse » : lignes alternées (fond gris très clair), **badges de couleur sur la moyenne** (BadgeNote : >15 vert succès `#2A9D8F`, 10–15 orange alerte `#F4A261`, <10 rouge `#E63946`, « — » badge neutre si null — le format d'affichage `formaterMoyenne` est **inchangé**), colonne « Actions » avec icône œil **décorative** (title « Détail à venir », aucun handler — pas de logique de détail dans le contrat). Sous-titre d'accroche ajouté.

**Pas touché (logique métier)** : `ouvrirSession`, `getPromotions`, `getTableau`, champs `titre`/`promotionId`, format « — » si moyenne null, erreurs `PROMOTION_INCONNUE` (message identique), `role="alert"/"status"` conservés.

**Adaptation de test (autorisée, texte seul)** : `FormateurPage.test.jsx` cherchait le bouton `name: 'Ouvrir la session'` → renommé `name: 'Lancer la session'`. La vérification (l'ouverture affiche le code généré) est **inchangée**.

**Bloqué** : un échec intermittent supplémentaire de workers Vitest observé sur `EtudiantPage.test.jsx` lors d'un run complet (passe seul 4/4, passe au run suivant) — même cause documentée (pool threads Windows, #34) ; aucun lien avec le layout formateur.

**IA** : a réorganisé la page, ajouté BadgeNote et la copie du code, adapté le libellé du test. Vérifié : `npm run build` → ✓ built in 1.86s, `npx vitest run` → 21/21, `npm run lint` → 0 erreur.

**Commit** : `feat(ui): refait l'ecran formateur`

---

### Issue #48 — Commit 3 : écran d'accueil

**Fait** : `App.jsx` — accueil en grille de **3 colonnes** (desktop), 1 colonne (mobile, ≤768 px), auto-fit (tablette) : chaque carte contient une **icône colorée par rôle** (formateur = primaire bleu, étudiant = succès vert, relecteur = tertiaire ocre, pastille 56 px), titre `<span class=carte-titre>` (style titre), description 1–2 lignes, et un « bouton » **Accéder** (span stylé `.bouton` avec `pointer-events: none` — c'est la carte entière qui est le lien `Link`, aucun lien imbriqué). Hover : élévation `translateY(-3px)` + `box-shadow` élevée + le pseudo-bouton fonce (par couleur de rôle). Libellés des écrans et routes **inchangés**. Styles ajoutés à `App.css` (`.accueil-*`).

**Pas touché (logique métier)** : `Link`/routes, données affichées, aucun appel API (l'accueil n'en fait aucun), tests.

**Bloqué** : aucun blocage significatif.

**IA** : a refondu l'accueil et ses styles. Vérifié : `npm run build` → ✓ built in 1.91s, `npx vitest run` → 21/21, `npm run lint` → 0 erreur.

**Commit** : `feat(ui): refait l'ecran d'accueil`

---

### Issue #48 — Commit 2 : layout global (Sidebar + Header)

**Fait** : nouveau `src/components/Sidebar.jsx` (logo KFOKAM 48 en haut à gauche, hauteur 40 px, image importée — jamais recréé en CSS/SVG ; navigation NavLink avec état actif stylé fond primaire-clair + barre interne, icônes SVG inline maison — choix documenté : aucune dépendance ajoutée à package.json ; bas de sidebar « Invité » + bouton « Déconnexion » désactivé, aucune auth inventée ; mobile : drawer overlay avec voile, fermeture par clic extérieur, Échap ou navigation), `src/components/Header.jsx` (fil d'Ariane « Accueil › Écran » dérivé de la route via useLocation — libellés d'écrans inchangés, cloche de notifications décorative désactivée avec aria-label, hamburger mobile), `src/layouts/AppLayout.jsx` (Sidebar + Header + `<Outlet/>`, état du drawer local au layout). `App.jsx` : les 4 routes sont **inchangées**, simplement enveloppées dans `<Route element={<AppLayout />}>`. `App.css` : styles sidebar (sticky 100svh, largeur variable 250px), header (sticky 64px), fil d'Ariane, voile, responsive (sidebar fixed translateX(-100%) sous 768 px, hamburger visible ; tablette 769–1024 px padding réduit).

**Pas touché (logique métier)** : routes, appels API, pages, tests, `client.js`/`useApi.js`, `main.jsx` (l'enveloppe passe par le routing imbriqué, pas par main).

**Bloqué** : environ 15 min d'instabilité des workers Vitest sur ce poste (résultats différents entre exécutions successives : 5 échecs puis 1 échec puis 21/21 sans changement de code) — comportement déjà rencontré au ticket #34, lié au pool `threads` Windows. La ligne de base fiable `npx vitest run` donne 21/21 sur 3 exécutions consécutives. Aucun test n'a été modifié, aucun échec ne concerne le layout (les échecs intermittents touchaient des tests API existants non liés).

**IA** : a créé Sidebar/Header/AppLayout et les styles. Vérifié : `npm run build` → ✓ built in 4.15s, `npx vitest run` → 21/21 (×3), `npm run lint` → 0 erreur/0 warning (17 fichiers).

**Commit** : `feat(ui): ajoute le layout global (Sidebar + Header)`

---

### Issue #48 — Commit 1 : design system (variables, typographie, couleurs)

**Fait** : `index.css` réécrit entièrement : palette de marque (primaire `#0077B6`/`#005F8E`, secondaire corail `#E63946`, tertiaire ocre `#D4A373`, succès `#2A9D8F`, alerte `#F4A261`, neutres `#F8F9FA/#212529/#6C757D/#DEE2E6` — jamais de noir pur, jamais de gris clair sur blanc), échelle typographique imposée (h1 32/700, h2 24/600, h3 18/600, corps 16/400 lh 1.6, labels 14/600, méta 13), variables du layout (`--sidebar-largeur: 250px`, `--header-hauteur: 64px`), ombres/rayons, `:focus-visible` uniforme, bloc `prefers-reduced-motion`. **Le violet `#aa3bff` du template Vite est supprimé partout** (plus de `prefers-color-scheme: dark` : thème clair de marque unique, décision documentée). Alias de compatibilité (`--accent`, `--text`, `--bg`, `--border`, `--shadow`…) pointant vers les nouvelles variables pour ne pas casser les classes existantes avant leur refonte. `App.css` refondu : base commune (layout flex, cartes, formulaires avec focus primaire + ombre, boutons avec états hover/active/focus-visible/disabled, messages, tableaux, badges, animation `apparait` 0.2s). `index.html` : police **Inter** via Google Fonts (preconnect + display=swap), meta description, titre « KFOKAM 48 — Suivi des exercices ». Logo : `src/assets/logo-kfokam48.png` créé (480×120 PNG placeholder généré par script — le fichier officiel du client n'étant pas disponible comme binaire dans cette session, il suffira de déposer l'image officielle au même chemin, le composant Sidebar le chargera tel quel).

**Pas touché (logique métier)** : aucun JSX, aucun appel API, aucune route, aucun test — les alias CSS garantissent que l'UI actuelle reste visuellement correcte pendant la transition.

**Bloqué** : environ 10 min — le script de génération du logo a été écrit au mauvais chemin (racine puis `frontend/` au lieu du projet) à cause du répertoire de travail du client ; corrigé, script exécuté puis supprimé.

**IA** : a rédigé le design system et la feuille de styles. Vérifié : `npm run build` → ✓ built in 2.05s, `npm test` → 21/21, `npm run lint` → 0 erreur, 0 warning.

**Commit** : `feat(ui): ajoute le design system (variables, typographie, couleurs)`

---

## Analyse préalable — Refonte UI/UX (issue #48, ÉTAPE 0)

**Fait** : exploration complète du frontend avant toute modification : `App.jsx` (accueil 3 cartes + `<Routes>` 4 routes), `main.jsx` (`BrowserRouter`), 3 pages (`FormateurPage` : ouverture session + tableau ; `EtudiantPage` : identification 2 selects + présence + dépôt + notes ; `RelecteurPage` : identification + liste + formulaire par relecture), couche API (`client.js` 12 fonctions, `useApi` hook), styles (`index.css` variables dont accent **violet `#aa3bff` à supprimer**, `App.css` ~340 lignes classes `.page/.section/.formulaire/.champ/.bouton/.message/.tableau/.carte`), tests (4 fichiers, 21 tests Testing Library qui ciblent **labels, textes et rôles, pas le markup CSS**), `index.html` (lang=fr, titre), `vite.config.js` (proxy /api → 8085, config vitest pool threads), `package.json` (react 19.2.8 — CDC annonce 18, écart déjà signalé à l'issue #32 ; aucune lib UI/icônes installée).

**Réponse à la question imposée — « Est-il possible de refaire l'UI/UX sans casser la logique existante ? Quels sont les risques ? Quels fichiers modifier, lesquels ne pas toucher ? »**

**OUI, c'est possible avec un risque faible et maîtrisé**, pour trois raisons structurelles :
1. **La logique est déjà isolée** : aucun `fetch` hors de `client.js`, aucune règle métier dans les pages (RG17), les pages ne font qu'appeler les fonctions API et afficher `{ data, loading, error }` du hook `useApi`. Réécrire le JSX des pages ne touche pas cette mécanique.
2. **Les tests sont robustes au changement de markup** : ils interrogent le DOM accessible (labels `getByLabelText`, textes `findByText`, rôles), pas les classes CSS. Je peux donc changer le layout, les classes et les composants tant que je conserve les libellés, labels et rôles — seuls 2 tests besoin d'adaptation prévisible (markup des cartes de notes → `getByText('12')` et structure des lignes exercice).
3. **Le layout demandé (Sidebar/Header) est purement additif** : un `AppLayout` qui enveloppe `<Routes>` dans `App.jsx` ne modifie ni les routes, ni les appels, ni les états.

**Risques identifiés et parades** :
- **R1 — Tests qui cassent** (le plus probable) : l'écran étudiant devient un stepper et ses sections se masquent/affichent conditionnellement → des tests qui cherchent un champ présent par défaut peuvent échouer. Parade : conserver tous les textes/labels/rôles actuels, ne masquer l'étape 1 qu'après sélection (comportement demandé), adapter les tests **sans changer ce qu'ils vérifient** (ce qui est permis).
- **R2 — RG7 (anonymat)** : le cahier demande d'afficher « le nom de l'étudiant » sur les cartes du relecteur — **l'API ne l'expose pas** (volontaire, RG7). Je ne l'invente pas : les cartes resteront « Exercice #id » avec lien, comme aujourd'hui. C'est déjà conforme, je le signale.
- **R3 — Logo non fourni** : le fichier `frontend/src/assets/logo-kfokam48.png` n'existe pas encore dans le dépôt ; vous le fournissez après cette analyse. Je coderai le layout avec un emplacement réservé (alt correct, hauteur 40 px) et je brancherai l'image à réception — ou j'utiliserai l'image fournie immédiatement si elle est là avant le commit 2.
- **R4 — React 19 vs 18** : le CDC impose React 18, le projet tourne en 19.2.8 (écart connu, signalé à l'issue #32, hooks identiques). Je n'y touche pas (changement de version = risque logique, hors périmètre visuel).
- **R5 — Copy du relecteur** : « Aucune relecture en attente. Bravo ! » → « Tout est à jour. Reposez-vous ! » : changement de texte **vérifié par un test** (`findByText('Aucune relecture en attente. Bravo !')`) → adaptation du test prévue au commit 6/8 (texte seul, pas la vérification).
- **R6 — Icônes** : aucune lib d'icônes installée. Deux options : SVG inline (zéro dépendance, style maîtrisé) ou `lucide-react` (justification demandée dans la mission). **Je recommande les SVG inline** : aucune modification de `package.json`, aucun risque supply-chain,contrôle total du rendu — je prendrai cette voie sauf contre-ordre.

**Fichiers à modifier** : `index.css` (variables design system), `App.css` (refonte complète), `index.html` (police + titre), `App.jsx` (accueil + enveloppe layout), `main.jsx` (seulement si besoin d'y placer le layout), les 3 pages (JSX visuel uniquement), nouveau `src/components/` (Sidebar, Header, Card, Badge, Toast…), nouveau `src/layouts/AppLayout.jsx`, nouveau `src/assets/logo-kfokam48.png` (à réception), les 4 fichiers de test (adaptation markup seulement), nouveau `docs/DESIGN_SYSTEM.md`, entrées `docs/JOURNAL.md`.

**Fichiers intouchables (confirmé)** : `src/api/client.js` et `src/api/useApi.js` (interdits), `vite.config.js` (interdit — la config vitest existante suffit), `nginx.conf` (interdit), tout `backend/` (interdit), `package.json` (sauf dépendance UI justifiée — aucune ne l'est avec les SVG inline).

**Stratégie** : branche `feat/48-refonte-ui-ux` créée depuis `feat/43-deux-relecteurs` (pour partir de l'état réel livré avec noteProvisoire — à valider : si vous préférez partir de `main`, dites-le, le coût est nul à ce stade). Commits atomiques dans l'ordre imposé (design system → layout → accueil → formateur → étudiant → relecteur → composants réutilisables → tests → docs), vérifications build+tests+lint après chaque commit, backend vérifié (compile+test) en début et fin de mission pour prouver qu'il n'a pas bougé.

**En attente de** : feu vert + logo KFOKAM 48. Aucune ligne de code UI écrite à ce stade.

---

### Issue #47 — Affichage de la note provisoire (frontend)

**Fait** : écran étudiant, section « Mes exercices et notes » : quand `noteProvisoire` vaut `true` (un seul des deux relecteurs a rendu — RG5 v2, contrat v1.1), la note est suivie de la mention italique « (provisoire) » (style `.note-provisoire` dans `App.css`) ; `noteProvisoire` à `false` (deux relectures rendues, note définitive) ou `null` (aucune relecture rendue, note « — ») n'affiche aucune mention. Aucune logique métier côté front (RG17) : le flag vient tel quel de `GET /api/etudiants/{id}/exercices`, la moyenne est celle calculée par l'API. `client.js` inchangé (les signatures n'ont pas changé). Test Vitest ajouté : note 12 + `noteProvisoire: true` → « 12 » + « provisoire » affichés, exercice sans relecture → « — ».

**Bloqué** : aucun blocage significatif. Point de vigilance respecté : la mention ne s'affiche QUE sur `noteProvisoire === true` strict — un exercice sans note (`noteProvisoire: null`) ne doit pas être marqué provisoire.

**IA** : m'a modifié la page, le style et le test. J'ai vérifié que la mention apparaît/disparaît selon les 3 valeurs du flag, que le RG7 reste respecté (aucune mention de relecteur), et `npm run build` → OK, `npm test` → 21/21, `npm run lint` → 0 erreur.

**Commit** : `feat(frontend): affiche la note provisoire (#47)`

---

### Issue #46 — Deux relecteurs et note moyenne (services backend)

**Fait** : adaptation des services à RG5 v2/décision A9 : `ExerciceService.deposerExercice` (désormais `@Transactional`) assigne DEUX relecteurs DISTINCTS tirés au hasard parmi les présents hors auteur (`Collections.shuffle` + `limit(2)`, 1 seul si un seul candidat — RG14 v2) et construit les `AssignationRelecture` via `exercice.ajouterAssignation` ; `reassignerRelecteur` (RG15 v2) complète la 2ᵉ assignation si l'exercice en a moins de 2, ou remplace le premier relecteur qui n'a PAS encore rendu (un relecteur ayant rendu est figé, RG9) ; `RelectureService.rendreRelecture` implémente la décision A10 (sans auth, l'appel émane de l'assigné qui n'a pas encore rendu) : 1ʳᵉ relecture rendue → l'exercice RESTE `EN_ATTENTE` (note provisoire), 2ᵉ → passage `RELUE`, les deux rendues → `409 RELECTURE_DEJA_RENDUE` ; `EtudiantService` calcule la note retenue (moyenne des relectures rendues, arrondie à l'entier le plus proche) et expose le nouveau champ `noteProvisoire` (true/false/null) dans `ExerciceEtudiantResponse` (RG7 inchangé : aucun champ relecteur) ; `TableauService.calculerMoyenne` calcule désormais la moyenne sur les notes RETENUES exercice par exercice (moyenne des moyennes, notes provisoires incluses conformément au contrat v1.1) et `relecturesEnAttente` compte les exercices où l'étudiant est assigné sans avoir rendu (via `AssignationRelectureRepository`) ; `RelectureEnAttenteService` liste les exercices assignés à l'étudiant où il n'a pas encore rendu. Test unitaire `RelectureServiceTest` (3 tests) : 1ʳᵉ relecture → statut `EN_ATTENTE` sans save exercice, 2ᵉ → `RELUE` + save, double rendu → `RelectureDejaRendueException`.

**Bloqué** : environ 20 min sur deux soucis révélés par les tests : (1) NPE sur des entités non persistées (id null) dans les comparaisons d'identité — corrigé par une comparaison référence-puis-id (`memeEtudiant`) dans `RelectureService` ; (2) `UnnecessaryStubbing` de Mockito sur le test RG9 (le stub des assignations n'est pas atteint) — corrigé avec `lenient()`. Par ailleurs, `mvnw test` exécute aussi un `CorsSmokeTempTest` présent dans target mais absent des sources (résidu de build) : sans incidence, 8/8 verts.

**IA** : m'a adapté les 5 services et écrit les 3 tests. J'ai vérifié : ordre des vérifications de `rendreRelecture` cohérent avec le D5 (exercice → RG9 → note → A10 → RG4), la note retournée par `POST /api/relectures/{id}` reste celle de la relecture qui vient d'être rendue (contrat B2 inchangé), la moyenne du tableau suit RG16/RG17 v1.1, `mvnw compile` → BUILD SUCCESS, `mvnw test` → 8/8 (dont 3 nouveaux).

**Commit** : `feat(relectures): deux relecteurs et note moyenne (#46)`

---

### Issue #45 — Migration Flyway V3 : deux relecteurs par exercice

**Fait** : `backend/src/main/resources/db/migration/V3__deux_relecteurs.sql` (aucune migration existante modifiée, B5) : création de la table `assignation_relecture` (id, exercice_id, relecteur_id, assignee_at, UNIQUE(exercice_id, relecteur_id) garantissant des relecteurs DISTINCTS, index sur les deux FK) ; transfert des assignations existantes (INSERT ... SELECT depuis `exercice.relecteur_id` non null) ; remplacement de la contrainte `uq_relecture_exercice` (une relecture par exercice) par `uq_relecture_exercice_relecteur` (UNE relecture par couple exercice/relecteur — RG9 v2) ; suppression de la colonne `exercice.relecteur_id` (et de sa FK + index). Entités alignées : nouvelle `AssignationRelecture`, `Exercice` avec `@OneToMany` vers ses assignations (cascade ALL, orphanRemoval) et méthode `ajouterAssignation`, `Relecture` passée de `@OneToOne` à `@ManyToOne` sur exercice avec UNIQUE(exercice_id, relecteur_id).

**Bloqué** : environ 10 min sur une première version de la migration contenant un CHECK aberrant (`relecteur_id <> relecteur_id`, toujours faux, aurait bloqué tout INSERT) — détecté en relisant le SQL avant exécution, retiré ; la limite « au plus 2 relecteurs par exercice » (RG5 v2) est contrôlée en service, comme l'unicité du couple l'est par la contrainte. Testé sur PostgreSQL 16 jetable (Docker) : Flyway applique V1 puis V3 (`Migrating schema "public" to version "3 - deux relecteurs"`, `Successfully applied 2 migrations`), Hibernate `ddl-auto=validate` passe, application démarre avec données de démo. Premier essai interrompu par « Port 8085 already in use » (backend déjà lancé sur le poste) — contourné avec SERVER_PORT=0.

**IA** : m'a généré la migration SQL, l'entité `AssignationRelecture` et mis à jour `Exercice`/`Relecture`. J'ai vérifié que la migration est purement additive (V1/V2 intacts), que le transfert de données préserve les relecteurs existants, et que `validate` n'émet aucune divergence schéma/entités.

**Commit** : `feat(db): migration V3 deux relecteurs par exercice (#45)`

---

### Issue #44 — Contrat d'API v1.1 : flag provisoire sur la note

**Fait** : `api/contrat.yaml` passé en version **1.1** : description d'en-tête décrivant le passage à 2 relecteurs (décision A9) ; `GET /api/etudiants/{id}/exercices` documente `note` (moyenne des relectures rendues) et le nouveau champ **`noteProvisoire`** (boolean nullable : true tant qu'un seul des 2 relecteurs a rendu, false quand les deux ont rendu, null si aucune relecture) ; `POST /api/relectures/{id}` décrit le statut de retour (`EN_ATTENTE` après la 1re des 2 relectures, `RELUE` après la 2e avec note = moyenne) et la règle d'identification du relecteur (décision A10) ; descriptions v1.1 ajoutées sur `/api/exercices` (2 relecteurs assignés au dépôt), `/api/tableau` (moyenne sur notes retenues), `/api/exercices/{id}/relecteur` (réassignation ou complément, RG15 v2) et `/api/etudiants/{id}/relectures` (exercices où CE relecteur n'a pas encore rendu). Chemins, verbes, codes de statut et format d'erreur des 5 opérations imposées **inchangés** (B2).

**Bloqué** : aucun blocage significatif. Vérification syntaxique : le YAML a été rechargé avec js-yaml (installé sans l'ajouter à package.json) → parse OK, 12 chemins, `noteProvisoire` présent ; un défaut de jointure `type: array items:` introduit pendant l'édition a été détecté et corrigé avant commit.

**IA** : m'a rédigé les modifications du contrat. J'ai vérifié que les 5 opérations imposées gardent leurs chemins/verbes/codes/format d'erreur à la lettre, que le champ `noteProvisoire` est nullable et aligné sur la décision A9, et que le YAML parse sans erreur.

**Commit** : `api(contrat): ajoute le flag provisoire sur la note (#44)`

---

### Issue #43 — Analyse mise à jour : passage à 2 relecteurs

**Fait** : mise à jour complète des livrables d'analyse pour le changement de besoin (décision A9) : CAHIER_DES_CHARGES.md (note « changements depuis la v2 », RG5 v2 « deux relecteurs différents + moyenne + provisoire », RG6 v2 « deux relecteurs distincts parmi les présents », RG9 v2 « RELUE quand les deux ont rendu », RG14/RG15 v2, EF9/EF12/EF15 v2, nouvelles décisions A9 et A10 en section 7, B5 « V3 ajoutée, jamais modifiée »), diagramme D2 (nouvelle entité `AssignationRelecture` 1..2 par exercice, Exercice sans plus de `relecteurId`, Relecture 0..2 avec UNIQUE(exercice_id, relecteur_id), correspondances RG et migrations à jour) et diagramme D4 (la 1re relecture rendue ne fait plus passer l'exercice RELUE : auto-transition EN_ATTENTE avec note provisoire ; RELUE à la 2e). Nouveau diagramme D5 `d5-sequence-relecture.md` : séquence de rendu de relecture à 2 relecteurs avec branches 200 provisoire / 200 définitive (moyenne) / 404 / 400 / 403 / 409, et règle d'identification du relecteur (décision A10, sans auth).

**Bloqué** : aucun blocage significatif. Point d'attention tranché dans A10 : le contrat imposé `POST /api/relectures/{id}` ne transporte pas l'identité du relecteur — sans auth, le backend considère que l'appel émane du relecteur assigné qui n'a pas encore rendu ; resoumission → 409 (RG9 inchangé).

**IA** : m'a rédigé les mises à jour du CDC et des diagrammes D2/D4 et le nouveau D5. J'ai vérifié la cohérence croisée : chaque mention « un seul relecteur » du CDC est soit mise à jour soit explicitement marquée v2 ; D2 correspond désormais à la future V3 (assignation_relecture, UNIQUE(exercice_id, relecteur_id) sur relecture, suppression de relecteur_id sur exercice) ; D4 et D5 décrivent le même flux que A9/A10.

**Commit** : `docs(analyse): mise a jour suite au passage a 2 relecteurs (#43)`

---

### Ticket #1 — Init backend Spring Boot + frontend React Vite + Docker Compose

**Fait** : création du squelette backend Spring Boot 3.3.5 (Java 17, Maven, wrapper `mvnw` commité) et frontend React 18 + Vite (port 4200). `docker-compose.yml` avec 3 services (`postgres` interne, `backend` sur 8085, `frontend` sur 4200). `application.properties` (port 8085, PostgreSQL, Flyway, `ddl-auto=validate`), `application-dev.properties` (SQL visible), `application-test.properties` (H2 en mémoire). `GlobalExceptionHandler` + `ErrorResponse` au format `{code, message}`. `Dockerfile` multi-stage pour chaque service.

**Bloqué** : environ 20 min sur le build Docker du backend (téléchargement complet des dépendances Maven dans le conteneur, ~10 min) — résolu en relançant le build une fois les couches en cache.

**IA** : m'a généré le `pom.xml`, le `Dockerfile`, le `docker-compose.yml` et les classes d'exception. J'ai vérifié que le port 8085 était bien pris en compte, que Flyway était activé (`ddl-auto=validate` et non `update`), et que le format d'erreur `{code, message}` était respecté. Testé avec `mvnw clean compile` → `BUILD SUCCESS`, `docker compose up` → les 3 conteneurs démarrent.

**Commit** : `feat: init backend Spring Boot + frontend React Vite + Docker Compose (Closes #1)`
### Ticket #2 — Migration Flyway V1 (`V1__init.sql`)

**Fait** : création de `backend/src/main/resources/db/migration/V1__init.sql` avec les 6 tables du diagramme D2 : `promotion`, `etudiant`, `session`, `presence`, `exercice`, `relecture`. Contraintes `UNIQUE` (présence unique par session/étudiant, un seul exercice par couple, une seule relecture par exercice), `CHECK` (source `ETUDIANT`/`FORMATEUR`, statut `EN_ATTENTE`/`RELUE`, note 0–20), index sur les clés étrangères, `relecteur_id` nullable.

**Jalon** : commit vide `[JALON] analyse` poussé après validation des 4 livrables (CDC, contrat, diagrammes, journal), et **avant** tout commit de code.

### Ticket #14 — Données de démo + 2 tests B6 + README

**Fait** : `DataInitializer` (charge 1 promotion, 10 étudiants, 1 session ouverte avec code `DEMO01`, des présences et des exercices déjà relus — uniquement si la base est vide, donc idempotent), test unitaire `PresenceServiceTest` (3 tests Mockito sur RG1 : code expiré, code inconnu, cas nominal), test d'intégration `PresenceControllerTest` (2 tests sur `POST /api/presences` avec H2 et profil `test`), README d'installation mis à jour (frontend React + Vite justifié en une ligne, `docker compose up`, alternative 3 commandes, endpoints principaux, tests B6, procédure testée depuis un clone vierge).

**Bloqué** : environ 10 min sur le nom du test d'intégration — le ticket disait `PresenceControllerIT`, mais Surefire ignore `*IT` avec `./mvnw test` et modifier le `pom.xml` était interdit. Renommé `PresenceControllerTest` (écart justifié, signalé). Vérifié : `./mvnw test` → 5 tests verts (3 unitaires + 2 d'intégration).

**IA** : m'a généré les 3 nouveaux fichiers et modifié le README. J'ai testé : `./mvnw test` → `BUILD SUCCESS` (5/5), `docker compose down -v` puis `up` → données de démo chargées (1 promo, 10 étudiants, session `DEMO01` active), flux réel (marquer une présence, consulter le tableau), idempotence (redémarrage backend → pas de doublon). Tout conforme.

**Commit** : `feat(demo): donnees de demo, tests B6 et README (Closes #14)`

**Jalon** : commit vide `[JALON] v0.1` posé après le merge de la dernière issue et stabilisation de `main`.

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

### Ticket #13 — `GET /api/promotions` + `GET /api/promotions/{id}/etudiants`

**Fait** : DTO records (`PromotionResponse`, `EtudiantResponse`), `PromotionService` (liste des promotions triées par nom, liste des étudiants d'une promotion triés par nom, `PromotionNotFoundException` si la promotion n'existe pas), `PromotionController` (`@GetMapping` et `@GetMapping("/{id}/etudiants")`).

**Bloqué** : environ 5 min sur une erreur de compilation (`Promotion::getNom` utilisé par erreur dans le stream des étudiants). Résolu en utilisant `Etudiant::getNom` avec un import propre.

**IA** : m'a généré les 4 nouveaux fichiers. J'ai testé : `GET /api/promotions` (`200` + liste triée), `GET /api/promotions/1/etudiants` (`200` + liste triée), promotion inconnue (`404 PROMOTION_INCONNUE`). Tout conforme.

feat/13-promotions-etudiants
**Commit** : `feat(promotions): implemente GET /api/promotions et /{id}/etudiants (Closes #13)`
**Commit** : `feat(promotions): implemente GET /api/promotions et /{id}/etudiants (Closes #13)

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

### Issue #32 — Couche API dédiée + hook useApi

**Fait** : `frontend/src/api/client.js` — constante `BASE_URL = 'http://localhost:8085'`, fonction privée `call(path, { method, body })` qui sérialise le corps JSON, lit la réponse, puis normalise toute erreur en `{ code, message, status }` (ENF6) avec repli `ERREUR_INCONNUE` et cas du serveur injoignable `ERREUR_RESEAU` ; les 12 fonctions exportées, une par endpoint du contrat (5 imposées + 7 libres). `frontend/src/api/useApi.js` — hook `useApi(fn, deps)` exposant `{ data, loading, error }`, avec drapeau `cancelled` pour ignorer une réponse arrivée après le démontage du composant. Aucun `fetch` en dehors de `client.js`, aucune règle métier dupliquée : la moyenne affichée viendra telle quelle de `GET /api/tableau` (RG17).

**Bloqué** : aucun blocage significatif. Deux points relevés et signalés, non traités ici : (a) le template Vite a installé React 19.2.8 alors que le CDC annonce React 18 (F1) — je conserve la version installée, les hooks utilisés (`useState`, `useEffect`) sont identiques ; (b) le backend ne déclare aucune configuration CORS, alors que le navigateur appellera `http://localhost:8085` depuis `http://localhost:4200` : les appels seront bloqués par le navigateur tant qu'un proxy Vite ou une configuration CORS côté backend n'est pas ajoutée. Point à traiter dans une issue dédiée, car les deux fichiers concernés sont hors périmètre de celle-ci (backend et `vite.config.js` interdits).

**IA** : l'IA a écrit `client.js` et `useApi.js` à partir du contrat `api/contrat.yaml`. J'ai vérifié : les 12 signatures et les noms de champs des corps JSON (`titre`/`promotionId`, `code`/`etudiantId`, `sessionId`/`etudiantId`/`lien`, `note`/`commentaire`, `etudiantId`, `lien`, `relecteurId`) sont alignés un par un sur les DTOs du backend (`SessionRequest`, `PresenceRequest`, `ExerciceRequest`, `RelectureRequest`, `PresenceManuelleRequest`, `RemplacementLienRequest`, `ReassignationRelecteurRequest`) ; le port 8085 est celui de `application.properties` et de `docker-compose.yml`. Vérifié par `npm run lint` (oxlint, 0 avertissement, code de sortie 0) et `npm run build` (`✓ built in 3.31s`, code de sortie 0). Le comportement réseau sera couvert par les tests de l'issue #34.

**Commit** : `feat(frontend): couche API dediee et hook useApi (Closes #32)`

---

### Issue #35 — Configuration CORS entre frontend et backend

**Fait** : côté backend, `CorsConfig.java` créé from scratch dans `config/` (`@Configuration` + `WebMvcConfigurer`) sur `/api/**` : origines `http://localhost:4200` et `http://frontend:4200`, méthodes GET/POST/PATCH/PUT/DELETE/OPTIONS, headers `Content-Type` et `Authorization`, préflight en cache 1 h. Côté dev, proxy Vite `/api` → `http://localhost:8085` dans `vite.config.js`. Côté prod, `location /api/` → `http://backend:8085` (nom du service Docker Compose) dans `nginx.conf`. Les trois voies se complètent : CORS pour un navigateur qui joint directement le port 8085, proxys pour rester sur une seule origine (4200).

**Bloqué** : aucun blocage significatif. Note : avec les proxys (dev et prod), le navigateur ne fait jamais de requête cross-origin, donc le CORS est une ceinture de sécurité supplémentaire plutôt que le chemin principal.

**IA** : m'a généré `CorsConfig.java` (sans réutiliser la config CORS d'un autre projet), le bloc `proxy` de `vite.config.js` et le `location /api/` de `nginx.conf`. J'ai vérifié la conformité avec l'issue (origines, méthodes, headers), `mvnw compile` → `BUILD SUCCESS`, `npm run build` → build OK.

**Commit** : `feat(cors): configure CORS backend + proxy Vite + Nginx (Closes #35)`

---

### Issue #33 — Routing 3 routes

**Fait** : `react-router-dom` (v7) installé (`package.json` + `package-lock.json` committés). `main.jsx` enveloppe `<App />` dans `<BrowserRouter>`. `App.jsx` remplacé : page d'accueil `/` (titre + 3 cartes cliquables vers `/formateur`, `/etudiant`, `/relecteur`) et `<Routes>` avec les 3 routes. 3 pages squelettes créées (`FormateurPage.jsx`, `EtudiantPage.jsx`, `RelecteurPage.jsx`) : titre, paragraphe « À venir », lien retour accueil — elles seront complétées aux issues #29/#30/#31. `App.css` et `index.css` allégés (suppression du template Vite), mise en page responsive (mobile 375 px) et focus visible pour l'accessibilité. `index.html` : `lang="fr"` + titre de l'application.

**Bloqué** : aucun blocage significatif.

**IA** : m'a généré le routing, les 3 squelettes de pages et les styles. J'ai vérifié que les 3 routes sont déclarées et que la navigation fonctionne dans les 2 sens (liens `Link` de react-router), et `npm run build` → `✓ built in 6.7 s`.

**Commit** : `feat(frontend): routing 3 routes et accueil (Closes #33)`

---

### Issue #29 — Écran formateur

**Fait** : deux sections claires. « Ouvrir une session » : titre + promotion (liste déroulante `getPromotions`), appel `ouvrirSession`, puis affichage du code généré et des dates d'ouverture/expiration formatées en `fr-FR`. « Tableau de synthèse » : sélection d'une promotion → `getTableau(promotionId)` via `useApi`, colonnes `nom`, `presences`, `exercicesDeposes`, `moyenne`, `relecturesEnAttente`, nombres alignés à droite, lignes alternées, tableau scrollable horizontalement sur mobile. Moyenne « — » si `null` (décision A7), jamais recalculée (RG17). Erreurs affichées avec `error.message` (format `{ code, message }`), `PROMOTION_INCONNUE` reformatée en message lisible. Loading et error gérés dans chaque section indépendamment, boutons désactivés pendant le traitement.

**Bloqué** : aucun blocage significatif.

**IA** : m'a généré la page et les styles. J'ai vérifié la correspondance des champs avec `SessionResponse` (code, ouvertureAt, expirationAt) et `TableauLigneResponse`, que la moyenne nulle affiche « — » et qu'aucune règle métier n'est dupliquée côté front. `npm run build` → `✓ built in 0.7 s`.

**Commit** : `feat(frontend): ecran formateur, session et tableau de synthese (Closes #29)`

---

### Issue #30 — Écran étudiant

**Fait** : identification en deux temps (promotion puis étudiant, via `getPromotions` + `getEtudiants`, EF19 sans mot de passe). Trois sections indépendantes avec leur propre état de chargement/erreur. « Marquer ma présence » : code 6 caractères → `marquerPresence`, erreurs métier mappées en messages lisibles (`CODE_INCONNU`, `DEJA_PRESENT`, `CODE_EXPIRE`, `TROP_DE_TENTATIVES`). « Déposer un exercice » : sessionId + lien → `deposerExercice`, erreurs `LIEN_INVALIDE` et `EXERCICE_DEJA_DEPOSE` mappées, confirmation avec le statut `EN_ATTENTE` retourné par l'API (RG17 : aucune logique métier côté front). « Mes exercices et notes » : `getExercicesEtudiant` affichant lien, statut, note (« — » si null) et commentaire — **jamais le nom du relecteur** (RG7, le DTO n'en contient pas). La liste se rafraîchit automatiquement après un dépôt réussi.

**Bloqué** : aucun blocage significatif. Décision : le champ « Numéro de la session » est une saisie numérique — la promotion sélectionnée ne permet pas de connaître les sessions ouvertes, et le contrat n'expose aucun endpoint de listing des sessions.

**IA** : m'a généré la page. J'ai vérifié que les 4 codes d'erreur présence et 2 codes dépôt sont mappés exactement comme dans l'issue, que le nom du relecteur n'apparaît nulle part (RG7) et que la moyenne/notes ne sont jamais recalculées. `npm run build` → `✓ built in 0.7 s`.

**Commit** : `feat(frontend): ecran etudiant presence depot et notes (Closes #30)`

---

### Issue #31 — Écran relecteur

**Fait** : identification (promotion + étudiant) comme pour l'écran étudiant, puis liste des relectures `EN_ATTENTE` via `getRelecturesEtudiant` (filtre fait côté API — RG9). Un **formulaire par exercice** (composant `FormulaireRelecture`) avec lien cliquable, note (validation locale : entier 0–20 avant l'appel — décision A1) et commentaire. Erreurs mappées : `NOTE_INVALIDE`, `AUTO_RELECTURE`, `RELECTURE_DEJA_RENDUE`. Après un rendu réussi : confirmation visuelle et rafraîchissement de la liste — l'exercice relu disparaît (l'API ne retourne plus que les `EN_ATTENTE`). L'identité de l'auteur n'apparaît jamais (l'API ne l'expose pas, le front ne l'invente pas).

**Bloqué** : aucun blocage significatif.

**IA** : m'a généré la page et son composant enfant. J'ai vérifié la validation locale de la note (bouton désactivé si note absente, non entière ou hors bornes), que les 3 codes d'erreur sont mappés, et que rien ne révèle l'auteur. `npm run build` → `✓ built in 2.8 s`.

**Commit** : `feat(frontend): ecran relecteur, relectures en attente et rendu de note (Closes #31)`

---

### Issue #34 — Tests frontend (Vitest + Testing Library)

**Fait** : Vitest 5 + @testing-library/react + jest-dom + user-event + jsdom installés en devDependencies (`package.json` + `package-lock.json` committés). Configuration `test` dans `vite.config.js` (jsdom, `globals`, `setupFiles`, `pool: 'threads'` — les workers `forks` ne démarrent pas sur ce poste Windows). Scripts `test` / `test:watch`. 5 fichiers de test, 20 tests : `client.test.js` (verbes, chemins, bodies, format d'erreur `{ code, message, status }`, `ERREUR_RESEAU`), `useApi.test.js` (loading/data/error, changement de deps, démontage), et un fichier par écran (promotions affichées, code de session, moyenne « — », confirmation + erreur 410, RG7 sans relecteur, liste en attente, retrait après rendu, erreurs lisibles). `frontend/README.md` réécrit avec la commande `npm test`.

**Bloqué** : environ 20 min sur le démarrage des workers Vitest sous Windows (`Timeout waiting for worker to respond` en pools `forks` puis `threads`) — résolu par `pool: 'threads'` + relance (cold start), puis 10 min sur deux sélections de `<select>` faites avant le chargement des données (résolu en attendant les options) et des valeurs de select passées en chaîne au lieu de nombre (coercion `Number()` ajoutée dans les pages).

**IA** : m'a généré les 5 fichiers de test, la config Vitest et le README. J'ai corrigé les pages (coercion `Number()`) révélée par les tests, vérifié `npm test` → **20/20 passés** (5 fichiers), `npm run build` → `✓ built in 0.8 s` (les tests ne sont pas dans le bundle de prod), `npm run lint` → 0 erreur, et que le Dockerfile n'est pas modifié.

**Commit** : `feat(frontend): tests vitest et testing library (Closes #34)`