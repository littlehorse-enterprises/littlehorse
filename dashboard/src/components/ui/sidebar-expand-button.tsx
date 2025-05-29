"use client"

import { ChevronLeft, ChevronRight } from "lucide-react"
import { Button } from "@/components/ui/button"

interface SidebarExpandButtonProps {
  isExpanded: boolean
  onClick: () => void
  position: "left" | "right"
}

export default function SidebarExpandButton({ isExpanded, onClick, position }: SidebarExpandButtonProps) {
  return (
    <Button
      onClick={onClick}
      size="icon"
      variant="outline"
      className="h-6 w-6 rounded-full p-0 border border-gray-200 bg-white text-[#656565] shadow-sm hover:bg-gray-50 z-20"
      aria-label={isExpanded ? "Collapse sidebar" : "Expand sidebar"}
    >
      {position === "left" ? (
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
