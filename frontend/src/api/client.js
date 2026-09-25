const BASE_URL = 'http://localhost:8085'

const ERREUR_RESEAU = {
  code: 'ERREUR_RESEAU',
  message: 'Impossible de contacter le serveur.',
}

async function call(path, { method = 'GET', body } = {}) {
  let reponse

  try {
    reponse = await fetch(BASE_URL + path, {
      method,
      headers: { 'Content-Type': 'application/json' },
      ...(body === undefined ? {} : { body: JSON.stringify(body) }),
    })
  } catch {
    // fetch ne rejette que si le serveur est injoignable (arrêté, hors ligne, CORS).
    throw { ...ERREUR_RESEAU }
  }

  const texte = await reponse.text()
  let donnees = null

  if (texte) {
    try {
      donnees = JSON.parse(texte)
    } catch {
      donnees = null
    }
  }

  if (!reponse.ok) {
    // ENF6 : toute erreur de l'API est normalisée en { code, message }.
    throw {
      code: donnees?.code || 'ERREUR_INCONNUE',
      message: donnees?.message || `Erreur HTTP ${reponse.status}`,
      status: reponse.status,
    }
  }

  return donnees
}

export const ouvrirSession = (titre, promotionId) =>
  call('/api/sessions', { method: 'POST', body: { titre, promotionId } })

export const marquerPresence = (code, etudiantId) =>
  call('/api/presences', { method: 'POST', body: { code, etudiantId } })

export const deposerExercice = (sessionId, etudiantId, lien) =>
  call('/api/exercices', { method: 'POST', body: { sessionId, etudiantId, lien } })

export const rendreRelecture = (exerciceId, note, commentaire) =>
  call(`/api/relectures/${exerciceId}`, {
    method: 'POST',
    body: { note, commentaire },
  })

export const getTableau = (promotionId) =>
  call(`/api/tableau?promotionId=${promotionId}`)

export const getPromotions = () => call('/api/promotions')

export const getEtudiants = (promotionId) =>
  call(`/api/promotions/${promotionId}/etudiants`)

export const ajouterPresenceManuelle = (sessionId, etudiantId) =>
  call(`/api/sessions/${sessionId}/presences`, {
    method: 'POST',
    body: { etudiantId },
  })

export const remplacerLien = (exerciceId, lien) =>
  call(`/api/exercices/${exerciceId}`, { method: 'PATCH', body: { lien } })

export const reassignerRelecteur = (exerciceId, relecteurId) =>
  call(`/api/exercices/${exerciceId}/relecteur`, {
    method: 'PATCH',
    body: { relecteurId },
  })

export const getExercicesEtudiant = (etudiantId) =>
  call(`/api/etudiants/${etudiantId}/exercices`)

export const getRelecturesEtudiant = (etudiantId) =>
  call(`/api/etudiants/${etudiantId}/relectures`)
