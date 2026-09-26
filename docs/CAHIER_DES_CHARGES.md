# Cahier des charges — Application de présence et de relecture KFOKAM48

Auteur : KF48-164  ·  Version 2  ·  Frontend choisi : React (avec Vite), parce que l'application se limite à trois écrans CRUD sans besoin de SSR ni de routing complexe, et que React permet une mise en œuvre rapide avec une couche API centralisée et une gestion d'état locale simple.

**Changements depuis la v1 :** ajout des opérations d'API libres manquantes (liste des étudiants, ajout manuel de présence, remplacement de lien, réassignation, consultation des relectures), rééquilibrage des priorités Must/Should/Could, deux nouvelles zones d'ombre tranchées (ajout manuel de présence, notification du relecteur), et démarrage par Docker Compose avec ports fixes (backend 8085, frontend 4200).

**Changements depuis la v2 (itération 3, changement de besoin) :** chaque exercice est relu par **deux relecteurs différents** au lieu d'un seul (RG5/RG6 v2, décision A9) ; la note retenue est la **moyenne des deux relectures**, affichée comme **provisoire** tant qu'un seul des deux relecteurs a rendu (RG9 v2, décision A9/A10) ; nouvelle migration Flyway `V3__deux_relecteurs.sql` (aucune migration existante modifiée, B5) ; contrat d'API passé en v1.1 (champ `noteProvisoire`, description des règles à 2 relecteurs).

## 1. Contexte et objectif

La direction de la formation KFOKAM48 gère aujourd'hui les présences et les
exercices de ses promotions de manière manuelle. Le formateur ouvre une session
de cours, les étudiants signent une feuille de présence, déposent leurs exercices
par des canaux informels, et la relecture par les pairs se fait sans traçabilité.

Ce fonctionnement pose trois problèmes :

- **Présence** : pas de contrôle fiable, pas de code de session, risque de
  signature pour autrui.
- **Exercices** : pas de dépôt centralisé, pas de suivi de qui a déposé quoi.
- **Relecture** : pas d'assignation systématique, pas d'anonymat du relecteur,
  pas de suivi des relectures en attente.

L'objectif de l'application est de fournir un outil unique permettant :

1. Au formateur d'ouvrir une session et d'obtenir un **code de présence** à
   durée de vie limitée.
2. À l'étudiant de **marquer sa présence** avec ce code, et de **déposer le
   lien** de son exercice pour la session.
3. Au système d'**assigner automatiquement** deux relecteurs à chaque exercice
   déposé, parmi les étudiants présents.
4. Au relecteur de **noter et commenter** l'exercice d'un pair, de façon
   **anonyme** vis-à-vis de l'auteur.
5. Au formateur de consulter un **tableau de synthèse** par promotion :
   présences, exercices déposés, moyenne des notes reçues, relectures en attente.

## 2. Acteurs et rôles

| Acteur | Ce qu'il peut faire |
|---|---|
| **Formateur** | Ouvrir une session de cours et obtenir un code de présence · Voir le tableau de synthèse d'une promotion · Ajouter manuellement une présence (marquée « ajouté par le formateur ») · Réassigner manuellement le relecteur d'un exercice resté en attente |
| **Étudiant** | Choisir son nom dans une liste préchargée (pas de mot de passe) · Marquer sa présence avec un code de session · Déposer le lien de son exercice pour une session · Remplacer le lien de son exercice tant que la relecture n'a pas commencé · Consulter la note et le commentaire reçus sur son exercice (sans connaître l'identité des relecteurs), avec mention « provisoire » tant que les deux relectures ne sont pas rendues |
| **Relecteur** | Consulter la liste des exercices qui lui ont été assignés et non encore relus · Saisir une note entière de 0 à 20 et un commentaire · Rendre sa relecture (action définitive) |

**Précisions :**

- Le **relecteur n'est pas un acteur distinct** : c'est un **étudiant** qui s'est vu assigner la relecture d'un exercice. Il cumule donc les rôles d'étudiant et de relecteur.
- Le **système** n'est pas listé comme acteur : il n'agit pas de sa propre initiative, il exécute les règles définies (assignation automatique d'un relecteur, calcul de la moyenne, expiration du code).
- L'**anonymat du relecteur** est garanti vis-à-vis de l'auteur de l'exercice (Q8). Le formateur, lui, voit tout.

## 3. Périmètre

### Inclus dans le projet

- Gestion des **promotions** (lecture seule : listes préchargées en base, exposées via l'API pour le frontend).
- Gestion des **étudiants** (lecture seule : listes préchargées en base, exposées via l'API pour la liste déroulante).
- **Ouverture d'une session** de cours par le formateur, avec génération d'un **code de présence** à durée de vie limitée (15 minutes).
- **Marquage de présence** par un étudiant, avec le code de session.
- **Ajout manuel de présence** par le formateur, via une opération dédiée (traçé via le champ `source`, voir section 7, décision A5).
- **Dépôt du lien d'un exercice** par un étudiant, pour une session.
- **Remplacement du lien** tant que la relecture n'a pas commencé.
- **Assignation automatique** de deux relecteurs distincts à chaque exercice déposé, parmi les étudiants présents à la session.
- **Réassignation manuelle** du relecteur par le formateur, en cas de relecteur indisponible ou défaillant.
- **Consultation par un relecteur** de ses relectures encore en attente.
- **Relecture** d'un exercice par chacun de ses deux relecteurs assignés : note entière de 0 à 20 et commentaire. La note retenue est la **moyenne des deux relectures** ; elle est affichée comme **provisoire** si un seul relecteur a rendu. La relecture d'un relecteur est **définitive** une fois rendue.
- **Consultation de la note et du commentaire** par l'auteur de l'exercice, sans connaître l'identité du relecteur.
- **Tableau de synthèse** par promotion : présences, exercices déposés, moyenne des notes reçues, relectures en attente.
- **Blocage temporaire** (2 minutes) après 5 erreurs de code consécutives.
- **Données de démonstration** chargées au démarrage.

### Explicité hors périmètre

- **Authentification par mot de passe** : exclue (Q1). L'étudiant choisit son nom dans une liste.
- **Rôles/permissions avancés** : pas de gestion fine des droits, pas de compte administrateur, pas de délégation.
- **Notifications** (email, SMS, push) : exclues (décision A6). Le relecteur découvre sa mission en se connectant.
- **Chat, commentaires en fil, messagerie** : exclus.
- **Upload de fichiers** : seuls les liens vers des exercices sont gérés, pas le stockage de fichiers.
- **Gestion de planning / calendrier / salles** : exclue.
- **Statistiques avancées** (graphiques, exports PDF/Excel) : exclues.
- **Application mobile native** : exclue. L'interface est web, responsive (usage mobile via navigateur).
- **Internationalisation** (multilingue) : exclue. L'interface est en français.
- **Paiement, facturation** : exclus.
- **Refonte visuelle / CSS soigné** : hors périmètre — le rendu visuel n'est pas noté.

## 4. Exigences fonctionnelles

| Réf | Exigence | Critère d'acceptation | Priorité |
|---|---|---|---|
| **EF1** | Le formateur ouvre une session de cours pour une promotion | Quand je crée une session avec un titre et une promotion, l'API renvoie `201` avec un `id`, un `code` à 6 caractères, une date d'ouverture et une date d'expiration à +15 minutes | Must |
| **EF2** | L'étudiant marque sa présence avec le code de session | Quand je saisis un code valide, ma présence est enregistrée avec `source = ETUDIANT` et apparaît dans le tableau du formateur. Si le code est inconnu, l'API renvoie `400 CODE_INCONNU` | Must |
| **EF3** | Le code de présence expire après 15 minutes | Quand je saisis un code expiré, l'API renvoie `410` avec `{ code: "CODE_EXPIRE" }` et aucune présence n'est créée | Must |
| **EF4** | Un étudiant ne peut marquer sa présence qu'une seule fois par session | Quand je resoumets un code déjà utilisé, l'API renvoie `409` avec `{ code: "DEJA_PRESENT" }` | Must |
| **EF5** | L'étudiant est bloqué 2 minutes après 5 erreurs de code consécutives | Après 5 codes erronés, l'API renvoie `429` avec `{ code: "TROP_DE_TENTATIVES" }` pendant 2 minutes | Should |
| **EF6** | Le formateur ajoute manuellement une présence pour un étudiant, sans code | Quand le formateur appelle `POST /api/sessions/{id}/presences` avec un `etudiantId`, la présence est enregistrée avec `source = FORMATEUR` et apparaît dans le tableau | Must |
| **EF7** | L'étudiant dépose le lien de son exercice pour une session | Quand je dépose un lien valide, l'API renvoie `201` avec `{ id, statut: "EN_ATTENTE" }` et l'exercice apparaît dans le tableau. Si le lien est invalide, `400 LIEN_INVALIDE`. Si un exercice existe déjà pour ce couple (session, étudiant), `409 EXERCICE_DEJA_DEPOSE` | Must |
| **EF8** | L'étudiant remplace le lien de son exercice tant que la relecture n'a pas commencé | Quand j'appelle `PATCH /api/exercices/{id}` avant toute relecture, l'API renvoie `200` ; après le début de relecture, elle renvoie `409` | Should |
| **EF9** | Le système assigne automatiquement deux relecteurs à chaque exercice déposé | Quand un exercice est déposé, deux relecteurs distincts sont choisis au hasard parmi les étudiants présents (hors auteur) — un seul si un seul est disponible — et l'exercice passe en `EN_ATTENTE` | Must |
| **EF10** | Si aucun relecteur n'est disponible, l'exercice reste en attente avec mention | Quand aucun présent n'est disponible, l'exercice reste `EN_ATTENTE` et le tableau indique « relecteur à assigner » | Should |
| **EF11** | Le formateur réassigne manuellement le relecteur d'un exercice en attente | Quand le formateur appelle `PATCH /api/exercices/{id}/relecteur`, l'exercice garde son statut `EN_ATTENTE` mais change de relecteur | Should |
| **EF12** | Chaque relecteur note et commente l'exercice assigné | Quand un relecteur soumet une note entière entre 0 et 20 et un commentaire, l'API renvoie `200` ; l'exercice passe en `RELUE` quand ses deux relecteurs ont rendu (avant cela il reste `EN_ATTENTE` et la note affichée est provisoire) | Must |
| **EF13** | Un étudiant ne peut jamais relire son propre exercice | Si un étudiant tente de relire son propre exercice, l'API renvoie `403` avec `{ code: "AUTO_RELECTURE" }` | Must |
| **EF14** | Une relecture rendue est définitive | Si on resoumet une relecture déjà rendue, l'API renvoie `409` avec `{ code: "RELECTURE_DEJA_RENDUE" }` | Must |
| **EF15** | L'auteur d'un exercice voit sa note et le commentaire, sans l'identité des relecteurs | Quand l'auteur appelle `GET /api/etudiants/{id}/exercices`, il voit la note (moyenne des relectures rendues) et le commentaire, avec `noteProvisoire = true` tant qu'un seul des deux relecteurs a rendu, jamais le nom des relecteurs | Must |
| **EF16** | Le formateur consulte le tableau de synthèse par promotion | Quand je demande `GET /api/tableau?promotionId=X`, je reçois un tableau avec `etudiantId, nom, presences, exercicesDeposes, moyenne, relecturesEnAttente` | Must |
| **EF17** | Le tableau affiche une promotion inconnue en erreur | Quand je demande une promotion qui n'existe pas, l'API renvoie `404` avec `{ code: "PROMOTION_INCONNUE" }` | Must |
| **EF18** | Le relecteur voit les relectures qu'il doit encore faire | Quand un relecteur appelle `GET /api/etudiants/{id}/relectures`, il voit la liste des exercices qui lui sont assignés et non encore rendus | Must |
| **EF19** | L'étudiant choisit son nom dans une liste (pas de mot de passe) | Quand j'ouvre l'app, `GET /api/promotions/{id}/etudiants` alimente une liste déroulante d'où je choisis mon nom | Must |
| **EF20** | Des données de démonstration sont chargées au démarrage | Au premier lancement, une promotion, des étudiants, une session et des exercices sont présents | Must |
| **EF21** | Le formateur (ou l'étudiant) consulte la liste des promotions disponibles | Quand j'appelle `GET /api/promotions`, je reçois la liste des promotions existantes | Could |

## 5. Exigences non fonctionnelles

| Réf | Exigence | Comment on la vérifie |
|---|---|---|
| **ENF1** | L'API doit répondre en moins de 500 ms sur les endpoints principaux en usage normal | Mesure du temps de réponse sur `GET /api/tableau` avec les données de démonstration |
| **ENF2** | L'application doit supporter une promotion d'environ **50 étudiants** (hypothèse de dimensionnement) et 10 sessions sans dégradation notable | Jeu de données de test avec 50 étudiants × 10 sessions ; vérification que le tableau se charge en moins d'1 s |
| **ENF3** | L'interface doit être utilisable sur mobile (navigateur), sans application native | Test manuel sur un écran de 375 px de large ; les 3 écrans restent lisibles et utilisables |
| **ENF4** | Le code source doit être versionné sur Git, avec un historique lisible | Inspection de l'historique : commits atomiques, branches par ticket, PR liées aux issues |
| **ENF5** | Le projet doit démarrer depuis un clone vierge en une commande (`docker compose up`) ou 3 commandes maximum | Test depuis un clone neuf : `docker compose up` ; vérification que les 3 conteneurs démarrent et que les données de démo sont chargées |
| **ENF6** | Les erreurs de l'API doivent respecter un format unique `{ code, message }`, y compris sur les opérations libres | Appel volontairement erroné sur chaque endpoint, imposé ou libre ; vérification du format de la réponse |
| **ENF7** | Aucun secret (mot de passe, clé, token) ne doit être commité | Inspection de l'historique Git ; présence d'un `.gitignore` couvrant `.env`, `*.pem`, `*.key` |
| **ENF8** | Le schéma de base de données doit être versionné par migrations | Présence de fichiers `V1__*.sql`, `V2__*.sql` dans `backend/src/main/resources/db/migration/` |
| **ENF9** | Le code doit être couvert par au moins un test unitaire métier et un test d'intégration endpoint | Exécution de `mvnw test` sur un poste vierge ; les deux tests passent |
| **ENF10** | Les données de démonstration doivent être chargées automatiquement au démarrage | Au premier lancement, une promotion, des étudiants, une session et des exercices sont présents sans action manuelle |

## 6. Règles de gestion

| Réf | Règle | Source |
|---|---|---|
| **RG1** | Le code de présence expire 15 minutes après l'ouverture de la session. Passé ce délai, toute tentative renvoie `410 CODE_EXPIRE`. | Q2 |
| **RG2** | Un étudiant ne peut marquer sa présence qu'une seule fois par session. Toute resoumission renvoie `409 DEJA_PRESENT`. | Q3, contrat |
| **RG3** | Après 5 erreurs de code consécutives, un étudiant est bloqué pendant 2 minutes. Toute tentative pendant le blocage renvoie `429 TROP_DE_TENTATIVES`. Le compteur est remis à zéro après un succès ou après expiration du blocage. | Q4 |
| **RG4** | Un étudiant ne peut jamais relire son propre exercice. Toute tentative renvoie `403 AUTO_RELECTURE`. | Q5, contrat |
| **RG5** | Chaque exercice est relu par **deux relecteurs différents**. La note retenue est la moyenne des deux relectures ; si un seul des deux a rendu, sa note est affichée en attendant, marquée comme provisoire. *(v2 : changement de besoin — « un exercice n'a qu'un seul relecteur » avant l'itération 3.)* | Q6 |
| **RG6** | Deux relecteurs **distincts** sont choisis automatiquement au hasard parmi les étudiants présents à la session, à l'exclusion de l'auteur de l'exercice. Si un seul étudiant (hors auteur) est présent, un seul relecteur est assigné et le second peut être complété par le formateur (RG15). L'assignation a lieu au moment du dépôt de l'exercice. | Q7 |
| **RG7** | L'auteur d'un exercice voit la note et le commentaire reçus, mais jamais l'identité du relecteur. Le formateur voit tout. | Q8 |
| **RG8** | La note est un entier de 0 à 20. Toute note hors bornes ou non entière renvoie `400 NOTE_INVALIDE`. | Q9, contrat |
| **RG9** | Une relecture rendue est définitive : elle ne peut plus être modifiée ni resoumise. Toute resoumission renvoie `409 RELECTURE_DEJA_RENDUE`. L'exercice passe `RELUE` (relecture définitive) quand ses deux relecteurs ont rendu ; en attendant, la note affichée à l'auteur est provisoire. | Q15, décision A1 ; v2 : A9 |
| **RG10** | Si un relecteur ne rend jamais sa relecture, l'exercice reste `EN_ATTENTE` et apparaît clairement dans le tableau du formateur. | Q11 |
| **RG11** | Un étudiant peut déposer le lien de son exercice jusqu'à la clôture de la session par le formateur. | Q12 |
| **RG12** | Un étudiant peut remplacer le lien de son exercice tant qu'aucune relecture n'a commencé. Après le début de relecture, toute tentative renvoie `409`. | Q13, décision A3 |
| **RG13** | Le formateur peut ajouter manuellement une présence, via une opération dédiée qui ne requiert pas de code. Cette présence est marquée avec `source = FORMATEUR` pour la distinguer d'une présence étudiante (`source = ETUDIANT`). | Q14, décision A5 |
| **RG14** | Si aucun (ou un seul) étudiant présent n'est disponible pour relire un exercice au moment du dépôt, l'exercice reste `EN_ATTENTE` avec la mention « relecteur à assigner » dans le tableau (0 ou 1 relecteur assigné). | Décision A2 |
| **RG15** | Le formateur peut réassigner manuellement le relecteur d'un exercice resté `EN_ATTENTE` : il remplace un relecteur qui n'a pas encore rendu sa relecture, ou complète la seconde assignation s'il n'y en a qu'une. | Décision A2 |
| **RG16** | Le tableau de synthèse affiche, pour chaque étudiant d'une promotion : ses présences par session, son nombre d'exercices déposés, la moyenne de ses notes retenues (chaque exercice compte pour la moyenne de ses relectures rendues, exercice par exercice), et le nombre de relectures qu'il doit encore faire. | Q16 |
| **RG17** | La moyenne affichée est calculée par l'API. Le frontend ne la recalcule jamais. | Contrainte F3 |

## 7. Zones d'ombre, hypothèses et contradictions

| Point | Réponse client (Qx) ou hypothèse | Décision retenue | Pourquoi |
|---|---|---|---|
| **Contradiction Q10 / Q15** — le relecteur peut-il corriger sa note après l'avoir rendue ? | Q10 dit « oui, tant que le formateur n'a pas clôturé la session ». Q15 dit « non, une fois validée, c'est fini ». Les deux réponses ne peuvent pas être vraies simultanément. | **Décision A1 — Q15 l'emporte** : une relecture rendue est **définitive**. Q10 est écartée. Toute resoumission renvoie `409 RELECTURE_DEJA_RENDUE`. | Le contrat d'API imposé précise que `POST /api/relectures/{id}` renvoie `409` si la relecture est déjà rendue. Q10 est donc incompatible avec le contrat. Q15 est également plus cohérente avec l'objectif d'honnêteté exprimé par le client. |
| **Trou : assignation du relecteur** — Q7 dit « au hasard parmi les présents », mais ne dit ni quand, ni ce qu'on fait si aucun présent n'est disponible. | Q7 est incomplète. Aucune autre question ne traite ce cas. | **Décision A2** : l'assignation a lieu **au moment du dépôt** de l'exercice. Si aucun étudiant présent (hors auteur) n'est disponible, l'exercice reste `EN_ATTENTE` avec la mention « relecteur à assigner » dans le tableau. Le formateur peut réassigner manuellement via `PATCH /api/exercices/{id}/relecteur`. | Sans cette règle, un exercice déposé sans présent disponible bloquerait tout le flux de relecture. La mention dans le tableau (Q11) rend le problème visible. La réassignation manuelle donne au formateur une porte de sortie. |
| **Contradiction contrat / Q13** — peut-on remplacer le lien de son exercice ? | Le contrat impose `409` sur `POST /api/exercices` si l'exercice est déjà déposé. Q13 dit qu'on peut remplacer le lien tant que personne n'a commencé à relire. | **Décision A3** : distinguer création et remplacement. `POST /api/exercices` reste la création (`409` si doublon, conforme au contrat). Le remplacement passe par une opération libre `PATCH /api/exercices/{id}` (`200` si relecture non commencée, `409` sinon). | Respecte le contrat à la lettre (le `POST` garde son `409` sur doublon) tout en satisfaisant Q13. Séparer les deux opérations clarifie aussi les responsabilités. |
| **Zone d'ombre : blocage après 5 erreurs** — Q4 dit de bloquer 2 minutes, mais le contrat n'impose aucun code HTTP. | Q4 ne précise pas le code HTTP à renvoyer pendant le blocage. | **Décision A4** : `429 Too Many Requests` avec `{ code: "TROP_DE_TENTATIVES", message: "Trop de tentatives, réessayez dans 2 minutes." }`. | `429` est le code standard HTTP pour le rate limiting. `403` serait sémantiquement moins juste. Le compteur est remis à zéro après un succès ou après expiration du blocage. |
| **Trou : ajout manuel de présence par le formateur** — l'opération imposée `POST /api/presences` exige un code en entrée. Or le formateur qui ajoute une présence à la main (Q14) n'a pas de raison de ressaisir le code de session, et rien n'indique comment il obtiendrait `source = FORMATEUR` via cet endpoint. | Ni le contrat, ni Q14, ne précisent le mécanisme technique de l'ajout manuel. | **Décision A5** : créer une opération libre dédiée `POST /api/sessions/{id}/presences` (`{ etudiantId }`, sans code), réservée au formateur, qui force `source = FORMATEUR`. `POST /api/presences` (opération imposée) reste strictement réservé au flux étudiant avec code, et produit toujours `source = ETUDIANT`. | Réutiliser `POST /api/presences` pour les deux cas aurait obligé à rendre `code` optionnel, ce qui affaiblit la validation d'entrée (B4) et complique la distinction des deux sources. Une opération séparée respecte le contrat imposé à la lettre tout en couvrant EF6/RG13 proprement. |
| **Zone d'ombre : notification du relecteur** — comment le relecteur sait-il qu'il a un exercice à relire ? | Aucune question du client ne traite la notification. | **Décision A6** : pas de notification (email/SMS/push). Le relecteur découvre sa mission via `GET /api/etudiants/{id}/relectures`. | Les notifications sont explicitement hors périmètre (section 3). Une notification aurait nécessité une infrastructure externe (SMTP, service tiers) non justifiée pour ce projet. |
| **Zone d'ombre : moyenne quand aucun exercice n'est relu** — que vaut la moyenne d'un étudiant sans note ? | Q16 demande la moyenne des notes reçues, mais ne dit pas quoi afficher si aucune note n'existe. | **Décision A7** : la moyenne vaut `null` (affichée comme « — » dans le frontend). Elle n'est pas forcée à 0, pour ne pas pénaliser un étudiant qui n'a pas encore été relu. | Une moyenne de 0 serait trompeuse : elle ferait croire à une mauvaise note au lieu d'une absence de note. `null` distingue les deux cas. Le schéma du contrat imposé (`moyenne: number, nullable: true`) confirme d'ailleurs que ce cas était anticipé. |
| **Zone d'ombre : présence après clôture** — Q3 dit qu'on ne peut pas marquer sa présence après la fin de session, mais Q12 autorise le dépôt d'exercice jusqu'à la clôture. | Q3 et Q12 semblent se contredire sur la notion de « fin ». | **Décision A8** : la « fin de session » pour la présence est **15 minutes après l'ouverture** (expiration du code). La « clôture » pour le dépôt est une **action manuelle du formateur**, distincte. | Le code de présence (Q2) et la clôture (Q12) sont deux mécanismes distincts. La présence est limitée par l'expiration du code ; le dépôt est limitée par la clôture manuelle. Il n'y a donc pas de contradiction réelle, juste une ambiguïté de vocabulaire. |
| **Changement de besoin (itération 3, Q6 v2)** — un seul relecteur laisse l'étudiant sans note quand il ne rend jamais sa relecture. | « Finalement, un seul relecteur ça ne marche pas : quand il ne rend rien, l'étudiant n'a aucune note. À partir de maintenant, chaque exercice est relu par deux pairs différents, et la note retenue est la moyenne des deux. Si un seul des deux a rendu, on affiche sa note en attendant, mais marquée comme provisoire. » | **Décision A9** : chaque exercice reçoit **deux relecteurs distincts** (RG5 v2, RG6 v2). La note retenue d'un exercice est la **moyenne de ses relectures rendues** ; elle est **provisoire** tant qu'un seul des deux relecteurs a rendu (`noteProvisoire = true` sur `GET /api/etudiants/{id}/exercices`), définitive quand les deux ont rendu (l'exercice passe alors `RELUE`). La moyenne d'un étudiant (RG16/RG17) est calculée sur les notes retenues, exercice par exercice. | Une relecture unique est un point de défaillance : sans rendu, l'étudiant reste sans note. Deux relecteurs + moyenne diluent le risque ; le flag provisoire préserve la transparence sans casser RG9 (chaque relecture individuelle reste définitive). |
| **Zone d'ombre (v2) : identifier le relecteur qui rend** — `POST /api/relectures/{id}` (opération imposée) ne transporte pas l'identité du relecteur dans son corps `{ note, commentaire }`. | Le contrat imposé ne décrit que `{ note, commentaire }` ; aucune authentification dans le projet. | **Décision A10** : sans auth, le backend détermine le relecteur comme **le relecteur assigné de l'exercice qui n'a pas encore rendu sa relecture**. Si ce relecteur a déjà rendu (resoumission), `409 RELECTURE_DEJA_RENDUE` (RG9). La réassignation (RG15 v2) remplace un relecteur n'ayant pas encore rendu, ou complète la seconde assignation. | Respecter le contrat imposé à la lettre (B2) sans introduire d'auth hors périmètre ; le flux reste déterministe car `GET /api/etudiants/{id}/relectures` n'expose à l'étudiant que les exercices qu'il doit encore relire. |

## 8. Contraintes techniques

### Backend — Spring Boot

| Réf | Contrainte | Mise en œuvre prévue |
|---|---|---|
| **B1** | Java 17 ou plus, Maven, wrapper `mvnw` commité | Java 17, Spring Boot 3.x, wrapper `mvnw` + `mvnw.cmd` commités dès l'initialisation du projet |
| **B2** | Le contrat `api/contrat.yaml` est respecté à la lettre : chemins, verbes, codes de statut, format d'erreur | Les 5 opérations imposées sont implémentées telles quelles. Les **7 opérations libres** suivantes sont ajoutées dans le même fichier `api/contrat.yaml`, avant le premier commit de code : `GET /api/promotions`, `GET /api/promotions/{id}/etudiants`, `POST /api/sessions/{id}/presences` (décision A5), `PATCH /api/exercices/{id}` (décision A3), `PATCH /api/exercices/{id}/relecteur` (décision A2), `GET /api/etudiants/{id}/exercices` (EF15), `GET /api/etudiants/{id}/relectures` (EF18) |
| **B3** | Séparation des couches contrôleur / service / repository. Aucune requête base dans un contrôleur, aucune entité JPA exposée en JSON — passage par des DTO | Architecture en 3 couches : `controller` (validation + mapping DTO), `service` (règles métier), `repository` (accès JPA). DTO en entrée (`*Request`) et en sortie (`*Response`) systématiques, y compris sur les opérations libres |
| **B4** | Validation des entrées et gestion centralisée des erreurs (`@RestControllerAdvice`). Une stack trace renvoyée au client est une faute | Annotations `@Valid` + `@NotNull`, `@Min`, `@Max` sur les DTO. Un `GlobalExceptionHandler` (`@RestControllerAdvice`) mappe toutes les exceptions vers le format imposé `{ code, message }`, y compris sur les opérations libres (ENF6) |
| **B5** | Schéma versionné par Flyway ou Liquibase, migrations commitées. `ddl-auto=update` interdit hors tests | Flyway. Migrations `V1__init.sql` puis `V3__deux_relecteurs.sql` (v2 : aucune migration existante n'est jamais modifiée, on ajoute `V3`) dans `backend/src/main/resources/db/migration/`. `spring.jpa.hibernate.ddl-auto=validate` en dev et prod, `create-drop` uniquement en test |
| **B6** | Deux tests qui prouvent quelque chose : un test unitaire sur une règle métier réelle, un test d'intégration sur un endpoint. Ils tournent sur un poste vierge, sans base locale | Test unitaire sur la règle d'expiration du code (RG1) : `PresenceServiceTest`. Test d'intégration sur `POST /api/presences` : `PresenceControllerIT` avec H2 en mémoire. Exécution via `mvnw test` |

### Frontend — React (Vite)

| Réf | Contrainte | Mise en œuvre prévue |
|---|---|---|
| **F1** | Framework déclaré et justifié en une ligne dans le `README`, et le build passe | React 18 + Vite. Justification dans le README : « React (avec Vite), parce que l'application se limite à trois écrans CRUD sans besoin de SSR ni de routing complexe, et que React permet une mise en œuvre rapide avec une couche API centralisée et une gestion d'état locale simple. » Build vérifié via `npm run build` |
| **F2** | Trois écrans : formateur (ouvrir une session, voir le tableau), étudiant (marquer sa présence, déposer son exercice), relecteur (faire une relecture) | 3 routes : `/formateur`, `/etudiant`, `/relecteur`. Chaque écran couvre les actions listées, en s'appuyant notamment sur `GET /api/promotions/{id}/etudiants` pour la liste déroulante. Pas de CSS soigné (hors périmètre), mais une mise en page lisible sur mobile |
| **F3** | Appels API dans une couche dédiée, pas de `fetch` dispersé · états de chargement et d'erreur gérés · aucune règle métier dupliquée : la moyenne affichée vient de l'API | `src/api/client.js` centralise tous les appels `fetch` (une fonction par endpoint, imposé ou libre). Chaque composant gère `loading` / `error` / `data` via un hook `useApi`. La moyenne affichée dans le tableau provient directement de `GET /api/tableau`, jamais recalculée côté client |

### Démarrage

- **Démarrage par Docker Compose** : un `docker-compose.yml` à la racine lance PostgreSQL + backend + frontend en une seule commande (`docker compose up`). Le backend écoute sur le port **8085**, le frontend sur le port **4200**, PostgreSQL sur le port interne **5432** (non exposé sur la machine hôte).
- **Alternative 3 commandes** (documentée dans le README, pour un poste sans Docker) :
  1. `cd backend && ./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=8085`
  2. `cd frontend && npm install && npm run dev -- --port 4200`
  3. Ouvrir `http://localhost:4200`
- **Données de démonstration** : un `DataInitializer` (ou un script Flyway `V99__demo_data.sql`) charge une promotion, une dizaine d'étudiants, une session ouverte et quelques exercices au premier démarrage. Objectif : le correcteur ouvre l'app et peut tout tester immédiatement.
- **README testé depuis un clone vierge** : la procédure de démarrage est validée sur un poste neuf avant le rendu final (étape 4).

## 9. Livrables

### Livrables de l'étape 1 — Analyse et conception

| Livrable | Emplacement | Statut attendu |
|---|---|---|
| Cahier des charges (10 sections) | `docs/CAHIER_DES_CHARGES.md` | Complet, avec EF et RG numérotées et critères vérifiables |
| Diagramme de cas d'utilisation (D1) | `docs/diagrammes/d1-cas-utilisation.md` | Mermaid, versionné, incluant les cas d'usage liés aux opérations libres (liste étudiants, ajout manuel, réassignation) |
| Diagramme de classes / modèle de données (D2) | `docs/diagrammes/d2-classes.md` | Mermaid, cohérent avec les migrations Flyway |
| Diagramme de séquence « marquer sa présence » (D3) | `docs/diagrammes/d3-sequence-presence.md` | Mermaid, avec au moins 2 cas d'erreur (410, 409), cohérent avec le contrat |
| Diagramme états-transitions d'un exercice (D4, bonus) | `docs/diagrammes/d4-etats-exercice.md` | Mermaid, cycle déposé → en attente → relue, incluant la transition de réassignation |
| Backlog en issues GitHub | Onglet *Issues* du dépôt | Titres-résultats, critères d'acceptation, priorité Must/Should/Could effectivement contrastée, renvoi EFx/RGx |
| Contrat d'API complété avec les 7 opérations libres listées en section 8 (B2), et figé | `api/contrat.yaml` | 5 opérations imposées (inchangées) + 7 opérations libres ajoutées, au format d'erreur `{ code, message }` uniforme |
| Journal d'étape 1 | `docs/JOURNAL.md` | Entrée « Étape 1 » avec Fait / Bloqué / IA |

### Livrables de l'étape 2 — Version v0.1

| Livrable | Emplacement | Statut attendu |
|---|---|---|
| Backend Spring Boot fonctionnel | `backend/` | Stories Must implémentées, couches contrôleur/service/repository, DTO, validation, gestion d'erreurs centralisée |
| Migrations Flyway | `backend/src/main/resources/db/migration/` | `V1__init.sql` + migrations suivantes, commitées |
| Wrapper Maven | `backend/mvnw`, `backend/mvnw.cmd` | Commit **és** |
| Tests | `backend/src/test/` | 1 test unitaire métier + 1 test d'intégration endpoint |
| Frontend React (Vite) | `frontend/` | 3 écrans (formateur, étudiant, relecteur), couche API dédiée, états chargement/erreur |
| Données de démonstration | Backend (initializer ou migration) | Promotion, étudiants, session, exercices |
| Commit `[JALON] v0.1` | Historique Git | Commit vide, message exact, poussé |

### Livrables de l'étape 4 — Version v1.0

| Livrable | Emplacement | Statut attendu |
|---|---|---|
| Backend final | `backend/` | Stories Must + Should + Could retenues |
| Frontend final | `frontend/` | 3 écrans complets |
| `CHANGELOG.md` | Racine du dépôt | Cohérent avec l'historique Git |
| `README.md` | Racine du dépôt | Installation testée depuis un clone vierge, 3 commandes maximum, framework déclaré et justifié |
| `docs/CAHIER_DES_CHARGES.md` mis à jour | `docs/` | Intègre les changements de l'enveloppe (étape 3) |
| Diagrammes mis à jour | `docs/diagrammes/` | Cohérents avec le nouveau besoin après enveloppe |
| Commit `[JALON] v1.0` | Historique Git | Commit vide, message exact, poussé |
| Backlog restant trié | Onglet *Issues* | Issues fermées ou explicitement reportées |

### Livrables de l'étape 5 — Épreuve Git

| Livrable | Emplacement | Statut attendu |
|---|---|---|
| Dépôt `kfokam48-gitlab-164` public | GitHub | Toutes les branches poussées (`git push origin --all`) |
| 5 situations résolues | Historique du dépôt Git | Conformes au README du bundle |

### Livrable de l'étape 6 — Soumission

| Livrable | Emplacement | Statut attendu |
|---|---|---|
| `SOUMISSION.md` | Téléversé sur la plateforme | Avant 18h00, avec les 2 liens publics + hash complets (40 caractères) |

## 10. Démarche prévue

### Enchaînement des six étapes

1. **Étape 1 — Analyser, spécifier, concevoir.** Aucun code. Rédaction du présent cahier des charges, des trois diagrammes Mermaid (D1, D2, D3) plus le bonus D4, du backlog en issues GitHub, et du contrat `api/contrat.yaml` complété avec les 7 opérations libres (section 8, B2). Une fois ces quatre livrables poussés, commit vide `[JALON] analyse`.

2. **Étape 2 — Construire la première version (v0.1).** Initialisation du backend Spring Boot et du frontend React (Vite). Traitement des stories **Must** uniquement, une branche par ticket, une pull request par branche, issues fermées par `Closes #x`. Migrations Flyway dès le début. Une fois toutes les Must mergées et `main` stable, commit vide `[JALON] v0.1`.

3. **Étape 3 — Ouvrir l'enveloppe.** Exécution de `./enveloppe` une fois `[JALON] v0.1` poussé. Le script révèle un bug signalé par le client et un changement de besoin. Ouverture d'une issue avant de coder, reproduction du bug, migration versionnée, mise à jour du contrat et du frontend. Le cahier des charges et les diagrammes sont mis à jour dans un commit dédié qui le dit explicitement.

4. **Étape 4 — Livrer la version finale (v1.0).** Traitement des stories **Should** et **Could** retenues (désormais plus nombreuses grâce à la priorisation contrastée de la section 4), rédaction du `CHANGELOG.md` cohérent avec l'historique, mise à jour du `README` d'installation testé depuis un clone vierge, tri du backlog restant. Puis commit vide `[JALON] v1.0`.

5. **Étape 5 — Épreuve Git.** Clone de `EPREUVE/git-lab.bundle` dans un dépôt séparé `kfokam48-gitlab-164`. Résolution des cinq situations décrites dans le README du bundle. Création d'un second dépôt public sur GitHub, ajout de `origin`, `git push origin --all`. Aucun mélange avec l'historique du projet.

6. **Étape 6 — Soumettre.** Rédaction de `SOUMISSION.md` avec les informations demandées, récupération des hash complets (40 caractères) des commits finaux des deux dépôts, téléversement sur la plateforme avant 18h00.

### Discipline Git tenue tout au long

- Une branche par ticket, nommée `feat/<numéro>-<slug>` ou `fix/<numéro>-<slug>`.
- Une pull request par branche, liée à l'issue via `Closes #x`.
- Commits atomiques, messages explicites (`feat:`, `fix:`, `docs:`, `chore:`, `test:`).
- `main` toujours stable : pas de commit direct sur `main`, tout passe par PR.
- `.gitignore` posé avant le premier commit de code.
- Aucun secret, aucun fichier généré (`target/`, `node_modules/`, `dist/`) commité.
- Les trois commits `[JALON]` présents et dans l'ordre : `analyse`, `v0.1`, `v1.0`.

### Definition of Done — un ticket est terminé quand :

1. Le code est écrit, respecte les contraintes techniques B1-B6 (backend) ou F1-F3 (frontend).
2. Le critère d'acceptation de l'issue est vérifié manuellement ou par un test.
3. Le code est commité sur une branche dédiée, poussé, et fait l'objet d'une pull request.
4. La pull request est mergée dans `main`, et l'issue est fermée (`Closes #x`).
5. Le cas échéant, une migration Flyway a été ajoutée et commitée.
6. Le cas échéant, le contrat `api/contrat.yaml` a été mis à jour.
7. Le `JOURNAL.md` est mis à jour si l'étape en cours est concernée.

### Gestion des aléas

- **Blocage technique > 20 minutes** : consigné dans `JOURNAL.md` avec la durée et la solution retenue (ou contournée).
- **Découverte d'une zone d'ombre non tranchée** : ajout d'une ligne en section 7 du présent cahier des charges, avec décision justifiée.
- **Changement de besoin (étape 3)** : ouverture d'une issue dédiée, correctif et évolution séparés, mise à jour de l'analyse dans un commit qui le dit.