import { useState } from 'react'
import { Link } from 'react-router-dom'
import {
  getEtudiants,
  getPromotions,
  getRelecturesEtudiant,
  rendreRelecture,
} from '../api/client.js'
import { useApi } from '../api/useApi.js'

const MESSAGES_ERREUR_RELECTURE = {
  NOTE_INVALIDE: 'La note doit être un entier entre 0 et 20.',
  AUTO_RELECTURE: 'Vous ne pouvez pas relire votre propre exercice.',
  RELECTURE_DEJA_RENDUE: 'Cette relecture a déjà été rendue.',
}

function messageErreur(erreur, messages) {
  return messages[erreur.code] || erreur.message || 'Une erreur est survenue.'
}

function validerNote(valeur) {
  const note = Number(valeur)
  return Number.isInteger(note) && note >= 0 && note <= 20
}

function FormulaireRelecture({ exerciceId, lien, onRendue }) {
  const [note, setNote] = useState('')
  const [commentaire, setCommentaire] = useState('')
  const [erreur, setErreur] = useState(null)
  const [enCours, setEnCours] = useState(false)

  const soumettre = async (event) => {
    event.preventDefault()
    setEnCours(true)
    setErreur(null)

    try {
      await rendreRelecture(exerciceId, Number(note), commentaire.trim())
      onRendue(exerciceId)
    } catch (e) {
      setErreur(e)
      setEnCours(false)
    }
  }

  return (
    <form className="formulaire formulaire-relecture" onSubmit={soumettre}>
      <p className="legende">
        Exercice <strong>#{exerciceId}</strong> —{' '}
        <a href={lien} target="_blank" rel="noreferrer">
          ouvrir le lien
        </a>{' '}
        — l&apos;auteur reste anonyme.
      </p>
      <div className="champ champ-note">
        <label htmlFor={`note-${exerciceId}`}>Note (0 à 20)</label>
        <input
          id={`note-${exerciceId}`}
          type="number"
          min="0"
          max="20"
          step="1"
          value={note}
          onChange={(e) => setNote(e.target.value)}
        />
      </div>
      <div className="champ">
        <label htmlFor={`commentaire-${exerciceId}`}>Commentaire</label>
        <textarea
          id={`commentaire-${exerciceId}`}
          rows={4}
          maxLength={2000}
          value={commentaire}
          onChange={(e) => setCommentaire(e.target.value)}
        />
      </div>
      <button
        className="bouton"
        type="submit"
        disabled={enCours || !validerNote(note)}
      >
        {enCours ? 'Envoi…' : 'Rendre la relecture'}
      </button>
      {erreur && (
        <p className="message erreur" role="alert">
          {messageErreur(erreur, MESSAGES_ERREUR_RELECTURE)}
        </p>
      )}
    </form>
  )
}

function RelecteurPage() {
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
    () => (promotionId ? getEtudiants(promotionId) : Promise.resolve(null)),
    [promotionId],
  )

  const [etudiantId, setEtudiantId] = useState('')
  const identifie = etudiantId !== ''

  const [rafraichir, setRafraichir] = useState(0)
  const {
    data: relectures,
    loading: relecturesEnCours,
    error: erreurRelectures,
  } = useApi(
    () => (identifie ? getRelecturesEtudiant(etudiantId) : Promise.resolve(null)),
    [etudiantId, rafraichir],
  )

  const [succes, setSucces] = useState(null)

  const changerPromotion = (event) => {
    setPromotionId(event.target.value)
    setEtudiantId('')
    setSucces(null)
  }

  const changerEtudiant = (event) => {
    setEtudiantId(event.target.value)
    setSucces(null)
  }

  const relectureRendue = (exerciceId) => {
    setSucces(`Relecture rendue pour l'exercice #${exerciceId}.`)
    setRafraichir((compteur) => compteur + 1)
  }

  return (
    <main className="page">
      <h1>Écran relecteur</h1>

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
              <label htmlFor="promotion-relecteur">Promotion</label>
              <select
                id="promotion-relecteur"
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
                <label htmlFor="etudiant-relecteur">Votre nom</label>
                <select
                  id="etudiant-relecteur"
                  value={etudiantId}
                  onChange={changerEtudiant}
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

      <section className="section" aria-labelledby="titre-relectures">
        <h2 id="titre-relectures">Mes relectures en attente</h2>

        {!identifie && <p>Sélectionnez votre nom pour voir vos relectures.</p>}

        {identifie && relecturesEnCours && <p>Chargement…</p>}
        {identifie && erreurRelectures && (
          <p className="message erreur" role="alert">
            {messageErreur(erreurRelectures, {})}
          </p>
        )}
        {identifie && relectures && relectures.length === 0 && (
          <p>Aucune relecture en attente. Bravo !</p>
        )}

        {identifie &&
          relectures &&
          relectures.map((relecture) => (
            <FormulaireRelecture
              key={relecture.exerciceId}
              exerciceId={relecture.exerciceId}
              lien={relecture.lien}
              onRendue={relectureRendue}
            />
          ))}

        {succes && (
          <p className="message succes" role="status">
            {succes}
          </p>
        )}
      </section>

      <Link className="retour" to="/">
        ← Retour à l&apos;accueil
      </Link>
    </main>
  )
}

export default RelecteurPage
