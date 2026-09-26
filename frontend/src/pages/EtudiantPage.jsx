import { Link } from 'react-router-dom'

function EtudiantPage() {
  return (
    <main className="page">
      <h1>Écran étudiant</h1>
      <p>À venir : présence, dépôt d&apos;exercice et consultation des notes (issue #30).</p>
      <Link className="retour" to="/">
        ← Retour à l&apos;accueil
      </Link>
    </main>
  )
}

export default EtudiantPage
