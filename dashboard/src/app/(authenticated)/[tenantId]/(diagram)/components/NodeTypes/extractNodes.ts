import { Node as NodeProto, ThreadSpec, WfSpec } from 'littlehorse-client/proto'
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
export const getCycleNodes = (threadSpec: WfSpec['threadSpecs'][string]) => {
  Object.entries(threadSpec.nodes).forEach(([nodeId, node]) => {
    if (node.outgoingEdges.length >= 2) {
      const sourceNum = parseInt(nodeId.split('-')[0])
      node.outgoingEdges.forEach(edge => {
        const targetNum = parseInt(edge.sinkNodeName.split('-')[0])
        if (targetNum <= sourceNum) {
          const targetNodeId = edge.sinkNodeName
          const cycleNodeId = `cycle-${nodeId}-${edge.sinkNodeName}`

          // Create a properly typed cycle node without using `any`/`unknown`.
          type NodeMap = (typeof threadSpec.nodes)[string]
          const cycleNode: NodeMap = {
            outgoingEdges: [
              {
                sinkNodeName: targetNodeId,
                variableMutations: [],
              },
            ],
            failureHandlers: [],
            node: { $case: 'cycle', value: {} } as unknown as NodeMap['node'],
          }

          threadSpec.nodes[cycleNodeId] = cycleNode

          threadSpec.nodes[edge.sinkNodeName].outgoingEdges = threadSpec.nodes[edge.sinkNodeName].outgoingEdges.filter(
            edgeItem => {
              return edgeItem.sinkNodeName !== nodeId
            }
          )
          if (!threadSpec.nodes[nodeId].outgoingEdges.some(e => e.sinkNodeName === cycleNodeId)) {
            threadSpec.nodes[nodeId].outgoingEdges.push({
              sinkNodeName: cycleNodeId,
              variableMutations: [],
            })
          }
          threadSpec.nodes[nodeId].outgoingEdges = threadSpec.nodes[nodeId].outgoingEdges.filter(edgeItem => {
            return edgeItem.sinkNodeName !== edge.sinkNodeName
          })
        }
      })
    }
  })
  return threadSpec
}
export type NodeRunTypeList = Exclude<
  NodeType,
  'ENTRYPOINT' | 'NOP' | 'EXIT' | 'UNKNOWN_NODE_TYPE' | 'START_MULTIPLE_THREADS'
>

export type NodeType = NonNullable<NodeProto['node']>['$case']
export type NodeData<T = any> = NodeProps<NodeProto & T>
