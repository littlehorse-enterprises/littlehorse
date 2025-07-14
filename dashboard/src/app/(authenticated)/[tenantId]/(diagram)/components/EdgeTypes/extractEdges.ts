import { getVariable } from '@/app/utils'
import { getComparatorSymbol } from '@/app/utils/comparatorUtils'
import { Edge as EdgeProto, ThreadSpec, VariableAssignment, WfSpec } from 'littlehorse-client/proto'
import { Edge, MarkerType } from 'reactflow'
import { ThreadSpecWithName } from '../Diagram'
import { getNodeType } from '../NodeTypes/extractNodes'

const extractEdgesFromThreadSpec = (wfSpec: WfSpec, threadSpecWithName: ThreadSpecWithName): Edge[] => {
  const edges: Edge[] = []

  const targetMap = new Map<string, number>()
  const sourceMap = new Map<string, number>()
  Object.entries(threadSpecWithName.threadSpec.nodes).forEach(([source, node]) => {
    if (getNodeType(node) === 'START_THREAD') {
      const startThreadNodeName = node.startThread?.threadSpecName
      if (!startThreadNodeName) return

      const moreEdges = extractEdgesFromThreadSpec(wfSpec, {
        name: startThreadNodeName,
        threadSpec: wfSpec.threadSpecs[startThreadNodeName],
      })
      edges.push(...moreEdges)
    }

    node.outgoingEdges.forEach(edge => {
      const sourceIndex = sourceMap.get(source) ?? 0
      let targetIndex = targetMap.get(edge.sinkNodeName) ?? 0
      const sourceTarget = sourceMap.get(edge.sinkNodeName) ?? 0

      if (sourceTarget > 0 && targetIndex !== 0) targetIndex++
      const edgeId = `${source}-${edge.sinkNodeName}`
      const id = sourceIndex === 0 && targetIndex === 0 ? edgeId : `${edgeId}-${sourceIndex}-${targetIndex}`
      targetMap.set(edge.sinkNodeName, targetIndex + 1)
      sourceMap.set(source, sourceIndex + 1)

      const label = extractEdgeLabel(edge)
      edges.push({
        id,
        source: `${source}:${threadSpecWithName.name}`,
        type: 'custom',
        target: `${edge.sinkNodeName}:${threadSpecWithName.name}`,
        label,
        data: edge,
        targetHandle: `target-${targetIndex}`,
        sourceHandle: `source-${sourceIndex}`,
        markerEnd: {
          type: MarkerType.ArrowClosed,
        },
        animated: true,
      })
    })
  })
  return edges
}

const formatVariableValue = (value?: VariableAssignment) => {
  if (value?.literalValue?.str === undefined) return getVariable(value)
  return `"${getVariable(value)}"`
}

const extractEdgeLabel = ({ condition }: EdgeProto) => {
  if (!condition) return

  const { left, right, comparator } = condition
  return `${formatVariableValue(left)} ${getComparatorSymbol(comparator)} ${formatVariableValue(right)}`
}

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
        type: 'custom',
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
          Object.entries(threadSpec.nodes).find(([id]) => id == startThreadNodeName)?.[1].startThread?.threadSpecName ??
          ''
        const waitingThreadSpec = wfSpec.threadSpecs[waitingThreadSpecName]
        const sortedNodes = Object.entries(waitingThreadSpec.nodes).sort(([id, _], [id2, __]) => id.localeCompare(id2))
        const exitNodeId = sortedNodes[sortedNodes.length - 1][0]

        const sourceId = `${exitNodeId}:${waitingThreadSpecName}`
        const targetId = `${id}:${threadName}`
        edges.push({
          id: `${sourceId}>${targetId}`,
          source: sourceId,
          type: 'custom',
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

export function extractEdges(wfSpec: WfSpec, threadSpec: ThreadSpecWithName): Edge[] {
  return [
    ...extractEdgesFromThreadSpec(wfSpec, threadSpec),
    ...Object.entries(wfSpec.threadSpecs).flatMap(([threadName, threadSpec]) => {
      return extractThreadConnectionEdges(threadSpec, threadName, wfSpec)
    }),
  ]
}
