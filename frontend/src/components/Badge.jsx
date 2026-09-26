/**
 * Badge réutilisable (mission #48) : pill colorée du design system.
 * `variante` : neutre | succes | alerte | erreur.
 * Aucune logique métier : la couleur est décidée par l'appelant
 * (les pages restent propriétaires des seuils >15 / 10-15 / <10).
 */
function Badge({ variante = 'neutre', children }) {
  return <span className={`badge ${variante}`}>{children}</span>
}

/** Choix de la variante pour une note 0-20 (>15 vert, 10-15 orange, <10 rouge). */
export function varianteNote(note) {
  const valeur = Number(note)
  if (valeur > 15) return 'succes'
  if (valeur >= 10) return 'alerte'
  return 'erreur'
}

export default Badge
