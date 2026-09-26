import { ThreadSpec } from 'littlehorse-client/proto'
import { Edge, MarkerType } from 'reactflow'
import { Edge as EdgeProto } from 'littlehorse-client/proto'
import type { EdgeRoute } from './elkRoute'

export type DiagramEdgeData = EdgeProto & { isElseEdge?: boolean; route?: EdgeRoute }

export const extractEdges = (spec: ThreadSpec): Edge<DiagramEdgeData>[] => {
  return Object.entries(spec.nodes).flatMap(([source, node]) => {
    const hasMultipleOutgoingEdges = node.outgoingEdges.length > 1
    return node.outgoingEdges.map((edge, index) => {
      const isElseEdge = hasMultipleOutgoingEdges && edge.edgeCondition?.oneofKind === undefined
      return {
        id: `${source}-${edge.sinkNodeName}-${index}`,
        source,
        type: 'custom',
        target: edge.sinkNodeName,
        data: { ...edge, isElseEdge },
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
