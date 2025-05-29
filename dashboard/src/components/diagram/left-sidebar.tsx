"use client"

import { Play } from "lucide-react"
import { useState } from "react"
import SidebarExpandButton from "@/components/ui/sidebar-expand-button"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { WfRun, WfSpec } from "littlehorse-client/proto"
import LeftSidebarTabs from "./left-sidebar-tabs"
import { LeftSidebarTabId } from "@/types/leftSidebarTabs"
import WfSpecTab from "./wf-spec-tab"

const tabDescriptions: Record<LeftSidebarTabId, string> = {
  WfSpec: "Workflow Specification",
  WfRuns: "Workflow Runs",
  ScheduledWfRuns: "Scheduled Workflow Runs",
}

interface LeftSidebarProps {
  wfSpec: WfSpec
  wfRun: WfRun
}

export default function LeftSidebar({
  wfSpec,
  wfRun,
}: LeftSidebarProps) {
  const [activeTab, setActiveTab] = useState<LeftSidebarTabId>("WfSpec")
  const [isExpanded, setExpanded] = useState(false)

  return (
    <div
      className={`h-full flex flex-col border-r border-gray-200 bg-white transition-all duration-300 ease-in-out ${isExpanded ? "w-full md:w-4/5 lg:w-1/2" : "w-[250px]"
        } relative`}
    >
      {/* Expand/Collapse Button */}
      <div className="z-20 absolute -right-3 top-3">
        <SidebarExpandButton isExpanded={isExpanded} onClick={() => setExpanded(!isExpanded)} position="right" />
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
            className={`ml-3 ${wfRun?.status === "COMPLETED"
              ? "text-emerald-600 bg-emerald-100"
              : wfRun?.status === "RUNNING"
                ? "text-blue-600 bg-blue-100"
                : "text-emerald-600 bg-emerald-100"
              } hover:bg-[#c5d0ff]/90`}
          >
            {wfRun?.status}
          </Badge>
        </div>
      </div>

      {/* Tabs */}
      <LeftSidebarTabs activeTab={activeTab} setActiveTab={setActiveTab} />

      {/* Tab Content */}
      <div>
        <div className="flex flex-col flex-1 overflow-hidden">
          <div className="border-b border-gray-200 p-2 text-xs text-[#656565]">
            <span className="font-medium">{tabDescriptions[activeTab]}</span>
          </div>
          {activeTab === "WfSpec" && (
            <WfSpecTab wfSpec={wfSpec} wfRun={wfRun} />
          )}
        </div>
      </div>

      {/* Run Workflow Button */}
      <div className="border-t border-gray-200 p-4 mt-auto">
        <Button className="w-full bg-[#3b81f5] hover:bg-[#3b81f5]/90">
          <Play className="mr-2 h-4 w-4" />
          Run Workflow
        </Button>
      </div>
    </div>
  )
}