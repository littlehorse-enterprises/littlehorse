'use client'
import { useWhoAmI } from '@/contexts/WhoAmIContext'
import { MagnifyingGlassIcon } from '@heroicons/react/24/outline'
import Link from 'next/link'
import { useSearchParams } from 'next/navigation'
import { FC, useEffect, useState } from 'react'
import { TaskDefTable } from './TaskDefTable'
import { UserTaskDefTable } from './UserTaskDefTable'
import { WfSpecTable } from './WfSpecTable'
import { SearchResponse, search } from './searchAction'

export const SEARCH_ENTITIES = ['wfSpec', 'taskDef', 'userTaskDef'] as const

export type SearchType = (typeof SEARCH_ENTITIES)[number]
export const Search: FC<{}> = () => {
  const [loading, setLoading] = useState<boolean>(false)
  const [response, setResponse] = useState<SearchResponse>()
  const searchParams = useSearchParams()
  const type = getType(searchParams.get('type'))
  const bookmark = searchParams.get('bookmark') || undefined
  const { tenantId } = useWhoAmI()

  useEffect(() => {
    setLoading(true)
    search({ type, bookmark, tenantId })
      .then(data => {
        setResponse(data)
      })
      .finally(() => setLoading(false))
  }, [tenantId, type, bookmark])

  return (
    <div className="flex flex-col">
      <div className="flex items-center justify-between">
        <h2 className="text-2xl font-bold">Metadata Search</h2>
        <div className="flex rounded-lg border-2">
          {SEARCH_ENTITIES.map(entity => (
            <Link
              key={entity}
              href={`/?type=${entity}`}
              className={`block p-2 ${type === entity ? 'bg-gray-100' : ''}`}
            >
              {entity}
            </Link>
          ))}
        </div>
        <div className="relative w-80">
          <div className="pointer-events-none absolute inset-y-0 start-0 flex items-center ps-3">
            <MagnifyingGlassIcon className="h-5 w-5 fill-none stroke-teal-500" />
          </div>
          <input
            type="text"
            className="block w-full rounded-lg border border-gray-300 p-2 ps-10 text-sm text-gray-900 focus:border-blue-500 focus:ring-blue-500 dark:border-gray-600 dark:bg-gray-700 dark:text-white dark:placeholder-gray-400 dark:focus:border-blue-500 dark:focus:ring-blue-500"
          />
        </div>
      </div>
      {response?.type === 'wfSpec' && <WfSpecTable items={response.results} />}
      {response?.type === 'taskDef' && <TaskDefTable items={response.results} />}
      {response?.type === 'userTaskDef' && <UserTaskDefTable items={response.results} />}
    </div>
  )
}

const getType = (type: any | null): SearchType => {
  if (!type) return 'wfSpec'

  return SEARCH_ENTITIES.includes(type) ? type : 'wfSpec'
}
