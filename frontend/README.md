# Frontend — Suivi des exercices (KFOKAM48)

Application React 19 + Vite (port 4200) pour l'épreuve fullstack : trois écrans
(formateur, étudiant, relecteur) consommant l'API Spring Boot du backend (port 8085).

## Installation

```bash
npm install
```

## Développement

```bash
npm run dev
```

L'application est servie sur http://localhost:4200 (host 0.0.0.0). Les appels
`/api` sont relayés vers http://localhost:8085 par le proxy Vite (issue #35).

## Build de production

```bash
npm run build
```

## Tests

Les tests utilisent **Vitest** avec **@testing-library/react** et **jsdom**.
La configuration (`environment: jsdom`, `globals`, `setupFiles`) vit dans
`vite.config.js` ; le fichier `src/test/setup.js` charge les matchers jest-dom.

```bash
npm test          # exécution unique (vitest run)
npm run test:watch  # mode watch (vitest)
```

Couverture :

- `src/api/client.test.js` — couche API : chemins, verbes, bodies, format d'erreur `{ code, message }`, erreur réseau ;
- `src/api/useApi.test.js` — hook `useApi` : `loading`, `data`, `error`, changement de dépendances, démontage ;
- `src/pages/FormateurPage.test.jsx` — promotions, code de session, tableau (moyenne « — » si null) ;
- `src/pages/EtudiantPage.test.jsx` — présence (confirmation + erreur 410), notes sans relecteur (RG7) ;
- `src/pages/RelecteurPage.test.jsx` — relectures en attente, retrait après rendu, erreurs lisibles.

Les tests ne font pas partie du build de production (`npm run build` ne les
inclut pas) et ne tournent pas dans Docker.
