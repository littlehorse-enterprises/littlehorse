"use client"

import { useEffect, useState } from "react"
import SidebarExpandButton from "@/components/ui/sidebar-expand-button"
import ExpandableText from "@/components/ui/expandable-text"
import VariableDisplay from "@/components/ui/variable-display"
import { Accordion, AccordionContent, AccordionItem, AccordionTrigger } from "@/components/ui/accordion"
import { Badge } from "@/components/ui/badge"
import { Separator } from "@/components/ui/separator"
import { useSelection } from "@/components/context/selection-context"
import { NodeRun, WfSpec, TaskRun, TaskStatus } from "littlehorse-client/proto"
import { getNodeType } from "@/utils/ui/node-utils"
import { formatDate, getStatusIcon, getStatusBadge, getTaskStatusIcon, calculateDuration } from "@/utils/ui/status-utils"
import { getVariableValue } from "@/utils/data/variables"

type SidebarState = "hidden" | "normal" | "expanded"

interface RightSidebarProps {
    wfSpec: WfSpec
    nodeRuns: NodeRun[]
    taskRuns: TaskRun[]
}



export default function RightSidebar({
    wfSpec,
    nodeRuns,
    taskRuns,
}: RightSidebarProps) {
    const [activeTab, setActiveTab] = useState(nodeRuns.length > 0 ? "runs" : "definition")
    const [sidebarState, setSidebarState] = useState<SidebarState>("normal")
    const [selectedAttempt, setSelectedAttempt] = useState<string | null>(null)
    const [selectedNodeRun, setSelectedNodeRun] = useState<NodeRun | null>(null)
    const { selectedId } = useSelection()

    // Find the selected node data
    const selectedNodeRuns = nodeRuns.filter(nodeRun => nodeRun.nodeName === selectedId)
    const nodeDef = wfSpec?.threadSpecs?.[wfSpec.entrypointThreadName]?.nodes?.[selectedId || ""]

    // Find TaskRuns that correspond to the selected specific node run (not all node runs)
    const selectedTaskRuns = selectedNodeRun ? taskRuns.filter(taskRun =>
        taskRun.source?.taskNode?.nodeRunId?.wfRunId?.id === selectedNodeRun.id?.wfRunId?.id &&
        taskRun.source?.taskNode?.nodeRunId?.position === selectedNodeRun.id?.position
    ) : []


    useEffect(() => {
        setActiveTab(nodeRuns.length > 0 ? "runs" : "definition")
        // Reset selected node run when selected node changes
        setSelectedNodeRun(null)
    }, [nodeRuns, selectedId])

    // Auto-select first node run by default
    useEffect(() => {
        if (selectedNodeRuns.length > 0 && !selectedNodeRun) {
            setSelectedNodeRun(selectedNodeRuns[0])
        }
    }, [selectedNodeRuns, selectedNodeRun])

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

                    </div>
                </div>

                {/* Tab Navigation */}
                <div className="border-b border-gray-200 px-4 pt-2 bg-white sticky top-[73px] z-10">
                    <div className="flex space-x-4">
                        {nodeRuns.length > 0 && <button
                            onClick={() => setActiveTab("runs")}
                            className={`pb-2 text-xs font-medium border-b-2 transition-colors ${activeTab === "runs"
                                ? "border-blue-500 text-blue-600"
                                : "border-transparent text-gray-500 hover:text-gray-700"
                                }`}
                        >
                            Runs
                        </button>}
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
                        <div className="p-4 flex flex-col h-full">
                            <h3 className="text-sm font-medium mb-2">NodeRuns</h3>

                            {/* NodeRuns List */}
                            <div className="max-h-1/3 h-fit overflow-y-auto">
                                <div className="space-y-2">
                                    {selectedNodeRuns.map((nodeRun, index) => (
                                        <div
                                            key={nodeRun.id?.wfRunId?.id + "-" + nodeRun.id?.position}
                                            className={`border rounded-md p-3 cursor-pointer transition-colors ${selectedNodeRun?.id?.wfRunId?.id === nodeRun.id?.wfRunId?.id &&
                                                selectedNodeRun?.id?.position === nodeRun.id?.position
                                                ? "border-blue-500 bg-blue-50"
                                                : "border-gray-200 hover:bg-gray-50"
                                                }`}
                                            onClick={() => setSelectedNodeRun(nodeRun)}
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
                                                    <span className="text-[#656565]">Duration:</span>
                                                    <span>{calculateDuration(nodeRun.arrivalTime, nodeRun.endTime)}</span>
                                                </div>
                                            </div>
                                        </div>
                                    ))}
                                </div>
                            </div>

                            {selectedNodeRuns.length === 0 && (
                                <div className="text-xs text-gray-500 italic">
                                    No runs found for this node
                                </div>
                            )}

                            {/* TaskRun Details Section */}
                            {selectedTaskRuns.length > 0 && getNodeType(selectedId || "") === "TASK" && selectedNodeRun && (
                                <div className="flex-shrink-0">
                                    <Separator className="my-4" />
                                    <div>
                                        <div className="flex items-center justify-between mb-2">
                                            <h3 className="text-sm font-medium">TaskRun</h3>
                                        </div>

                                        <div className="space-y-2 text-xs mb-4">
                                            <div className="flex justify-between items-center">
                                                <span className="text-[#656565]">Status:</span>
                                                <div className="flex items-center">
                                                    {getTaskStatusIcon(selectedTaskRuns[0].status)}
                                                    <span>{TaskStatus[selectedTaskRuns[0].status]}</span>
                                                </div>
                                            </div>
                                            <div className="flex justify-between items-center">
                                                <span className="text-[#656565]">Timeout:</span>
                                                <span>{selectedTaskRuns[0].timeoutSeconds || "N/A"} s</span>
                                            </div>
                                            <div className="flex justify-between items-center">
                                                <span className="text-[#656565]">Max Attempts:</span>
                                                <span>{selectedTaskRuns[0].totalAttempts || "N/A"}</span>
                                            </div>
                                        </div>

                                        <h4 className="text-xs font-medium mb-2">Task Attempts ({selectedTaskRuns[0].attempts?.length || 0})</h4>

                                        {/* Task Attempts Accordion */}
                                        <Accordion type="single" collapsible className="w-full" defaultValue={selectedTaskRuns[0].id?.wfRunId?.id + "-" + 0}>
                                            {selectedTaskRuns[0].attempts?.map((attempt, index) => (
                                                <AccordionItem key={selectedTaskRuns[0].id?.wfRunId?.id + "-" + index} value={selectedTaskRuns[0].id?.wfRunId?.id + "-" + index || ""}>
                                                    <AccordionTrigger
                                                        className={`text-xs py-2 ${selectedAttempt === (selectedTaskRuns[0].id?.wfRunId?.id + "-" + index) ? "text-blue-600" : ""}`}
                                                        onClick={() => setSelectedAttempt(selectedTaskRuns[0].id?.wfRunId?.id + "-" + index || "")}
                                                    >
                                                        <div className="flex items-center">
                                                            {getTaskStatusIcon(attempt.status)}
                                                            <span>Attempt {index + 1}</span>
                                                        </div>
                                                    </AccordionTrigger>
                                                    <AccordionContent>
                                                        <div className="space-y-2 text-xs pl-2">
                                                            <div className="flex justify-between">
                                                                <span className="text-[#656565]">Started:</span>
                                                                <span>{formatDate(attempt.startTime)}</span>
                                                            </div>
                                                            <div className="flex justify-between">
                                                                <span className="text-[#656565]">Duration:</span>
                                                                <span>{calculateDuration(attempt.startTime, attempt.endTime)}</span>
                                                            </div>
                                                            <div>
                                                                <div className="text-[#656565] mb-1">Result:</div>
                                                                <ExpandableText
                                                                    text={attempt.error ? JSON.stringify(attempt.error, null, 2) : attempt.exception ? JSON.stringify(attempt.exception, null, 2) : attempt.output ? getVariableValue(attempt.output)?.toString() : "No result"}
                                                                    isCode={true}
                                                                    maxLength={100}
                                                                />
                                                            </div>
                                                            <div>
                                                                <div className="text-[#656565] mb-1">Log Output:</div>
                                                                <ExpandableText
                                                                    text={attempt.logOutput ? getVariableValue(attempt.logOutput)?.toString() : "No log output"}
                                                                    isCode={true}
                                                                    maxLength={100}
                                                                />
                                                            </div>
                                                        </div>
                                                    </AccordionContent>
                                                </AccordionItem>
                                            ))}
                                        </Accordion>
                                    </div>
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
                                            {/* Display task variables if available */}
                                            {nodeDef?.task?.variables && Object.entries(nodeDef.task.variables).map(([key, variable]) => (
                                                <VariableDisplay
                                                    key={key}
                                                    name={key}
                                                    type={variable.variableName || "UNKNOWN"}
                                                    value={variable.jsonPath || variable.literalValue || "N/A"}
                                                />
                                            ))}
                                        </div>
                                    </div>

                                    <div className="bg-gray-50 rounded-md p-3 border border-gray-200">
                                        <h4 className="text-xs font-medium mb-2">Output Type</h4>
                                        <div className="text-xs font-mono">
                                            <span className="text-purple-600">Object</span>{" "}
                                            <span className="text-blue-600">result</span>
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