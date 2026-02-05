'use client'
import { SEARCH_DEFAULT_LIMIT, SEARCH_ENTITIES, SearchType } from '@/app/constants'
import { RefreshCwIcon, SearchIcon } from 'lucide-react'
import { useParams, useSearchParams } from 'next/navigation'
import { FC, useCallback, useEffect, useMemo, useRef, useState } from 'react'
import useSWRInfinite from 'swr/infinite'
import { SearchFooter } from './SearchFooter'
import { SearchHeader, SortBy, SortOrder } from './SearchHeader'
import { SearchResponse, search } from './searchAction'
import {
  ExternalEventDefTable,
  StructDefTable,
  TaskDefTable,
  UserTaskDefTable,
  WfSpecTable,
  WorkflowEventDefTable,
} from './tables'

const SearchInput: FC<{
  prefix: string | undefined
  setPrefix: (value: string | undefined) => void
  placeholder: string
}> = ({ prefix, setPrefix, placeholder }) => {
  const [localValue, setLocalValue] = useState<string>(prefix || '')
  const timeoutRef = useRef<NodeJS.Timeout>()

  useEffect(() => {
    setLocalValue(prefix || '')
  }, [prefix])

  const handleChange = useCallback(
    (e: React.ChangeEvent<HTMLInputElement>) => {
      const { value } = e.target
      setLocalValue(value)

      if (timeoutRef.current) {
        clearTimeout(timeoutRef.current)
      }

      timeoutRef.current = setTimeout(() => {
        setPrefix(value || undefined)
      }, 300)
    },
    [setPrefix]
  )

  useEffect(() => {
    return () => {
      if (timeoutRef.current) {
        clearTimeout(timeoutRef.current)
      }
    }
  }, [])

  return (
    <div className="relative w-full max-w-md">
      <div className="pointer-events-none absolute inset-y-0 start-0 flex items-center ps-3">
        <SearchIcon className="h-5 w-5 text-gray-400" />
      </div>
      <input
        type="text"
        placeholder={placeholder}
        value={localValue}
        onChange={handleChange}
        className="block w-full rounded-lg border border-gray-300 p-2 ps-10 text-sm text-gray-900 focus:border-blue-500 focus:ring-1 focus:ring-blue-500"
      />
    </div>
  )
}

export const Search: FC = () => {
  const [prefix, setPrefix] = useState<string | undefined>()
  const searchParams = useSearchParams()
  const type = getType(searchParams.get('type'))
  const [limit, setLimit] = useState<number>(SEARCH_DEFAULT_LIMIT)
  const [sortBy, setSortBy] = useState<SortBy>(null)
  const [sortOrder, setSortOrder] = useState<SortOrder>('asc')
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

  const searchPlaceholder = useMemo(() => `Search ${type}s...`, [type])

  const showSearchInput = type === 'WfSpec' || type === 'TaskDef'

  return (
    <div className="flex flex-col space-y-4">
      <SearchHeader currentType={type} />

      {showSearchInput && (
        <div className="mb-4">
          <SearchInput prefix={prefix} setPrefix={setPrefix} placeholder={searchPlaceholder} />
        </div>
      )}

      {isPending ? (
        <div className="flex min-h-[360px] items-center justify-center text-center">
          <RefreshCwIcon className="h-8 w-8 animate-spin text-blue-500" />
        </div>
      ) : (
        <div>
          {type === 'WfSpec' && (
            <WfSpecTable
              pages={data}
              sortBy={sortBy}
              sortOrder={sortOrder}
              setSortBy={setSortBy}
              setSortOrder={setSortOrder}
            />
          )}
          {type === 'TaskDef' && (
            <TaskDefTable
              pages={data}
              sortBy={sortBy}
              sortOrder={sortOrder}
              setSortBy={setSortBy}
              setSortOrder={setSortOrder}
            />
          )}
          {type === 'UserTaskDef' && <UserTaskDefTable pages={data} />}
          {type === 'ExternalEventDef' && <ExternalEventDefTable pages={data} />}
          {type === 'WorkflowEventDef' && <WorkflowEventDefTable pages={data} />}
          {type === 'StructDef' && <StructDefTable pages={data} />}
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
