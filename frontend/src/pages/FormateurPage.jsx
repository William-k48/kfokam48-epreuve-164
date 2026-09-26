import { useState } from 'react'
import { Link } from 'react-router-dom'
import { getPromotions, getTableau, ouvrirSession } from '../api/client.js'
import { useApi } from '../api/useApi.js'

function formaterDate(valeur) {
  return new Date(valeur).toLocaleString('fr-FR', {
    dateStyle: 'medium',
    timeStyle: 'short',
  })
}

function formaterMoyenne(moyenne) {
  if (moyenne === null) return '—'
  return Number(moyenne.toFixed(2)).toLocaleString('fr-FR')
}

function messageErreur(erreur) {
  if (erreur.code === 'PROMOTION_INCONNUE') {
    return "Cette promotion n'existe pas."
  }
  return erreur.message || 'Une erreur est survenue.'
}

function FormateurPage() {
  const {
    data: promotions,
    loading: promotionsEnCours,
    error: erreurPromotions,
  } = useApi(getPromotions)

  const [titre, setTitre] = useState('')
  const [promotionSessionId, setPromotionSessionId] = useState('')
  const [sessionCreee, setSessionCreee] = useState(null)
  const [erreurSession, setErreurSession] = useState(null)
  const [ouvertureEnCours, setOuvertureEnCours] = useState(false)

  const [promotionTableauId, setPromotionTableauId] = useState('')
  const {
    data: lignes,
    loading: tableauEnCours,
    error: erreurTableau,
  } = useApi(
    () =>
      promotionTableauId
        ? getTableau(Number(promotionTableauId))
        : Promise.resolve(null),
    [promotionTableauId],
  )

  const soumettreOuverture = async (event) => {
    event.preventDefault()
    setOuvertureEnCours(true)
    setErreurSession(null)
    setSessionCreee(null)

    try {
      const session = await ouvrirSession(titre.trim(), Number(promotionSessionId))
      setSessionCreee(session)
      setTitre('')
    } catch (e) {
      setErreurSession(e)
    } finally {
      setOuvertureEnCours(false)
    }
  }

  return (
    <main className="page">
      <h1>Écran formateur</h1>

      <section className="section" aria-labelledby="titre-ouverture">
        <h2 id="titre-ouverture">Ouvrir une session</h2>

        {promotionsEnCours && <p>Chargement des promotions…</p>}
        {erreurPromotions && (
          <p className="message erreur" role="alert">
            {messageErreur(erreurPromotions)}
          </p>
        )}

        {promotions && (
          <form className="formulaire" onSubmit={soumettreOuverture}>
            <div className="champ">
              <label htmlFor="titre-session">Titre de la session</label>
              <input
                id="titre-session"
                type="text"
                value={titre}
                onChange={(e) => setTitre(e.target.value)}
                placeholder="Ex. TP 4 — Design patterns"
                maxLength={200}
              />
            </div>
            <div className="champ">
              <label htmlFor="promotion-session">Promotion</label>
              <select
                id="promotion-session"
                value={promotionSessionId}
                onChange={(e) => setPromotionSessionId(e.target.value)}
              >
                <option value="">Sélectionner…</option>
                {promotions.map((promotion) => (
                  <option key={promotion.id} value={promotion.id}>
                    {promotion.nom}
                  </option>
                ))}
              </select>
            </div>
            <button
              className="bouton"
              type="submit"
              disabled={!titre.trim() || !promotionSessionId || ouvertureEnCours}
            >
              {ouvertureEnCours ? 'Ouverture…' : 'Ouvrir la session'}
            </button>
          </form>
        )}

        {sessionCreee && (
          <div className="message succes" role="status">
            <p>Session ouverte !</p>
            <p className="code-session">
              Code : <strong>{sessionCreee.code}</strong>
            </p>
            <p>
              Ouverte le {formaterDate(sessionCreee.ouvertureAt)} · expire le{' '}
              {formaterDate(sessionCreee.expirationAt)}
            </p>
          </div>
        )}
        {erreurSession && (
          <p className="message erreur" role="alert">
            {messageErreur(erreurSession)}
          </p>
        )}
      </section>

      <section className="section" aria-labelledby="titre-tableau">
        <h2 id="titre-tableau">Tableau de synthèse</h2>

        {promotions && (
          <div className="champ champ-tableau">
            <label htmlFor="promotion-tableau">Promotion</label>
            <select
              id="promotion-tableau"
              value={promotionTableauId}
              onChange={(e) => setPromotionTableauId(e.target.value)}
            >
              <option value="">Sélectionner…</option>
              {promotions.map((promotion) => (
                <option key={promotion.id} value={promotion.id}>
                  {promotion.nom}
                </option>
              ))}
            </select>
          </div>
        )}

        {tableauEnCours && <p>Chargement du tableau…</p>}
        {erreurTableau && (
          <p className="message erreur" role="alert">
            {messageErreur(erreurTableau)}
          </p>
        )}
        {lignes && lignes.length === 0 && (
          <p>Aucun étudiant dans cette promotion.</p>
        )}
        {lignes && lignes.length > 0 && (
          <div className="tableau-wrapper">
            <table className="tableau">
              <thead>
                <tr>
                  <th scope="col">Étudiant</th>
                  <th scope="col" className="nombre">
                    Présences
                  </th>
                  <th scope="col" className="nombre">
                    Exercices déposés
                  </th>
                  <th scope="col" className="nombre">
                    Moyenne
                  </th>
                  <th scope="col" className="nombre">
                    Relectures en attente
                  </th>
                </tr>
              </thead>
              <tbody>
                {lignes.map((ligne) => (
                  <tr key={ligne.etudiantId}>
                    <td>{ligne.nom}</td>
                    <td className="nombre">{ligne.presences}</td>
                    <td className="nombre">{ligne.exercicesDeposes}</td>
                    <td className="nombre">{formaterMoyenne(ligne.moyenne)}</td>
                    <td className="nombre">{ligne.relecturesEnAttente}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>

      <Link className="retour" to="/">
        ← Retour à l&apos;accueil
      </Link>
    </main>
  )
}

export default FormateurPage
