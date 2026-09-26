import { afterEach, describe, expect, it, vi } from 'vitest'
import {
  getTableau,
  marquerPresence,
  ouvrirSession,
  rendreRelecture,
} from './client.js'

function reponseJson(ok, status, body) {
  return {
    ok,
    status,
    text: () => Promise.resolve(body === undefined ? '' : JSON.stringify(body)),
  }
}

describe('couche API client', () => {
  afterEach(() => {
    vi.unstubAllGlobals()
  })

  it('ouvrirSession appelle POST /api/sessions avec le bon body', async () => {
    const fetchSimule = vi
      .fn()
      .mockResolvedValue(reponseJson(true, 201, { id: 1, code: 'ABC123' }))
    vi.stubGlobal('fetch', fetchSimule)

    await ouvrirSession('Cours', 1)

    expect(fetchSimule).toHaveBeenCalledTimes(1)
    const [url, options] = fetchSimule.mock.calls[0]
    expect(url).toBe('http://localhost:8085/api/sessions')
    expect(options.method).toBe('POST')
    expect(options.headers).toEqual({ 'Content-Type': 'application/json' })
    expect(options.body).toBe(JSON.stringify({ titre: 'Cours', promotionId: 1 }))
  })

  it('marquerPresence appelle POST /api/presences', async () => {
    const fetchSimule = vi
      .fn()
      .mockResolvedValue(reponseJson(true, 201, { id: 1, source: 'ETUDIANT' }))
    vi.stubGlobal('fetch', fetchSimule)

    await marquerPresence('ABC123', 1)

    const [url, options] = fetchSimule.mock.calls[0]
    expect(url).toBe('http://localhost:8085/api/presences')
    expect(options.method).toBe('POST')
    expect(options.body).toBe(JSON.stringify({ code: 'ABC123', etudiantId: 1 }))
  })

  it('getTableau appelle GET /api/tableau?promotionId=', async () => {
    const fetchSimule = vi.fn().mockResolvedValue(reponseJson(true, 200, []))
    vi.stubGlobal('fetch', fetchSimule)

    await getTableau(1)

    const [url, options] = fetchSimule.mock.calls[0]
    expect(url).toBe('http://localhost:8085/api/tableau?promotionId=1')
    expect(options.method).toBe('GET')
    expect(options.body).toBeUndefined()
  })

  it('rendreRelecture appelle POST /api/relectures/{id}', async () => {
    const fetchSimule = vi
      .fn()
      .mockResolvedValue(reponseJson(true, 200, { exerciceId: 1, statut: 'RELUE' }))
    vi.stubGlobal('fetch', fetchSimule)

    await rendreRelecture(1, 15, 'Bien')

    const [url, options] = fetchSimule.mock.calls[0]
    expect(url).toBe('http://localhost:8085/api/relectures/1')
    expect(options.method).toBe('POST')
    expect(options.body).toBe(JSON.stringify({ note: 15, commentaire: 'Bien' }))
  })

  it("propage le format d'erreur { code, message, status } sur une réponse 400", async () => {
    vi.stubGlobal(
      'fetch',
      vi
        .fn()
        .mockResolvedValue(
          reponseJson(false, 400, { code: 'CODE_INCONNU', message: 'Code inconnu.' }),
        ),
    )

    await expect(marquerPresence('ZZZZZZ', 1)).rejects.toEqual({
      code: 'CODE_INCONNU',
      message: 'Code inconnu.',
      status: 400,
    })
  })

  it('lève ERREUR_RESEAU quand le serveur est injoignable', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn().mockRejectedValue(new TypeError('Failed to fetch')),
    )

    await expect(getTableau(1)).rejects.toEqual({
      code: 'ERREUR_RESEAU',
      message: 'Impossible de contacter le serveur.',
    })
  })
})
