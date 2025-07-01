'use client'
import ScheduledWfRunsTab from '@/components/diagram/left-sidebar/tab-content/scheduled-wf-runs-tab'
import { Badge } from '@littlehorse-enterprises/ui-library/badge'
import SidebarExpandButton from '@/components/ui/sidebar-expand-button'
import { LeftSidebarTabId } from '@/types/leftSidebarTabs'
import { LHStatus, WfRun, WfSpec } from 'littlehorse-client/proto'
import { useEffect, useState } from 'react'
import WfRunTab from './tab-content/wf-run-tab'
import WfSpecTab from './tab-content/wf-spec-tab'
import LeftSidebarTabs from './tabs'
import ActionButton from './action-button'

const tabDescriptions: Record<LeftSidebarTabId, string> = {
  WfSpec: 'Workflow Specification',
  WfRuns: 'Workflow Runs',
  ScheduledWfRuns: 'Scheduled Workflow Runs',
}

type SidebarState = 'hidden' | 'normal' | 'expanded'

interface LeftSidebarProps {
  wfSpec: WfSpec
  wfRun: WfRun | undefined
}

export default function LeftSidebar({ wfSpec, wfRun }: LeftSidebarProps) {
  const [activeTab, setActiveTab] = useState<LeftSidebarTabId>('WfSpec')
  const [sidebarState, setSidebarState] = useState<SidebarState>('normal')

  // Auto-expand sidebar when tab is WfRuns or ScheduledWfRuns
  useEffect(() => {
    if (activeTab === 'WfRuns' || activeTab === 'ScheduledWfRuns') {
      setSidebarState(prev => (prev === 'hidden' ? 'hidden' : 'expanded'))
    } else if (activeTab === 'WfSpec') {
      setSidebarState(prev => (prev === 'hidden' ? 'hidden' : 'normal'))
    }
  }, [activeTab])

  // Sidebar width based on state
  const sidebarWidth =
    sidebarState === 'expanded' ? 'w-full md:w-4/5 lg:w-1/2' : sidebarState === 'normal' ? 'w-[250px]' : 'w-0 min-w-0'

  return (
    <div
      className={`relative flex h-full flex-col border-r border-gray-200 bg-white transition-all duration-300 ease-in-out ${sidebarWidth}`}
      style={{ minWidth: sidebarState === 'hidden' ? 0 : undefined }}
    >
      {/* Handle/Show button when hidden */}
      {sidebarState === 'hidden' && (
        <div className="absolute top-3 left-0 z-30 flex h-8 w-6 items-center justify-center">
          <SidebarExpandButton isExpanded={false} onClick={() => setSidebarState('normal')} position="right" />
        </div>
      )}

      {/* Sidebar content (hidden when sidebar is hidden) */}
      <div
        className={`flex h-full flex-col ${sidebarState === 'hidden' ? 'pointer-events-none opacity-0 select-none' : 'opacity-100'}`}
      >
        {/* Expand/Collapse/Hide Buttons */}
        {sidebarState !== 'hidden' && (
          <div className="absolute top-3 -right-3 z-20 flex flex-col items-center gap-2">
            <SidebarExpandButton
              isExpanded={sidebarState === 'expanded'}
              onClick={() => setSidebarState(sidebarState === 'expanded' ? 'normal' : 'expanded')}
              position="right"
            />
            <button
              className="rounded-full border border-gray-200 bg-white p-1 shadow transition hover:bg-gray-50"
              onClick={() => setSidebarState('hidden')}
              aria-label="Hide sidebar"
            >
              {/* Use left chevron to indicate hide */}
              <svg width="16" height="16" fill="none" viewBox="0 0 24 24">
                <path
                  d="M15 18l-6-6 6-6"
                  stroke="#656565"
                  strokeWidth="2"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                />
              </svg>
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
            {activeTab === 'WfSpec' && <WfSpecTab wfSpec={wfSpec} wfRun={wfRun} />}
            {activeTab === 'WfRuns' && <WfRunTab />}
            {activeTab === 'ScheduledWfRuns' && <ScheduledWfRunsTab />}
          </div>
        </div>

        {/* Action Button */}
        {!wfRun && (
          <ActionButton
            variant="run"
            wfSpec={wfSpec}
          />
        )}
        {wfRun && (wfRun.status === LHStatus.RUNNING) && (
          <>
            <ActionButton
              variant="stop"
              wfRun={wfRun}
            />
          </>
        )}
        {wfRun && (wfRun.status === LHStatus.ERROR) && (
          <ActionButton
            variant="rescue"
            wfRun={wfRun}
          />
        )}
        {wfRun?.status === LHStatus.HALTED && (
          <ActionButton
            variant="resume"
            wfRun={wfRun}
          />
        )}
      </div>
    </div>
  )
}
