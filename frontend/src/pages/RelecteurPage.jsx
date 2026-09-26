import { Link } from 'react-router-dom'

function RelecteurPage() {
  return (
    <main className="page">
      <h1>Écran relecteur</h1>
      <p>À venir : relectures en attente et rendu de note (issue #31).</p>
      <Link className="retour" to="/">
        ← Retour à l&apos;accueil
      </Link>
    </main>
  )
}

export default RelecteurPage
