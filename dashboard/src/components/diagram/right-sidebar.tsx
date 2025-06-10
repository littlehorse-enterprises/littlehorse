"use client"

import { useState } from "react"
import { Clock, CheckCircle, XCircle, Loader2, MoreHorizontal } from "lucide-react"
import SidebarExpandButton from "@/components/ui/sidebar-expand-button"
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from "@/components/ui/dropdown-menu"
import { Badge } from "@/components/ui/badge"
import { useSelection } from "@/components/context/selection-context"
import { NodeRun, WfSpec, LHStatus } from "littlehorse-client/proto"
import { getNodeType } from "@/utils/ui/node-utils"

type SidebarState = "hidden" | "normal" | "expanded"

interface RightSidebarProps {
    wfSpec?: WfSpec
    nodeRuns?: NodeRun[]
}

export default function RightSidebar({
    wfSpec,
    nodeRuns = [],
}: RightSidebarProps) {
    const [activeTab, setActiveTab] = useState("runs")
    const [sidebarState, setSidebarState] = useState<SidebarState>("normal")
    const { selectedId } = useSelection()

    // Find the selected node data
    const selectedNodeRuns = nodeRuns.filter(nodeRun => nodeRun.nodeName === selectedId)
    const nodeDef = wfSpec?.threadSpecs?.[wfSpec.entrypointThreadName]?.nodes?.[selectedId || ""]

    // Format dates for display - more compact format
    function formatDate(timestamp: any): string {
        if (!timestamp) return "In progress"
        try {
            const date = timestamp.seconds
                ? new Date(timestamp.seconds * 1000 + (timestamp.nanos || 0) / 1000000)
                : new Date(timestamp)
            return (
                date.toLocaleTimeString() +
                " " +
                date.toLocaleDateString(undefined, { month: "numeric", day: "numeric", year: "2-digit" })
            )
        } catch {
            return "Invalid date"
        }
    }

    // Get status icon based on status string
    function getStatusIcon(status: LHStatus) {
        if (status === LHStatus.COMPLETED) {
            return <CheckCircle className="h-3 w-3 text-green-500 mr-1" />
        } else if (status === LHStatus.ERROR || status === LHStatus.EXCEPTION) {
            return <XCircle className="h-3 w-3 text-red-500 mr-1" />
        } else if (status === LHStatus.RUNNING) {
            return <Loader2 className="h-3 w-3 text-blue-500 animate-spin mr-1" />
        } else {
            return <Clock className="h-3 w-3 text-[#656565] mr-1" />
        }
    }

    // Get status badge based on status
    function getStatusBadge(status: LHStatus) {
        if (status === LHStatus.COMPLETED) {
            return <Badge variant="secondary" className="bg-green-100 text-green-800 hover:bg-green-100">COMPLETED</Badge>
        } else if (status === LHStatus.ERROR || status === LHStatus.EXCEPTION) {
            return <Badge variant="destructive" className="bg-red-100 text-red-800 hover:bg-red-100">FAILED</Badge>
        } else if (status === LHStatus.RUNNING) {
            return <Badge variant="secondary" className="bg-blue-100 text-blue-800 hover:bg-blue-100">RUNNING</Badge>
        } else {
            return <Badge variant="outline" className="bg-gray-100 text-gray-800 hover:bg-gray-100">HALTED</Badge>
        }
    }

    // Sidebar width based on state
    const sidebarWidth = sidebarState === "expanded"
        ? "w-full md:w-4/5 lg:w-1/2"
        : sidebarState === "normal"
            ? "w-[280px]"
            : "w-0 min-w-0"

    return (
        <aside
            className={`h-full flex flex-col border-l border-gray-200 bg-white transition-all duration-300 ease-in-out relative ${sidebarWidth}`}
            style={{ minWidth: sidebarState === "hidden" ? 0 : undefined }}
        >
            {/* Handle/Show button when hidden */}
            {sidebarState === "hidden" && (
                <div className="absolute right-0 top-3 z-30 h-8 w-6 flex items-center justify-center">
                    <SidebarExpandButton isExpanded={false} onClick={() => setSidebarState("normal")} position="left" />
                </div>
            )}

            {/* Sidebar content (hidden when sidebar is hidden) */}
            <div className={`flex flex-col h-full ${sidebarState === "hidden" ? "opacity-0 pointer-events-none select-none" : "opacity-100"}`}>
                {/* Expand/Collapse/Hide Buttons */}
                {sidebarState !== "hidden" && (
                    <div className="z-20 absolute -left-3 top-3 flex flex-col gap-2 items-center">
                        <SidebarExpandButton
                            isExpanded={sidebarState === "expanded"}
                            onClick={() => setSidebarState(sidebarState === "expanded" ? "normal" : "expanded")}
                            position="left"
                        />
                        <button
                            className="bg-white border border-gray-200 rounded-full shadow p-1 hover:bg-gray-50 transition"
                            onClick={() => setSidebarState("hidden")}
                            aria-label="Hide sidebar"
                        >
                            {/* Use right chevron to indicate hide */}
                            <svg width="16" height="16" fill="none" viewBox="0 0 24 24"><path d="M9 18l6-6-6-6" stroke="#656565" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" /></svg>
                        </button>
                    </div>
                )}

                {/* Node Properties Header - Fixed at top */}
                <div className="border-b border-gray-200 p-4 sticky top-0 bg-white z-10">
                    <div className="text-xs text-[#656565]">
                        <span className="font-medium">Node Details</span>
                    </div>
                    <div className="mb-1 font-medium flex items-center justify-between">
                        <div className="flex items-center">
                            {selectedId}
                            {getNodeType(selectedId || "") === "TASK" && (
                                <Badge variant="secondary" className="ml-2 bg-blue-100 text-blue-800 hover:bg-blue-100">Task</Badge>
                            )}
                        </div>
                        <DropdownMenu>
                            <DropdownMenuTrigger asChild>
                                <button className="p-1 rounded-md hover:bg-gray-100">
                                    <MoreHorizontal className="h-4 w-4 text-gray-500" />
                                </button>
                            </DropdownMenuTrigger>
                            <DropdownMenuContent align="end">
                                <DropdownMenuItem>View Details</DropdownMenuItem>
                                <DropdownMenuItem>View in Graph</DropdownMenuItem>
                                <DropdownMenuItem>Copy ID</DropdownMenuItem>
                            </DropdownMenuContent>
                        </DropdownMenu>
                    </div>
                </div>

                {/* Tab Navigation */}
                <div className="border-b border-gray-200 px-4 pt-2 bg-white sticky top-[73px] z-10">
                    <div className="flex space-x-4">
                        <button
                            onClick={() => setActiveTab("runs")}
                            className={`pb-2 text-xs font-medium border-b-2 transition-colors ${activeTab === "runs"
                                ? "border-blue-500 text-blue-600"
                                : "border-transparent text-gray-500 hover:text-gray-700"
                                }`}
                        >
                            Runs
                        </button>
                        <button
                            onClick={() => setActiveTab("definition")}
                            className={`pb-2 text-xs font-medium border-b-2 transition-colors ${activeTab === "definition"
                                ? "border-blue-500 text-blue-600"
                                : "border-transparent text-gray-500 hover:text-gray-700"
                                }`}
                        >
                            Definition
                        </button>
                    </div>
                </div>

                {/* Tab Content */}
                <div className="flex-1 overflow-y-auto">
                    {activeTab === "runs" && (
                        <div className="p-4">
                            <h3 className="text-sm font-medium mb-2">NodeRuns</h3>

                            {/* NodeRuns List */}
                            <div className="space-y-2">
                                {selectedNodeRuns.map((nodeRun, index) => (
                                    <div
                                        key={nodeRun.id?.wfRunId?.id + "-" + nodeRun.id?.position}
                                        className="border rounded-md p-3 border-gray-200 hover:bg-gray-50"
                                    >
                                        <div className="flex items-center justify-between mb-1">
                                            <div className="flex items-center">
                                                {getStatusIcon(nodeRun.status)}
                                                <span className="text-xs font-medium">Run {index + 1}</span>
                                            </div>
                                            {getStatusBadge(nodeRun.status)}
                                        </div>
                                        <div className="space-y-1 text-xs">
                                            <div className="flex justify-between">
                                                <span className="text-[#656565]">Started:</span>
                                                <span>{formatDate(nodeRun.arrivalTime)}</span>
                                            </div>
                                            <div className="flex justify-between">
                                                <span className="text-[#656565]">Status:</span>
                                                <span>{LHStatus[nodeRun.status]}</span>
                                            </div>
                                        </div>
                                    </div>
                                ))}
                            </div>

                            {selectedNodeRuns.length === 0 && (
                                <div className="text-xs text-gray-500 italic">
                                    No runs found for this node
                                </div>
                            )}
                        </div>
                    )}

                    {activeTab === "definition" && (
                        <div className="p-4">
                            <h3 className="text-sm font-medium mb-2">Node Definition</h3>

                            {getNodeType(selectedId || "") === "TASK" ? (
                                <div className="space-y-4">
                                    <div className="bg-gray-50 rounded-md p-3 border border-gray-200">
                                        <h4 className="text-xs font-medium mb-2">Task Properties</h4>
                                        <div className="space-y-1 text-xs">
                                            <div className="flex justify-between">
                                                <span className="text-[#656565]">Type:</span>
                                                <span className="font-mono">TASK</span>
                                            </div>
                                            <div className="flex justify-between">
                                                <span className="text-[#656565]">Timeout:</span>
                                                <span className="font-mono">{nodeDef?.task?.timeoutSeconds || "N/A"} s</span>
                                            </div>
                                            <div className="flex justify-between">
                                                <span className="text-[#656565]">Retries:</span>
                                                <span className="font-mono">{nodeDef?.task?.retries || "N/A"}</span>
                                            </div>
                                        </div>
                                    </div>

                                    <div className="bg-gray-50 rounded-md p-3 border border-gray-200">
                                        <h4 className="text-xs font-medium mb-2">Input Variables</h4>
                                        <div className="space-y-1">
                                            {nodeDef?.task?.taskDefId && (
                                                <div className="text-xs font-mono">
                                                    <span className="text-purple-600">TaskDef:</span>{" "}
                                                    <span className="text-blue-600">{nodeDef.task.taskDefId.name}</span>
                                                </div>
                                            )}
                                        </div>
                                    </div>
                                </div>
                            ) : (
                                <div className="text-xs text-gray-500 italic">
                                    Definition information is only available for task nodes.
                                </div>
                            )}
                        </div>
                    )}
                </div>

                {/* If no node is selected */}
                {!selectedId && (
                    <div className="p-4 text-[#656565] text-xs italic flex-1 flex items-center justify-center">
                        Select a node to view details
                    </div>
                )}
            </div>
        </aside>
    )
} 