import { ThreadSpec, WfSpec } from 'littlehorse-client/proto'
import { Edge, MarkerType } from 'reactflow'
import { ThreadSpecWithName } from '../Diagram'
import { getNodeType } from '../NodeTypes/extractWfSpecNodes'
import { extractEdges } from './extractEdges'

function extractThreadConnectionEdges(threadSpec: ThreadSpec, threadName: string, wfSpec: WfSpec): Edge[] {
  const edges: Edge[] = []

  Object.entries(threadSpec.nodes).forEach(([id, node]) => {
    const type = getNodeType(node)
    if (type === 'START_THREAD') {
      const startedThreadSpecName = node.startThread?.threadSpecName ?? ''
      const sourceId = `${id}:${threadName}`
      const targetId = `0-entrypoint-ENTRYPOINT:${startedThreadSpecName}`

      edges.push({
        id: `${sourceId}>${targetId}:${threadName}`,
        source: sourceId,
        sourceHandle: 'bottom-0',
        target: targetId,
        markerEnd: {
          type: MarkerType.ArrowClosed,
        },
        animated: true,
      })
    }

    if (type === 'WAIT_FOR_THREADS') {
      node.waitForThreads?.threads?.threads.forEach(thread => {
        const startThreadNodeName = thread.threadRunNumber?.variableName ?? ''
        const waitingThreadSpecName =
          Object.entries(threadSpec.nodes).find(([id, node]) => id == startThreadNodeName)?.[1].startThread
            ?.threadSpecName ?? ''
        const waitingThreadSpec = wfSpec.threadSpecs[waitingThreadSpecName]
        const sortedNodes = Object.entries(waitingThreadSpec.nodes).sort(([id, _], [id2, __]) => id.localeCompare(id2))
        const exitNodeId = sortedNodes[sortedNodes.length - 1][0]

        const sourceId = `${exitNodeId}:${waitingThreadSpecName}`
        const targetId = `${id}:${threadName}`
        edges.push({
          id: `${sourceId}>${targetId}`,
          source: sourceId,
          target: targetId,
          targetHandle: 'bottom-0',
          markerEnd: { type: MarkerType.ArrowClosed },
          animated: true,
        })
      })
    }
  })
  return edges
}

export function extractAllEdges(wfSpec: WfSpec, threadSpec: ThreadSpecWithName): Edge[] {
  return [
    ...extractEdges(wfSpec, threadSpec),
    ...Object.entries(wfSpec.threadSpecs).flatMap(([threadName, threadSpec]) => {
      return extractThreadConnectionEdges(threadSpec, threadName, wfSpec)
    }),
  ]
}
