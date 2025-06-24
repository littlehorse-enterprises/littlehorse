'use client'

import { Filter } from 'lucide-react'
import type React from 'react'
import { useEffect, useState } from 'react'

import {
  DropdownMenu,
  DropdownMenuCheckboxItem,
  DropdownMenuContent,
  DropdownMenuLabel,
  DropdownMenuRadioGroup,
  DropdownMenuRadioItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@littlehorse-enterprises/ui-library/dropdown-menu'
import { Button } from '@littlehorse-enterprises/ui-library/button'

export interface FilterOption {
  value: string
  label: string
  icon?: React.ReactNode
}

interface DropdownFilterProps {
  title: string
  options: FilterOption[]
  labelText: string
  icon?: React.ReactNode
  selectionMode: 'single' | 'multiple'
  visualStyle?: 'checkbox' | 'radio' // Optional visual style
  onFilterChange: (values: string | string[]) => void
  initialSelected?: string | string[]
  currentValue?: string | string[] // New prop to sync with parent state
}

export function DropdownFilter({
  title,
  options,
  labelText,
  icon = <Filter className="text-muted-foreground/70 h-3.5 w-3.5" />,
  selectionMode = 'multiple',
  visualStyle, // If undefined, defaults to matching selectionMode
  onFilterChange,
  initialSelected = selectionMode === 'single' ? options[0]?.value || '' : [],
  currentValue, // For controlled component behavior
}: DropdownFilterProps) {
  // For multiple selection mode
  const [selectedValues, setSelectedValues] = useState<Set<string>>(
    selectionMode === 'multiple' && Array.isArray(initialSelected) ? new Set(initialSelected) : new Set()
  )

  // For single selection mode
  const [selectedValue, setSelectedValue] = useState<string>(
    selectionMode === 'single' && typeof initialSelected === 'string' ? initialSelected : options[0]?.value || ''
  )

  // Sync with parent state when it changes
  useEffect(() => {
    if (currentValue !== undefined) {
      if (selectionMode === 'multiple' && Array.isArray(currentValue)) {
        setSelectedValues(new Set(currentValue))
      } else if (selectionMode === 'single' && typeof currentValue === 'string') {
        setSelectedValue(currentValue)
      }
    }
  }, [currentValue, selectionMode])

  // Determine visual style (checkbox or radio)
  const useCheckboxStyle = visualStyle === 'checkbox' || (visualStyle === undefined && selectionMode === 'multiple')

  const handleCheckedChange = (checked: boolean, value: string) => {
    if (selectionMode === 'multiple') {
      // Multiple selection behavior
      const newSelected = new Set(selectedValues)

      if (checked) {
        newSelected.add(value)
      } else {
        newSelected.delete(value)
      }

      setSelectedValues(newSelected)
      onFilterChange(Array.from(newSelected))
    } else {
      // Single selection behavior but with checkbox UI
      setSelectedValue(value)
      onFilterChange(value)
    }
  }

  const handleRadioChange = (value: string) => {
    if (selectionMode !== 'single') return

    setSelectedValue(value)
    onFilterChange(value)
  }

  // Determine if filter is active for indicator dot
  const isFilterActive = selectionMode === 'multiple' ? selectedValues.size > 0 : selectedValue !== options[0]?.value

  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button variant="ghost" size="sm" className="h-8 gap-1 px-2">
          {title}
          {isFilterActive && <span className="bg-primary ml-1 h-2 w-2 rounded-full" />}
          {icon}
        </Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent align="start" className="w-[200px]">
        <DropdownMenuLabel className="text-xs">{labelText}</DropdownMenuLabel>
        <DropdownMenuSeparator />

        {useCheckboxStyle ? (
          // Checkbox UI (for multiple selection or forced checkbox style)
          options.map(option => (
            <DropdownMenuCheckboxItem
              key={option.value}
              checked={selectionMode === 'multiple' ? selectedValues.has(option.value) : selectedValue === option.value}
              onCheckedChange={checked => handleCheckedChange(checked, option.value)}
            >
              <div className="flex items-center">
                {option.icon}
                <span className={option.icon ? 'ml-2' : ''}>{option.label}</span>
              </div>
            </DropdownMenuCheckboxItem>
          ))
        ) : (
          // Radio UI (for single selection)
          <DropdownMenuRadioGroup value={selectedValue} onValueChange={handleRadioChange}>
            {options.map(option => (
              <DropdownMenuRadioItem key={option.value} value={option.value}>
                <div className="flex items-center">
                  {option.icon}
                  <span className={option.icon ? 'ml-2' : ''}>{option.label}</span>
                </div>
              </DropdownMenuRadioItem>
            ))}
          </DropdownMenuRadioGroup>
        )}
      </DropdownMenuContent>
    </DropdownMenu>
  )
}
