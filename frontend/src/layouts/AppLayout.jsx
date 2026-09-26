import { useState } from 'react'
import { Outlet } from 'react-router-dom'
import Header from '../components/Header.jsx'
import Sidebar from '../components/Sidebar.jsx'

/**
 * Enveloppe visuelle de l'application (issue #48) : sidebar gauche fixe
 * (drawer sur mobile), header supérieur fixe, zone de contenu centrale.
 * Les routes et les pages restent exactement les mêmes (Outlet).
 */
function AppLayout() {
  const [menuOuvert, setMenuOuvert] = useState(false)

  return (
    <div className="layout">
      <Sidebar ouvert={menuOuvert} onFerme={() => setMenuOuvert(false)} />

      <div className="layout-principal">
        <Header onOuvreMenu={() => setMenuOuvert(true)} />
        <main className="contenu">
          <Outlet />
        </main>
      </div>
    </div>
  )
}

export default AppLayout
