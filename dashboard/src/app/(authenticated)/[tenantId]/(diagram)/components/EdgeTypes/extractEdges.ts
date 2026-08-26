import { ThreadSpec } from 'littlehorse-client/proto'
import { Edge, MarkerType } from 'reactflow'

/**
 * ThreadSpec -> reactflow edges.
 *
 * The handle contract is deliberately simple so it can never address a handle
 * a component does not render:
 *  - sourceHandle is `source-<i>` where i is the edge's index within its
 *    source node's outgoingEdges. Every node type renders `source-0`; NOP
 *    renders one source handle per outgoing edge, id-aligned with this index
 *    (see NodeTypes/nopHandleLayout.ts).
 *  - targetHandle is always `target-0` — every node type renders exactly one
 *    target handle.
 *
 * (The previous version numbered target handles per incoming edge —
 * `target-1`, `target-2`, ... — which no component renders; reactflow drops
 * an edge whose handle id does not resolve, so merge points silently lost
 * their second and later incoming edges.)
 */
export const extractEdges = (spec: ThreadSpec): Edge[] => {
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
        sourceHandle: `source-${index}`,
        targetHandle: 'target-0',
        markerEnd: {
          type: MarkerType.ArrowClosed,
        },
        animated: true,
      }
    })
  })
}
