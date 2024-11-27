'use client'
import { SEARCH_DEFAULT_LIMIT, SEARCH_ENTITIES, SearchType } from '@/app/constants'
import { useInfiniteQuery } from '@tanstack/react-query'
import { RefreshCwIcon } from 'lucide-react'
import { useParams, useSearchParams } from 'next/navigation'
import { FC, useState } from 'react'
import { SearchFooter } from './SearchFooter'
import { SearchHeader } from './SearchHeader'
import { SearchResponse, search } from './searchAction'
import { ExternalEventDefTable, TaskDefTable, UserTaskDefTable, WfSpecTable, WorkflowEventDefTable } from './tables'

export const Search: FC<{}> = () => {
  const [prefix, setPrefix] = useState<string | undefined>()
  const searchParams = useSearchParams()
  const type = getType(searchParams.get('type'))
  const [limit, setLimit] = useState<number>(SEARCH_DEFAULT_LIMIT)
  const tenantId = useParams().tenantId as string

  const { isPending, data, hasNextPage, fetchNextPage } = useInfiniteQuery({
    queryKey: ['search', type, tenantId, limit, prefix],
    initialPageParam: undefined,
    getNextPageParam: (lastPage: SearchResponse) => lastPage.bookmark,
    queryFn: async ({ pageParam: bookmark }) => {
      return search({ type, limit, prefix, bookmark, tenantId })
    },
  })

  return (
    <div className="flex flex-col">
      <SearchHeader currentType={type} setPrefix={setPrefix} />
      {isPending ? (
        <div className="flex min-h-[360px] items-center justify-center text-center">
          <RefreshCwIcon className="h-8 w-8 animate-spin text-blue-500" />
        </div>
      ) : (
        <div>
          {type === 'WfSpec' && <WfSpecTable pages={data?.pages} />}
          {type === 'TaskDef' && <TaskDefTable pages={data?.pages} />}
          {type === 'UserTaskDef' && <UserTaskDefTable pages={data?.pages} />}
          {type === 'ExternalEventDef' && <ExternalEventDefTable pages={data?.pages} />}
          {type === 'WorkflowEventDef' && <WorkflowEventDefTable pages={data?.pages} />}
        </div>
      )}
      <SearchFooter currentLimit={limit} setLimit={setLimit} hasNextPage={hasNextPage} fetchNextPage={fetchNextPage} />
    </div>
  )
}

const getType = (type: any | null): SearchType => {
  if (!type) return 'WfSpec'

  return SEARCH_ENTITIES.includes(type) ? type : 'WfSpec'
}
