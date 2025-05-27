"use client"

import { useState, useEffect } from "react"
import { Clock, CheckCircle, XCircle, Loader2, MoreHorizontal } from "lucide-react"
import SidebarExpandButton from "@/components/ui/sidebar-expand-button"
import { initialNodes } from "@/components/flow/mock-data"
import ExpandableText from "@/components/ui/expandable-text"
import VariableDisplay from "@/components/ui/variable-display"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Accordion, AccordionContent, AccordionItem, AccordionTrigger } from "@/components/ui/accordion"
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from "@/components/ui/dropdown-menu"
import { Badge } from "@/components/ui/badge"
import { Separator } from "@/components/ui/separator"
import { useNodes, useReactFlow, Node } from "reactflow"
import { NodeRun, Node as LHNode, LHStatus } from "littlehorse-client/proto"
import { useWorkflow } from "../context/workflow-context"
import { getNodeType } from "../flow/node-utils"
import { useExecuteRPCWithSWR } from "@/hooks/useExecuteRPCWithSWR"

// Define type for node data to match the data structure
interface NodeData {
  label?: string;
  status?: string;
  taskRun?: {
    timeout?: number;
    maxAttempts?: number;
    inputVariables?: Array<{
      name: string;
      type: string;
      value: string;
    }>;
  };
}

interface RightSidebarProps {
  isExpanded: boolean
  setExpanded: (expanded: boolean) => void
  nodeName?: string
  nodeId?: string
  onExpandRequest: () => void
}

export default function RightSidebar({
  isExpanded,
  setExpanded,
  nodeName = "ProcessPayment",
  nodeId,
  onExpandRequest,
}: RightSidebarProps) {
  const [activeTab, setActiveTab] = useState("runs")
  const [selectedAttempt, setSelectedAttempt] = useState<string | null>(null)
  const { wfSpec, wfRun } = useWorkflow();

  // Use React Flow hook at the top level component with error catching
  const nodes = useNodes<NodeRun>();
  const reactFlowInstance = useReactFlow();

  const handleExpandToggle = () => {
    if (isExpanded) {
      setExpanded(false)
    } else {
      onExpandRequest()
    }
  }

  // Find the selected node data
  const selectedNode = nodeId ? nodes.find((node) => node.id === nodeId) : null
  const nodeDef = Object.entries(wfSpec?.threadSpecs?.[wfSpec.entrypointThreadName].nodes ?? {}).find(([nodeName, node]) => nodeName === selectedNode?.data.nodeName)?.[1]
  const taskRun = useExecuteRPCWithSWR("getTaskRun", {
    wfRunId: wfRun?.wfRun.id,
    taskGuid: selectedNode?.data.task?.taskRunId?.taskGuid!
  })
  const [selectedNodeRun, setSelectedNodeRun] = useState<string | null>(selectedNode?.data.nodeName!)
  useEffect(() => {
    setSelectedNodeRun(selectedNode?.data.nodeName!)
  }, [selectedNode])
  console.log("selectedNodeRun", selectedNodeRun)
  // Format dates for display - more compact format
  const formatDate = (dateString: string) => {
    if (!dateString) return "In progress"
    const date = new Date(dateString)
    return (
      date.toLocaleTimeString() +
      " " +
      date.toLocaleDateString(undefined, { month: "numeric", day: "numeric", year: "2-digit" })
    )
  }

  // Calculate time difference in milliseconds
  const calculateDuration = (startDate: string, endDate: string) => {
    return Math.floor(Math.random() * 4) + 1
  }

  // Get status icon based on status string
  const getStatusIcon = (status: string) => {
    if (status.includes("SUCCESS") || status.includes("COMPLETED")) {
      return <CheckCircle className="h-3 w-3 text-green-500 mr-1" />
    } else if (status.includes("FAILURE") || status.includes("ERROR") || status.includes("FAILED")) {
      return <XCircle className="h-3 w-3 text-red-500 mr-1" />
    } else if (status.includes("RUNNING")) {
      return <Loader2 className="h-3 w-3 text-blue-500 animate-spin mr-1" />
    } else {
      return <Clock className="h-3 w-3 text-[#656565] mr-1" />
    }
  }

  // Get status badge based on status
  const getStatusBadge = (status: string) => {
    if (status.includes("SUCCESS") || status.includes("COMPLETED")) {
      return <Badge className="bg-green-100 text-green-800 hover:bg-green-100">COMPLETED</Badge>
    } else if (status.includes("FAILURE") || status.includes("ERROR") || status.includes("FAILED")) {
      return <Badge className="bg-red-100 text-red-800 hover:bg-red-100">FAILED</Badge>
    } else if (status.includes("RUNNING")) {
      return <Badge className="bg-blue-100 text-blue-800 hover:bg-blue-100">RUNNING</Badge>
    } else {
      return <Badge className="bg-gray-100 text-gray-800 hover:bg-gray-100">HALTED</Badge>
    }
  }

  // Find the currently selected TaskAttempt
  return (
    <aside
      className={`flex flex-col border-l border-gray-200 bg-white transition-all duration-300 ease-in-out ${isExpanded ? "w-full md:w-3/4 lg:w-1/2" : "w-[280px]"
        } relative`}
    >
      {/* Expand/Collapse Button - Increased z-index */}
      <div className="z-20 absolute -left-3 top-3">
        <SidebarExpandButton isExpanded={isExpanded} onClick={handleExpandToggle} position="left" />
      </div>

      {/* Node Properties Header - Fixed at top */}
      <div className="border-b border-gray-200 p-4 sticky top-0 bg-white z-10">
        <div className="text-xs text-[#656565]">
          <span className="font-medium">Node Details</span>
        </div>
        <div className="mb-1 font-medium flex items-center justify-between">
          <div className="flex items-center">
            {selectedNode?.data?.nodeName}
            {getNodeType(selectedNode?.data.nodeName ?? "") === "TASK" && <Badge className="ml-2 bg-blue-100 text-blue-800 hover:bg-blue-100">Task</Badge>}
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

      {/* Tabs for different views */}
      <Tabs value={activeTab} onValueChange={setActiveTab} className="flex-1 flex flex-col">
        <TabsList className="px-4 pt-2 bg-white sticky top-[73px] z-10 justify-start">
          <TabsTrigger value="runs" className="text-xs">
            Runs
          </TabsTrigger>
          <TabsTrigger value="definition" className="text-xs">
            Definition
          </TabsTrigger>
        </TabsList>

        {/* Runs Tab Content */}
        <TabsContent
          value="runs"
          className="flex-1 overflow-y-auto p-0 data-[state=active]:flex data-[state=active]:flex-col"
        >
          <div className="p-4">
            <h3 className="text-sm font-medium mb-2">NodeRuns</h3>

            {/* NodeRuns List */}
            <div className="space-y-2">
              {nodes.filter(nodeRun => nodeRun.data.nodeName === selectedNode?.data.nodeName).map((nodeRun) => (
                <div
                  key={nodeRun.id}
                  className={`border rounded-md p-3 cursor-pointer transition-colors ${selectedNodeRun === nodeRun.id ? "border-blue-500 bg-blue-50" : "border-gray-200 hover:bg-gray-50"
                    }`}
                  onClick={() => setSelectedNodeRun(nodeRun.id)}
                >
                  <div className="flex items-center justify-between mb-1">
                    <div className="flex items-center">
                      {getStatusIcon(nodeRun.data.status)}
                      <span className="text-xs font-medium">{nodeRun.id}</span>
                    </div>
                    {getStatusBadge(nodeRun.data.status)}
                  </div>
                  <div className="space-y-1 text-xs">
                    <div className="flex justify-between">
                      <span className="text-[#656565]">Started:</span>
                      <span>{formatDate(nodeRun.data.arrivalTime ?? "")}</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-[#656565]">Duration:</span>
                      <span>{nodeRun.data.status === LHStatus.HALTED ? "N/A" : calculateDuration(nodeRun.data.arrivalTime ?? "", nodeRun.data.endTime ?? "")} ms</span>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>

          <Separator />

          {/* Selected NodeRun Details */}
          {selectedNode && selectedNode?.data.task && (
            <div className="p-4">
              <div className="flex items-center justify-between mb-2">
                <h3 className="text-sm font-medium">TaskRun</h3>
              </div>

              <div className="space-y-2 text-xs mb-4">
                <div className="flex justify-between items-center">
                  <span className="text-[#656565]">Status:</span>
                  <div className="flex items-center">
                    {getStatusIcon(selectedNode.data.status)}
                    <span>{taskRun?.data?.status}</span>
                  </div>
                </div>
                <div className="flex justify-between items-center">
                  <span className="text-[#656565]">Timeout:</span>
                  <span>{nodeDef?.task?.timeoutSeconds} s</span>
                </div>
                <div className="flex justify-between items-center">
                  <span className="text-[#656565]">Max Attempts:</span>
                  <span>{nodeDef?.task?.retries}</span>
                </div>
              </div>

              <h4 className="text-xs font-medium mb-2">Task Attempts ({nodes.filter(nodeRun => nodeRun.data.nodeName === selectedNode?.data.nodeName).length})</h4>

              {/* Task Attempts Accordion */}
              <Accordion type="single" collapsible className="w-full">
                {nodes.filter(nodeRun => nodeRun.data.nodeName === selectedNode?.data.nodeName).map((nodeRun) => (
                  <AccordionItem key={nodeRun.id} value={nodeRun.id}>
                    <AccordionTrigger
                      className={`text-xs py-2 ${selectedAttempt === nodeRun.id ? "text-blue-600" : ""}`}
                      onClick={() => setSelectedAttempt(nodeRun.id)}
                    >
                      <div className="flex items-center">
                        {getStatusIcon(nodeRun.data.status)}
                        <span>Attempt {1}</span>
                      </div>
                    </AccordionTrigger>
                    <AccordionContent>
                      <div className="space-y-2 text-xs pl-2">
                        <div className="flex justify-between">
                          <span className="text-[#656565]">Started:</span>
                          <span>{formatDate(nodeRun.data.arrivalTime ?? "")}</span>
                        </div>
                        <div className="flex justify-between">
                          <span className="text-[#656565]">Duration:</span>
                          <span>{calculateDuration(nodeRun.data.arrivalTime ?? "", nodeRun.data.endTime ?? "")} ms</span>
                        </div>
                        <div>
                          <div className="text-[#656565] mb-1">Result:</div>
                          <ExpandableText text={String(taskRun?.data?.attempts?.[0]?.output?.str || "")} isCode={true} maxLength={100} />
                        </div>
                        <div>
                          <div className="text-[#656565] mb-1">Worker Log:</div>
                          <ExpandableText text={String(taskRun?.data?.attempts?.[0]?.logOutput ?? "")} isCode={true} maxLength={100} />
                        </div>
                      </div>
                    </AccordionContent>
                  </AccordionItem>
                ))}
              </Accordion>
            </div>
          )}
        </TabsContent>

        {/* Definition Tab Content */}
        <TabsContent value="definition" className="flex-1 overflow-y-auto p-4">
          <h3 className="text-sm font-medium mb-2">Node Definition</h3>

          {getNodeType(selectedNode?.data.nodeName ?? "") === "TASK" ? (
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
                    <span className="font-mono">{nodeDef?.task?.timeoutSeconds} s</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-[#656565]">Retries:</span>
                    <span className="font-mono">{nodeDef?.task?.retries}</span>
                  </div>
                </div>
              </div>

              <div className="bg-gray-50 rounded-md p-3 border border-gray-200">
                <h4 className="text-xs font-medium mb-2">Input Variables</h4>
                <div className="space-y-1">
                  {/* {taskRun?.?.inputVariables?.map((variable, index) => (
                    <div key={index} className="text-xs font-mono">
                      <span className="text-purple-600">{variable.type}</span>{" "}
                      <span className="text-blue-600">{variable.name}</span>
                    </div>
                  ))} */}
                </div>
              </div>

              <div className="bg-gray-50 rounded-md p-3 border border-gray-200">
                <h4 className="text-xs font-medium mb-2">Output Type</h4>
                <div className="text-xs font-mono">
                  <span className="text-purple-600">{ }</span> <span className="text-blue-600">result</span>
                </div>
              </div>
            </div>
          ) : (
            <div className="text-xs text-gray-500 italic">Definition information is only available for task nodes.</div>
          )}
        </TabsContent>
      </Tabs>

      {/* If no node is selected */}
      {(!nodeId || !selectedNode?.data.nodeName) && (
        <div className="p-4 text-[#656565] text-xs italic flex-1 flex items-center justify-center">
          Select a node to view details
        </div>
      )}
    </aside>
  )
}
