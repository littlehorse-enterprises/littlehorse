import { Node as NodeProto, ThreadSpec } from 'littlehorse-client/proto'
import { Node } from 'reactflow'

export const extractNodes = (spec: ThreadSpec): Node<NodeProto, NodeType>[] => {
  return Object.entries(spec.nodes).map(([id, node]) => {
    const nodeType = node.node!
    const payload = nodeType.oneofKind ? (nodeType as Record<string, unknown>)[nodeType.oneofKind] : undefined
    return {
      id,
      type: nodeType.oneofKind,
      data: { ...node, ...(payload as Record<string, unknown> | undefined) },
      position: { x: 0, y: 0 },
    }
  })
}
export const getNodeAfterEntrypoint = (threadSpec: ThreadSpec): string | undefined => {
  for (const node of Object.values(threadSpec.nodes)) {
    if (node.node?.oneofKind === 'entrypoint') {
      return node.outgoingEdges[0]?.sinkNodeName
    }
  }
  return undefined
}

export type NodeType = Exclude<NonNullable<NodeProto['node']>['oneofKind'], undefined>
