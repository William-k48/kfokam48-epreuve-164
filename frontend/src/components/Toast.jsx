import { useEffect } from 'react'

/**
 * Toast réutilisable (mission #48) : notification transitoire en bas d'écran.
 * Accessible (role="status", annoncé par les lecteurs d'écran), disparait
 * seul après `duree` ms. Aucune logique métier : c'est un canal d'affichage
 * que les pages peuvent alimenter avec leurs messages existants.
 */
function Toast({ message, onFerme, duree = 3000 }) {
  useEffect(() => {
    if (!message) return undefined
    const minuteur = setTimeout(onFerme, duree)
    return () => clearTimeout(minuteur)
  }, [message, onFerme, duree])

  if (!message) return null

  return (
    <div className="toast" role="status">
      {message}
    </div>
  )
}

export default Toast
