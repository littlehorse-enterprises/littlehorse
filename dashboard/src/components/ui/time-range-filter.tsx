'use client'

import { TIME_RANGES } from '@/utils/ui/constants'
import { Clock } from 'lucide-react'
import { useState } from 'react'

import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuLabel,
  DropdownMenuRadioGroup,
  DropdownMenuRadioItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import { Button } from '@littlehorse-enterprises/ui-library/button'

export interface TimeRangeFilterProps {
  title: string
  onFilterChange: (minutes: number | null) => void
  initialValue?: string
}

export function TimeRangeFilter({ title, onFilterChange, initialValue = 'all' }: TimeRangeFilterProps) {
  const [selectedValue, setSelectedValue] = useState<string>(initialValue)

  const handleValueChange = (value: string) => {
    setSelectedValue(value)
    const selectedOption = TIME_RANGES.find(option => option.value === value)
    onFilterChange(selectedOption?.minutes ?? null)
  }

  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button variant="ghost" size="sm" className="h-8 gap-1 px-2">
          {title}
          {selectedValue !== 'all' && <span className="bg-primary ml-1 h-2 w-2 rounded-full" />}
          <Clock className="text-muted-foreground/70 h-3.5 w-3.5" />
        </Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent align="start" className="w-[200px]">
        <DropdownMenuLabel className="text-xs">Filter by time range</DropdownMenuLabel>
        <DropdownMenuSeparator />
        <DropdownMenuRadioGroup value={selectedValue} onValueChange={handleValueChange}>
          {TIME_RANGES.map(option => (
            <DropdownMenuRadioItem key={option.value} value={option.value}>
              {option.label}
            </DropdownMenuRadioItem>
          ))}
        </DropdownMenuRadioGroup>
      </DropdownMenuContent>
    </DropdownMenu>
  )
}
