import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import {
  getEtudiants,
  getPromotions,
  getRelecturesEtudiant,
  rendreRelecture,
} from '../api/client.js'
import RelecteurPage from './RelecteurPage.jsx'

vi.mock('../api/client.js', () => ({
  getPromotions: vi.fn(),
  getEtudiants: vi.fn(),
  getRelecturesEtudiant: vi.fn(),
  rendreRelecture: vi.fn(),
}))

const RELECTURE_EN_ATTENTE = {
  exerciceId: 7,
  lien: 'https://exercice.example/7',
  statut: 'EN_ATTENTE',
}

function renderPage() {
  return render(
    <MemoryRouter>
      <RelecteurPage />
    </MemoryRouter>,
  )
}

async function identifier(user) {
  await screen.findByRole('option', { name: 'L3 GL' })
  await user.selectOptions(screen.getByLabelText('Promotion'), '1')
  await screen.findByRole('option', { name: 'Alice' })
  await user.selectOptions(screen.getByLabelText('Votre nom'), '1')
}

describe('RelecteurPage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    getPromotions.mockResolvedValue([{ id: 1, nom: 'L3 GL' }])
    getEtudiants.mockResolvedValue([{ id: 1, nom: 'Alice' }])
    getRelecturesEtudiant.mockResolvedValue([RELECTURE_EN_ATTENTE])
  })

  it('affiche la liste des relectures en attente', async () => {
    const user = userEvent.setup()

    renderPage()
    await identifier(user)

    expect(await screen.findByText('#7')).toBeInTheDocument()
    expect(
      screen.getByRole('link', { name: 'ouvrir le lien' }),
    ).toHaveAttribute('href', 'https://exercice.example/7')
    expect(getRelecturesEtudiant).toHaveBeenCalledWith(1)
  })

  it('retire l’exercice de la liste après avoir rendu la relecture', async () => {
    const user = userEvent.setup()
    getRelecturesEtudiant
      .mockResolvedValueOnce([RELECTURE_EN_ATTENTE])
      .mockResolvedValue([])
    rendreRelecture.mockResolvedValue({ exerciceId: 7, statut: 'RELUE' })

    renderPage()
    await identifier(user)
    await screen.findByText('#7')

    await user.type(screen.getByLabelText('Note (0 à 20)'), '15')
    await user.type(screen.getByLabelText('Commentaire'), 'Bien.')
    await user.click(
      screen.getByRole('button', { name: 'Rendre la relecture' }),
    )

    expect(rendreRelecture).toHaveBeenCalledWith(7, 15, 'Bien.')
    expect(
      await screen.findByText("Relecture rendue pour l'exercice #7."),
    ).toBeInTheDocument()
    // Nouveau message de l'état vide (mission #48) : bienveillant, non condescendant
    expect(await screen.findByText('Tout est à jour. Reposez-vous !')).toBeInTheDocument()
    expect(screen.queryByText('#7')).not.toBeInTheDocument()
  })

  it('affiche le message lisible sur une erreur 403 AUTO_RELECTURE', async () => {
    const user = userEvent.setup()
    rendreRelecture.mockRejectedValue({
      code: 'AUTO_RELECTURE',
      message: "L'étudiant est l'auteur de l'exercice.",
    })

    renderPage()
    await identifier(user)
    await screen.findByText('#7')

    await user.type(screen.getByLabelText('Note (0 à 20)'), '12')
    await user.click(screen.getByRole('button', { name: 'Rendre la relecture' }))

    expect(
      await screen.findByText('Vous ne pouvez pas relire votre propre exercice.'),
    ).toBeInTheDocument()
    expect(screen.queryByText(/auteur de l'exercice/i)).not.toBeInTheDocument()
  })
})
