import { getVariable } from '@/app/utils'
import { getComparatorSymbol } from '@/app/utils/comparatorUtils'
import { Edge as EdgeProto, WfSpec } from 'littlehorse-client/proto'
import { Edge, MarkerType } from 'reactflow'
import { ThreadSpecWithName } from '../Diagram'
import { getNodeType } from '../NodeTypes/extractWfSpecNodes'

export const extractEdges = (wfSpec: WfSpec, threadSpecWithName: ThreadSpecWithName): Edge[] => {
  const edges: Edge[] = []

  const targetMap = new Map<string, number>()
  const sourceMap = new Map<string, number>()
  Object.entries(threadSpecWithName.threadSpec.nodes).forEach(([source, node]) => {
    if (getNodeType(node) === 'START_THREAD') {
      const startThreadNodeName = node.startThread?.threadSpecName
      if (!startThreadNodeName) return

      const moreEdges = extractEdges(wfSpec, {
        name: startThreadNodeName,
        threadSpec: wfSpec.threadSpecs[startThreadNodeName],
      })
      edges.push(...moreEdges)
    }

    node.outgoingEdges.forEach(edge => {
      const sourceIndex = sourceMap.get(source) ?? 0
      let targetIndex = targetMap.get(edge.sinkNodeName) ?? 0
      const sourceTarget = sourceMap.get(edge.sinkNodeName) ?? 0

      if (sourceTarget > 0 && targetIndex !== 0) targetIndex++
      const edgeId = `${source}-${edge.sinkNodeName}`
      const id = sourceIndex === 0 && targetIndex === 0 ? edgeId : `${edgeId}-${sourceIndex}-${targetIndex}`
      targetMap.set(edge.sinkNodeName, targetIndex + 1)
      sourceMap.set(source, sourceIndex + 1)

      const label = extractEdgeLabel(edge)
      edges.push({
        id,
        source: `${source}:${threadSpecWithName.name}`,
        type: 'default',
        target: `${edge.sinkNodeName}:${threadSpecWithName.name}`,
        label,
        data: edge,
        targetHandle: `target-${targetIndex}`,
        sourceHandle: `source-${sourceIndex}`,
        markerEnd: {
          type: MarkerType.ArrowClosed,
        },
        animated: true,
      })
    })
  })
  return edges
}

const extractEdgeLabel = ({ condition }: EdgeProto) => {
  if (!condition) return

  const { left, right, comparator } = condition
  return `${getVariable(left)} ${getComparatorSymbol(comparator)} ${getVariable(right)}`
}
