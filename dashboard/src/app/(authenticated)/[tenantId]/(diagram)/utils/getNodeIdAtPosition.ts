import { ThreadSpec } from 'littlehorse-client/proto'

/**
 * Returns the node id at the given execution position in the thread spec.
 * Traverses from entrypoint following the first outgoing edge at each step.
 */
export const getNodeIdAtPosition = (threadSpec: ThreadSpec, position: number): string | undefined => {
  const { nodes } = threadSpec
  const entrypointId = Object.entries(nodes).find(([, node]) => node.node?.$case === 'entrypoint')?.[0]
  if (!entrypointId) return undefined

  let currentId = entrypointId
  for (let i = 0; i <= position; i++) {
    if (i === position) return currentId
    const node = nodes[currentId]
    const firstEdge = node?.outgoingEdges?.[0]
    if (!firstEdge?.sinkNodeName) return undefined
    currentId = firstEdge.sinkNodeName
  }
  return undefined
}
