'use client'

import { Button } from '@littlehorse-enterprises/ui-library/button'
import { X } from 'lucide-react'

interface FilterResetButtonProps {
  onReset: () => void
  isActive: boolean
}

export function FilterResetButton({ onReset, isActive }: FilterResetButtonProps) {
  if (!isActive) return null

  return (
    <Button
      variant="ghost"
      size="sm"
      onClick={e => {
        e.stopPropagation()
        onReset()
      }}
      className="h-6 px-2 text-xs"
    >
      <X className="mr-1 h-3 w-3" />
      Clear filters
    </Button>
  )
}
