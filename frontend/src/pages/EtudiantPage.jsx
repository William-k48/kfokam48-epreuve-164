import { useCallback, useState } from 'react'
import Toast from '../components/Toast.jsx'
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

/** Badge de statut d'exercice (libellés lisibles, valeurs API inchangées). */
function BadgeStatut({ statut }) {
  if (statut === 'RELUE') {
    return <span className="badge succes">Relu</span>
  }
  return <span className="badge neutre">En attente</span>
}

/** Badge de couleur pour une note individuelle (mission #48). */
function BadgeNote({ note }) {
  if (note === null || note === undefined) {
    return <span className="badge neutre">—</span>
  }
  const valeur = Number(note)
  if (valeur > 15) return <span className="badge succes">{valeur}</span>
  if (valeur >= 10) return <span className="badge alerte">{valeur}</span>
  return <span className="badge erreur">{valeur}</span>
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

  // Nom et promotion sélectionnés (pour le résumé de l'étape 1)
  const nomSelectionne = etudiants?.find((e) => String(e.id) === etudiantId)?.nom
  const promotionSelectionnee = promotions?.find((p) => String(p.id) === promotionId)?.nom

  const changerPromotion = (event) => {
    setPromotionId(event.target.value)
    setEtudiantId('')
  }

  const fermerToast = useCallback(() => setPresenceOk(false), [])

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
    <div className="page">
      <h1>Écran étudiant</h1>
      <p className="sous-titre">
        Identifiez-vous, marquez votre présence, déposez votre exercice.
      </p>

      {/* Étape 1 — Qui êtes-vous ? Se réduit à un résumé une fois identifié. */}
      <section className="section carte-bloc" aria-labelledby="titre-identification">
        <h2 id="titre-identification">
          <span className="etape-numero" aria-hidden="true">1</span>
          Qui êtes-vous ?
        </h2>

        {promotionsEnCours && <p>Chargement des promotions…</p>}
        {erreurPromotions && (
          <p className="message erreur" role="alert">
            {messageErreur(erreurPromotions, {})}
          </p>
        )}

        {!identifie && promotions && (
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

        {identifie && (
          <div className="identification-resume" role="status">
            <span className="badge succes">✓</span>
            <p>
              <strong>{nomSelectionne}</strong>
              {promotionSelectionnee && ` — ${promotionSelectionnee}`}
            </p>
          </div>
        )}
      </section>

      {/* Étape 2 — Marquer ma présence */}
      <section
        className={`section carte-bloc${!identifie ? ' etape-verrouillee' : ''}`}
        aria-labelledby="titre-presence"
      >
        <h2 id="titre-presence">
          <span className="etape-numero" aria-hidden="true">2</span>
          Marquer ma présence
        </h2>

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
              className="champ-code"
            />
          </div>
          <button
            className="bouton"
            type="submit"
            disabled={!identifie || code.trim().length !== 6 || presenceEnCours}
          >
            <svg viewBox="0 0 24 24" width="18" height="18" aria-hidden="true" fill="none"
              stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <path d="M20 6L9 17l-5-5" />
            </svg>
            {presenceEnCours ? 'Envoi…' : 'Marquer ma présence'}
          </button>
        </form>

        {erreurPresence && (
          <p className="message erreur" role="alert">
            {messageErreur(erreurPresence, MESSAGES_ERREUR_PRESENCE)}
          </p>
        )}
      </section>

      {/* Étape 3 — Déposer un exercice */}
      <section
        className={`section carte-bloc${!identifie ? ' etape-verrouillee' : ''}`}
        aria-labelledby="titre-depot"
      >
        <h2 id="titre-depot">
          <span className="etape-numero" aria-hidden="true">3</span>
          Déposer un exercice
        </h2>

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
            <svg viewBox="0 0 24 24" width="18" height="18" aria-hidden="true" fill="none"
              stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <path d="M21 15v4a2 2 0 01-2 2H5a2 2 0 01-2-2v-4" />
              <path d="M17 8l-5-5-5 5" />
              <path d="M12 3v12" />
            </svg>
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

      {/* Mes exercices et notes — cartes individuelles */}
      <section className="section carte-bloc" aria-labelledby="titre-notes">
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
          <ul className="notes-cartes">
            {exercices.map((exercice) => (
              <li key={exercice.id} className="note-carte">
                <div className="note-carte-haut">
                  <a href={exercice.lien} target="_blank" rel="noreferrer">
                    {exercice.lien}
                  </a>
                  <BadgeStatut statut={exercice.statut} />
                </div>
                <div className="note-carte-bas">
                  <span className="note-carte-note">
                    <BadgeNote note={exercice.note} />
                    {exercice.noteProvisoire === true && (
                      <span className="note-provisoire"> (provisoire)</span>
                    )}
                  </span>
                  <p className="note-carte-commentaire">
                    {exercice.commentaire ?? '—'}
                  </p>
                </div>
              </li>
            ))}
          </ul>
        )}
      </section>

      <p className="retour-mention">
        Utilisez le menu à gauche pour revenir à l&apos;accueil.
      </p>

      <Toast
        message={presenceOk ? '✓ Présence marquée, merci !' : null}
        onFerme={fermerToast}
      />
    </div>
  )
}

export default EtudiantPage
