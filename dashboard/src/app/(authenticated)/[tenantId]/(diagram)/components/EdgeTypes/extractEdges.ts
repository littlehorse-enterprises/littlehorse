import { getVariable } from '@/app/utils'
import { getComparatorSymbol } from '@/app/utils/comparatorUtils'
import { Edge as EdgeProto, ThreadSpec } from 'littlehorse-client/proto'
import { Edge, MarkerType } from 'reactflow'

export const extractEdges = (spec: ThreadSpec, threadName: string): Edge[] => {
  const targetMap = new Map<string, number>()
  const sourceMap = new Map<string, number>()
  return Object.entries(spec.nodes).flatMap(([source, node]) => {
    return node.outgoingEdges.map(edge => {
      const sourceIndex = sourceMap.get(source) ?? 0
      let targetIndex = targetMap.get(edge.sinkNodeName) ?? 0
      const sourceTarget = sourceMap.get(edge.sinkNodeName) ?? 0

      if (sourceTarget > 0 && targetIndex !== 0) targetIndex++
      const edgeId = `${source}-${edge.sinkNodeName}`
      const id = sourceIndex === 0 && targetIndex === 0 ? edgeId : `${edgeId}-${sourceIndex}-${targetIndex}`
      targetMap.set(edge.sinkNodeName, targetIndex + 1)
      sourceMap.set(source, sourceIndex + 1)

      const label = extractEdgeLabel(edge)
      return {
        id,
        source: `${threadName}:${source}`,
        type: 'default',
        target: `${threadName}:${edge.sinkNodeName}`,
        label,
        data: edge,
        targetHandle: `target-${targetIndex}`,
        sourceHandle: `source-${sourceIndex}`,
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
