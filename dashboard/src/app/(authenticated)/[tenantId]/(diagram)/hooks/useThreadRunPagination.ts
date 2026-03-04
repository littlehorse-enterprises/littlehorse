import { getArchivedThreadRun } from '@/app/actions/getArchivedThreadRun'
import { ThreadRun, WfRun } from 'littlehorse-client/proto'
import { useEffect, useMemo, useRef, useState } from 'react'

const PAGE_SIZE = 10

type UseThreadRunPaginationProps = {
  wfRun?: WfRun
  page: number
  tenantId: string
}

export const useThreadRunPagination = ({ wfRun, page, tenantId }: UseThreadRunPaginationProps) => {
  const [archivedThreadRuns, setArchivedThreadRuns] = useState<Map<number, ThreadRun>>(new Map())
  const [isLoading, setIsLoading] = useState(false)
  const archivedRef = useRef(archivedThreadRuns)

  useEffect(() => {
    archivedRef.current = archivedThreadRuns
  }, [archivedThreadRuns])

  let threadRunNumbersNeeded = useMemo(() => {
    if (wfRun?.greatestThreadrunNumber === undefined) return []
    const start = (page - 1) * PAGE_SIZE
    const end = Math.min(start + PAGE_SIZE - 1, wfRun.greatestThreadrunNumber)
    return Array.from({ length: end - start + 1 }, (_, i) => start + i)
  }, [wfRun?.greatestThreadrunNumber, page])

  useEffect(() => {
    if (!wfRun?.id || threadRunNumbersNeeded.length === 0) {
      setArchivedThreadRuns(new Map())
      return
    }

    const presentNumbers = new Set(wfRun.threadRuns.map(tr => tr.number))
    const missingNumbers = threadRunNumbersNeeded.filter(num => !presentNumbers.has(num))
    const numbersToFetch = missingNumbers.filter(num => !archivedRef.current.has(num))

    if (numbersToFetch.length === 0) return

    setIsLoading(true)
    Promise.all(numbersToFetch.map(num => getArchivedThreadRun({ wfRunId: wfRun.id!, threadRunNumber: num, tenantId })))
      .then(results => {
        setArchivedThreadRuns(prev => {
          const updated = new Map(prev)
          results.forEach((archived, i) => {
            if (archived?.threadRun) {
              updated.set(numbersToFetch[i], archived.threadRun)
            }
          })
          return updated
        })
      })
      .finally(() => setIsLoading(false))
  }, [wfRun?.id, wfRun?.threadRuns, threadRunNumbersNeeded.join(','), tenantId])

  const threadRuns = useMemo(() => {
    if (!wfRun) return []
    const presentMap = new Map(wfRun.threadRuns.map(tr => [tr.number, tr]))
    return threadRunNumbersNeeded
      .map(num => presentMap.get(num) ?? archivedThreadRuns.get(num))
      .filter((tr): tr is ThreadRun => !!tr)
      .sort((a, b) => a.number - b.number)
  }, [wfRun, threadRunNumbersNeeded, archivedThreadRuns])

  const totalPages = useMemo(
    () => (wfRun?.greatestThreadrunNumber ? Math.ceil((wfRun.greatestThreadrunNumber + 1) / PAGE_SIZE) : 1),
    [wfRun?.greatestThreadrunNumber]
  )

  return { threadRuns, isLoading, totalPages }
}
