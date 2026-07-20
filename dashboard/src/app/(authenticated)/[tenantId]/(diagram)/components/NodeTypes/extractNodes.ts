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
      node: NodeProto['node'] | { oneofKind: 'cycle'; cycle: {} }
    }
    const cycleNode: ExtendedNode = {
      outgoingEdges: [
        {
          sinkNodeName: targetId,
          variableMutations: [],
          edgeCondition: { oneofKind: undefined },
        },
      ],
      failureHandlers: [],
      node: { oneofKind: 'cycle', cycle: {} },
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
        edgeCondition: { oneofKind: undefined },
      })
    }

    // Remove the original edge that creates the cycle
    sourceNode.outgoingEdges = sourceNode.outgoingEdges.filter(edge => edge.sinkNodeName !== targetId)
  })
  return threadSpec
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
