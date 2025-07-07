'use client'
import ScheduledWfRunsTab from '@/components/diagram/left-sidebar/tab-content/scheduled-wf-runs-tab'
import { Badge } from '@littlehorse-enterprises/ui-library/badge'
import { LeftSidebarTabId } from '@/types'
import { LHStatus, WfRun, WfSpec } from 'littlehorse-client/proto'
import { useState } from 'react'
import WfRunTab from './tab-content/wf-run-tab'
import WorkflowTab from './tab-content/workflow-tab'
import LeftSidebarTabs from './tabs'
import ActionButton from './action-button'

const tabDescriptions: Record<LeftSidebarTabId, string> = {
  Workflow: 'WfSpec & WfRun',
  WfRuns: 'Workflow Runs',
  ScheduledWfRuns: 'Scheduled Workflow Runs',
}

interface LeftSidebarProps {
  wfSpec: WfSpec
  wfRun: WfRun | undefined
}

export default function LeftSidebar({ wfSpec, wfRun }: LeftSidebarProps) {
  const [activeTab, setActiveTab] = useState<LeftSidebarTabId>('Workflow')

  return (
    <div className="flex h-full flex-col border-r border-gray-200 bg-white">
      {/* Workflow Information */}
      <div className="border-b border-gray-200 p-4">
        <div className="mb-1">
          <span className="font-medium">{wfSpec?.id?.name}</span>
        </div>
        <div className="flex items-center">
          <span className="text-sm text-[#656565]">{`v${wfSpec?.id?.majorVersion}.${wfSpec?.id?.revision}`}</span>
          <Badge
            variant="outline"
            className={`ml-3 ${
              wfRun?.status === LHStatus.COMPLETED
                ? 'bg-emerald-100 text-emerald-600'
                : wfRun?.status === LHStatus.RUNNING
                  ? 'bg-blue-100 text-blue-600'
                  : 'bg-emerald-100 text-emerald-600'
            } hover:bg-[#c5d0ff]/90`}
          >
            {wfRun?.status ?? wfSpec?.status}
          </Badge>
        </div>
      </div>

      {/* Tabs */}
      <LeftSidebarTabs activeTab={activeTab} setActiveTab={setActiveTab} />

      {/* Tab Content */}
      <div className="flex flex-1 flex-col overflow-hidden">
        <div className="border-b border-gray-200 p-2 text-xs text-[#656565]">
          <span className="font-medium">{tabDescriptions[activeTab]}</span>
        </div>
        <div className="flex-1 overflow-auto">
          {activeTab === 'Workflow' && <WorkflowTab wfSpec={wfSpec} wfRun={wfRun} />}
          {activeTab === 'WfRuns' && <WfRunTab />}
          {activeTab === 'ScheduledWfRuns' && <ScheduledWfRunsTab />}
        </div>
      </div>

      {/* Action Button */}
      {!wfRun && <ActionButton variant="run" wfSpec={wfSpec} />}
      {wfRun && wfRun.status === LHStatus.RUNNING && (
        <>
          <ActionButton variant="stop" wfRun={wfRun} />
        </>
      )}
      {wfRun && wfRun.status === LHStatus.ERROR && <ActionButton variant="rescue" wfRun={wfRun} />}
      {wfRun?.status === LHStatus.HALTED && <ActionButton variant="resume" wfRun={wfRun} />}
    </div>
  )
}
