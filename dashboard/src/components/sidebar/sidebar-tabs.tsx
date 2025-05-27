"use client"
import { Tabs, TabsList, TabsTrigger } from "@/components/ui/tabs"

interface SidebarTabsProps {
  activeTab: string
  setActiveTab: (tab: string) => void
}

export default function SidebarTabs({ activeTab, setActiveTab }: SidebarTabsProps) {
  const tabs = [
    { id: "WfSpec", label: "WfSpec" },
    { id: "WfRuns", label: "WfRuns" },
    { id: "ScheduledWfRuns", label: "Scheduled" },
  ]

  return (
    <div className="border-b border-gray-200 px-2 py-1">
      <Tabs value={activeTab} onValueChange={setActiveTab} className="w-full">
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
