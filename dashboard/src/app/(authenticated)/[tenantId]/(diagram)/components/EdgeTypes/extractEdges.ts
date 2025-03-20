import { getVariable } from '@/app/utils'
import { getComparatorSymbol } from '@/app/utils/comparatorUtils'
import { Edge as EdgeProto, ThreadSpec } from 'littlehorse-client/proto'
import { Edge, MarkerType } from 'reactflow'

export const extractEdges = (spec: ThreadSpec, threadName: string): Edge[] => {
  return Object.entries(spec.nodes).flatMap(([source, node]) => {
    return node.outgoingEdges.map(edge => {
      const edgeId = `${source}-${edge.sinkNodeName}`
      const label = extractEdgeLabel(edge)
      return {
        id: `${threadName}:${edgeId}`,
        source: `${threadName}:${source}`,
        type: 'default',
        target: `${threadName}:${edge.sinkNodeName}`,
        label,
        data: edge,
        markerEnd: {
          type: MarkerType.ArrowClosed,
        },
        animated: true,
        arrowHeadType: 'arrowclosed',
      }
    })
  })
}

const extractEdgeLabel = ({ condition }: EdgeProto) => {
  if (!condition) return

  const { left, right, comparator } = condition
  return `${getVariable(left)} ${getComparatorSymbol(comparator)} ${getVariable(right)}`
}
