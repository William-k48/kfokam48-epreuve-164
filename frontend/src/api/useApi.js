import { useEffect, useState } from 'react'

export function useApi(fn, deps = []) {
  const [data, setData] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    let cancelled = false

    const charger = async () => {
      setLoading(true)
      setError(null)

      try {
        const resultat = await fn()
        if (!cancelled) setData(resultat)
      } catch (e) {
        if (cancelled) return
        setData(null)
        setError(e)
      } finally {
        if (!cancelled) setLoading(false)
      }
    }

    charger()

    // Une réponse arrivée après le démontage (ou après un changement de deps)
    // ne doit plus provoquer de mise à jour d'état sur un composant mort.
    return () => {
      cancelled = true
    }
    // La liste de dépendances est fournie par l'appelant (signature imposée du hook) :
    // l'analyse statique d'oxlint ne peut donc pas la vérifier.
    // oxlint-disable-next-line react-hooks/exhaustive-deps
  }, deps)

  return { data, loading, error }
}
