"use client"

import { Play } from "lucide-react"
import { useEffect } from "react"
import SidebarTabs from "@/components/sidebar/sidebar-tabs"
import ScheduledWfRunsTab from "@/components/sidebar/scheduled-wf-runs-tab"
import WfRunTabSimple from "@/components/sidebar/wf-run-tab-simple"
import WfSpecTab from "@/components/sidebar/wf-spec-tab"
import SidebarExpandButton from "@/components/ui/sidebar-expand-button"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { SelectionProvider, useSelection } from "@/components/context/selection-context"
import { useWorkflow } from "../context/workflow-context"

interface LeftSidebarProps {
  activeTab: string
  setActiveTab: (tab: string) => void
  isExpanded: boolean
  setExpanded: (expanded: boolean) => void
  onExpandRequest: () => void
  selectedNodeId?: string
  onNodeSelect: (nodeId: string) => void
}

export default function LeftSidebar({
  activeTab,
  setActiveTab,
  isExpanded,
  setExpanded,
  onExpandRequest,
  selectedNodeId,
  onNodeSelect,
}: LeftSidebarProps) {
  const { wfSpec, wfRunIds: wfRuns, wfRun, isLoading, error } = useWorkflow()

  const handleExpandToggle = () => {
    if (isExpanded) {
      setExpanded(false)
    } else {
      onExpandRequest()
    }
  }

  // Auto-expand when switching to WfRuns or ScheduledWfRuns tabs
  // Auto-shrink when switching away from these tabs
  useEffect(() => {
    if (activeTab === "WfRuns" || activeTab === "ScheduledWfRuns") {
      if (!isExpanded) {
        onExpandRequest()
      }
    } else if (isExpanded) {
      // Only auto-shrink if we're coming from one of the table tabs
      const previousTab = localStorage.getItem("previousTab")
      if (previousTab === "WfRuns" || previousTab === "ScheduledWfRuns") {
        setExpanded(false)
      }
    }

    // Store the current tab for reference
    localStorage.setItem("previousTab", activeTab)
  }, [activeTab, isExpanded, onExpandRequest, setExpanded])

  return (
    <aside
      className={`flex flex-col border-r border-gray-200 bg-white transition-all duration-300 ease-in-out ${isExpanded ? "w-full md:w-4/5 lg:w-1/2" : "w-[250px]"
        } relative`}
    >
      {/* Expand/Collapse Button */}
      <div className="z-20 absolute -right-3 top-3">
        <SidebarExpandButton isExpanded={isExpanded} onClick={handleExpandToggle} position="right" />
      </div>

      {/* Workflow Information */}
      <div className="border-b border-gray-200 p-4">
        <div className="mb-1">
          <span className="font-medium">{wfSpec?.id?.name}</span>
        </div>
        <div className="flex items-center">
          <span className="text-sm text-[#656565]">{`v${wfSpec?.id?.majorVersion}.${wfSpec?.id?.revision}`}</span>
          <Badge
            variant="outline"
            className={`ml-3 ${wfRun?.wfRun.status === "COMPLETED"
              ? "text-emerald-600 bg-emerald-100"
              : wfRun?.wfRun.status === "RUNNING"
                ? "text-blue-600 bg-blue-100"
                : "text-emerald-600 bg-emerald-100"
              } hover:bg-[#c5d0ff]/90`}
          >
            {wfRun?.wfRun.status}
          </Badge>
        </div>
      </div>

      {/* Tabs */}
      <SidebarTabs activeTab={activeTab} setActiveTab={setActiveTab} />

      {/* Tab Content */}
      <SelectionProvider>
        <TabContent activeTab={activeTab} onNodeSelect={onNodeSelect} selectedNodeId={selectedNodeId} />
      </SelectionProvider>

      {/* Run Workflow Button */}
      <div className="border-t border-gray-200 p-4 mt-auto">
        <Button className="w-full bg-[#3b81f5] hover:bg-[#3b81f5]/90">
          <Play className="mr-2 h-4 w-4" />
          Run Workflow
        </Button>
      </div>
    </aside>
  )
}

// Separate component for tab content to use the selection context
function TabContent({
  activeTab,
  onNodeSelect,
  selectedNodeId,
}: {
  activeTab: string
  onNodeSelect: (nodeId: string) => void
  selectedNodeId?: string
}) {
  const { selectedId, setSelectedId: _ } = useSelection()

  // Sync selection with parent component when needed

  return (
    <div className="flex flex-1 flex-col overflow-hidden">
      {activeTab === "WfSpec" && (
        <div className="flex flex-col flex-1 overflow-hidden">
          <div className="border-b border-gray-200 p-2 text-xs text-[#656565]">
            <span className="font-medium">Workflow Specification</span>
          </div>
          <WfSpecTab onNodeSelect={onNodeSelect} selectedNodeId={selectedNodeId} />
        </div>
      )}

      {activeTab === "WfRuns" && (
        <div className="flex flex-col flex-1 overflow-hidden">
          <div className="border-b border-gray-200 p-2 text-xs text-[#656565]">
            <span className="font-medium">Workflow Runs</span>
          </div>
          <WfRunTabSimple />
        </div>
      )}

      {activeTab === "ScheduledWfRuns" && (
        <div className="flex flex-col flex-1 overflow-hidden">
          <div className="border-b border-gray-200 p-2 text-xs text-[#656565]">
            <span className="font-medium">Scheduled Workflow Runs</span>
          </div>
          <ScheduledWfRunsTab />
        </div>
      )}
    </div>
  )
}
