'use client'

import { WfRunDetails } from '@/actions/getWfRunDetails'
import { useNodeSelection } from '@/components/context/selection-context'
import SidebarExpandButton from '@/components/ui/sidebar-expand-button'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@littlehorse-enterprises/ui-library/tabs'
import { WfSpec } from 'littlehorse-client/proto'
import { useState } from 'react'
import NodeDefinition from './node-definition'
import NodeRuns from './node-runs'

type SidebarState = 'hidden' | 'normal' | 'expanded'

interface RightSidebarProps {
  wfSpec: WfSpec
  wfRunDetails?: WfRunDetails
}

export default function RightSidebar({ wfSpec, wfRunDetails }: RightSidebarProps) {
  const [sidebarState, setSidebarState] = useState<SidebarState>('normal')
  const { selectedId } = useNodeSelection()

  // Sidebar width based on state
  const sidebarWidth =
    sidebarState === 'expanded' ? 'w-full md:w-4/5 lg:w-1/2' : sidebarState === 'normal' ? 'w-[280px]' : 'w-0 min-w-0'
  const node = selectedId ? wfSpec.threadSpecs[selectedId.split(':')[1]].nodes[selectedId.split(':')[0]] : undefined

  return (
    <aside
      className={`relative flex h-full flex-col border-l border-gray-200 bg-white transition-all duration-300 ease-in-out ${sidebarWidth}`}
      style={{ minWidth: sidebarState === 'hidden' ? 0 : undefined }}
    >
      {/* Handle/Show button when hidden */}
      {sidebarState === 'hidden' && (
        <div className="absolute top-3 right-0 z-30 flex h-8 w-6 items-center justify-center">
          <SidebarExpandButton isExpanded={false} onClick={() => setSidebarState('normal')} position="left" />
        </div>
      )}

      {/* Sidebar content (hidden when sidebar is hidden) */}
      <div
        className={`flex h-full flex-col ${sidebarState === 'hidden' ? 'pointer-events-none opacity-0 select-none' : 'opacity-100'}`}
      >
        {/* Expand/Collapse/Hide Buttons */}
        {sidebarState !== 'hidden' && (
          <div className="absolute top-3 -left-3 z-20 flex flex-col items-center gap-2">
            <SidebarExpandButton
              isExpanded={sidebarState === 'expanded'}
              onClick={() => setSidebarState(sidebarState === 'expanded' ? 'normal' : 'expanded')}
              position="left"
            />
            <button
              className="rounded-full border border-gray-200 bg-white p-1 shadow transition hover:bg-gray-50"
              onClick={() => setSidebarState('hidden')}
              aria-label="Hide sidebar"
            >
              {/* Use right chevron to indicate hide */}
              <svg width="16" height="16" fill="none" viewBox="0 0 24 24">
                <path d="M9 18l6-6-6-6" stroke="#656565" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
              </svg>
            </button>
          </div>
        )}

        {!node || !selectedId ? (
          <div className="flex flex-1 items-center justify-center p-4 text-xs italic">
            Select a node to view details
          </div>
        ) : (
          <>
            {/* Node Properties Header - Fixed at top */}
            <div className="bg-background sticky top-0 z-10 border-b border-gray-200 p-4">
              <div className="text-neutral text-xs">
                <span className="font-medium">Node Details</span>
              </div>
              <div className="mb-1 flex items-center justify-between font-medium">
                <div className="flex items-center">{selectedId}</div>
              </div>
            </div>

            <div className="flex h-full w-full justify-center px-4 pt-2">
              <Tabs className="w-full" defaultValue="specification">
                <TabsList>
                  {wfRunDetails && <TabsTrigger value="runs">Runs</TabsTrigger>}
                  <TabsTrigger value="specification">Specification</TabsTrigger>
                </TabsList>
                {wfRunDetails && (
                  <TabsContent value="runs">
                    <NodeRuns selectedId={selectedId} wfRun={wfRunDetails.wfRun} />
                  </TabsContent>
                )}
                <TabsContent value="specification">
                  <NodeDefinition node={node} />
                </TabsContent>
              </Tabs>
            </div>
          </>
        )}
      </div>
    </aside>
  )
}
