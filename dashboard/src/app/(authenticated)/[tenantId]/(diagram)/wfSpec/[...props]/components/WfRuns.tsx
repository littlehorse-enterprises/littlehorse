'use client'
import { SearchFooter } from '@/app/(authenticated)/[tenantId]/components/SearchFooter'
import { SelectionLink } from '@/app/(authenticated)/[tenantId]/components/SelectionLink'
import { SEARCH_DEFAULT_LIMIT, TIME_RANGES, TimeRange } from '@/app/constants'
import { getStatus, wfRunIdToPath } from '@/app/utils'
import { computeStartTimeWindow, StartTimeWindow } from '@/app/utils/dateTime'
import { useWhoAmI } from '@/contexts/WhoAmIContext'
import { cn } from '@/lib/utils'
import { LHStatus, WfSpec } from 'littlehorse-client/proto'
import { RefreshCwIcon } from 'lucide-react'
import { useSearchParams } from 'next/navigation'
import { FC, Fragment, useMemo, useState } from 'react'
import useSWRInfinite, { SWRInfiniteKeyLoader } from 'swr/infinite'
import { PaginatedWfRunResponseList, searchWfRun } from '../actions/searchWfRun'
import { WfRunsHeader } from './WfRunsHeader'
import { WF_RUN_STATUS } from '../../../components/Sidebar/Components/StatusColor'

type WfRunsKey = [
  'wfRun',
  LHStatus | 'ALL',
  string,
  number,
  StartTimeWindow,
  string | undefined,
  string,
  number,
  number,
]
export const WfRuns: FC<WfSpec> = spec => {
  const searchParams = useSearchParams()
  const status = (searchParams.get('status') ? getStatus(searchParams.get('status')) || 'ALL' : 'ALL') as
    | LHStatus
    | 'ALL'
  const [limit, setLimit] = useState<number>(SEARCH_DEFAULT_LIMIT)
  const [window, setWindow] = useState<TimeRange>(TIME_RANGES[0])
  const { tenantId } = useWhoAmI()

  const startTime = useMemo(() => computeStartTimeWindow(window), [window])

  const getKey: SWRInfiniteKeyLoader<PaginatedWfRunResponseList, WfRunsKey | null> = (_pageIndex, previousPageData) => {
    if (previousPageData && !previousPageData.bookmarkAsString) return null // reached the end
    return [
      'wfRun',
      status,
      tenantId,
      limit,
      startTime,
      previousPageData?.bookmarkAsString,
      spec.id!.name,
      spec.id!.majorVersion,
      spec.id!.revision,
    ] as WfRunsKey
  }

  const { data, error, size, setSize } = useSWRInfinite<PaginatedWfRunResponseList>(getKey, async (key: WfRunsKey) => {
    const [, status, tenantId, limit, startTime, bookmarkAsString, wfSpecName, wfSpecMajorVersion, wfSpecRevision] = key
    return await searchWfRun({
      wfSpecName,
      wfSpecMajorVersion,
      wfSpecRevision,
      variableFilters: [],
      limit,
      status: status === 'ALL' ? undefined : status,
      tenantId,
      bookmarkAsString,
      ...startTime,
    })
  })

  const isPending = !data && !error
  const hasNextPage = !!(data && data[data.length - 1]?.bookmarkAsString)

  return (
    <div className="mb-4 flex flex-col">
      <WfRunsHeader currentStatus={status} currentWindow={window} setWindow={setWindow} spec={spec} />
      {isPending ? (
        <div className="flex min-h-[360px] items-center justify-center text-center">
          <RefreshCwIcon className="h-8 w-8 animate-spin text-blue-500" />
        </div>
      ) : (
        <div className="flex min-h-[360px] flex-col">
          {data?.map((page, i) => (
            <Fragment key={i}>
              {page.results.map(wfRun => {
                if (!wfRun.wfRun.id) return null
                return (
                  <SelectionLink key={wfRun.wfRun.id.id} href={`/wfRun/${wfRunIdToPath(wfRun.wfRun.id)}`}>
                    <p>{wfRun.wfRun.id.id}</p>
                    <span className={cn('ml-2 rounded px-2', WF_RUN_STATUS[wfRun.wfRun.status].color)}>
                      {`${wfRun.wfRun.status ?? ''}`}
                    </span>
                    <span className="ml-2 rounded bg-gray-200 px-2">
                      Started:
                      {(() => {
                        const startTime = wfRun.wfRun.startTime
                        return startTime ? new Date(startTime).toLocaleString() : ''
                      })()}
                    </span>
                  </SelectionLink>
                )
              })}
            </Fragment>
          ))}
        </div>
      )}
      <SearchFooter
        currentLimit={limit}
        setLimit={setLimit}
        hasNextPage={hasNextPage}
        fetchNextPage={() => setSize(size + 1)}
      />
    </div>
  )
}
