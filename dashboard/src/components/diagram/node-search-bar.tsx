import { Search } from 'lucide-react'
import { Input } from '@littlehorse-enterprises/ui-library/input'

interface NodeSearchBarProps {
  searchTerm: string
  onSearchChange: (value: string) => void
}

export function NodeSearchBar({ searchTerm, onSearchChange }: NodeSearchBarProps) {
  return (
    <div className="relative mb-3">
      <div className="pointer-events-none absolute inset-y-0 left-0 z-10 flex items-center pl-2">
        <Search className="h-3.5 w-3.5 text-gray-400" />
      </div>
      <Input
        type="text"
        placeholder="Search nodes..."
        className="h-8 w-full pl-8 text-xs"
        value={searchTerm}
        onChange={e => onSearchChange(e.target.value)}
      />
    </div>
  )
}
