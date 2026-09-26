import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import {
  getEtudiants,
  getExercicesEtudiant,
  getPromotions,
  marquerPresence,
} from '../api/client.js'
import EtudiantPage from './EtudiantPage.jsx'

vi.mock('../api/client.js', () => ({
  getPromotions: vi.fn(),
  getEtudiants: vi.fn(),
  getExercicesEtudiant: vi.fn(),
  marquerPresence: vi.fn(),
  deposerExercice: vi.fn(),
}))

function renderPage() {
  return render(
    <MemoryRouter>
      <EtudiantPage />
    </MemoryRouter>,
  )
}

async function identifier(user) {
  await screen.findByRole('option', { name: 'L3 GL' })
  await user.selectOptions(screen.getByLabelText('Promotion'), '1')
  await screen.findByRole('option', { name: 'Alice' })
  await user.selectOptions(screen.getByLabelText('Votre nom'), '1')
}

describe('EtudiantPage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    getPromotions.mockResolvedValue([{ id: 1, nom: 'L3 GL' }])
    getEtudiants.mockResolvedValue([{ id: 1, nom: 'Alice' }])
    getExercicesEtudiant.mockResolvedValue([])
  })

  it('affiche une confirmation quand la présence est marquée', async () => {
    const user = userEvent.setup()
    marquerPresence.mockResolvedValue({
      id: 1,
      sessionId: 1,
      etudiantId: 1,
      source: 'ETUDIANT',
    })

    renderPage()
    await identifier(user)

    await user.type(screen.getByLabelText('Code de la session'), 'ABC123')
    await user.click(screen.getByRole('button', { name: 'Marquer ma présence' }))

    expect(await screen.findByText('Présence marquée, merci !')).toBeInTheDocument()
    expect(marquerPresence).toHaveBeenCalledWith('ABC123', 1)
  })

  it("affiche « Le code a expiré. » sur une erreur 410 CODE_EXPIRE", async () => {
    const user = userEvent.setup()
    marquerPresence.mockRejectedValue({
      code: 'CODE_EXPIRE',
      message: 'Le code de session a expiré.',
    })

    renderPage()
    await identifier(user)

    await user.type(screen.getByLabelText('Code de la session'), 'ABC123')
    await user.click(screen.getByRole('button', { name: 'Marquer ma présence' }))

    expect(await screen.findByText('Le code a expiré.')).toBeInTheDocument()
  })

  it('affiche les exercices et notes sans jamais mentionner le relecteur (RG7)', async () => {
    const user = userEvent.setup()
    getExercicesEtudiant.mockResolvedValue([
      {
        id: 5,
        sessionId: 1,
        lien: 'https://exercice.example/5',
        statut: 'RELUE',
        note: 15,
        noteProvisoire: false,
        commentaire: 'Bon travail.',
      },
    ])

    renderPage()
    await identifier(user)

    expect(await screen.findByText('Bon travail.')).toBeInTheDocument()
    expect(screen.getByText('RELUE')).toBeInTheDocument()
    expect(screen.queryByText(/relecteur/i)).not.toBeInTheDocument()
  })

  it('affiche la mention (provisoire) quand un seul des deux relecteurs a rendu (RG5 v2)', async () => {
    const user = userEvent.setup()
    getExercicesEtudiant.mockResolvedValue([
      {
        id: 6,
        sessionId: 1,
        lien: 'https://exercice.example/6',
        statut: 'EN_ATTENTE',
        note: 12,
        noteProvisoire: true,
        commentaire: 'Correct mais rapidement traite.',
      },
      {
        id: 7,
        sessionId: 1,
        lien: 'https://exercice.example/7',
        statut: 'EN_ATTENTE',
        note: null,
        noteProvisoire: null,
        commentaire: null,
      },
    ])

    renderPage()
    await identifier(user)

    expect(await screen.findByText('12')).toBeInTheDocument()
    expect(screen.getByText(/provisoire/i)).toBeInTheDocument()
    // Aucune note affichée quand aucune relecture n'est rendue (noteProvisoire null) :
    // la ligne du second exercice ne porte aucune mention (provisoire)
    const celluleNoteExercice7 = screen.getByText('Correct mais rapidement traite.').closest('tr')
      ?.nextElementSibling
    expect(celluleNoteExercice7).not.toBeNull()
    expect(celluleNoteExercice7.textContent).not.toMatch(/provisoire/i)
  })
})
