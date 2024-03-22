'use client'
import { SEARCH_DEFAULT_LIMIT, SEARCH_ENTITIES, SEARCH_LIMITS, SearchType } from '@/app/constants'
import { useWhoAmI } from '@/contexts/WhoAmIContext'
import { ArrowPathIcon } from '@heroicons/react/16/solid'
import { useSearchParams } from 'next/navigation'
import { FC, useEffect, useState } from 'react'
import { SearchHeader } from './SearchHeader'
import { SearchResponse, search } from './searchAction'
import { ExternalEventDefTable, TaskDefTable, UserTaskDefTable, WfSpecTable } from './tables'

export const Search: FC<{}> = () => {
  const [loading, setLoading] = useState<boolean>(true)
  const [response, setResponse] = useState<SearchResponse>()
  const [prefix, setPrefix] = useState<string | undefined>()
  const searchParams = useSearchParams()
  const type = getType(searchParams.get('type'))
  const [bookmark, setBookmark] = useState<string>()
  const [limit, setLimit] = useState<number>(SEARCH_DEFAULT_LIMIT)
  // We use the tenantId from context to trigger render on change
  const { tenantId } = useWhoAmI()

  // TODO: Add proper error handling
  useEffect(() => {
    setLoading(true)
    search({ type, limit, prefix, tenantId })
      .then(data => {
        setResponse(data)
      })
      .finally(() => setLoading(false))
    return () => {
      setBookmark(undefined)
    }
  }, [type, prefix, limit, tenantId])

  useEffect(() => {
    if (bookmark) {
      search({ type, limit, prefix, bookmark, tenantId }).then(data => {
        setResponse({
          ...data,
          results: [...response?.results, ...data.results] as any, // it's impossible know at build time which type would contain response
        })
      })
    }
    // response is not really a dependency of this hook
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [bookmark])

  return (
    <div className="flex flex-col">
      <SearchHeader currentType={type} setPrefix={setPrefix} />
      {loading ? (
        <div className="flex min-h-[360px] items-center justify-center text-center">
          <ArrowPathIcon className="h-8 w-8 animate-spin fill-blue-500 stroke-none" />
        </div>
      ) : (
        <div className="min-h-[360px]">
          {response?.type === 'WfSpec' && <WfSpecTable items={response.results} />}
          {response?.type === 'TaskDef' && <TaskDefTable items={response.results} />}
          {response?.type === 'UserTaskDef' && <UserTaskDefTable items={response.results} />}
          {response?.type === 'ExternalEventDef' && <ExternalEventDefTable items={response.results} />}
        </div>
      )}
      <div className="flex justify-between">
        <div className="flex items-center gap-2 text-gray-400">
          Items per load:
          <select
            value={limit}
            onChange={e => {
              setLimit(parseInt(e.target.value))
            }}
            className="rounded bg-blue-500 px-2 text-white"
          >
            {SEARCH_LIMITS.map(searchLimit => (
              <option key={searchLimit} value={searchLimit}>
                {searchLimit}
              </option>
            ))}
          </select>
        </div>
        {response?.bookmark && (
          <button className="rounded bg-blue-500 px-4 py-2 text-white" onClick={() => setBookmark(response.bookmark)}>
            Load More
          </button>
        )}
      </div>
    </div>
  )
}

const getType = (type: any | null): SearchType => {
  if (!type) return 'WfSpec'

  return SEARCH_ENTITIES.includes(type) ? type : 'WfSpec'
}
