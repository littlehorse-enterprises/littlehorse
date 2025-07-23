'use client'
import { SEARCH_DEFAULT_LIMIT, SEARCH_ENTITIES, SearchType } from '@/app/constants'
import { RefreshCwIcon } from 'lucide-react'
import { useParams, useSearchParams } from 'next/navigation'
import { FC, useState } from 'react'
import useSWRInfinite from 'swr/infinite'
import { SearchFooter } from './SearchFooter'
import { SearchHeader } from './SearchHeader'
import { SearchResponse, search } from './searchAction'
import { ExternalEventDefTable, TaskDefTable, UserTaskDefTable, WfSpecTable, WorkflowEventDefTable } from './tables'

export const Search: FC = () => {
  const [prefix, setPrefix] = useState<string | undefined>()
  const searchParams = useSearchParams()
  const type = getType(searchParams.get('type'))
  const [limit, setLimit] = useState<number>(SEARCH_DEFAULT_LIMIT)
  const tenantId = useParams().tenantId as string

  const getKey = (_pageIndex: number, previousPageData: SearchResponse | null) => {
    if (previousPageData && !previousPageData.bookmark) return null // reached the end
    return ['search', type, tenantId, limit, prefix, previousPageData?.bookmark]
  }

  const { data, error, size, setSize } = useSWRInfinite<SearchResponse>(getKey, async key => {
    const [, type, tenantId, limit, prefix, bookmark] = key
    return search({ type, limit, prefix, bookmark, tenantId })
  })

  const isPending = !data && !error
  const hasNextPage = !!(data && data[data.length - 1]?.bookmark)

  return (
    <div className="flex flex-col">
      <SearchHeader currentType={type} setPrefix={setPrefix} />
      {isPending ? (
        <div className="flex min-h-[360px] items-center justify-center text-center">
          <RefreshCwIcon className="h-8 w-8 animate-spin text-blue-500" />
        </div>
      ) : (
        <div>
          {type === 'WfSpec' && <WfSpecTable pages={data} />}
          {type === 'TaskDef' && <TaskDefTable pages={data} />}
          {type === 'UserTaskDef' && <UserTaskDefTable pages={data} />}
          {type === 'ExternalEventDef' && <ExternalEventDefTable pages={data} />}
          {type === 'WorkflowEventDef' && <WorkflowEventDefTable pages={data} />}
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

const getType = (type: any | null): SearchType => {
  if (!type) return 'WfSpec'

  return SEARCH_ENTITIES.includes(type) ? type : 'WfSpec'
}
