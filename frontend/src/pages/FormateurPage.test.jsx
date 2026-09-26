import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { getPromotions, getTableau, ouvrirSession } from '../api/client.js'
import FormateurPage from './FormateurPage.jsx'

vi.mock('../api/client.js', () => ({
  getPromotions: vi.fn(),
  getTableau: vi.fn(),
  ouvrirSession: vi.fn(),
}))

function renderPage() {
  return render(
    <MemoryRouter>
      <FormateurPage />
    </MemoryRouter>,
  )
}

describe('FormateurPage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    getPromotions.mockResolvedValue([{ id: 1, nom: 'L3 GL' }])
  })

  it('affiche les promotions après chargement', async () => {
    renderPage()

    const selects = await screen.findAllByLabelText('Promotion')
    expect(selects).toHaveLength(2)

    await waitFor(() =>
      expect(screen.getAllByRole('option', { name: 'L3 GL' }).length).toBe(2),
    )
  })

  it("l'ouverture de session affiche le code généré", async () => {
    const user = userEvent.setup()
    ouvrirSession.mockResolvedValue({
      id: 9,
      code: 'ABC123',
      ouvertureAt: '2026-09-26T10:00:00',
      expirationAt: '2026-09-26T10:15:00',
    })

    renderPage()

    await screen.findByLabelText('Titre de la session')
    await user.type(
      screen.getByLabelText('Titre de la session'),
      'TP 4 — Design patterns',
    )
    const selectPromotion = (await screen.findAllByLabelText('Promotion'))[0]
    await user.selectOptions(selectPromotion, '1')
    await user.click(screen.getByRole('button', { name: 'Lancer la session' }))

    expect(await screen.findByText('ABC123')).toBeInTheDocument()
    expect(ouvrirSession).toHaveBeenCalledWith('TP 4 — Design patterns', 1)
  })

  it('affiche le tableau avec la moyenne « — » si null (décision A7)', async () => {
    const user = userEvent.setup()
    getTableau.mockResolvedValue([
      {
        etudiantId: 1,
        nom: 'Alice',
        presences: 2,
        exercicesDeposes: 1,
        moyenne: null,
        relecturesEnAttente: 0,
      },
    ])

    renderPage()

    const selectTableau = (await screen.findAllByLabelText('Promotion'))[1]
    await user.selectOptions(selectTableau, '1')

    expect(await screen.findByText('Alice')).toBeInTheDocument()
    expect(screen.getByText('—')).toBeInTheDocument()
    expect(getTableau).toHaveBeenCalledWith(1)
  })
})
