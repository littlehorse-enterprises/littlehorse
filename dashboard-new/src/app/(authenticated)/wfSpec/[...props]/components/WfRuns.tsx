'use client'
import { SearchFooter } from '@/app/(authenticated)/components/SearchFooter'
import { SEARCH_DEFAULT_LIMIT } from '@/app/constants'
import { useWhoAmI } from '@/contexts/WhoAmIContext'
import { ArrowPathIcon } from '@heroicons/react/16/solid'
import { useInfiniteQuery } from '@tanstack/react-query'
import { LHStatus, lHStatusFromJSON } from 'littlehorse-client/dist/proto/common_enums'
import { WfRunIdList } from 'littlehorse-client/dist/proto/service'
import { WfSpec } from 'littlehorse-client/dist/proto/wf_spec'
import Link from 'next/link'
import { useSearchParams } from 'next/navigation'
import { FC, useState } from 'react'
import { WfRunSearchProps, searchWfRun } from '../actions/searchWfRun'
import { WfRunsHeader } from './WfRunsHeader'

type Props = Pick<WfSpec, 'id'>
export const WfRuns: FC<Props> = ({ id }) => {
  const searchParams = useSearchParams()
  const status = getStatus(searchParams.get('status')) || LHStatus.RUNNING
  const [limit, setLimit] = useState<number>(SEARCH_DEFAULT_LIMIT)
  const [bookmark, setBookmark] = useState<string>()
  const [searchProps, setSearchProps] = useState<WfRunSearchProps>({
    wfSpecName: id!.name,
    wfSpecMajorVersion: id!.majorVersion,
    wfSpecRevision: id!.revision,
    variableFilters: [],
  })
  const { tenantId } = useWhoAmI()

  const { isPending, data, hasNextPage, fetchNextPage } = useInfiniteQuery({
    queryKey: ['wfRun', status, tenantId, limit],
    initialPageParam: undefined,
    getNextPageParam: (lastPage: WfRunIdList) => lastPage.bookmark?.toString('base64'),
    queryFn: async ({ pageParam }) => {
      return await searchWfRun({
        ...searchProps,
        limit,
        status,
        tenantId,
        bookmark: pageParam ? Buffer.from(pageParam, 'base64') : undefined,
      })
    },
  })

  return (
    <div className="mb-4 flex flex-col">
      <WfRunsHeader currentStatus={status} />
      {isPending ? (
        <div className="flex min-h-[360px] items-center justify-center text-center">
          <ArrowPathIcon className="h-8 w-8 animate-spin fill-blue-500 stroke-none" />
        </div>
      ) : (
        <div className="flex min-h-[360px] flex-col gap-4">
          {data?.pages.map(page => (
            <>
              {page.results.map(({ id }) => (
                <div key={id}>
                  <Link className="py-2 text-blue-500 hover:underline" href={`/wfRun/${id}`}>
                    {id}
                  </Link>
                </div>
              ))}
            </>
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
