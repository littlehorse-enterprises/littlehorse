import { SEARCH_LIMITS } from '@/app/constants'
import { FC } from 'react'

type Props = {
  currentLimit: number
  setLimit: (limit: number) => void
  hasNextPage: boolean
  fetchNextPage: () => void
}
export const SearchFooter: FC<Props> = ({ hasNextPage, fetchNextPage, currentLimit, setLimit }) => {
  return (
    <div className="flex justify-between">
      <div className="flex items-center gap-2 text-gray-400">
        Items per load:
        <select
          value={currentLimit}
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
      {hasNextPage && (
        <button className="rounded bg-blue-500 px-4 py-2 text-white" onClick={() => fetchNextPage()}>
          Load More
        </button>
      )}
    </div>
  )
}
