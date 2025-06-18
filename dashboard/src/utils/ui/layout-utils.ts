import dagre from '@dagrejs/dagre'
import { CustomNode, CustomEdge } from '@/types/node'

const dagreGraph = new dagre.graphlib.Graph()

export function getLayoutedElements(nodes: CustomNode[], edges: CustomEdge[]) {
  // #region GraphSetup
  dagreGraph.setGraph({ rankdir: 'LR', align: 'DL' })

  nodes.forEach(node => {
    dagreGraph.setNode(node.id, { width: 100, height: 100 })
  })
  edges.forEach(edge => {
    dagreGraph.setEdge(edge.source, edge.target, { width: edge.label ? 100 : 10 })
  })

  dagre.layout(dagreGraph)
  // #endregion

  const layoutedNodes = nodes.map(node => {
    const dagreNode = dagreGraph.node(node.id)

    return {
      ...node,
      position: { x: dagreNode.x - 100 / 2, y: dagreNode.y - 100 / 2 },
      layouted: true,
    }
  })

  return { nodes: layoutedNodes, edges }
}
