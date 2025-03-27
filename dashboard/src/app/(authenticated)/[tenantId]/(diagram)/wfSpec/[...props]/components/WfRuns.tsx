'use client'
import { SearchFooter } from '@/app/(authenticated)/[tenantId]/components/SearchFooter'
import { SelectionLink } from '@/app/(authenticated)/[tenantId]/components/SelectionLink'
import { getWfRun, WfRunResponse } from '@/app/actions/getWfRun'
import { SEARCH_DEFAULT_LIMIT, TIME_RANGES, TimeRange } from '@/app/constants'
import { concatWfRunIds } from '@/app/utils'
import { cn } from '@/lib/utils'
import { lHStatusFromJSON, WfSpec } from 'littlehorse-client/proto'
import { RefreshCwIcon } from 'lucide-react'
import { useParams, useSearchParams } from 'next/navigation'
import { FC, Fragment, useEffect, useMemo, useState } from 'react'
import useSWRInfinite from 'swr/infinite'
import { statusColors } from '../../../wfRun/[...ids]/components/Details'
import { PaginatedWfRunIdList, searchWfRun } from '../actions/searchWfRun'
import { WfRunsHeader } from './WfRunsHeader'

export const WfRuns: FC<WfSpec> = spec => {
  const searchParams = useSearchParams()
  const status = searchParams.get('status') ? getStatus(searchParams.get('status')) || 'ALL' : 'ALL'
  const [limit, setLimit] = useState<number>(SEARCH_DEFAULT_LIMIT)
  const [window, setWindow] = useState<TimeRange>(TIME_RANGES[0])
  const tenantId = useParams().tenantId as string
  const [resolvedWfRuns, setResolvedWfRuns] = useState<Record<string, WfRunResponse>>({})

  const startTime = useMemo(() => {
    if (window === -1) return undefined
    const now = new Date()
    const latestStart = now.toISOString()
    const earliestStart = new Date(now.getTime() - window * 6e4).toISOString()

    return {
      latestStart,
      earliestStart,
    }
  }, [window])

  const getKey = (pageIndex: number, previousPageData: PaginatedWfRunIdList | null) => {
    if (previousPageData && !previousPageData.bookmarkAsString) return null // reached the end
    return ['wfRun', status, tenantId, limit, startTime, previousPageData?.bookmarkAsString]
  }

  const { data, error, size, setSize } = useSWRInfinite<PaginatedWfRunIdList>(
    getKey,
    async (key) => {
      const [, status, tenantId, limit, startTime, bookmarkAsString] = key
      return await searchWfRun({
        wfSpecName: spec.id!.name,
        wfSpecMajorVersion: spec.id!.majorVersion,
        wfSpecRevision: spec.id!.revision,
        variableFilters: [],
        limit,
        status: status === 'ALL' ? undefined : status,
        tenantId,
        bookmarkAsString,
        ...startTime,
      })
    }
  )

  const wfRunPromises = useMemo(() => {
    return data?.flatMap(page =>
      page.results.map(wfRunId => ({
        wfRunId: wfRunId.id,
        promise: getWfRun({ wfRunId, tenantId })
      }))
    ) ?? []
  }, [data, tenantId])

  useEffect(() => {
    wfRunPromises.forEach(async ({ wfRunId, promise }) => {
      const data = await promise
      setResolvedWfRuns(prev => ({
        ...prev,
        [wfRunId]: data
      }));
    })
  }, [wfRunPromises])


  const isPending = !data && !error
  const hasNextPage = !!(data && data[data.length - 1]?.bookmarkAsString)
  console.log(resolvedWfRuns)

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
              {page.results.map(wfRunId => (
                <SelectionLink key={wfRunId.id} href={`/wfRun/${concatWfRunIds(wfRunId)}`}>
                  <p>{wfRunId.id}</p>
                  <span className={cn('ml-2 rounded px-2', statusColors[resolvedWfRuns[wfRunId.id]?.wfRun.status])}>
                    {`${resolvedWfRuns[wfRunId.id]?.wfRun.status}`}
                  </span>
                  <span className="ml-2 rounded px-2 bg-gray-200">
                    Started: {resolvedWfRuns[wfRunId.id]?.wfRun.startTime ?
                      new Date(resolvedWfRuns[wfRunId.id]?.wfRun.startTime!).toLocaleString()
                      : ''}
                  </span>
                </SelectionLink>
              ))}
            </Fragment>
          ))}
        </div>
      )}
      <SearchFooter currentLimit={limit} setLimit={setLimit} hasNextPage={hasNextPage} fetchNextPage={() => setSize(size + 1)} />
    </div>
  )
}

const getStatus = (status: string | null) => {
  if (!status) return undefined
  return lHStatusFromJSON(status)
}