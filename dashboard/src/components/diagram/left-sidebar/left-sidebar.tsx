"use client"
import ScheduledWfRunsTab from "@/components/diagram/left-sidebar/scheduled-wf-runs-tab"
import { Badge } from "@/components/ui/badge"
import SidebarExpandButton from "@/components/ui/sidebar-expand-button"
import { LeftSidebarTabId } from "@/types/leftSidebarTabs"
import { Button } from "@littlehorse-enterprises/ui-library/button"
import { WfRun, WfSpec } from "littlehorse-client/proto"
import { Play } from "lucide-react"
import { useEffect, useState } from "react"
import WfRunTab from "../wf-run-tab"
import WfSpecTab from "../wf-spec-tab"
import LeftSidebarTabs from "./left-sidebar-tabs"

const tabDescriptions: Record<LeftSidebarTabId, string> = {
  WfSpec: "Workflow Specification",
  WfRuns: "Workflow Runs",
  ScheduledWfRuns: "Scheduled Workflow Runs",
}

type SidebarState = "hidden" | "normal" | "expanded"

interface LeftSidebarProps {
  wfSpec: WfSpec
  wfRun?: WfRun
}

export default function LeftSidebar({
  wfSpec,
  wfRun,
}: LeftSidebarProps) {
  const [activeTab, setActiveTab] = useState<LeftSidebarTabId>("WfSpec")
  const [sidebarState, setSidebarState] = useState<SidebarState>("normal")

  // Auto-expand sidebar when tab is WfRuns or ScheduledWfRuns
  useEffect(() => {
    if (activeTab === "WfRuns" || activeTab === "ScheduledWfRuns") {
      setSidebarState((prev) => prev === "hidden" ? "hidden" : "expanded")
    } else if (activeTab === "WfSpec") {
      setSidebarState((prev) => prev === "hidden" ? "hidden" : "normal")
    }
  }, [activeTab])

  // Sidebar width based on state
  const sidebarWidth = sidebarState === "expanded"
    ? "w-full md:w-4/5 lg:w-1/2"
    : sidebarState === "normal"
      ? "w-[250px]"
      : "w-0 min-w-0"

  return (
    <div
      className={`h-full flex flex-col border-r border-gray-200 bg-white transition-all duration-300 ease-in-out relative ${sidebarWidth}`}
      style={{ minWidth: sidebarState === "hidden" ? 0 : undefined }}
    >
      {/* Handle/Show button when hidden */}
      {sidebarState === "hidden" && (
        <div className="absolute left-0 top-3 z-30 h-8 w-6 flex items-center justify-center">
          <SidebarExpandButton isExpanded={false} onClick={() => setSidebarState("normal")} position="right" />
        </div>
      )}

      {/* Sidebar content (hidden when sidebar is hidden) */}
      <div className={`flex flex-col h-full ${sidebarState === "hidden" ? "opacity-0 pointer-events-none select-none" : "opacity-100"}`}>
        {/* Expand/Collapse/Hide Buttons */}
        {sidebarState !== "hidden" && (
          <div className="z-20 absolute -right-3 top-3 flex flex-col gap-2 items-center">
            <SidebarExpandButton
              isExpanded={sidebarState === "expanded"}
              onClick={() => setSidebarState(sidebarState === "expanded" ? "normal" : "expanded")}
              position="right"
            />
            <button
              className="bg-white border border-gray-200 rounded-full shadow p-1 hover:bg-gray-50 transition"
              onClick={() => setSidebarState("hidden")}
              aria-label="Hide sidebar"
            >
              {/* Use left chevron to indicate hide */}
              <svg width="16" height="16" fill="none" viewBox="0 0 24 24"><path d="M15 18l-6-6 6-6" stroke="#656565" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" /></svg>
            </button>
          </div>
        )}

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
              {wfSpec?.status ?? wfRun?.status}
            </Badge>
          </div>
        </div>

        {/* Tabs */}
        <LeftSidebarTabs activeTab={activeTab} setActiveTab={setActiveTab} />

        {/* Tab Content */}
        <div className="flex-1 flex flex-col overflow-hidden">
          <div className="border-b border-gray-200 p-2 text-xs text-[#656565]">
            <span className="font-medium">{tabDescriptions[activeTab]}</span>
          </div>
          <div className="flex-1 overflow-auto">
            {activeTab === "WfSpec" && (
              <WfSpecTab wfSpec={wfSpec} wfRun={wfRun} />
            )}
            {activeTab === "WfRuns" && (
              <WfRunTab />
            )}
            {activeTab === "ScheduledWfRuns" && (
              <ScheduledWfRunsTab />
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
    </div>
  )
}
