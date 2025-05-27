"use client"

import MainContent from "@/components/layout/main-content"
import LeftSidebar from "@/components/layout/left-sidebar"
import RightSidebar from "@/components/layout/right-sidebar"
import { useState, useEffect } from "react"
import { ReactFlowProvider } from "reactflow"
import { initialNodes } from "@/components/flow/mock-data"
import { useParams, useSearchParams } from "next/navigation"
import { getWorkflowSpecById } from "@/services/workflow-service"
import { WfSpecId } from "littlehorse-client/proto"
import { WorkflowProvider } from "@/components/context/workflow-context"

export default function DiagramPage() {
  const params = useParams()
  const wfSpecName = params.id as string
  const tenantId = "default"

  const searchParams = useSearchParams()
  const splitVersion = (searchParams.get("version") ?? "0.0").split(".")
  const wfSpecVersion: WfSpecId = {
    name: wfSpecName,
    majorVersion: parseInt(splitVersion[0]),
    revision: parseInt(splitVersion[1])
  }
  const wfRunId = searchParams.get("wfRunId") ?? ""

  const [activeTab, setActiveTab] = useState("WfSpec")
  const [isLeftSidebarExpanded, setLeftSidebarExpanded] = useState(false)
  const [isRightSidebarExpanded, setRightSidebarExpanded] = useState(false)
  const [selectedNodeId, setSelectedNodeId] = useState<string | undefined>(undefined)

  // State for workflow data
  const [_workflowData, setWorkflowData] = useState<any>(null)

  // Effect to handle loading workflow data based on ID
  useEffect(() => {
    if (wfSpecVersion) {
      console.log(`Loading workflow spec with ID: ${wfSpecVersion}`)

      // Get workflow data from service
      const data = getWorkflowSpecById(wfSpecVersion.name)

      if (data) {
        setWorkflowData(data)
        document.title = `LittleHorse - ${data.name}`

        // If we have custom nodes/edges, use them
        if (data.nodes && data.edges) {
          // In a real app, you would update the nodes and edges here
          console.log("Custom workflow data loaded")
        }
      }
    }
  }, [wfSpecVersion])

  // Get the selected node name from the node data
  const selectedNode = selectedNodeId ? initialNodes.find((node) => node.id === selectedNodeId) : null
  const selectedNodeName = selectedNode?.data?.label || "Select a node"

  // Handle expansion requests with mutual exclusivity
  const handleLeftSidebarExpandRequest = () => {
    if (isRightSidebarExpanded) {
      setRightSidebarExpanded(false)
    }
    setLeftSidebarExpanded(true)
  }

  const handleRightSidebarExpandRequest = () => {
    if (isLeftSidebarExpanded) {
      setLeftSidebarExpanded(false)
    }
    setRightSidebarExpanded(true)
  }

  // Handle node selection - removed auto-expansion
  const handleNodeSelect = (nodeId: string) => {
    setSelectedNodeId(nodeId)
    // Removed auto-expansion of right sidebar
  }

  return (
    <WorkflowProvider wfSpecVersion={wfSpecVersion} wfRunId={wfRunId}>
      <ReactFlowProvider>
        <div className="flex flex-1 overflow-hidden">
          <LeftSidebar
            activeTab={activeTab}
            setActiveTab={setActiveTab}
            isExpanded={isLeftSidebarExpanded}
            setExpanded={setLeftSidebarExpanded}
            onExpandRequest={handleLeftSidebarExpandRequest}
            selectedNodeId={selectedNodeId}
            onNodeSelect={handleNodeSelect}
          />
          <MainContent
            isLeftSidebarExpanded={isLeftSidebarExpanded}
            isRightSidebarExpanded={isRightSidebarExpanded}
            onNodeSelect={handleNodeSelect}
            selectedNodeId={selectedNodeId}
          />
          <RightSidebar
            isExpanded={isRightSidebarExpanded}
            setExpanded={setRightSidebarExpanded}
            nodeName={selectedNodeName}
            nodeId={selectedNodeId}
            onExpandRequest={handleRightSidebarExpandRequest}
          />
        </div>
      </ReactFlowProvider>
    </WorkflowProvider >
  )
}
