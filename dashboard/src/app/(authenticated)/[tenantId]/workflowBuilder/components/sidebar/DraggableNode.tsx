'use client'

import { useRef, useState } from 'react'
import { useDraggable } from '@neodrag/react'
import { XYPosition } from 'reactflow'
import { NodeType } from '@/app/(authenticated)/[tenantId]/(diagram)/components/NodeTypes/extractNodes'

interface DraggableNodeProps {
  className?: string
  children: React.ReactNode
  nodeType: NodeType
  onDrop: (nodeType: NodeType, position: XYPosition) => void
}

const baseClasses =
  'h-5 p-2 border border-gray-500 rounded-md mb-2.5 flex justify-center items-center cursor-grab touch-none bg-gray-700 text-xs whitespace-nowrap active:cursor-grabbing'

export function DraggableNode({ className, children, nodeType, onDrop }: DraggableNodeProps) {
  const draggableRef = useRef<HTMLDivElement>(null)
  const [position, setPosition] = useState<XYPosition>({ x: 0, y: 0 })

  useDraggable(draggableRef as React.RefObject<HTMLElement>, {
    position: position,
    onDrag: ({ offsetX, offsetY }: { offsetX: number; offsetY: number }) => {
      setPosition({
        x: offsetX,
        y: offsetY,
      })
    },
    onDragEnd: ({ event }) => {
      setPosition({ x: 0, y: 0 })
      onDrop(nodeType, {
        x: event.clientX,
        y: event.clientY,
      })
    },
  })

  return (
    <div className={`${baseClasses} ${className || ''}`} ref={draggableRef}>
      {children}
    </div>
  )
}
