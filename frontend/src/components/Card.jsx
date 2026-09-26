/**
 * Carte réutilisable (mission #48) : bloc blanc ombré du design system.
 * Aucune logique métier — pur conteneur visuel.
 */
function Card({ titre, idTitre, children, className = '' }) {
  return (
    <section className={`carte-bloc ${className}`.trim()} aria-labelledby={idTitre}>
      {titre && <h2 id={idTitre}>{titre}</h2>}
      {children}
    </section>
  )
}

export default Card
