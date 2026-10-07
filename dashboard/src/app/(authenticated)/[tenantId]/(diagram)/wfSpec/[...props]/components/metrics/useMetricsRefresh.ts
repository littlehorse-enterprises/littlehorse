import { useCallback, useState } from 'react'
import { REFRESH_SPIN_MS } from './metricsConstants'

export const useMetricsRefresh = (revalidate: () => Promise<unknown>) => {
  const [isManualRefreshing, setIsManualRefreshing] = useState(false)
  const [redrawKey, setRedrawKey] = useState(0)

  const refresh = useCallback(async () => {
    setIsManualRefreshing(true)
    try {
      await new Promise(resolve => setTimeout(resolve, REFRESH_SPIN_MS))
      await revalidate()
      setRedrawKey(key => key + 1)
    } finally {
      setIsManualRefreshing(false)
    }
  }, [revalidate])

  return { isManualRefreshing, redrawKey, refresh }
}
