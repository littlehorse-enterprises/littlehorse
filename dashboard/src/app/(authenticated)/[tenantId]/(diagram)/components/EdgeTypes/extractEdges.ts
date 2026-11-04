import { Edge as EdgeProto, ThreadSpec, VariableValue } from 'littlehorse-client/proto'
import { Edge, MarkerType } from 'reactflow'
import { isNopConditionalBranch } from './edgeConditionDisplay'
import type { EdgeRoute } from './elkRoute'

export type DiagramEdgeData = EdgeProto & {
  isElseEdge?: boolean
  isConditionalBranchEdge?: boolean
  fade?: boolean
  nodeOutputValues?: Record<string, VariableValue>
  route?: EdgeRoute
}

export const extractEdges = (spec: ThreadSpec): Edge<DiagramEdgeData>[] => {
  return Object.entries(spec.nodes).flatMap(([source, node]) => {
    const hasMultipleOutgoingEdges = node.outgoingEdges.length > 1
    const isNopBranch = node.node.oneofKind === 'nop' && isNopConditionalBranch(node.outgoingEdges)
    return node.outgoingEdges.map((edge, index) => {
      const isElseEdge = hasMultipleOutgoingEdges && edge.edgeCondition?.oneofKind === undefined
      const isConditionalBranchEdge = isNopBranch && (edge.edgeCondition?.oneofKind !== undefined || isElseEdge)
      return {
        id: `${source}-${edge.sinkNodeName}-${index}`,
        source,
        type: 'custom',
        target: edge.sinkNodeName,
        data: { ...edge, isElseEdge, isConditionalBranchEdge },
        sourceHandle: edge.sinkNodeName.startsWith('cycle-') ? 'source-loop' : 'source-0',
        targetHandle: 'target-0',
        markerEnd: {
          type: MarkerType.ArrowClosed,
        },
        animated: true,
      }
    })
  })
}
