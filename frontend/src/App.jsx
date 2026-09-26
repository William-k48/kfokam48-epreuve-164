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
  },
  {
    to: '/etudiant',
    titre: 'Écran étudiant',
    description:
      'Marquer sa présence avec le code de la session, déposer un exercice et consulter ses notes.',
  },
  {
    to: '/relecteur',
    titre: 'Écran relecteur',
    description:
      'Consulter les exercices assignés et rendre une note avec un commentaire.',
  },
]

function Accueil() {
  return (
    <div className="page">
      <h1>Suivi des exercices</h1>
      <p className="sous-titre">
        Présences, dépôt d&apos;exercices et relectures anonymes, session par session.
      </p>
      <ul className="cartes">
        {ecrans.map((ecran) => (
          <li key={ecran.to}>
            <Link className="carte" to={ecran.to}>
              <span className="carte-titre">{ecran.titre}</span>
              <span className="carte-description">{ecran.description}</span>
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
