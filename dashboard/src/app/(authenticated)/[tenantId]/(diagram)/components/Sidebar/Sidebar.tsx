import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { FC, useState } from 'react'
import { useDiagram } from '../../hooks/useDiagram'
import { NodeInfo } from './NodeInfo/NodeInfo'
import { Node } from './Node'

export const Sidebar: FC<{ showNodeRun?: boolean }> = ({ showNodeRun }) => {
  const { selectedNode } = useDiagram()
  const [currentTab, setCurrentTab] = useState(showNodeRun ? 'node-run' : 'node')

  return (
    <aside className="flex max-w-full flex-col pl-4">
      {selectedNode && (
        <Tabs value={currentTab} onValueChange={value => setCurrentTab(value)} className="w-full">
          <TabsList className="w-full">
            <TabsTrigger className="flex-1" value="node">
              Node
            </TabsTrigger>
            {showNodeRun && (
              <TabsTrigger className="flex-1" value="node-run">
                NodeRun
              </TabsTrigger>
            )}
          </TabsList>
          <TabsContent value="node">
            <NodeInfo />
            <Node />
          </TabsContent>
        </Tabs>
      )}
      {!selectedNode && (
        <div className="flex h-full flex-row items-center justify-center p-4">
          <p className="text-center text-gray-400">Select a node to view its details</p>
        </div>
      )}
    </aside>
  )
}
