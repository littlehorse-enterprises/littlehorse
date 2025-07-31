import { getVariable } from '@/app/utils'
import { Comparator, Edge as EdgeProto, ThreadSpec } from 'littlehorse-client/proto'
import { Edge, MarkerType } from 'reactflow'

export const extractEdges = (spec: ThreadSpec): Edge[] => {
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
        source,
        type: 'custom',
        target: edge.sinkNodeName,
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
  return `${getVariable(left)} ${getComparator(comparator)} ${getVariable(right)}`
}

const getComparator = (comparator: Comparator) => Conditions[comparator]
export const Conditions: Record<Comparator, string> = {
  [Comparator.LESS_THAN]: '<',
  [Comparator.GREATER_THAN]: '>',
  [Comparator.LESS_THAN_EQ]: '<=',
  [Comparator.GREATER_THAN_EQ]: '>=',
  [Comparator.EQUALS]: '==',
  [Comparator.NOT_EQUALS]: '!=',
  [Comparator.IN]: 'IN',
  [Comparator.NOT_IN]: 'NOT IN',
  [Comparator.UNRECOGNIZED]: '',
}
