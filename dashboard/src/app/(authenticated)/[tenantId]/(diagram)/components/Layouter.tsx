import { ThreadRunWithNodeRuns } from '@/app/actions/getWfRun'
import dagre from 'dagre'
import { WfRun } from 'littlehorse-client/proto'
import { FC, useCallback, useEffect } from 'react'
import { Edge, Node, useReactFlow, useStore } from 'reactflow'

// used to calculate the width of the
export const EDGE_WIDTH = 200

export const Layouter: FC<{ wfRun?: WfRun & { threadRuns: ThreadRunWithNodeRuns[] }; nodeRunNameToBeHighlighted?: string }> = ({
  wfRun,
  nodeRunNameToBeHighlighted,
}) => {
  const nodes = useStore(store => store.getNodes())
  const edges = useStore(store => store.edges)
  const setNodes = useStore(store => store.setNodes)
  const { fitView } = useReactFlow()

  const onLoad = useCallback(
    (nodes: Node[], edges: Edge[]) => {
      const dagreGraph = new dagre.graphlib.Graph()
      dagreGraph.setDefaultEdgeLabel(() => ({}))
      dagreGraph.setGraph({ rankdir: 'LR', ranksep: 100, nodesep: 100 })
      nodes.forEach(node => {
        dagreGraph.setNode(node.id, { width: node.width, height: node.height })
      })

      edges.forEach(edge => {
        dagreGraph.setEdge(edge.source, edge.target, { width: edge.label ? EDGE_WIDTH : undefined })
      })

      dagre.layout(dagreGraph)

      const layoutedNodes = nodes.map(node => {
        const nodeWithPosition = dagreGraph.node(node.id)

        const [nodeName, threadRunName] = node.id.split(':')
        const threadRun = wfRun?.threadRuns.find(threadRun => threadRun.threadSpecName === threadRunName) as ThreadRunWithNodeRuns | undefined

        const nodeRun = threadRun?.nodeRuns.find(nodeRun => {
          return nodeRun.nodeName === nodeName
        })
        const nodeRunsList = threadRun?.nodeRuns.filter(nodeRun => {
          return nodeRun.nodeName === nodeName
        })

        const fade = threadRun?.nodeRuns && !nodeRun || (wfRun && !threadRun)
        const nodeNeedsToBeHighlighted = nodeName === nodeRunNameToBeHighlighted

        return {
          ...node,
          data: { ...node.data, nodeRun, fade, nodeNeedsToBeHighlighted, nodeRunsList },
          position: { x: nodeWithPosition.x - node.width! / 2, y: nodeWithPosition.y - node.height! / 2 },
          layouted: true,
        }
      })

      setNodes(layoutedNodes)
      fitView()
    },
    [fitView, setNodes, nodeRunNameToBeHighlighted]
  )

  useEffect(() => {
    if (
      nodes.some(
        (node: Node & { layouted?: boolean }) => node.width !== undefined && node.height !== undefined && !node.layouted
      )
    ) {
      onLoad(nodes, edges)
    }
  }, [nodes, edges, onLoad])
  return <></>
}
