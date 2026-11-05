import type { ThreadRunWithNodeRuns } from '@/app/actions/getWfRun'
import { WorkflowDefinition } from '@/types'
import {
  FailureHandlerDef,
  FailureHandlerDef_LHFailureType,
  LHStatus,
  NodeRun,
  ThreadSpec,
} from 'littlehorse-client/proto'
import { Edge, MarkerType, Node } from 'reactflow'
import { extractEdges, type DiagramEdgeData } from '../EdgeTypes/extractEdges'
import { extractNodes, getCycleNodes } from '../NodeTypes/extractNodes'

export const EXCEPTION_COLOR = '#f43f5e'

export type HandlerRun = {
  number: number
  status: LHStatus
  failureName: string
  failureMessage: string
}

export type ExceptionHandlerData = {
  groupId: string
  /** The handler box this one is nested in, if the throwing node is itself inside a handler. */
  parentGroupId?: string
  /** The throwing node's id in its own ThreadSpec. */
  sourceNodeId: string
  handlerIndex: number
  failureHandlers: FailureHandlerDef[]
  handlerSpecName: string
  catchLabel: string
  rethrows: boolean
  size?: { w: number; h: number }
  run?: HandlerRun
  fade?: boolean
}

export type HandlerChildData = {
  groupId: string
  specNodeId: string
}

export const catchLabel = (handler: FailureHandlerDef): string => {
  const toCatch = handler.failureToCatch
  if (toCatch.oneofKind === 'specificFailure') return toCatch.specificFailure
  if (toCatch.oneofKind === 'anyFailureOfType')
    return toCatch.anyFailureOfType === FailureHandlerDef_LHFailureType.FAILURE_TYPE_ERROR
      ? 'any ERROR'
      : 'any EXCEPTION'
  return 'any failure'
}

// LittleHorse ERRORs are the server's UPPER_SNAKE names (TASK_FAILURE,
// TIMEOUT, ...); EXCEPTIONs are user-defined kebab-case names.
const isError = (failureName: string) => /^[A-Z_]+$/.test(failureName)

export const handlerCatches = (handler: FailureHandlerDef, failureName: string): boolean => {
  const toCatch = handler.failureToCatch
  if (toCatch.oneofKind === 'specificFailure') return toCatch.specificFailure === failureName
  if (toCatch.oneofKind === 'anyFailureOfType')
    return (toCatch.anyFailureOfType === FailureHandlerDef_LHFailureType.FAILURE_TYPE_ERROR) === isError(failureName)
  return true
}

const isFailingExit = (spec: ThreadSpec, nodeId: string) => {
  const result = spec.nodes[nodeId]?.node
  return result?.oneofKind === 'exit' && result.exit.result.oneofKind === 'failureDef'
}

// Guards against a handler whose own nodes are handled by itself.
const MAX_HANDLER_DEPTH = 4

/**
 * Inlines every node-level failure handler of `threadName` next to the node
 * that can throw: the handler ThreadSpec becomes a group of nodes, an
 * exception edge leaves the throwing node from below, and resume edges run
 * from the handler's last nodes back to wherever the throwing node would
 * have gone next. Nodes inside a handler can have handlers of their own,
 * which nest as groups inside that handler's group.
 */
export const extractExceptionHandlers = (
  spec: WorkflowDefinition,
  threadName: string
): { nodes: Node[]; edges: Edge<DiagramEdgeData>[] } => {
  const nodes: Node[] = []
  const edges: Edge<DiagramEdgeData>[] = []

  /**
   * `idOf` maps a node of `threadSpec` to its diagram id; `targetsOf` maps it
   * to the diagram nodes the flow reaches there (a hidden handler exit
   * stands for wherever that handler resumes).
   */
  const inline = (
    threadSpec: ThreadSpec,
    idOf: (id: string) => string,
    targetsOf: (id: string) => string[],
    parentGroupId?: string,
    depth = 0
  ) => {
    if (depth >= MAX_HANDLER_DEPTH) return
    for (const [sourceNodeId, sourceNode] of Object.entries(threadSpec.nodes)) {
      sourceNode.failureHandlers.forEach((handler, index) => {
        const handlerSpec = spec.threadSpecs[handler.handlerSpecName]
        if (!handlerSpec) return
        getCycleNodes(handlerSpec)
        const groupId = `${parentGroupId ? `${parentGroupId}/` : ''}handler:${sourceNodeId}:${index}`
        const childId = (id: string) => `${groupId}/${id}`

        const entrypointId = Object.keys(handlerSpec.nodes).find(
          id => handlerSpec.nodes[id].node.oneofKind === 'entrypoint'
        )
        const isExit = (id: string) => handlerSpec.nodes[id]?.node.oneofKind === 'exit'
        const resumingExits = Object.keys(handlerSpec.nodes).filter(id => isExit(id) && !isFailingExit(handlerSpec, id))
        const resumeTargets = [...new Set(sourceNode.outgoingEdges.flatMap(next => targetsOf(next.sinkNodeName)))]
        const childTargets = (id: string) =>
          resumingExits.includes(id) ? resumeTargets : isExit(id) ? [] : [childId(id)]
        const children = extractNodes(handlerSpec).filter(child => child.id !== entrypointId && !isExit(child.id))
        if (children.length === 0) return

        nodes.push({
          id: groupId,
          type: 'exceptionHandler',
          position: { x: 0, y: 0 },
          selectable: false,
          focusable: false,
          zIndex: 0,
          data: {
            groupId,
            parentGroupId,
            sourceNodeId,
            handlerIndex: index,
            failureHandlers: sourceNode.failureHandlers,
            handlerSpecName: handler.handlerSpecName,
            catchLabel: catchLabel(handler),
            rethrows: resumingExits.length === 0,
          } satisfies ExceptionHandlerData,
        })

        for (const child of children) {
          nodes.push({
            ...child,
            id: childId(child.id),
            data: { ...child.data, groupId, specNodeId: child.id } as typeof child.data & HandlerChildData,
          })
        }

        // Exits stay hidden: whatever led to a normal exit resumes the parent.
        const lastNodes = new Set<string>()
        for (const edge of extractEdges(handlerSpec)) {
          if (edge.source === entrypointId) continue
          if (resumingExits.includes(edge.target)) lastNodes.add(edge.source)
          if (isExit(edge.target)) continue
          edges.push({ ...edge, id: childId(edge.id), source: childId(edge.source), target: childId(edge.target) })
        }

        const firstNodes = entrypointId ? handlerSpec.nodes[entrypointId].outgoingEdges.map(e => e.sinkNodeName) : []
        firstNodes
          .filter(target => !isExit(target))
          .forEach((target, i) =>
            edges.push(
              flowEdge(
                `${groupId}/throw-${i}`,
                idOf(sourceNodeId),
                childId(target),
                'source-failure',
                EXCEPTION_COLOR,
                {
                  isExceptionEdge: true,
                  handlerGroupId: groupId,
                }
              )
            )
          )

        lastNodes.forEach(lastId =>
          resumeTargets.forEach(target =>
            edges.push(
              flowEdge(`${groupId}/resume-${lastId}-${target}`, childId(lastId), target, 'source-0', undefined, {
                isResumeEdge: true,
                handlerGroupId: groupId,
              })
            )
          )
        )

        inline(handlerSpec, childId, childTargets, groupId, depth + 1)
      })
    }
  }

  const threadSpec = spec.threadSpecs[threadName]
  if (threadSpec)
    inline(
      threadSpec,
      id => id,
      id => [id]
    )
  return { nodes, edges }
}

const flowEdge = (
  id: string,
  source: string,
  target: string,
  sourceHandle: string,
  color: string | undefined,
  flags: Pick<DiagramEdgeData, 'isExceptionEdge' | 'isResumeEdge' | 'handlerGroupId'>
): Edge<DiagramEdgeData> => ({
  id,
  source,
  target,
  type: 'custom',
  sourceHandle,
  targetHandle: 'target-0',
  data: {
    sinkNodeName: target,
    variableMutations: [],
    edgeCondition: { oneofKind: undefined },
    ...flags,
  },
  markerEnd: { type: MarkerType.ArrowClosed, ...(color && { color }) },
  animated: true,
})

/**
 * For each inlined handler group, the handler ThreadRun the server actually
 * started for the throwing node (latest attempt wins). The server runs the
 * first FailureHandlerDef that catches the failure, so the group is the first
 * one whose catch matches and whose ThreadSpec is the one that ran. A nested
 * group looks for its throwing node among its parent handler's NodeRuns.
 */
export const findHandlerRuns = (
  handlerGroups: ExceptionHandlerData[],
  threadNodeRuns: NodeRun[] | undefined,
  threadRuns: ThreadRunWithNodeRuns[]
): Map<string, { run: HandlerRun; nodeRuns: NodeRun[] }> => {
  const result = new Map<string, { run: HandlerRun; nodeRuns: NodeRun[] }>()
  // extractExceptionHandlers emits parents before the groups nested in them.
  for (const group of handlerGroups) {
    const containerRuns = group.parentGroupId ? result.get(group.parentGroupId)?.nodeRuns : threadNodeRuns
    const sourceRuns = (containerRuns ?? [])
      .filter(nodeRun => nodeRun.nodeName === group.sourceNodeId)
      .sort((a, b) => (a.id?.position ?? 0) - (b.id?.position ?? 0))
    for (const nodeRun of sourceRuns) {
      for (const failure of nodeRun.failures) {
        if (failure.failureHandlerThreadrunId === undefined) continue
        const handlerThread = threadRuns.find(tr => tr.number === failure.failureHandlerThreadrunId)
        if (!handlerThread) continue
        const index = group.failureHandlers.findIndex(
          h => h.handlerSpecName === handlerThread.threadSpecName && handlerCatches(h, failure.failureName)
        )
        if (index !== group.handlerIndex) continue
        result.set(group.groupId, {
          run: {
            number: handlerThread.number,
            status: handlerThread.status,
            failureName: failure.failureName,
            failureMessage: failure.message,
          },
          nodeRuns: handlerThread.nodeRuns,
        })
      }
    }
  }
  return result
}
