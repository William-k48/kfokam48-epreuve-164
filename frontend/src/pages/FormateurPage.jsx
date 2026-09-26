import { useState } from 'react'
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

/** Badge de couleur pour une note (mission #48 : >15 vert, 10–15 orange, <10 rouge). */
function BadgeNote({ note }) {
  if (note === null || note === undefined) {
    return <span className="badge neutre">—</span>
  }
  const valeur = Number(note)
  if (valeur > 15) return <span className="badge succes">{formaterMoyenne(valeur)}</span>
  if (valeur >= 10) return <span className="badge alerte">{formaterMoyenne(valeur)}</span>
  return <span className="badge erreur">{formaterMoyenne(valeur)}</span>
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
  const [codeCopie, setCodeCopie] = useState(false)

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
      setCodeCopie(false)
    } catch (e) {
      setErreurSession(e)
    } finally {
      setOuvertureEnCours(false)
    }
  }

  const copierCode = async () => {
    if (!sessionCreee?.code) return
    try {
      await navigator.clipboard.writeText(sessionCreee.code)
      setCodeCopie(true)
      setTimeout(() => setCodeCopie(false), 2000)
    } catch {
      // Presse-papier indisponible (permission, contexte non sécurisé) :
      // le code reste affiché, l'étudiant peut le recopier à la main.
    }
  }

  return (
    <div className="page">
      <h1>Écran formateur</h1>
      <p className="sous-titre">
        Ouvrez une session, partagez son code, suivez la promotion en un coup d&apos;œil.
      </p>

      <div className="formateur-grille">
        <section className="section carte-bloc" aria-labelledby="titre-ouverture">
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
                <svg viewBox="0 0 24 24" width="18" height="18" aria-hidden="true" fill="none"
                  stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <polygon points="6 3 20 12 6 21 6 3" />
                </svg>
                {ouvertureEnCours ? 'Ouverture…' : 'Lancer la session'}
              </button>
            </form>
          )}

          {sessionCreee && (
            <div className="message succes session-creee" role="status">
              <p>Session ouverte !</p>
              <p className="code-session">{sessionCreee.code}</p>
              <button
                type="button"
                className="bouton secondaire bouton-copier"
                onClick={copierCode}
              >
                <svg viewBox="0 0 24 24" width="16" height="16" aria-hidden="true" fill="none"
                  stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <rect x="9" y="9" width="12" height="12" rx="2" />
                  <path d="M5 15V5a2 2 0 012-2h10" />
                </svg>
                {codeCopie ? 'Copié !' : 'Copier'}
              </button>
              <p className="session-dates">
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

        <section className="section carte-bloc" aria-labelledby="titre-tableau">
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
                    <th scope="col" className="tableau-actions">
                      <span className="sr-only">Détail</span>
                    </th>
                  </tr>
                </thead>
                <tbody>
                  {lignes.map((ligne) => (
                    <tr key={ligne.etudiantId}>
                      <td>{ligne.nom}</td>
                      <td className="nombre">{ligne.presences}</td>
                      <td className="nombre">{ligne.exercicesDeposes}</td>
                      <td className="nombre">
                        <BadgeNote note={ligne.moyenne} />
                      </td>
                      <td className="nombre">{ligne.relecturesEnAttente}</td>
                      <td className="tableau-actions">
                        <span className="tableau-oeil" title="Détail à venir">
                          <svg viewBox="0 0 24 24" width="18" height="18" aria-hidden="true" fill="none"
                            stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                            <path d="M2 12s3.5-7 10-7 10 7 10 7-3.5 7-10 7-10-7-10-7z" />
                            <circle cx="12" cy="12" r="3" />
                          </svg>
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </section>
      </div>
    </div>
  )
}

export default FormateurPage
