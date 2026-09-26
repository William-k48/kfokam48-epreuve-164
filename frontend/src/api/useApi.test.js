import { renderHook, waitFor } from '@testing-library/react'
import { describe, expect, it, vi } from 'vitest'
import { useApi } from './useApi.js'

describe('hook useApi', () => {
  it('passe de loading true à loading false après le succès', async () => {
    const { result } = renderHook(() => useApi(() => Promise.resolve('ok')))

    expect(result.current.loading).toBe(true)

    await waitFor(() => expect(result.current.loading).toBe(false))

    expect(result.current.data).toBe('ok')
    expect(result.current.error).toBeNull()
  })

  it('remplit data avec le résultat de la fonction', async () => {
    const promotions = [
      { id: 1, nom: 'L3 GL' },
      { id: 2, nom: 'M1 SI' },
    ]
    const { result } = renderHook(() => useApi(() => Promise.resolve(promotions)))

    await waitFor(() => expect(result.current.loading).toBe(false))

    expect(result.current.data).toEqual(promotions)
  })

  it('remplit error quand la fonction échoue', async () => {
    const erreur = { code: 'PROMOTION_INCONNUE', message: 'Promotion introuvable.' }
    const { result } = renderHook(() =>
      useApi(() => Promise.reject(erreur), []),
    )

    await waitFor(() => expect(result.current.loading).toBe(false))

    expect(result.current.error).toEqual(erreur)
    expect(result.current.data).toBeNull()
  })

  it('relance le chargement quand les dépendances changent', async () => {
    const fn = vi.fn((id) => Promise.resolve(`resultat-${id}`))
    const { result, rerender } = renderHook(({ id }) => useApi(() => fn(id), [id]), {
      initialProps: { id: 1 },
    })

    await waitFor(() => expect(result.current.data).toBe('resultat-1'))

    rerender({ id: 2 })

    await waitFor(() => expect(result.current.data).toBe('resultat-2'))
    expect(fn).toHaveBeenCalledTimes(2)
  })

  it('ne met pas à jour les états après le démontage', async () => {
    let resoudre
    const { unmount } = renderHook(
      () => useApi(() => new Promise((resolve) => (resoudre = resolve))),
    )

    unmount()
    resoudre('trop tard')

    await new Promise((resolve) => setTimeout(resolve, 0))
  })
})
