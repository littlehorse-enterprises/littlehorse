import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { FC, useMemo, useState } from 'react'
import { useDiagram } from '../../hooks/useDiagram'
import { Node } from './Node'
import { NodeInfo } from './NodeInfo/NodeInfo'
import { SelectedNodeRun } from './NodeInfo/SelectNodeRun'
import { Failures } from './NodeRunInfo/Failures'
import { NodeRunComponent } from './NodeRunInfo/NodeRunComponent'
import { NodeRunInfo } from './NodeRunInfo/NodeRunInfo'

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
    return nodeRun?.failures?.length > 0
  }, [selectedNode, nodeRunIndex])

  const nodeType = useMemo(() => {
    if (!selectedNode || !selectedNode.data.node) return ''

    return selectedNode.data.node.$case || ''
  }, [selectedNode, nodeRunIndex])

  const capitalizedNodeType = useMemo(() => {
    return nodeType.charAt(0).toUpperCase() + nodeType.slice(1)
  }, [nodeType])

  return (
    <aside className={`flex max-w-full flex-col overflow-hidden pl-4 ${maxHeightClass}`}>
      {isValidNode && selectedNode && (selectedNode as { type?: string }).type !== 'cycle' && (
        <>
          {showNodeRun && <SelectedNodeRun nodeRunIndex={nodeRunIndex} setNodeRunIndex={setNodeRunIndex} />}
          <Tabs
            value={currentTab}
            onValueChange={value => setCurrentTab(value)}
            className="flex h-full min-h-0 w-full flex-col"
          >
            <TabsList className="w-full flex-shrink-0">
              <TabsTrigger value="overview" className="flex-1">
                Overview
              </TabsTrigger>
              <TabsTrigger value="node" className="flex-1">
                Node
              </TabsTrigger>
              {showNodeRun && (
                <TabsTrigger value="nodeRun" className="flex-1">
                  NodeRun
                </TabsTrigger>
              )}
              {showNodeRun && hasFailures && (
                <TabsTrigger value="failures" className="flex-1">
                  Failures
                </TabsTrigger>
              )}
            </TabsList>
            <div className="mt-3 min-h-0 flex-1 overflow-y-auto pr-4">
              <TabsContent value="overview" className="mt-0">
                {showNodeRun ? <NodeRunInfo nodeRunIndex={nodeRunIndex} /> : <NodeInfo />}
              </TabsContent>
              <TabsContent value="node" className="mt-0">
                <Node />
              </TabsContent>
              {showNodeRun && (
                <TabsContent value="nodeRun" className="mt-0">
                  <NodeRunComponent nodeRunIndex={nodeRunIndex} />
                </TabsContent>
              )}
              <TabsContent value="failures" className="mt-0">
                {showNodeRun && <Failures nodeRunIndex={nodeRunIndex} />}
              </TabsContent>
            </div>
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
