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
/**
 * Detects cycles in the graph using DFS and topological ordering
 * Returns a set of edges that create cycles (back edges)
 */
const detectCycleEdges = (nodes: ThreadSpec['nodes']): Set<string> => {
  const visited = new Set<string>()
  const recursionStack = new Set<string>()
  const cycleEdges = new Set<string>()

  const dfs = (nodeId: string, path: string[] = []): void => {
    visited.add(nodeId)
    recursionStack.add(nodeId)

    const node = nodes[nodeId]
    if (!node) return

    for (const edge of node.outgoingEdges) {
      const targetId = edge.sinkNodeName

      // If target is in recursion stack, we found a back edge (cycle)
      if (recursionStack.has(targetId)) {
        cycleEdges.add(`${nodeId}->${targetId}`)
      } else if (!visited.has(targetId)) {
        dfs(targetId, [...path, nodeId])
      }
    }

    recursionStack.delete(nodeId)
  }

  // Start DFS from all nodes (to handle disconnected components)
  Object.keys(nodes).forEach(nodeId => {
    if (!visited.has(nodeId)) {
      dfs(nodeId)
    }
  })

  return cycleEdges
}

export const getCycleNodes = (threadSpec: ThreadSpec) => {
  // Check if cycle nodes already exist (function already executed)
  const hasCycleNodes = Object.keys(threadSpec.nodes).some(nodeId => nodeId.startsWith('cycle-'))
  if (hasCycleNodes) {
    return threadSpec // Already processed, skip
  }

  // Detect all cycle edges in the graph
  const cycleEdges = detectCycleEdges(threadSpec.nodes)
  if (cycleEdges.size === 0) return
  // Process each cycle edge and insert cycle nodes
  cycleEdges.forEach(edgeKey => {
    const [sourceId, targetId] = edgeKey.split('->')
    const sourceNode = threadSpec.nodes[sourceId]

    if (!sourceNode) return

    // Find the edge that creates the cycle
    const cycleEdge = sourceNode.outgoingEdges.find(edge => edge.sinkNodeName === targetId)

    if (!cycleEdge) return

    const cycleNodeId = `cycle-${sourceId}-${targetId}`
    type ExtendedNode = Omit<NodeProto, 'node'> & {
      node: NodeProto['node'] | { $case: 'cycle'; value: {} }
    }
    const cycleNode: ExtendedNode = {
      outgoingEdges: [
        {
          sinkNodeName: targetId,
          variableMutations: [],
        },
      ],
      failureHandlers: [],
      node: { $case: 'cycle', value: {} },
    }
    // Add the cycle node to the graph
    threadSpec.nodes[cycleNodeId] = cycleNode as unknown as NodeProto

    // Remove back edge from target to source (if it exists)
    if (threadSpec.nodes[targetId]) {
      threadSpec.nodes[targetId].outgoingEdges = threadSpec.nodes[targetId].outgoingEdges.filter(
        edge => edge.sinkNodeName !== sourceId
      )
    }

    // Replace the direct cycle edge with an edge to the cycle node
    if (!sourceNode.outgoingEdges.some(e => e.sinkNodeName === cycleNodeId)) {
      sourceNode.outgoingEdges.push({
        sinkNodeName: cycleNodeId,
        variableMutations: [],
      })
    }

    // Remove the original edge that creates the cycle
    sourceNode.outgoingEdges = sourceNode.outgoingEdges.filter(edge => edge.sinkNodeName !== targetId)
  })
  return threadSpec
}
export type NodeRunTypeList = Exclude<
  NodeType,
  'ENTRYPOINT' | 'NOP' | 'EXIT' | 'UNKNOWN_NODE_TYPE' | 'START_MULTIPLE_THREADS'
>

export type NodeType = NonNullable<NodeProto['node']>['$case']
export type NodeData<T = any> = NodeProps<NodeProto & T>
