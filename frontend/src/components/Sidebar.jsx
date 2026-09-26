import { useEffect } from 'react'
import { NavLink } from 'react-router-dom'
import logo from '../assets/logo-kfokam48.png'

const items = [
  { to: '/', label: 'Tableau de bord', fin: true, icone: IconeTableauDeBord },
  { to: '/formateur', label: 'Espace Formateur', icone: IconeFormateur },
  { to: '/etudiant', label: 'Espace Étudiant', icone: IconeEtudiant },
  { to: '/relecteur', label: 'Espace Relecteur', icone: IconeRelecteur },
]

function IconeTableauDeBord() {
  return (
    <svg viewBox="0 0 24 24" width="20" height="20" aria-hidden="true" fill="none"
      stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <rect x="3" y="3" width="7" height="9" rx="1.5" />
      <rect x="14" y="3" width="7" height="5" rx="1.5" />
      <rect x="14" y="12" width="7" height="9" rx="1.5" />
      <rect x="3" y="16" width="7" height="5" rx="1.5" />
    </svg>
  )
}

function IconeFormateur() {
  return (
    <svg viewBox="0 0 24 24" width="20" height="20" aria-hidden="true" fill="none"
      stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M2 7l10-3 10 3-10 3L2 7z" />
      <path d="M6 9v5c0 1.5 2.7 3 6 3s6-1.5 6-3V9" />
      <path d="M22 7v6" />
    </svg>
  )
}

function IconeEtudiant() {
  return (
    <svg viewBox="0 0 24 24" width="20" height="20" aria-hidden="true" fill="none"
      stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <circle cx="12" cy="8" r="4" />
      <path d="M4 21c0-4 3.6-6 8-6s8 2 8 6" />
    </svg>
  )
}

function IconeRelecteur() {
  return (
    <svg viewBox="0 0 24 24" width="20" height="20" aria-hidden="true" fill="none"
      stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M20 6L9 17l-5-5" />
    </svg>
  )
}

/**
 * Barre latérale de navigation (issue #48).
 * Purement visuelle : aucune logique métier, aucune auth (le bouton
 * « Déconnexion » est désactivé, le nom affiché est « Invité »).
 * Mobile : drawer en overlay, fermé par le voile, par Échap ou par un clic
 * sur un lien (l'état `ouvert` est possédé par AppLayout).
 */
function Sidebar({ ouvert, onFerme }) {
  useEffect(() => {
    if (!ouvert) return
    const onTouche = (event) => {
      if (event.key === 'Escape') onFerme()
    }
    window.addEventListener('keydown', onTouche)
    return () => window.removeEventListener('keydown', onTouche)
  }, [ouvert, onFerme])

  return (
    <>
      {ouvert && <div className="sidebar-voile" onClick={onFerme} aria-hidden="true" />}

      <aside
        className={`sidebar${ouvert ? ' sidebar-ouverte' : ''}`}
        aria-label="Navigation principale"
      >
        <div className="sidebar-haut">
          <NavLink to="/" className="sidebar-logo" onClick={onFerme}>
            <img src={logo} alt="KFOKAM 48" height="40" width="160" />
          </NavLink>

          <nav className="sidebar-nav">
            <ul>
              {items.map((item) => (
                <li key={item.to}>
                  <NavLink
                    to={item.to}
                    end={item.fin}
                    className={({ isActive }) => `sidebar-item${isActive ? ' active' : ''}`}
                    onClick={onFerme}
                  >
                    <item.icone />
                    <span>{item.label}</span>
                  </NavLink>
                </li>
              ))}
            </ul>
          </nav>
        </div>

        <div className="sidebar-bas">
          <span className="sidebar-utilisateur">Invité</span>
          <button type="button" className="bouton secondaire bouton-deconnexion" disabled>
            Déconnexion
          </button>
        </div>
      </aside>
    </>
  )
}

export default Sidebar
