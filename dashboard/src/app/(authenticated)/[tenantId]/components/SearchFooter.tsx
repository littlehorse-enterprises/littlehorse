import { SEARCH_LIMITS, SearchLimit } from '@/app/constants'
import { ChevronDown } from 'lucide-react'
import { FC } from 'react'

type Props = {
  currentLimit: number
  setLimit: (limit: SearchLimit) => void
  hasNextPage: boolean
  fetchNextPage: () => void
}
export const SearchFooter: FC<Props> = ({ hasNextPage, fetchNextPage, currentLimit, setLimit }) => {
  return (
    <div className="flex items-center justify-between pt-4">
      <div className="flex items-center gap-3">
        <span className="text-sm text-gray-600">Items per load:</span>
        <div className="relative">
          <select
            value={currentLimit}
            onChange={e => {
              setLimit(parseInt(e.target.value) as SearchLimit)
            }}
            className="cursor-pointer appearance-none rounded-lg border border-gray-300 bg-white px-4 py-2 pr-8 text-sm text-gray-900 transition-colors hover:border-gray-400 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
          >
            {SEARCH_LIMITS.map(searchLimit => (
              <option key={searchLimit} value={searchLimit}>
                {searchLimit}
              </option>
            ))}
          </select>
          <div className="pointer-events-none absolute inset-y-0 right-0 flex items-center pr-2">
            <ChevronDown className="h-4 w-4 text-gray-400" />
          </div>
        </div>
      </div>
      {hasNextPage && (
        <button
          className="rounded-lg border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-700 transition-colors hover:border-gray-400 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
          onClick={() => fetchNextPage()}
        >
          Load More
        </button>
      )}
    </div>
  )
}
