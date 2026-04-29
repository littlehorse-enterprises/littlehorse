import { SEARCH_DEFAULT_LIMIT, SEARCH_LIMITS, SearchLimit } from '@/app/constants'
import { useCallback, useEffect, useState } from 'react'

const STORAGE_PREFIX = 'lh-dashboard-items-per-load:'

const parseStored = (raw: string | null): SearchLimit => {
  if (raw === null) return SEARCH_DEFAULT_LIMIT
  const n = parseInt(raw, 10)
  return (SEARCH_LIMITS as readonly number[]).includes(n) ? (n as SearchLimit) : SEARCH_DEFAULT_LIMIT
}

export const usePersistedSearchLimit = (scope: string): [SearchLimit, (limit: SearchLimit) => void] => {
  const [limit, setLimitState] = useState<SearchLimit>(SEARCH_DEFAULT_LIMIT)

  useEffect(() => {
    try {
      setLimitState(parseStored(localStorage.getItem(STORAGE_PREFIX + scope)))
    } catch {
      setLimitState(SEARCH_DEFAULT_LIMIT)
    }
  }, [scope])

  const setLimit = useCallback(
    (next: SearchLimit) => {
      setLimitState(next)
      try {
        localStorage.setItem(STORAGE_PREFIX + scope, String(next))
      } catch {
        // swallow: storage may be unavailable (quota exceeded, private mode)
      }
    },
    [scope]
  )

  return [limit, setLimit]
}
