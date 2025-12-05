import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { FC, useMemo, useState } from 'react'
import { useDiagram } from '../../hooks/useDiagram'
import { NodeInfo } from './NodeInfo/NodeInfo'
import { Node } from './Node'
import { NodeRunInfo } from './NodeRunInfo/NodeRunInfo'
import { SelectedNodeRun } from './NodeInfo/SelectNodeRun'
import { NodeRunComponent } from './NodeRunInfo/NodeRunComponent'
import { Failures } from './NodeRunInfo/Failures'

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
  const maxHeightClass = `max-h-[600px]`
  const hasFailures = useMemo(() => {
    if (!showNodeRun) return false
    if (!selectedNode) {
      return false
    }
    if (!('nodeRunsList' in selectedNode.data)) {
      return false
    }
    const nodeRun = selectedNode.data.nodeRunsList[nodeRunIndex]
    return nodeRun.failures.length > 0
  }, [selectedNode, nodeRunIndex])

  const nodeType = useMemo(() => {
    if (!selectedNode || !selectedNode.data.node) return ''

    return selectedNode.data.node.$case || ''
  }, [selectedNode, nodeRunIndex])
  return (
    <aside className={`overflow-y flex max-w-full flex-col overflow-x-hidden pl-4 ${maxHeightClass} overflow-y-auto `}>
      {isValidNode && selectedNode && (
        <>
          {showNodeRun && <SelectedNodeRun nodeRunIndex={nodeRunIndex} setNodeRunIndex={setNodeRunIndex} />}
          <Tabs value={currentTab} onValueChange={value => setCurrentTab(value)} className="w-full">
            <TabsList className="w-full">
              <TabsTrigger className="flex-1" value="overview">
                Overview
              </TabsTrigger>
              <TabsTrigger className="flex-1" value="node">
                Node {nodeType}
              </TabsTrigger>
              {showNodeRun && hasFailures && (
                <TabsTrigger className="flex-1" value="failures">
                  Failures
                </TabsTrigger>
              )}
            </TabsList>
            <TabsContent value="overview">
              {showNodeRun ? <NodeRunInfo nodeRunIndex={nodeRunIndex} /> : <NodeInfo />}
            </TabsContent>
            <TabsContent value="node">
              {showNodeRun ? <NodeRunComponent nodeRunIndex={nodeRunIndex} /> : <Node />}
            </TabsContent>
            <TabsContent value="failures">{showNodeRun && <Failures nodeRunIndex={nodeRunIndex} />}</TabsContent>
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
