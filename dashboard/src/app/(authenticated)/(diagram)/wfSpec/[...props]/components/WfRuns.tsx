'use client'
import { SearchFooter } from '@/app/(authenticated)/components/SearchFooter'
import { SEARCH_DEFAULT_LIMIT, TIME_RANGES, TimeRange } from '@/app/constants'
import { concatWfRunIds } from '@/app/utils'
import { useWhoAmI } from '@/contexts/WhoAmIContext'
import { useInfiniteQuery } from '@tanstack/react-query'
import { lHStatusFromJSON } from 'littlehorse-client/dist/proto/common_enums'
import { WfSpec } from 'littlehorse-client/dist/proto/wf_spec'
import { RefreshCwIcon } from 'lucide-react'
import Link from 'next/link'
import { useSearchParams } from 'next/navigation'
import { FC, Fragment, useMemo, useState } from 'react'
import { PaginatedWfRunIdList, searchWfRun } from '../actions/searchWfRun'
import { WfRunsHeader } from './WfRunsHeader'

export const WfRuns: FC<WfSpec> = ({ id, ...spec }) => {
  const searchParams = useSearchParams()
  const status = searchParams.get('status') ? getStatus(searchParams.get('status')) || 'ALL' : 'ALL'
  console.log('status:', status)
  const [limit, setLimit] = useState<number>(SEARCH_DEFAULT_LIMIT)
  const [window, setWindow] = useState<TimeRange>(TIME_RANGES[0])
  const { tenantId } = useWhoAmI()

  const startTime = useMemo(() => {
    const now = new Date()
    const latestStart = now.toISOString()
    const earliestStart = new Date(now.getTime() - window * 6e4).toISOString()

    return {
      latestStart,
      earliestStart,
    }
  }, [window])

  const { isPending, data, hasNextPage, fetchNextPage } = useInfiniteQuery({
    queryKey: ['wfRun', status, tenantId, limit, startTime],
    initialPageParam: undefined,
    getNextPageParam: (lastPage: PaginatedWfRunIdList) => lastPage.bookmarkAsString,
    queryFn: async ({ pageParam }) => {
      return await searchWfRun({
        wfSpecName: id!.name,
        wfSpecMajorVersion: id!.majorVersion,
        wfSpecRevision: id!.revision,
        variableFilters: [],
        limit,
        status: status === 'ALL' ? undefined : status,
        tenantId,
        bookmarkAsString: pageParam,
        ...startTime,
      })
    },
  })

  return (
    <div className="mb-4 flex flex-col">
      <WfRunsHeader currentStatus={status} currentWindow={window} setWindow={setWindow} />
      {isPending ? (
        <div className="flex min-h-[360px] items-center justify-center text-center">
          <RefreshCwIcon className="h-8 w-8 animate-spin fill-blue-500 stroke-none" />
        </div>
      ) : (
        <div className="flex min-h-[360px] flex-col gap-4">
          {data?.pages.map((page, i) => (
            <Fragment key={i}>
              {page.results.map(wfRunId => (
                <div key={wfRunId.id}>
                  <Link className="py-2 text-blue-500 hover:underline" href={`/wfRun/${concatWfRunIds(wfRunId)}`}>
                    {wfRunId.id}
                  </Link>
                </div>
              ))}
            </Fragment>
          ))}
        </div>
      )}
      <SearchFooter currentLimit={limit} setLimit={setLimit} hasNextPage={hasNextPage} fetchNextPage={fetchNextPage} />
    </div>
  )
}

const getStatus = (status: string | null) => {
  if (!status) return undefined
  return lHStatusFromJSON(status)
}
