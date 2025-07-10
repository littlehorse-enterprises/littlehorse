'use client'

import { TreeNode } from '@/types'
import { LHStatus } from 'littlehorse-client/proto'
import { CheckCircle, ChevronDown, ChevronRight, Loader2, XCircle } from 'lucide-react'
import React, { useEffect, useMemo, useState } from 'react'
import { useNodeSelection } from '../context/selection-context'
import { NODE_STYLES } from '@/constants'

interface TreeNodeComponentProps {
  node: TreeNode
  isRoot?: boolean
  searchTerm: string
}

export function TreeNodeComponent({ node, isRoot = false, searchTerm }: TreeNodeComponentProps) {
  const [isExpanded, setIsExpanded] = useState(isRoot)
  const hasChildren = node.children && node.children.length > 0
  const { selectedId, setSelectedId } = useNodeSelection()
  const isSelected = useMemo(() => selectedId === node.id, [selectedId, node.id])

  // Expand parent nodes when a child is selected
  useEffect(() => {
    if (hasChildren && node.children.some(child => child.id === selectedId)) {
      setIsExpanded(true)
    }
  }, [selectedId, hasChildren, node.children])

  // Auto-expand when search term is present and node matches
  useEffect(() => {
    if (searchTerm && !isExpanded) {
      const nodeMatches = node.label.toLowerCase().includes(searchTerm.toLowerCase())
      const childMatches = node.children?.some(child => child.label.toLowerCase().includes(searchTerm.toLowerCase()))
      if (nodeMatches || childMatches) {
        setIsExpanded(true)
      }
    }
  }, [searchTerm, node, isExpanded])

  const getNodeStatusIcon = () => {
    const nodeType = node.type

    if (nodeType) {
      const nodeStyle = NODE_STYLES[nodeType]
      if (nodeStyle) {
        const IconComponent = nodeStyle.icon
        return (
          <div className="mr-1.5 flex items-center justify-center">
            <div className="flex h-3 w-3 items-center justify-center">
              <IconComponent className={`h-3 w-3 ${nodeStyle.iconColor}`} />
            </div>
          </div>
        )
      }
    }

    // For task nodes, show status with proper task icon
    if (node.status === LHStatus.COMPLETED) return <CheckCircle className="mr-1.5 h-3 w-3 text-green-500" />
    if (node.status === LHStatus.ERROR || node.status === LHStatus.EXCEPTION)
      return <XCircle className="mr-1.5 h-3 w-3 text-red-500" />
    if (node.status === LHStatus.RUNNING) return <Loader2 className="mr-1.5 h-3 w-3 animate-spin text-blue-500" />

    // Default to task icon if no specific type detected
    const taskStyle = NODE_STYLES.task
    const TaskIcon = taskStyle.icon
    return (
      <div className="mr-1.5 flex items-center justify-center">
        <div className="flex h-3 w-3 items-center justify-center">
          <TaskIcon className="h-3 w-3" />
        </div>
      </div>
    )
  }

  const handleToggle = (e: React.MouseEvent) => {
    e.stopPropagation()
    setIsExpanded(!isExpanded)
  }

  const handleClick = () => {
    setSelectedId(node.id)
  }

  // Highlight text that matches search term
  const highlightMatch = (text: string, term: string) => {
    if (!term) return text

    const regex = new RegExp(`(${term})`, 'gi')
    const parts = text.split(regex)

    return (
      <>
        {parts.map((part, i) =>
          regex.test(part) ? (
            <span key={i} className="bg-yellow-200">
              {part}
            </span>
          ) : (
            part
          )
        )}
      </>
    )
  }

  return (
    <div className="select-none">
      <div
        className={`flex cursor-pointer items-center gap-1.5 rounded-md px-2 py-1.5 text-sm transition-colors ${isSelected ? 'bg-blue-100 text-blue-900 ring-1 ring-blue-400' : 'hover:bg-gray-100'} ${searchTerm && node.label.toLowerCase().includes(searchTerm.toLowerCase()) ? 'bg-yellow-50' : ''}`}
        style={{ paddingLeft: `${node.level * 12 + 4}px` }}
        onClick={handleClick}
      >
        {hasChildren && (
          <button onClick={handleToggle} className="rounded p-0.5 transition-colors hover:bg-gray-200">
            {isExpanded ? (
              <ChevronDown className="h-4 w-4 text-gray-500" />
            ) : (
              <ChevronRight className="h-4 w-4 text-gray-500" />
            )}
          </button>
        )}
        {!hasChildren && <div className="w-5" />}
        {getNodeStatusIcon()}
        <div className="flex-1 truncate">{searchTerm ? highlightMatch(node.label, searchTerm) : node.label}</div>
      </div>

      {isExpanded && hasChildren && (
        <div>
          {node.children.map(child => (
            <TreeNodeComponent key={child.id} node={child} searchTerm={searchTerm} />
          ))}
        </div>
      )}
    </div>
  )
}
