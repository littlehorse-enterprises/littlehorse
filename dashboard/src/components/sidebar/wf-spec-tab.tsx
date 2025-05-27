"use client"

import type React from "react"

import { ChevronDown, ChevronRight, CheckCircle, XCircle, Loader2, Search } from "lucide-react"
import { useState, useEffect, useRef } from "react"
import { buildNodeTree, type TreeNode } from "@/components/flow/mock-data"
import { Input } from "@/components/ui/input"
import { useWorkflow } from "../context/workflow-context"
import { getNodeIcon } from "@/components/flow/node-utils"

interface WfSpecTabProps {
  onNodeSelect: (nodeId: string) => void
  selectedNodeId?: string
}

export default function WfSpecTab({ onNodeSelect, selectedNodeId }: WfSpecTabProps) {
  const { wfSpec, isLoading } = useWorkflow()

  const nodes = wfSpec?.threadSpecs.entrypoint?.nodes!


  const [searchTerm, setSearchTerm] = useState("")
  const selectedNodeRef = useRef<HTMLDivElement>(null)

  // Scroll to selected node when it changes
  useEffect(() => {
    if (selectedNodeId && selectedNodeRef.current) {
      selectedNodeRef.current.scrollIntoView({ behavior: "smooth", block: "nearest" })
    }
  }, [selectedNodeId])


  const nodeTree = buildNodeTree(nodes)

  // Sort nodes in descending order based on the first character (number)
  const sortedNodeTree = [...nodeTree].sort((a, b) => {
    const aFirstChar = a.id.charAt(0);
    const bFirstChar = b.id.charAt(0);

    // If the first character is a number, sort numerically in descending order
    if (!isNaN(Number(aFirstChar)) && !isNaN(Number(bFirstChar))) {
      return Number(aFirstChar) - Number(bFirstChar); // Descending order
    }

    // Fallback to alphabetical sorting if not numbers
    return bFirstChar.localeCompare(aFirstChar);
  });

  if (isLoading) return <div>Loading...</div>

  return (
    <div className="flex-1 overflow-y-auto p-2">
      {/* Search bar */}
      <div className="mb-3 relative">
        <div className="absolute inset-y-0 left-0 flex items-center pl-2 pointer-events-none z-10">
          <Search className="h-3.5 w-3.5 text-gray-400" />
        </div>
        <Input
          type="text"
          placeholder="Search nodes..."
          className="w-full pl-8 h-8 text-xs"
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
        />
      </div>

      {/* Render each node in the flat structure */}
      <div>
        {sortedNodeTree.map((node) => (
          <div key={node.id} className="mb-1">
            <TreeNodeComponent
              node={node}
              onNodeSelect={onNodeSelect}
              selectedNodeId={selectedNodeId}
              isRoot
              searchTerm={searchTerm}
              selectedNodeRef={selectedNodeRef}
            />
          </div>
        ))}
      </div>
    </div>
  )
}

interface TreeNodeProps {
  node: TreeNode
  onNodeSelect: (nodeId: string) => void
  selectedNodeId?: string
  isRoot?: boolean
  searchTerm: string
  selectedNodeRef: React.RefObject<HTMLDivElement | null>
}

function TreeNodeComponent({
  node,
  onNodeSelect,
  selectedNodeId,
  isRoot = false,
  searchTerm,
  selectedNodeRef,
}: TreeNodeProps) {
  // Check if this node or any of its children match the search term
  const matchesSearch = (node: TreeNode, term: string): boolean => {
    if (!term) return true

    const nodeMatches = node.label.toLowerCase().includes(term.toLowerCase())

    if (nodeMatches) return true

    // Check if any children match
    return node.children.some((child) => matchesSearch(child, term))
  }

  const nodeMatches = matchesSearch(node, searchTerm)

  // Don't render if this node and none of its children match the search
  if (searchTerm && !nodeMatches) return null

  // Auto-expand if we're searching or if this is the root
  const [isExpanded, setIsExpanded] = useState(isRoot || !!searchTerm)

  // Update expanded state when search term changes
  useEffect(() => {
    if (searchTerm) {
      setIsExpanded(true)
    } else if (!isRoot) {
      setIsExpanded(false)
    }
  }, [searchTerm, isRoot])

  // Auto-expand parent nodes when a child is selected
  useEffect(() => {
    if (selectedNodeId) {
      const isChildSelected = (node: TreeNode, id: string): boolean => {
        if (node.id === id) return true
        return node.children.some((child) => isChildSelected(child, id))
      }

      if (isChildSelected(node, selectedNodeId)) {
        setIsExpanded(true)
      }
    }
  }, [selectedNodeId, node])

  const hasChildren = node.children && node.children.length > 0

  const getNodeTypeFromLabel = (label: string): string => {
    if (label.includes('ENTRYPOINT')) return "ENTRYPOINT"
    if (label.includes('EXIT')) return "EXIT"
    if (label.includes('TASK')) return "TASK"
    if (label.includes('EXTERNAL_EVENT')) return "EXTERNAL_EVENT"
    return "UNKNOWN"
  }

  const getNodeStatusIcon = () => {
    // Check for pattern in node label/ID
    const nodeType = getNodeTypeFromLabel(node.label)

    if (nodeType !== "UNKNOWN") {
      return getNodeIcon(nodeType, "sm")
    }

    // Original type-based icons as fallback
    if (node.type === "start") return <div className="w-3 h-3 rounded-full bg-green-500 mr-1.5" />
    if (node.type === "end") return <div className="w-3 h-3 rounded-full bg-red-500 mr-1.5" />
    if (node.type === "decision") return <div className="w-3 h-3 rotate-45 bg-yellow-500 mr-1.5" />

    // For task nodes, show status
    if (node.status === "completed") return <CheckCircle className="w-3 h-3 text-green-500 mr-1.5" />
    if (node.status === "error") return <XCircle className="w-3 h-3 text-red-500 mr-1.5" />
    if (node.status === "running") return <Loader2 className="w-3 h-3 text-blue-500 animate-spin mr-1.5" />

    return <div className="w-3 h-3 rounded-sm bg-gray-400 mr-1.5" />
  }

  const handleToggle = (e: React.MouseEvent) => {
    e.stopPropagation()
    setIsExpanded(!isExpanded)
  }

  const handleSelect = () => {
    onNodeSelect(node.id)
  }

  // Highlight text that matches search term
  const highlightMatch = (text: string, term: string) => {
    if (!term) return text

    const regex = new RegExp(`(${term})`, "gi")
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
          ),
        )}
      </>
    )
  }

  // Extract the middle part of the node name
  const extractNodeName = (fullName: string): string => {
    const match = RegExp(/^[^-]+-(.+)-[^-]+$/).exec(fullName);
    return match?.[1] || fullName;
  }

  return (
    <div className="select-none">
      <div
        ref={selectedNodeId === node.id ? selectedNodeRef : undefined}
        className={`flex items-center py-1 px-1 gap-1 rounded cursor-pointer text-xs hover:bg-gray-100 ${selectedNodeId === node.id ? "bg-blue-50 text-blue-700" : ""
          }`}
        style={{ paddingLeft: `${node.level * 12 + 4}px` }}
        onClick={handleSelect}
      >
        {hasChildren ? (
          <span onClick={handleToggle} className="mr-1 flex items-center justify-center w-4 h-4">
            {isExpanded ? <ChevronDown size={14} /> : <ChevronRight size={14} />}
          </span>
        ) : (
          <span className="mr-1 w-4" />
        )}

        {getNodeStatusIcon()}
        <span className="truncate">{highlightMatch(extractNodeName(node.label), searchTerm)}</span>
      </div>

      {isExpanded && hasChildren && (
        <div>
          {node.children.map((child) => (
            <TreeNodeComponent
              key={child.id}
              node={child}
              onNodeSelect={onNodeSelect}
              selectedNodeId={selectedNodeId}
              searchTerm={searchTerm}
              selectedNodeRef={selectedNodeRef}
            />
          ))}
        </div>
      )}
    </div>
  )
}
