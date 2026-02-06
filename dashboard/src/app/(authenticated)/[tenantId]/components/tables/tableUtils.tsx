import { ArrowDownAZ, ArrowUpAZ, ArrowUpDown } from 'lucide-react'
import { SortBy, SortOrder } from '../SearchHeader'

export const getSortIcon = (column: SortBy, currentSortBy: SortBy | undefined, currentSortOrder: SortOrder) => {
  if (currentSortBy !== column) return <ArrowUpDown className="ml-1 h-4 w-4 opacity-50" />
  return currentSortOrder === 'asc' ? <ArrowUpAZ className="ml-1 h-4 w-4" /> : <ArrowDownAZ className="ml-1 h-4 w-4" />
}

export const handleNameSort = (
  currentSortBy: SortBy | undefined,
  currentSortOrder: SortOrder,
  setSortBy: (sortBy: SortBy) => void,
  setSortOrder: (order: SortOrder) => void
) => {
  if (currentSortBy === 'name') {
    setSortOrder(currentSortOrder === 'asc' ? 'desc' : 'asc')
  } else {
    setSortBy('name')
    setSortOrder('asc')
  }
}

export const handleDateSort = (
  currentSortBy: SortBy | undefined,
  currentSortOrder: SortOrder,
  setSortBy: (sortBy: SortBy) => void,
  setSortOrder: (order: SortOrder) => void
) => {
  if (currentSortBy === 'createdAt') {
    setSortOrder(currentSortOrder === 'asc' ? 'desc' : 'asc')
  } else {
    setSortBy('createdAt')
    setSortOrder('desc') // Default to newest first
  }
}

type SortableItem = {
  name: string
  createdAt?: Date | undefined
}

export const sortData = <T extends SortableItem>(items: T[], sortBy: SortBy | undefined, sortOrder: SortOrder): T[] => {
  if (!sortBy) return items

  const sorted = [...items]
  if (sortBy === 'name') {
    sorted.sort((a, b) => {
      const comparison = a.name.localeCompare(b.name)
      return sortOrder === 'asc' ? comparison : -comparison
    })
  } else if (sortBy === 'createdAt') {
    sorted.sort((a, b) => {
      const aDate = a.createdAt?.getTime() ?? 0
      const bDate = b.createdAt?.getTime() ?? 0
      const comparison = aDate - bDate
      return sortOrder === 'asc' ? comparison : -comparison
    })
  }
  return sorted
}

export const formatDate = (date: Date | undefined): string => {
  if (!date) return 'N/A'
  return date.toLocaleString()
}
