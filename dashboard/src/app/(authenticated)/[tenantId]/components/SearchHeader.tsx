'use client'
import { SEARCH_ENTITIES, SearchType } from '@/app/constants'
import { SearchIcon } from 'lucide-react'
import LinkWithTenant from './LinkWithTenant'

import { FC } from 'react'

type Props = {
  currentType: SearchType
  setPrefix: (value: string | undefined) => void
}

export const SearchHeader: FC<Props> = ({ currentType, setPrefix }) => {
  return (
    <div className="flex items-center justify-between">
      <h2 className="text-2xl font-bold">Metadata Search</h2>
      <div className="flex rounded-lg border-2">
        {SEARCH_ENTITIES.map(type => (
          <LinkWithTenant
            key={type}
            href={`?type=${type}`}
            className={`block p-2 ${type === currentType ? 'bg-gray-100' : ''}`}
          >
            {type}
          </LinkWithTenant>
        ))}
      </div>
      <div className="relative w-80">
        <div className="pointer-events-none absolute inset-y-0 start-0 flex items-center ps-3">
          <SearchIcon className="h-5 w-5 text-blue-500" />
        </div>
        <input
          type="text"
          onChange={e => {
            const { value } = e.target
            if (value) {
              setPrefix(value)
            } else {
              setPrefix(undefined)
            }
          }}
          className="block w-full rounded-lg border border-gray-300 p-2 ps-10 text-sm text-gray-900 focus:border-blue-500 focus:ring-blue-500 dark:border-gray-600 dark:bg-gray-700 dark:text-white dark:placeholder-gray-400 dark:focus:border-blue-500 dark:focus:ring-blue-500"
        />
      </div>
    </div>
  )
}
