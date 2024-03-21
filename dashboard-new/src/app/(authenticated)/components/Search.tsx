'use client'
import { SEARCH_DEFAULT_LIMIT, SEARCH_ENTITIES, SEARCH_LIMITS, SearchType } from '@/app/constants'
import { useWhoAmI } from '@/contexts/WhoAmIContext'
import { ArrowPathIcon } from '@heroicons/react/16/solid'
import Link from 'next/link'
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
  const bookmark = searchParams.get('bookmark') || undefined
  const [bookmarks, setBookmarks] = useState<string[]>([])
  const [limit, setLimit] = useState<number>(SEARCH_DEFAULT_LIMIT)
  // We use the tenantId from context to trigger render on change
  const { tenantId } = useWhoAmI()

  useEffect(() => {
    setLoading(true)
    search({ type, prefix, bookmark, limit, tenantId })
      .then(data => {
        setResponse(data)
      })
      .finally(() => setLoading(false))
  }, [tenantId, type, prefix, bookmark, limit])

  useEffect(() => {
    if (bookmark) {
      setBookmarks([...bookmarks, bookmark])
    }
  }, [bookmark, bookmarks])

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
          <Link
            className="rounded bg-blue-500 px-4 py-2 text-white"
            href={`/?type=${type}&bookmark=${response.bookmark}`}
          >
            Load More
          </Link>
        )}
      </div>
    </div>
  )
}

const getType = (type: any | null): SearchType => {
  if (!type) return 'WfSpec'

  return SEARCH_ENTITIES.includes(type) ? type : 'WfSpec'
}
