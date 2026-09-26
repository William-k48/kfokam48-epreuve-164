import { Link, useLocation } from 'react-router-dom'

// Libellés de fil d'Ariane par route (les libellés d'écrans restent inchangés)
const LIBELLES = {
  '/formateur': 'Formateur',
  '/etudiant': 'Étudiant',
  '/relecteur': 'Relecteur',
}

/**
 * Barre supérieure fixe (issue #48) : hamburger (mobile), fil d'Ariane
 * « Accueil > {Écran} », cloche de notifications décorative.
 * Aucune logique métier : le fil d'Ariane est dérivé de la route courante.
 */
function Header({ onOuvreMenu }) {
  const { pathname } = useLocation()
  const libelle = LIBELLES[pathname]

  return (
    <header className="header">
      <div className="header-gauche">
        <button
          type="button"
          className="header-hamburger"
          aria-label="Ouvrir le menu de navigation"
          onClick={onOuvreMenu}
        >
          <svg viewBox="0 0 24 24" width="22" height="22" aria-hidden="true" fill="none"
            stroke="currentColor" strokeWidth="2" strokeLinecap="round">
            <path d="M4 6h16M4 12h16M4 18h16" />
          </svg>
        </button>

        <nav className="fil-ariane" aria-label="Fil d'Ariane">
          <Link to="/">Accueil</Link>
          {libelle && (
            <>
              <span className="fil-separateur" aria-hidden="true">
                ›
              </span>
              <span aria-current="page">{libelle}</span>
            </>
          )}
        </nav>
      </div>

      <div className="header-droite">
        <button
          type="button"
          className="header-cloche"
          aria-label="Notifications (aucune notification)"
          disabled
        >
          <svg viewBox="0 0 24 24" width="20" height="20" aria-hidden="true" fill="none"
            stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <path d="M18 8a6 6 0 10-12 0c0 7-3 9-3 9h18s-3-2-3-9" />
            <path d="M13.7 21a2 2 0 01-3.4 0" />
          </svg>
        </button>
      </div>
    </header>
  )
}

export default Header
