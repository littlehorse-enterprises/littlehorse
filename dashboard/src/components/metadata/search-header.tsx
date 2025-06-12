'use client'
import { Input } from '@/components/ui/input'
import { Search } from 'lucide-react'

interface SearchHeaderProps {
  prefix: string
  onPrefixChange: (value: string) => void
}

export function SearchHeader({ prefix, onPrefixChange }: SearchHeaderProps) {
  return (
    <div className="mb-8 flex items-center justify-between">
      <h1 className="text-4xl font-bold">Metadata Search</h1>
      <div className="flex items-center gap-2">
        <div className="relative w-80">
          <Search className="text-muted-foreground absolute top-1/2 left-3 h-4 w-4 -translate-y-1/2" />
          <Input
            placeholder="Search metadata..."
            className="pr-4 pl-10"
            value={prefix}
            onChange={e => onPrefixChange(e.target.value)}
          />
        </div>
      </div>
    </div>
  )
}
