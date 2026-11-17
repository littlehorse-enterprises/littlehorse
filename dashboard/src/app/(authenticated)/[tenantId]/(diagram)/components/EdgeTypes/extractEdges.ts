import { Edge as EdgeProto, ThreadSpec, VariableValue } from 'littlehorse-client/proto'
import { Edge, MarkerType } from 'reactflow'
import { isNopConditionalBranch } from './edgeConditionDisplay'
import type { EdgeRoute } from './elkRoute'

export type DiagramEdgeData = EdgeProto & {
  isConditionalBranchEdge?: boolean
  fade?: boolean
  nodeOutputValues?: Record<string, VariableValue>
  route?: EdgeRoute
}

export const extractEdges = (spec: ThreadSpec): Edge<DiagramEdgeData>[] => {
  return Object.entries(spec.nodes).flatMap(([source, node]) => {
    const isNopBranch = node.node.oneofKind === 'nop' && isNopConditionalBranch(node.outgoingEdges)
    return node.outgoingEdges.map((edge, index) => {
      return {
        id: `${source}-${edge.sinkNodeName}-${index}`,
        source,
        type: 'custom',
        target: edge.sinkNodeName,
        data: { ...edge, isConditionalBranchEdge: isNopBranch },
        sourceHandle: 'source-0',
        targetHandle: 'target-0',
        markerEnd: {
          type: MarkerType.ArrowClosed,
        },
        animated: true,
      }
    })
  })
}
