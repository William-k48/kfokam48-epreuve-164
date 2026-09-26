import { Link, Route, Routes } from 'react-router-dom'
import './App.css'
import AppLayout from './layouts/AppLayout.jsx'
import EtudiantPage from './pages/EtudiantPage.jsx'
import FormateurPage from './pages/FormateurPage.jsx'
import RelecteurPage from './pages/RelecteurPage.jsx'

const ecrans = [
  {
    to: '/formateur',
    titre: 'Écran formateur',
    description:
      'Ouvrir une session, communiquer le code aux étudiants et consulter le tableau de synthèse.',
    variante: 'formateur',
    icone: IconeFormateur,
  },
  {
    to: '/etudiant',
    titre: 'Écran étudiant',
    description:
      'Marquer sa présence avec le code de la session, déposer un exercice et consulter ses notes.',
    variante: 'etudiant',
    icone: IconeEtudiant,
  },
  {
    to: '/relecteur',
    titre: 'Écran relecteur',
    description:
      'Consulter les exercices assignés et rendre une note avec un commentaire.',
    variante: 'relecteur',
    icone: IconeRelecteur,
  },
]

function IconeFormateur() {
  return (
    <svg viewBox="0 0 24 24" width="28" height="28" aria-hidden="true" fill="none"
      stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M2 7l10-3 10 3-10 3L2 7z" />
      <path d="M6 9v5c0 1.5 2.7 3 6 3s6-1.5 6-3V9" />
      <path d="M22 7v6" />
    </svg>
  )
}

function IconeEtudiant() {
  return (
    <svg viewBox="0 0 24 24" width="28" height="28" aria-hidden="true" fill="none"
      stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <circle cx="12" cy="8" r="4" />
      <path d="M4 21c0-4 3.6-6 8-6s8 2 8 6" />
    </svg>
  )
}

function IconeRelecteur() {
  return (
    <svg viewBox="0 0 24 24" width="28" height="28" aria-hidden="true" fill="none"
      stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M9 11l3 3 8-8" />
      <path d="M20 12v6a2 2 0 01-2 2H6a2 2 0 01-2-2V6a2 2 0 012-2h9" />
    </svg>
  )
}

function Accueil() {
  return (
    <div className="page">
      <h1>Suivi des exercices</h1>
      <p className="sous-titre">
        Présences, dépôt d&apos;exercices et relectures anonymes, session par session.
      </p>

      <ul className="cartes accueil-cartes">
        {ecrans.map((ecran) => (
          <li key={ecran.to}>
            <Link className="carte accueil-carte" to={ecran.to}>
              <span className={`accueil-icone accueil-icone-${ecran.variante}`}>
                <ecran.icone />
              </span>
              <span className="carte-titre">{ecran.titre}</span>
              <span className="carte-description">{ecran.description}</span>
              <span className={`bouton accueil-acceder accueil-acceder-${ecran.variante}`}>
                Accéder
              </span>
            </Link>
          </li>
        ))}
      </ul>
    </div>
  )
}

function App() {
  return (
    <Routes>
      <Route element={<AppLayout />}>
        <Route path="/" element={<Accueil />} />
        <Route path="/formateur" element={<FormateurPage />} />
        <Route path="/etudiant" element={<EtudiantPage />} />
        <Route path="/relecteur" element={<RelecteurPage />} />
      </Route>
    </Routes>
  )
}

export default App
