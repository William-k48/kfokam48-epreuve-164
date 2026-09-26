/**
 * État vide réutilisable (mission #48) : illustration + titre + texte.
 * Aucune logique métier.
 */
function EmptyState({ illustration, titre, texte }) {
  return (
    <div className="relectures-vide" role="status">
      {illustration}
      <p className="relectures-vide-titre">{titre}</p>
      {texte && <p className="relectures-vide-texte">{texte}</p>}
    </div>
  )
}

export default EmptyState
