'use client'
import { Button } from '@/components/ui/button'
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from '@/components/ui/dropdown-menu'
import { ChevronDown } from 'lucide-react'

export function DiagramDataGroupIndexer({
  index,
  setIndex,
  indexes,
}: {
  index: number
  setIndex: (index: number) => void
  indexes: number
}) {
  if (indexes <= 1) return null
  return (
    <div className="absolute -top-5 right-0">
      <DropdownMenu>
        <DropdownMenuTrigger asChild>
          <Button variant="outline" className="h-8 w-fit border-none px-1 py-2 drop-shadow-none">
            {indexes - 1 - index} <ChevronDown className="w-4" />
          </Button>
        </DropdownMenuTrigger>
        <DropdownMenuContent className="max-h-[300px] overflow-y-auto">
          {Array.from({ length: indexes }, (_, i) => (
            <DropdownMenuItem className="cursor-pointer" key={i} onClick={() => setIndex(i)}>
              {indexes - 1 - i}
            </DropdownMenuItem>
          ))}
        </DropdownMenuContent>
      </DropdownMenu>
    </div>
  )
}
