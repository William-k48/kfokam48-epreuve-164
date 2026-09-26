import { useState } from 'react'
import { Link } from 'react-router-dom'
import {
  deposerExercice,
  getEtudiants,
  getExercicesEtudiant,
  getPromotions,
  marquerPresence,
} from '../api/client.js'
import { useApi } from '../api/useApi.js'

const MESSAGES_ERREUR_PRESENCE = {
  CODE_INCONNU: 'Code inconnu.',
  DEJA_PRESENT: 'Vous avez déjà marqué votre présence.',
  CODE_EXPIRE: 'Le code a expiré.',
  TROP_DE_TENTATIVES: 'Trop de tentatives, réessayez dans 2 minutes.',
}

const MESSAGES_ERREUR_DEPOT = {
  LIEN_INVALIDE: 'Lien invalide (http ou https attendu).',
  EXERCICE_DEJA_DEPOSE: 'Vous avez déjà déposé un exercice pour cette session.',
}

function messageErreur(erreur, messages) {
  return messages[erreur.code] || erreur.message || 'Une erreur est survenue.'
}

function EtudiantPage() {
  const {
    data: promotions,
    loading: promotionsEnCours,
    error: erreurPromotions,
  } = useApi(getPromotions)

  const [promotionId, setPromotionId] = useState('')
  const {
    data: etudiants,
    loading: etudiantsEnCours,
    error: erreurEtudiants,
  } = useApi(
    () => (promotionId ? getEtudiants(Number(promotionId)) : Promise.resolve(null)),
    [promotionId],
  )

  const [etudiantId, setEtudiantId] = useState('')
  const identifie = etudiantId !== ''

  const [code, setCode] = useState('')
  const [presenceOk, setPresenceOk] = useState(false)
  const [erreurPresence, setErreurPresence] = useState(null)
  const [presenceEnCours, setPresenceEnCours] = useState(false)

  const [sessionId, setSessionId] = useState('')
  const [lien, setLien] = useState('')
  const [depotOk, setDepotOk] = useState(null)
  const [erreurDepot, setErreurDepot] = useState(null)
  const [depotEnCours, setDepotEnCours] = useState(false)

  const [rafraichirExercices, setRafraichirExercices] = useState(0)
  const {
    data: exercices,
    loading: exercicesEnCours,
    error: erreurExercices,
  } = useApi(
    () =>
      identifie ? getExercicesEtudiant(Number(etudiantId)) : Promise.resolve(null),
    [etudiantId, rafraichirExercices],
  )

  const changerPromotion = (event) => {
    setPromotionId(event.target.value)
    setEtudiantId('')
  }

  const soumettrePresence = async (event) => {
    event.preventDefault()
    setPresenceEnCours(true)
    setErreurPresence(null)
    setPresenceOk(false)

    try {
      await marquerPresence(code.trim().toUpperCase(), Number(etudiantId))
      setPresenceOk(true)
      setCode('')
    } catch (e) {
      setErreurPresence(e)
    } finally {
      setPresenceEnCours(false)
    }
  }

  const soumettreDepot = async (event) => {
    event.preventDefault()
    setDepotEnCours(true)
    setErreurDepot(null)
    setDepotOk(null)

    try {
      const exercice = await deposerExercice(
        Number(sessionId),
        Number(etudiantId),
        lien.trim(),
      )
      setDepotOk(exercice)
      setLien('')
      setRafraichirExercices((compteur) => compteur + 1)
    } catch (e) {
      setErreurDepot(e)
    } finally {
      setDepotEnCours(false)
    }
  }

  return (
    <main className="page">
      <h1>Écran étudiant</h1>

      <section className="section" aria-labelledby="titre-identification">
        <h2 id="titre-identification">Qui êtes-vous ?</h2>

        {promotionsEnCours && <p>Chargement des promotions…</p>}
        {erreurPromotions && (
          <p className="message erreur" role="alert">
            {messageErreur(erreurPromotions, {})}
          </p>
        )}

        {promotions && (
          <div className="formulaire">
            <div className="champ">
              <label htmlFor="promotion-etudiant">Promotion</label>
              <select
                id="promotion-etudiant"
                value={promotionId}
                onChange={changerPromotion}
              >
                <option value="">Sélectionner…</option>
                {promotions.map((promotion) => (
                  <option key={promotion.id} value={promotion.id}>
                    {promotion.nom}
                  </option>
                ))}
              </select>
            </div>

            {etudiantsEnCours && <p>Chargement des étudiants…</p>}
            {erreurEtudiants && (
              <p className="message erreur" role="alert">
                {messageErreur(erreurEtudiants, {})}
              </p>
            )}

            {etudiants && (
              <div className="champ">
                <label htmlFor="etudiant">Votre nom</label>
                <select
                  id="etudiant"
                  value={etudiantId}
                  onChange={(e) => setEtudiantId(e.target.value)}
                >
                  <option value="">Sélectionner…</option>
                  {etudiants.map((etudiant) => (
                    <option key={etudiant.id} value={etudiant.id}>
                      {etudiant.nom}
                    </option>
                  ))}
                </select>
              </div>
            )}
          </div>
        )}
      </section>

      <section className="section" aria-labelledby="titre-presence">
        <h2 id="titre-presence">Marquer ma présence</h2>

        <form className="formulaire" onSubmit={soumettrePresence}>
          <div className="champ">
            <label htmlFor="code-session">Code de la session</label>
            <input
              id="code-session"
              type="text"
              value={code}
              onChange={(e) => setCode(e.target.value)}
              placeholder="6 caractères"
              maxLength={6}
              autoComplete="off"
            />
          </div>
          <button
            className="bouton"
            type="submit"
            disabled={!identifie || code.trim().length !== 6 || presenceEnCours}
          >
            {presenceEnCours ? 'Envoi…' : 'Marquer ma présence'}
          </button>
        </form>

        {presenceOk && (
          <p className="message succes" role="status">
            Présence marquée, merci !
          </p>
        )}
        {erreurPresence && (
          <p className="message erreur" role="alert">
            {messageErreur(erreurPresence, MESSAGES_ERREUR_PRESENCE)}
          </p>
        )}
      </section>

      <section className="section" aria-labelledby="titre-depot">
        <h2 id="titre-depot">Déposer un exercice</h2>

        <form className="formulaire" onSubmit={soumettreDepot}>
          <div className="champ">
            <label htmlFor="session-depot">Numéro de la session</label>
            <input
              id="session-depot"
              type="number"
              min="1"
              value={sessionId}
              onChange={(e) => setSessionId(e.target.value)}
              placeholder="Ex. 1"
            />
          </div>
          <div className="champ">
            <label htmlFor="lien-depot">Lien de l&apos;exercice</label>
            <input
              id="lien-depot"
              type="url"
              value={lien}
              onChange={(e) => setLien(e.target.value)}
              placeholder="https://…"
            />
          </div>
          <button
            className="bouton"
            type="submit"
            disabled={!identifie || !sessionId || !lien.trim() || depotEnCours}
          >
            {depotEnCours ? 'Dépôt…' : 'Déposer'}
          </button>
        </form>

        {depotOk && (
          <p className="message succes" role="status">
            Exercice déposé (statut : {depotOk.statut}).
          </p>
        )}
        {erreurDepot && (
          <p className="message erreur" role="alert">
            {messageErreur(erreurDepot, MESSAGES_ERREUR_DEPOT)}
          </p>
        )}
      </section>

      <section className="section" aria-labelledby="titre-notes">
        <h2 id="titre-notes">Mes exercices et notes</h2>

        {!identifie && <p>Sélectionnez votre nom pour voir vos exercices.</p>}

        {identifie && exercicesEnCours && <p>Chargement…</p>}
        {identifie && erreurExercices && (
          <p className="message erreur" role="alert">
            {messageErreur(erreurExercices, {})}
          </p>
        )}
        {identifie && exercices && exercices.length === 0 && (
          <p>Aucun exercice déposé pour le moment.</p>
        )}
        {identifie && exercices && exercices.length > 0 && (
          <div className="tableau-wrapper">
            <table className="tableau">
              <thead>
                <tr>
                  <th scope="col">Exercice</th>
                  <th scope="col">Statut</th>
                  <th scope="col" className="nombre">
                    Note
                  </th>
                  <th scope="col">Commentaire</th>
                </tr>
              </thead>
              <tbody>
                {exercices.map((exercice) => (
                  <tr key={exercice.id}>
                    <td>
                      <a href={exercice.lien} target="_blank" rel="noreferrer">
                        {exercice.lien}
                      </a>
                    </td>
                    <td>{exercice.statut}</td>
                    <td className="nombre">
                      {exercice.note ?? '—'}
                      {exercice.noteProvisoire === true && (
                        <span className="note-provisoire"> (provisoire)</span>
                      )}
                    </td>
                    <td>{exercice.commentaire ?? '—'}</td>
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

export default EtudiantPage
