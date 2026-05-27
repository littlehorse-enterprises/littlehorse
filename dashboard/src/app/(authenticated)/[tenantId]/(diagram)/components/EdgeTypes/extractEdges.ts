import { ThreadSpec } from 'littlehorse-client/proto'
import { Edge, MarkerType } from 'reactflow'
import { isNopConditionalBranch } from './edgeConditionDisplay'

export const extractEdges = (spec: ThreadSpec): Edge[] => {
  const targetMap = new Map<string, number>()
  const sourceMap = new Map<string, number>()
  return Object.entries(spec.nodes).flatMap(([source, node]) => {
    const conditionOnSourceNode =
      node.node?.$case === 'nop' && isNopConditionalBranch(node.outgoingEdges)

    return node.outgoingEdges.map(edge => {
      const sourceIndex = sourceMap.get(source) ?? 0
      let targetIndex = targetMap.get(edge.sinkNodeName) ?? 0
      const sourceTarget = sourceMap.get(edge.sinkNodeName) ?? 0

      if (sourceTarget > 0 && targetIndex !== 0) targetIndex++
      const edgeId = `${source}-${edge.sinkNodeName}`
      const id = sourceIndex === 0 && targetIndex === 0 ? edgeId : `${edgeId}-${sourceIndex}-${targetIndex}`
      targetMap.set(edge.sinkNodeName, targetIndex + 1)
      sourceMap.set(source, sourceIndex + 1)

      const hasMultipleOutgoingEdges = node.outgoingEdges.length > 1
      const isElseEdge = hasMultipleOutgoingEdges && !edge.edgeCondition
      const branchLabel = conditionOnSourceNode
        ? edge.edgeCondition
          ? ('true' as const)
          : isElseEdge
            ? ('false' as const)
            : undefined
        : undefined

      return {
        id,
        source,
        type: 'custom',
        target: edge.sinkNodeName,
        data: { ...edge, isElseEdge, conditionOnSourceNode, branchLabel },
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
