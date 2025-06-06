"use client"

import { TreeNode } from "@/types/buildNodeTree"
import { CheckCircle, ChevronDown, ChevronRight, Loader2, XCircle } from "lucide-react"
import type React from "react"
import { useState, useEffect, useMemo } from "react"
import { useSelection } from "../context/selection-context"

interface TreeNodeComponentProps {
    node: TreeNode
    isRoot?: boolean
    searchTerm: string
}

export function TreeNodeComponent({
    node,
    isRoot = false,
    searchTerm,
}: TreeNodeComponentProps) {
    const [isExpanded, setIsExpanded] = useState(isRoot)
    const hasChildren = node.children && node.children.length > 0
    const { selectedId, setSelectedId } = useSelection()
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
            const childMatches = node.children?.some(child =>
                child.label.toLowerCase().includes(searchTerm.toLowerCase())
            )
            if (nodeMatches || childMatches) {
                setIsExpanded(true)
            }
        }
    }, [searchTerm, node, isExpanded])

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
            // return getNodeIcon(nodeType, "sm")
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

    const handleClick = () => {
        setSelectedId(node.id)
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
                    )
                )}
            </>
        )
    }

    return (
        <div className="select-none">
            <div
                className={`flex items-center py-1.5 px-2 gap-1.5 rounded-md cursor-pointer text-sm transition-colors
                    ${isSelected ? "bg-blue-100 text-blue-900 ring-1 ring-blue-400" : "hover:bg-gray-100"}
                    ${searchTerm && node.label.toLowerCase().includes(searchTerm.toLowerCase()) ? "bg-yellow-50" : ""}`}
                style={{ paddingLeft: `${node.level * 12 + 4}px` }}
                onClick={handleClick}
            >
                {hasChildren && (
                    <button
                        onClick={handleToggle}
                        className="p-0.5 hover:bg-gray-200 rounded transition-colors"
                    >
                        {isExpanded ? (
                            <ChevronDown className="h-4 w-4 text-gray-500" />
                        ) : (
                            <ChevronRight className="h-4 w-4 text-gray-500" />
                        )}
                    </button>
                )}
                {!hasChildren && <div className="w-5" />}
                {getNodeStatusIcon()}
                <div className="flex-1 truncate">
                    {searchTerm ? highlightMatch(node.label, searchTerm) : node.label}
                </div>
            </div>

            {isExpanded && hasChildren && (
                <div>
                    {node.children.map((child) => (
                        <TreeNodeComponent
                            key={child.id}
                            node={child}
                            searchTerm={searchTerm}
                        />
                    ))}
                </div>
            )}
        </div>
    )
}
