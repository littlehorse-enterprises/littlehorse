'use client'

import { Filter } from 'lucide-react'
import type React from 'react'
import { useState } from 'react'

import {
  DropdownMenu,
  DropdownMenuCheckboxItem,
  DropdownMenuContent,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@littlehorse-enterprises/ui-library/dropdown-menu'
import { Button } from '@littlehorse-enterprises/ui-library/button'

interface StatusOption {
  value: string
  label: string
  icon: React.ReactNode
}

interface SimpleStatusFilterProps {
  title: string
  options: StatusOption[]
  onFilterChange: (values: string[]) => void
  initialSelected?: string[]
}

export function SimpleStatusFilter({ title, options, onFilterChange, initialSelected = [] }: SimpleStatusFilterProps) {
  const [selectedValues, setSelectedValues] = useState<Set<string>>(new Set(initialSelected))

  const handleCheckedChange = (checked: boolean, value: string) => {
    const newSelected = new Set(selectedValues)

    if (checked) {
      newSelected.add(value)
    } else {
      newSelected.delete(value)
    }

    setSelectedValues(newSelected)
    onFilterChange(Array.from(newSelected))
  }

  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button variant="ghost" size="sm" className="h-8 gap-1 px-2">
          {title}
          {selectedValues.size > 0 && <span className="bg-primary ml-1 h-2 w-2 rounded-full" />}
          <Filter className="text-muted-foreground/70 h-3.5 w-3.5" />
        </Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent align="start" className="w-[200px]">
        <DropdownMenuLabel className="text-xs">Filter by status</DropdownMenuLabel>
        <DropdownMenuSeparator />
        {options.map(option => (
          <DropdownMenuCheckboxItem
            key={option.value}
            checked={selectedValues.has(option.value)}
            onCheckedChange={checked => handleCheckedChange(checked, option.value)}
          >
            <div className="flex items-center">
              {option.icon}
              <span className="ml-2">{option.label}</span>
            </div>
          </DropdownMenuCheckboxItem>
        ))}
      </DropdownMenuContent>
    </DropdownMenu>
  )
}
