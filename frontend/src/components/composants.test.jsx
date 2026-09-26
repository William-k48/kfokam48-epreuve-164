import { render, screen } from '@testing-library/react'
import { afterEach, describe, expect, it, vi } from 'vitest'
import Badge, { varianteNote } from './Badge.jsx'
import Card from './Card.jsx'
import EmptyState from './EmptyState.jsx'
import Toast from './Toast.jsx'

describe('Badge', () => {
  it('rend la variante demandée avec son contenu', () => {
    render(<Badge variante="succes">15,5</Badge>)
    const badge = screen.getByText('15,5')
    expect(badge).toHaveClass('badge', 'succes')
  })

  it('rend la variante neutre par défaut', () => {
    render(<Badge>—</Badge>)
    expect(screen.getByText('—')).toHaveClass('badge', 'neutre')
  })

  it.each([
    [16, 'succes'],
    [10, 'alerte'],
    [9, 'erreur'],
    [20, 'succes'],
    [0, 'erreur'],
  ])('varianteNote(%s) donne %s (>15 vert, 10-15 orange, <10 rouge)', (note, attendu) => {
    expect(varianteNote(note)).toBe(attendu)
  })
})

describe('Card', () => {
  it('affiche son titre lié au contenu (aria-labelledby) et ses enfants', () => {
    render(
      <Card titre="Ma carte" idTitre="titre-carte">
        <p>Contenu</p>
      </Card>,
    )
    expect(screen.getByRole('heading', { name: 'Ma carte' })).toBeInTheDocument()
    expect(screen.getByText('Contenu')).toBeInTheDocument()
    expect(screen.getByText('Contenu').closest('section')).toHaveAttribute(
      'aria-labelledby',
      'titre-carte',
    )
  })

  it('rend les enfants sans titre', () => {
    render(
      <Card>
        <p>Seul</p>
      </Card>,
    )
    expect(screen.getByText('Seul')).toBeInTheDocument()
    expect(screen.queryByRole('heading')).not.toBeInTheDocument()
  })
})

describe('EmptyState', () => {
  it('affiche titre et texte avec role status', () => {
    render(
      <EmptyState
        illustration={<svg data-testid="illu" />}
        titre="Tout est à jour. Reposez-vous !"
        texte="Aucune relecture en attente pour le moment."
      />,
    )
    expect(screen.getByRole('status')).toBeInTheDocument()
    expect(screen.getByText('Tout est à jour. Reposez-vous !')).toBeInTheDocument()
    expect(screen.getByText('Aucune relecture en attente pour le moment.')).toBeInTheDocument()
    expect(screen.getByTestId('illu')).toBeInTheDocument()
  })
})

describe('Toast', () => {
  afterEach(() => {
    vi.useRealTimers()
  })

  it("n'affiche rien sans message", () => {
    render(<Toast message={null} onFerme={() => {}} />)
    expect(screen.queryByRole('status')).not.toBeInTheDocument()
  })

  it('affiche le message avec role status puis disparait après la durée', () => {
    vi.useFakeTimers()
    const onFerme = vi.fn()
    render(<Toast message="✓ Fait" onFerme={onFerme} duree={3000} />)

    expect(screen.getByRole('status')).toHaveTextContent('✓ Fait')

    vi.advanceTimersByTime(2999)
    expect(onFerme).not.toHaveBeenCalled()
    vi.advanceTimersByTime(1)
    expect(onFerme).toHaveBeenCalledTimes(1)
  })
})
