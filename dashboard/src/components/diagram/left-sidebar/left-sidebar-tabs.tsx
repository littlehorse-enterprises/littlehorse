"use client"

import { Tabs, TabsList, TabsTrigger } from "@littlehorse-enterprises/ui-library/tabs"
import { LeftSidebarTabId } from "@/types/leftSidebarTabs"
import { Dispatch, SetStateAction } from "react"

const tabs: { id: LeftSidebarTabId, label: string }[] = [
  { id: "WfSpec", label: "WfSpec" },
  { id: "WfRuns", label: "WfRuns" },
  { id: "ScheduledWfRuns", label: "Scheduled" },
]

interface LeftSidebarTabsProps {
  activeTab: LeftSidebarTabId
  setActiveTab: Dispatch<SetStateAction<LeftSidebarTabId>>
}

export default function LeftSidebarTabs({ activeTab, setActiveTab }: LeftSidebarTabsProps) {
  return (
    <div className="border-b border-gray-200 px-2 py-1 h-10">
      {/* type casting with "as" here is fine because we know that the value is a valid LeftSidebarTabId and it will never not be */}
      <Tabs value={activeTab} onValueChange={(value: string) => setActiveTab(value as LeftSidebarTabId)} className="w-full">
        <TabsList className="w-full h-8 bg-transparent">
          {tabs.map((tab) => (
            <TabsTrigger
              key={tab.id}
              value={tab.id}
              className="text-xs data-[state=active]:bg-transparent data-[state=active]:text-[#3b81f5] data-[state=active]:shadow-none data-[state=active]:border-b-2 data-[state=active]:border-[#3b81f5] rounded-none flex-1"
              title={tab.id === "ScheduledWfRuns" ? "Scheduled Workflow Runs" : tab.id}
            >
              {tab.label}
            </TabsTrigger>
          ))}
        </TabsList>
      </Tabs>
    </div>
  )
}
