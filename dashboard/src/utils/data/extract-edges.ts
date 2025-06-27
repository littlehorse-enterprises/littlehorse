import { getVariable } from '@/utils/data/variables'
import { getComparatorSymbol } from '@/utils/data/getComparatorSymbol'
import { Edge as EdgeProto, ThreadSpec, VariableAssignment, WfSpec } from 'littlehorse-client/proto'
import { CustomEdge } from '@/types/node'
import { getNodeAndType } from './node'

function extractEdgesFromThreadSpec(wfSpec: WfSpec, threadSpec: ThreadSpec): CustomEdge[] {
  const threadSpecName = Object.keys(wfSpec.threadSpecs).find(function (key) {
    return wfSpec.threadSpecs[key] === threadSpec
  })
  const edges: CustomEdge[] = []

  const targetMap = new Map<string, number>()
  const sourceMap = new Map<string, number>()

  Object.entries(threadSpec.nodes).forEach(function ([source, node]) {
    if (getNodeAndType(node).type === 'START_THREAD') {
      const startThreadNodeName = node.startThread?.threadSpecName
      if (!startThreadNodeName) return

      const moreEdges = extractEdgesFromThreadSpec(wfSpec, wfSpec.threadSpecs[startThreadNodeName])
      edges.push(...moreEdges)
    }

    node.outgoingEdges.forEach(function (edge) {
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
        source: `${source}:${threadSpecName}`,
        target: `${edge.sinkNodeName}:${threadSpecName}`,
        label,
        data: { edge },
      })
    })
  })
  return edges
}

function extractThreadConnectionEdges(threadSpec: ThreadSpec, threadName: string, wfSpec: WfSpec): CustomEdge[] {
  const edges: CustomEdge[] = []

  Object.entries(threadSpec.nodes).forEach(function ([id, node]) {
    const { type } = getNodeAndType(node)
    if (type === 'START_THREAD') {
      const startedThreadSpecName = node.startThread?.threadSpecName ?? ''
      const sourceId = `${id}:${threadName}`
      const targetId = `0-entrypoint-ENTRYPOINT:${startedThreadSpecName}`

      edges.push({
        id: `${sourceId}>${targetId}:${threadName}`,
        source: sourceId,
        target: targetId,
      })
    }

    if (type === 'WAIT_FOR_THREADS') {
      node.waitForThreads?.threads?.threads.forEach(function (thread) {
        const startThreadNodeName = thread.threadRunNumber?.variableName ?? ''
        const waitingThreadSpecName =
          Object.entries(threadSpec.nodes).find(function ([id]) {
            return id == startThreadNodeName
          })?.[1].startThread?.threadSpecName ?? ''
        const waitingThreadSpec = wfSpec.threadSpecs[waitingThreadSpecName]
        const sortedNodes = Object.entries(waitingThreadSpec.nodes).sort(function ([id], [id2]) {
          return id.localeCompare(id2)
        })
        const exitNodeId = sortedNodes[sortedNodes.length - 1][0]

        const sourceId = `${exitNodeId}:${waitingThreadSpecName}`
        const targetId = `${id}:${threadName}`
        edges.push({
          id: `${sourceId}>${targetId}`,
          source: sourceId,
          target: targetId,
        })
      })
    }
  })
  return edges
}

export function extractEdges(wfSpec: WfSpec): CustomEdge[] {
  return [
    ...extractEdgesFromThreadSpec(wfSpec, wfSpec.threadSpecs[wfSpec.entrypointThreadName]),
    ...Object.entries(wfSpec.threadSpecs).flatMap(function ([threadName, threadSpec]) {
      return extractThreadConnectionEdges(threadSpec, threadName, wfSpec)
    }),
  ]
}

function formatVariableValue(value?: VariableAssignment) {
  if (value?.literalValue?.str === undefined) return getVariable(value)
  return `"${getVariable(value)}"`
}

function extractEdgeLabel({ condition }: EdgeProto) {
  if (!condition) return

  const { left, right, comparator } = condition
  return `${formatVariableValue(left)} ${getComparatorSymbol(comparator)} ${formatVariableValue(right)}`
}
