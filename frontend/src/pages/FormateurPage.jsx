import { Link } from 'react-router-dom'

function FormateurPage() {
  return (
    <main className="page">
      <h1>Écran formateur</h1>
      <p>À venir : ouverture de session et tableau de synthèse (issue #29).</p>
      <Link className="retour" to="/">
        ← Retour à l&apos;accueil
      </Link>
    </main>
  )
}

export default FormateurPage
