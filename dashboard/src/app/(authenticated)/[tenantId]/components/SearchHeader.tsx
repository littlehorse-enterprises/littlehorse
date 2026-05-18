'use client'
import { SEARCH_ENTITIES, SearchType } from '@/app/constants'
import { routes } from '@/app/routes'
import LinkWithTenant from './LinkWithTenant'

import { FC } from 'react'

export type SortBy = 'name' | 'createdAt' | null
export type SortOrder = 'asc' | 'desc'

type Props = {
  currentType: SearchType
}

export const SearchHeader: FC<Props> = ({ currentType }) => {
  return (
    <div className="mb-6 flex items-center">
      <h2 className="text-2xl font-bold">Metadata Search</h2>
      <div className="flex flex-1 items-center justify-center">
        <div className="flex rounded-lg border-2">
          {SEARCH_ENTITIES.map(type => (
            <LinkWithTenant
              key={type}
              href={routes.search.typeQuery(type)}
              className={`block p-2 ${type === currentType ? 'bg-gray-100' : ''}`}
            >
              {type}
            </LinkWithTenant>
          ))}
        </div>
      </div>
    </div>
  )
}
