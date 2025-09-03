'use client'

import { SearchFooter } from '@/app/(authenticated)/[tenantId]/components/SearchFooter'
import { SelectionLink } from '@/app/(authenticated)/[tenantId]/components/SelectionLink'
import { getWfRun, WfRunResponse } from '@/app/actions/getWfRun'
import { SEARCH_DEFAULT_LIMIT, TIME_RANGES, TimeRange } from '@/app/constants'
import { wfRunIdToPath } from '@/app/utils'
import { computeStartTimeWindow } from '@/app/utils/dateTime'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { useWhoAmI } from '@/contexts/WhoAmIContext'
import { cn } from '@/lib/utils'
import { LHStatus, lHStatusFromJSON, WfRunId, WfSpec } from 'littlehorse-client/proto'
import { RefreshCwIcon } from 'lucide-react'
import { useSearchParams } from 'next/navigation'
import { useEffect, useMemo, useState } from 'react'
import useSWRInfinite, { SWRInfiniteKeyLoader } from 'swr/infinite'
import { PaginatedWfRunIdList, searchWfRun } from '../../../wfSpec/[...props]/actions/searchWfRun'
import { WfRunsHeader } from '../../../wfSpec/[...props]/components/WfRunsHeader'
import { statusColors } from './Details'

type StartTimeRange = { latestStart: string; earliestStart: string } | undefined
type ChildWfRunsKey = ['childWfRuns', LHStatus | 'ALL', string, number, StartTimeRange, string | undefined, WfRunId]

export default function ChildWorkflows({ parentWfRunId, spec }: { parentWfRunId: WfRunId; spec: WfSpec }) {
  const { tenantId } = useWhoAmI()
  const searchParams = useSearchParams()
  const status = (searchParams.get('status') ? getStatus(searchParams.get('status')) || 'ALL' : 'ALL') as
    | LHStatus
    | 'ALL'
  const [limit, setLimit] = useState<number>(SEARCH_DEFAULT_LIMIT)
  const [resolvedWfRuns, setResolvedWfRuns] = useState<Record<string, WfRunResponse>>({})
  const [window, setWindow] = useState<TimeRange>(TIME_RANGES[0])

  const startTime = useMemo(() => computeStartTimeWindow(window), [window])

  const getKey: SWRInfiniteKeyLoader<PaginatedWfRunIdList, ChildWfRunsKey | null> = (_pageIndex, previousPageData) => {
    if (previousPageData && !previousPageData.bookmarkAsString) return null // reached the end
    return [
      'childWfRuns',
      status,
      tenantId,
      limit,
      startTime,
      previousPageData?.bookmarkAsString,
      parentWfRunId,
    ] as ChildWfRunsKey
  }
  const { data, error, size, setSize } = useSWRInfinite<PaginatedWfRunIdList>(getKey, async (key: ChildWfRunsKey) => {
    const [, status, tenantId, limit, startTime, bookmarkAsString, parentWfRunId] = key
    return await searchWfRun({
      wfSpecName: '',
      status: status === 'ALL' ? undefined : status,
      tenantId,
      parentWfRunId,
      limit,
      bookmarkAsString,
      variableFilters: [],
      ...startTime,
    })
  })

  const wfRunPromises = useMemo(() => {
    return (
      data?.flatMap(page =>
        page.results.map(wfRunId => ({
          wfRunId: wfRunId,
          promise: getWfRun({ wfRunId, tenantId }),
        }))
      ) ?? []
    )
  }, [data, tenantId])

  useEffect(() => {
    wfRunPromises.forEach(async ({ wfRunId, promise }) => {
      const data = await promise
      setResolvedWfRuns(prev => ({
        ...prev,
        [wfRunId.id]: data,
      }))
    })
  }, [wfRunPromises])

  const isPending = !data && !error
  const hasNextPage = !!(data && data[data.length - 1]?.bookmarkAsString)

  return (
    <div>
      <Tabs defaultValue="child-wfruns">
        <TabsList>
          <TabsTrigger value="child-wfruns">Child WfRuns</TabsTrigger>
        </TabsList>
        <TabsContent value="child-wfruns">
          <WfRunsHeader currentStatus={status} currentWindow={window} setWindow={setWindow} spec={spec} />
          <div className="flex min-h-[180px] flex-col">
            {isPending ? (
              <div className="flex min-h-[180px] items-center justify-center text-center">
                <RefreshCwIcon className="h-8 w-8 animate-spin text-blue-500" />
              </div>
            ) : (
              data?.map((page, i) => (
                <div key={i}>
                  {page.results.map(wfRunId => (
                    <SelectionLink key={wfRunId.id} href={`/wfRun/${wfRunIdToPath(wfRunId)}`}>
                      <p>{wfRunId.id}</p>
                      <span className={cn('ml-2 rounded px-2', statusColors[resolvedWfRuns[wfRunId.id]?.wfRun.status])}>
                        {`${resolvedWfRuns[wfRunId.id]?.wfRun.status ?? ''}`}
                      </span>
                      <span className="ml-2 rounded bg-gray-200 px-2">
                        Started:{' '}
                        {(() => {
                          const startTime = resolvedWfRuns[wfRunId.id]?.wfRun.startTime
                          return startTime ? new Date(startTime).toLocaleString() : ''
                        })()}
                      </span>
                    </SelectionLink>
                  ))}
                </div>
              ))
            )}
          </div>
          <SearchFooter
            currentLimit={limit}
            setLimit={setLimit}
            hasNextPage={hasNextPage}
            fetchNextPage={() => setSize(size + 1)}
          />
        </TabsContent>
      </Tabs>
    </div>
  )
}

const getStatus = (status: string | null) => {
  if (!status) return undefined
  return lHStatusFromJSON(status)
}
