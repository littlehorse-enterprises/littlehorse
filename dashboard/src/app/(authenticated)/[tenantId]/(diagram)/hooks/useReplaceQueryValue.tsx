import { usePathname, useSearchParams } from 'next/navigation'
import { useCallback } from 'react'

export const useReplaceQueryValue = () => {
  const pathname = usePathname()
  const searchParams = useSearchParams()

  // Get a new searchParams string by merging the current
  // searchParams with a provided key/value pair
  return useCallback(
    (name: string, value: string) => {
      const params = new URLSearchParams(searchParams.toString())
      params.set(name, value)

      return pathname + '?' + params.toString()
    },
    [pathname, searchParams]
  )
}
