import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { FC, useMemo, useState } from 'react'
import { useDiagram } from '../../hooks/useDiagram'
import { NodeInfo } from './NodeInfo/NodeInfo'
import { Node } from './Node'
import { NodeRunInfo } from './NodeRunInfo/NodeRunInfo'
import { SelectedNodeRun } from './NodeInfo/SelectNodeRun'
import { NodeRunComponent } from './NodeRunInfo/NodeRunComponent'

export const Sidebar: FC<{ showNodeRun?: boolean }> = ({ showNodeRun }) => {
  const { selectedNode } = useDiagram()

  const [currentTab, setCurrentTab] = useState('overview')
  const [nodeRunIndex, setNodeRunIndex] = useState<number>(0)

  const isValidNode = useMemo(() => {
    if (!selectedNode) {
      return false
    } else {
      if (showNodeRun) {
        if (!('nodeRunsList' in selectedNode.data)) {
          return false
        }
        return selectedNode.data.nodeRunsList?.length > 0
      } else {
        return true
      }
    }
  }, [selectedNode, showNodeRun])
  return (
    <aside className="flex max-w-full flex-col pl-4">
      {isValidNode && selectedNode && (
        <>
          {showNodeRun &&<SelectedNodeRun
            nodeName={selectedNode.id}
            nodeRunIndex={nodeRunIndex}
            setNodeRunIndex={setNodeRunIndex}
            arrayRunNodeLength={selectedNode.data.nodeRunsList.length} /// fix types any is not a right one
          />}
          <Tabs value={currentTab} onValueChange={value => setCurrentTab(value)} className="w-full">
            <TabsList className="w-full">
              <TabsTrigger className="flex-1" value="overview">
                Overview
              </TabsTrigger>
              <TabsTrigger className="flex-1" value="node">
                Node
              </TabsTrigger>
            </TabsList>
            <TabsContent value="overview">
              {showNodeRun ? <NodeRunInfo nodeRunIndex={nodeRunIndex} /> : <NodeInfo />}
            </TabsContent>
            <TabsContent value="node">{showNodeRun ? <NodeRunComponent nodeRunIndex={nodeRunIndex}/> : <Node />}</TabsContent>
          </Tabs>
        </>
      )}
      {!isValidNode && (
        <div className="flex h-full flex-row items-center justify-center p-4">
          <p className="text-center text-gray-400">Select a node to view its details</p>
        </div>
      )}
    </aside>
  )
}
