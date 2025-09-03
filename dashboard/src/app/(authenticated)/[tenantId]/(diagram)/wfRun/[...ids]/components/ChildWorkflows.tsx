'use client'

import { SearchFooter } from '@/app/(authenticated)/[tenantId]/components/SearchFooter'
import { SelectionLink } from '@/app/(authenticated)/[tenantId]/components/SelectionLink'
import { SEARCH_DEFAULT_LIMIT, TIME_RANGES, TimeRange } from '@/app/constants'
import { getStatus, wfRunIdToPath } from '@/app/utils'
import { computeStartTimeWindow, StartTimeWindow } from '@/app/utils/dateTime'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { useWhoAmI } from '@/contexts/WhoAmIContext'
import { cn } from '@/lib/utils'
import { LHStatus, WfRunId, WfSpec } from 'littlehorse-client/proto'
import { RefreshCwIcon } from 'lucide-react'
import { useSearchParams } from 'next/navigation'
import { FC, useMemo, useState } from 'react'
import useSWRInfinite, { SWRInfiniteKeyLoader } from 'swr/infinite'
import { PaginatedWfRunResponseList, searchWfRun } from '../../../wfSpec/[...props]/actions/searchWfRun'
import { WfRunsHeader } from '../../../wfSpec/[...props]/components/WfRunsHeader'
import { statusColors } from './Details'

type ChildWfRunsKey = ['childWfRuns', LHStatus | 'ALL', string, number, StartTimeWindow, string | undefined, WfRunId]

export const ChildWorkflows: FC<{ parentWfRunId: WfRunId; spec: WfSpec }> = ({ parentWfRunId, spec }) => {
  const { tenantId } = useWhoAmI()
  const searchParams = useSearchParams()
  const status = (searchParams.get('status') ? getStatus(searchParams.get('status')) || 'ALL' : 'ALL') as
    | LHStatus
    | 'ALL'
  const [limit, setLimit] = useState<number>(SEARCH_DEFAULT_LIMIT)
  const [window, setWindow] = useState<TimeRange>(TIME_RANGES[0])

  const startTime = useMemo(() => computeStartTimeWindow(window), [window])

  const getKey: SWRInfiniteKeyLoader<PaginatedWfRunResponseList, ChildWfRunsKey | null> = (
    _pageIndex,
    previousPageData
  ) => {
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
  const { data, size, setSize, isLoading } = useSWRInfinite<PaginatedWfRunResponseList>(
    getKey,
    async (key: ChildWfRunsKey) => {
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
    }
  )

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
            {isLoading ? (
              <div className="flex min-h-[180px] items-center justify-center text-center">
                <RefreshCwIcon className="h-8 w-8 animate-spin text-blue-500" />
              </div>
            ) : (
              data?.map((page, i) => (
                <div key={i}>
                  {page.results.map(wfRun => {
                    if (!wfRun.wfRun.id) return null
                    return (
                      <SelectionLink key={wfRun.wfRun.id.id} href={`/wfRun/${wfRunIdToPath(wfRun.wfRun.id)}`}>
                        <p>{wfRun.wfRun.id.id}</p>
                        <span className={cn('ml-2 rounded px-2', statusColors[wfRun.wfRun.status])}>
                          {`${wfRun.wfRun.status ?? ''}`}
                        </span>
                        <span className="ml-2 rounded bg-gray-200 px-2">
                          Started:{' '}
                          {(() => {
                            const startTime = wfRun.wfRun.startTime
                            return startTime ? new Date(startTime).toLocaleString() : ''
                          })()}
                        </span>
                      </SelectionLink>
                    )
                  })}
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
