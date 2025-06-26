'use client'

import { LeftSidebarTabId } from '@/types/leftSidebarTabs'
import { Tabs, TabsList, TabsTrigger } from '@littlehorse-enterprises/ui-library/tabs'
import { Dispatch, SetStateAction } from 'react'

const tabs: { id: LeftSidebarTabId; label: string }[] = [
  { id: 'WfSpec', label: 'WfSpec' },
  { id: 'WfRuns', label: 'WfRuns' },
  { id: 'ScheduledWfRuns', label: 'Scheduled' },
]

interface LeftSidebarTabsProps {
  activeTab: LeftSidebarTabId
  setActiveTab: Dispatch<SetStateAction<LeftSidebarTabId>>
}

export default function LeftSidebarTabs({ activeTab, setActiveTab }: LeftSidebarTabsProps) {
  return (
    <div className="h-10 border-b border-gray-200 px-2 py-1">
      {/* type casting with "as" here is fine because we know that the value is a valid LeftSidebarTabId and it will never not be */}
      <Tabs
        value={activeTab}
        onValueChange={(value: string) => setActiveTab(value as LeftSidebarTabId)}
        className="w-full"
      >
        <TabsList className="h-8 w-full bg-transparent">
          {tabs.map(tab => (
            <TabsTrigger
              key={tab.id}
              value={tab.id}
              className="flex-1 rounded-none text-xs data-[state=active]:border-b-2 data-[state=active]:border-[#3b81f5] data-[state=active]:bg-transparent data-[state=active]:text-[#3b81f5] data-[state=active]:shadow-none"
              title={tab.id === 'ScheduledWfRuns' ? 'Scheduled Workflow Runs' : tab.id}
            >
              {tab.label}
            </TabsTrigger>
          ))}
        </TabsList>
      </Tabs>
    </div>
  )
}
