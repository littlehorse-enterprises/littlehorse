import { CustomEdge } from '@/types/node'
import { getComparatorSymbol } from '@/utils/data/getComparatorSymbol'
import { getVariable } from '@/utils/data/variables'
import {
  Edge as LHEdge,
  Node as LHNode,
  ThreadSpec,
  VariableAssignment,
  WaitForThreadsNode_ThreadToWaitFor,
  WfSpec,
} from 'littlehorse-client/proto'

export function extractEdges(wfSpec: WfSpec): CustomEdge[] {
  return [
    ...extractEdgesFromThreadSpec(wfSpec, wfSpec.threadSpecs[wfSpec.entrypointThreadName]),
    ...Object.entries(wfSpec.threadSpecs).flatMap(function ([threadName, threadSpec]) {
      return extractThreadConnectionEdges(threadSpec, threadName, wfSpec)
    }),
  ]
}

function extractEdgesFromThreadSpec(wfSpec: WfSpec, threadSpec: ThreadSpec): CustomEdge[] {
  const threadSpecName = Object.keys(wfSpec.threadSpecs).find(function (key) {
    return wfSpec.threadSpecs[key] === threadSpec
  })
  const edges: CustomEdge[] = []

  const targetMap = new Map<string, number>()
  const sourceMap = new Map<string, number>()

  Object.entries(threadSpec.nodes).forEach(function ([source, node]) {
    const nodeCase = node.node?.$case
    if (nodeCase === 'startThread') {
      const startThreadNodeName = node.node.startThread?.threadSpecName
      if (!startThreadNodeName) return

      const moreEdges = extractEdgesFromThreadSpec(wfSpec, wfSpec.threadSpecs[startThreadNodeName])
      edges.push(...moreEdges)
    }

    node.outgoingEdges.forEach(function (edge) {
      const sourceIndex = sourceMap.get(source) ?? 0
      let targetIndex = targetMap.get(edge.sinkNodeName) ?? 0
      const sourceTarget = sourceMap.get(edge.sinkNodeName) ?? 0

      if (sourceTarget > 0 && targetIndex !== 0) targetIndex++
      const edgeId = `${source}-${edge.sinkNodeName}:${threadSpecName}`
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

  Object.entries(threadSpec.nodes).forEach(function ([id, lhNode]) {
    const nodeCase = lhNode.node?.$case
    if (nodeCase === 'startThread') {
      const startedThreadSpecName = lhNode.node.startThread?.threadSpecName
      if (!startedThreadSpecName) return

      const sourceId = `${id}:${threadName}`
      const targetId = `0-entrypoint-ENTRYPOINT:${startedThreadSpecName}`

      edges.push({
        id: `${sourceId}>${targetId}:${threadName}`,
        source: sourceId,
        target: targetId,
      })
    }

    if (nodeCase === 'waitForThreads') {
      const waitForThreadsEdges = extractWaitForThreadsEdges(lhNode, id, threadName, threadSpec, wfSpec)
      edges.push(...waitForThreadsEdges)
    }
  })
  return edges
}

function extractWaitForThreadsEdges(
  lhNode: LHNode,
  waitForThreadsNodeId: string,
  threadName: string,
  threadSpec: ThreadSpec,
  wfSpec: WfSpec
): CustomEdge[] {
  const edges: CustomEdge[] = []

  if (lhNode.node?.$case !== 'waitForThreads' || lhNode.node.waitForThreads.threadsToWaitFor?.$case !== 'threads') {
    return edges
  }

  lhNode.node.waitForThreads.threadsToWaitFor.threads.threads.forEach(function (
    thread: WaitForThreadsNode_ThreadToWaitFor
  ) {
    const edge = createWaitForThreadEdge(thread, waitForThreadsNodeId, threadName, threadSpec, wfSpec)
    if (edge) {
      edges.push(edge)
    }
  })

  return edges
}

function createWaitForThreadEdge(
  thread: WaitForThreadsNode_ThreadToWaitFor,
  waitForThreadsNodeId: string,
  threadName: string,
  threadSpec: ThreadSpec,
  wfSpec: WfSpec
): CustomEdge | null {
  const startThreadNodeName = extractStartThreadNodeName(thread)
  if (!startThreadNodeName) {
    return null
  }

  const startThreadNode = findStartThreadNode(threadSpec, startThreadNodeName)
  if (!startThreadNode) {
    return null
  }

  const waitingThreadSpecName = extractThreadSpecName(startThreadNode)
  if (!waitingThreadSpecName) {
    return null
  }

  const waitingThreadSpec = wfSpec.threadSpecs[waitingThreadSpecName]
  if (!waitingThreadSpec) {
    return null
  }

  const exitNodeId = findExitNodeId(waitingThreadSpec)
  if (!exitNodeId) {
    return null
  }

  const sourceId = `${exitNodeId}:${waitingThreadSpecName}`
  const targetId = `${waitForThreadsNodeId}:${threadName}`

  return {
    id: `${sourceId}>${targetId}`,
    source: sourceId,
    target: targetId,
  }
}

function extractStartThreadNodeName(thread: WaitForThreadsNode_ThreadToWaitFor): string | null {
  if (thread.threadRunNumber?.source?.$case === 'variableName') {
    return thread.threadRunNumber.source.variableName
  }
  return null
}

function findStartThreadNode(threadSpec: ThreadSpec, startThreadNodeName: string): LHNode | null {
  const nodeEntry = Object.entries(threadSpec.nodes).find(function ([id, node]) {
    return id === startThreadNodeName && node.node?.$case === 'startThread'
  })
  return nodeEntry?.[1] || null
}

function extractThreadSpecName(startThreadNode: LHNode): string | null {
  if (startThreadNode?.node?.$case === 'startThread') {
    return startThreadNode.node.startThread.threadSpecName
  }
  return null
}

function findExitNodeId(threadSpec: ThreadSpec): string | null {
  const sortedNodes = Object.entries(threadSpec.nodes).sort(function ([id1], [id2]) {
    return id1.localeCompare(id2)
  })

  if (sortedNodes.length === 0) {
    return null
  }

  return sortedNodes[sortedNodes.length - 1][0]
}

function formatVariableValue(value?: VariableAssignment) {
  if (value?.source?.$case === 'literalValue' && value.source.literalValue.value === undefined)
    return getVariable(value)
  return `"${getVariable(value)}"`
}

function extractEdgeLabel({ condition }: LHEdge) {
  if (!condition) return

  const { left, right, comparator } = condition
  return `${formatVariableValue(left)} ${getComparatorSymbol(comparator)} ${formatVariableValue(right)}`
}
