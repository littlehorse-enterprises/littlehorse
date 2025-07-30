import { Node as NodeProto, ThreadSpec } from 'littlehorse-client/proto'
import { Node, NodeProps } from 'reactflow'

export const extractNodes = (spec: ThreadSpec): Node<NodeProto, NodeType>[] => {
  return Object.entries(spec.nodes).map(([id, node]) => {
    const nodeType = node.node!
    return {
      id,
      type: nodeType.$case,
      data: { ...node, ...nodeType.value },
      position: { x: 0, y: 0 },
    }
  })
}

export type NodeRunTypeList = Exclude<
  NodeType,
  'ENTRYPOINT' | 'NOP' | 'EXIT' | 'UNKNOWN_NODE_TYPE' | 'START_MULTIPLE_THREADS'
  >

export type NodeType = NonNullable<NodeProto['node']>['$case']
export type NodeData<T = any> = NodeProps<NodeProto & T>
