'use client'

import { Button } from '@littlehorse-enterprises/ui-library/button'
import { ChevronLeft, ChevronRight } from 'lucide-react'

interface SidebarExpandButtonProps {
  isExpanded: boolean
  onClick: () => void
  position: 'left' | 'right'
}

export default function SidebarExpandButton({ isExpanded, onClick, position }: SidebarExpandButtonProps) {
  return (
    <Button
      onClick={onClick}
      size="icon"
      variant="outline"
      className="z-20 h-6 w-6 rounded-full border border-gray-200 bg-white p-0 text-[#656565] shadow-sm hover:bg-gray-50"
      aria-label={isExpanded ? 'Collapse sidebar' : 'Expand sidebar'}
    >
      {position === 'left' ? (
        isExpanded ? (
          <ChevronRight size={14} />
        ) : (
          <ChevronLeft size={14} />
        )
      ) : isExpanded ? (
        <ChevronLeft size={14} />
      ) : (
        <ChevronRight size={14} />
      )}
    </Button>
  )
}
