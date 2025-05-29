"use client"

import { X } from "lucide-react"
import { Button } from "@/components/ui/button"

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
      onClick={(e) => {
        e.stopPropagation()
        onReset()
      }}
      className="h-6 px-2 text-xs"
    >
      <X className="h-3 w-3 mr-1" />
      Clear filters
    </Button>
  )
}
