"use client"

import type React from "react"

import { ChevronRight } from "lucide-react"
import { useState } from "react"

interface ExpandableItemProps {
  title: string
  children?: React.ReactNode
}

export default function ExpandableItem({ title, children }: ExpandableItemProps) {
  const [isExpanded, setIsExpanded] = useState(false)

  return (
    <div>
      <div className="flex cursor-pointer items-center" onClick={() => setIsExpanded(!isExpanded)}>
        <ChevronRight className={`mr-1 h-4 w-4 text-[#656565] transition-transform ${isExpanded ? "rotate-90" : ""}`} />
        <span>{title}</span>
      </div>
      {isExpanded && children && <div className="ml-5 mt-2">{children}</div>}
    </div>
  )
}
