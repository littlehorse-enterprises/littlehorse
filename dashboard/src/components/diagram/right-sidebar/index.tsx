'use client'

import { WfRunDetails } from '@/actions/getWfRunDetails'
import { useNodeSelection } from '@/components/context/selection-context'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@littlehorse-enterprises/ui-library/tabs'
import { WfSpec } from 'littlehorse-client/proto'
import NodeDefinition from './node-definition'
import NodeRuns from './node-runs'

interface RightSidebarProps {
  wfSpec: WfSpec
  wfRunDetails?: WfRunDetails
}

export default function RightSidebar({ wfSpec, wfRunDetails }: RightSidebarProps) {
  const { selectedId } = useNodeSelection()
  const node = selectedId ? wfSpec.threadSpecs[selectedId.split(':')[1]].nodes[selectedId.split(':')[0]] : undefined

  return (
    <aside className="flex h-full flex-col border-l border-gray-200 bg-white">
      {!node || !selectedId ? (
        <div className="flex flex-1 items-center justify-center p-4 text-xs italic">Select a node to view details</div>
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

          <div className="flex h-full max-h-[calc(100vh-100px)] w-full justify-center overflow-y-auto px-4 pt-2">
            <Tabs className="w-full" defaultValue="spec">
              <TabsList>
                {wfRunDetails && <TabsTrigger value="run">Run</TabsTrigger>}
                <TabsTrigger value="spec">Spec</TabsTrigger>
              </TabsList>
              {wfRunDetails && (
                <TabsContent value="run">
                  <NodeRuns selectedId={selectedId} wfRun={wfRunDetails.wfRun} />
                </TabsContent>
              )}
              <TabsContent value="spec">
                <NodeDefinition node={node} />
              </TabsContent>
            </Tabs>
          </div>
        </>
      )}
    </aside>
  )
}
