import { useState } from 'react'
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

/** Illustration de l'état vide (tasse de café, SVG inline — mission #48). */
function IllustrationVide() {
  return (
    <svg viewBox="0 0 64 64" width="72" height="72" aria-hidden="true" fill="none"
      stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
      <path d="M14 26h28v14a10 10 0 01-10 10h-8a10 10 0 01-10-10V26z" />
      <path d="M42 30h4a6 6 0 010 12h-4" />
      <path d="M22 20c0-3 2-3 2-6M30 20c0-3 2-3 2-6" />
    </svg>
  )
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
    <form
      className="formulaire formulaire-relecture carte-bloc"
      onSubmit={soumettre}
    >
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
        <svg viewBox="0 0 24 24" width="18" height="18" aria-hidden="true" fill="none"
          stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
          <path d="M20 6L9 17l-5-5" />
        </svg>
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
    () => (promotionId ? getEtudiants(Number(promotionId)) : Promise.resolve(null)),
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
    () =>
      identifie ? getRelecturesEtudiant(Number(etudiantId)) : Promise.resolve(null),
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
    <div className="page">
      <h1>Écran relecteur</h1>
      <p className="sous-titre">
        Relisez les exercices qui vous sont assignés, en toute sérénité.
      </p>

      <section className="section carte-bloc" aria-labelledby="titre-identification">
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

        {!identifie && (
          <p className="carte-bloc">Sélectionnez votre nom pour voir vos relectures.</p>
        )}

        {identifie && relecturesEnCours && <p className="carte-bloc">Chargement…</p>}
        {identifie && erreurRelectures && (
          <p className="message erreur carte-bloc" role="alert">
            {messageErreur(erreurRelectures, {})}
          </p>
        )}

        {identifie && relectures && relectures.length === 0 && (
          <div className="carte-bloc relectures-vide" role="status">
            <IllustrationVide />
            <p className="relectures-vide-titre">Tout est à jour. Reposez-vous !</p>
            <p className="relectures-vide-texte">
              Aucune relecture en attente pour le moment.
            </p>
          </div>
        )}

        {identifie &&
          relectures &&
          relectures.length > 0 && (
            <div className="relectures-liste">
              {relectures.map((relecture) => (
                <FormulaireRelecture
                  key={relecture.exerciceId}
                  exerciceId={relecture.exerciceId}
                  lien={relecture.lien}
                  onRendue={relectureRendue}
                />
              ))}
            </div>
          )}

        {succes && (
          <p className="message succes" role="status">
            {succes}
          </p>
        )}
      </section>
    </div>
  )
}

export default RelecteurPage
