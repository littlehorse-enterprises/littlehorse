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
  /** The header's width; the laid-out box is never narrower. */
  minWidth?: number
  /** A collapsed chip's fixed footprint. */
  footprint?: { w: number; h: number }
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

/** What the diagram currently shows of each handler group. */
export type HandlerView = {
  /** Groups left out entirely (with everything nested in them). Defaults to drawing every group. */
  isVisible?: (groupId: string) => boolean
  /** Collapsed groups draw as a compact chip on the throwing node's exception edge. */
  isCollapsed?: (groupId: string) => boolean
  /** The handler ThreadRun that ran for a group, when looking at a WfRun. */
  runOf?: (groupId: string) => HandlerRun | undefined
}

/** The top-level node whose handlers a group belongs to (nested groups belong to the same node). */
export const rootSourceNodeId = (groupId: string): string | undefined =>
  /^handler:(.+):\d+$/.exec(groupId.split('/')[0])?.[1]

/**
 * Inlines every node-level failure handler of `threadName` next to the node
 * that can throw, BPMN-style: the handler ThreadSpec becomes a box holding
 * its own entry, nodes and exits; an exception edge leaves the throwing node
 * from below into the handler's entry, and resume edges run from the
 * handler's normal exits back to wherever the throwing node would have gone
 * next. A failing exit (rethrow) has no resume edge. Nodes inside a handler
 * can have handlers of their own, which nest as boxes inside that handler's
 * box. A collapsed handler is a single chip instead of a box.
 */
export const extractExceptionHandlers = (
  spec: WorkflowDefinition,
  threadName: string,
  view: HandlerView = {}
): { nodes: Node[]; edges: Edge<DiagramEdgeData>[] } => {
  const nodes: Node[] = []
  const edges: Edge<DiagramEdgeData>[] = []
  const { isVisible = () => true, isCollapsed = () => false, runOf = () => undefined } = view

  /**
   * `idOf` maps a node of `threadSpec` to its diagram id.
   */
  const inline = (threadSpec: ThreadSpec, idOf: (id: string) => string, parentGroupId?: string, depth = 0) => {
    if (depth >= MAX_HANDLER_DEPTH) return
    for (const [sourceNodeId, sourceNode] of Object.entries(threadSpec.nodes)) {
      sourceNode.failureHandlers.forEach((handler, index) => {
        const handlerSpec = spec.threadSpecs[handler.handlerSpecName]
        if (!handlerSpec) return
        const groupId = `${parentGroupId ? `${parentGroupId}/` : ''}handler:${sourceNodeId}:${index}`
        if (!isVisible(groupId)) return
        getCycleNodes(handlerSpec)
        const childId = (id: string) => `${groupId}/${id}`

        const entrypointId = Object.keys(handlerSpec.nodes).find(
          id => handlerSpec.nodes[id].node.oneofKind === 'entrypoint'
        )
        if (!entrypointId) return
        const isExit = (id: string) => handlerSpec.nodes[id]?.node.oneofKind === 'exit'
        const resumingExits = Object.keys(handlerSpec.nodes).filter(id => isExit(id) && !isFailingExit(handlerSpec, id))
        const resumeTargets = sourceNode.outgoingEdges.map(next => idOf(next.sinkNodeName))
        const collapsed = isCollapsed(groupId)

        const data: ExceptionHandlerData = {
          groupId,
          parentGroupId,
          sourceNodeId,
          handlerIndex: index,
          failureHandlers: sourceNode.failureHandlers,
          handlerSpecName: handler.handlerSpecName,
          catchLabel: catchLabel(handler),
          rethrows: resumingExits.length === 0,
          run: runOf(groupId),
        }

        if (collapsed) {
          nodes.push({
            id: groupId,
            type: 'exceptionHandlerChip',
            position: { x: 0, y: 0 },
            selectable: false,
            focusable: false,
            data: { ...data, footprint: chipFootprint(data) },
          })
          edges.push(
            flowEdge(`${groupId}/throw`, idOf(sourceNodeId), groupId, 'source-failure', EXCEPTION_COLOR, {
              isExceptionEdge: true,
              handlerGroupId: groupId,
            })
          )
          return
        }

        nodes.push({
          id: groupId,
          type: 'exceptionHandler',
          position: { x: 0, y: 0 },
          selectable: false,
          focusable: false,
          zIndex: 0,
          data: { ...data, minWidth: handlerHeaderWidth(data) },
        })

        for (const child of extractNodes(handlerSpec)) {
          nodes.push({
            ...child,
            id: childId(child.id),
            data: { ...child.data, groupId, specNodeId: child.id } as typeof child.data & HandlerChildData,
          })
        }

        for (const edge of extractEdges(handlerSpec)) {
          edges.push({ ...edge, id: childId(edge.id), source: childId(edge.source), target: childId(edge.target) })
        }

        edges.push(
          flowEdge(`${groupId}/throw`, idOf(sourceNodeId), childId(entrypointId), 'source-failure', EXCEPTION_COLOR, {
            isExceptionEdge: true,
            handlerGroupId: groupId,
          })
        )

        resumingExits.forEach(exitId =>
          resumeTargets.forEach(target =>
            edges.push(
              flowEdge(`${groupId}/resume-${exitId}-${target}`, childId(exitId), target, 'source-0', undefined, {
                isResumeEdge: true,
                handlerGroupId: groupId,
              })
            )
          )
        )

        inline(handlerSpec, childId, groupId, depth + 1)
      })
    }
  }

  const threadSpec = spec.threadSpecs[threadName]
  if (threadSpec) inline(threadSpec, id => id)
  return { nodes, edges }
}

/** Every handler group of a thread, expanded, whether or not it is drawn. */
export const listHandlerGroups = (spec: WorkflowDefinition, threadName: string): ExceptionHandlerData[] =>
  extractExceptionHandlers(spec, threadName)
    .nodes.filter(node => node.type === 'exceptionHandler')
    .map(node => node.data as ExceptionHandlerData)

export const RUN_STATE: Partial<Record<LHStatus, { text: string; className: string }>> = {
  [LHStatus.COMPLETED]: { text: 'handled', className: 'bg-emerald-100 text-emerald-800' },
  [LHStatus.RUNNING]: { text: 'handling', className: 'bg-blue-100 text-blue-800' },
  [LHStatus.ERROR]: { text: 'handler failed', className: 'bg-red-100 text-red-800' },
  [LHStatus.EXCEPTION]: { text: 'rethrown', className: 'bg-red-100 text-red-800' },
  [LHStatus.HALTED]: { text: 'halted', className: 'bg-gray-100 text-gray-800' },
}

/** The badge a handler shows next to its name, if any. */
export const handlerBadge = (data: Pick<ExceptionHandlerData, 'run' | 'rethrows'>) =>
  data.run
    ? RUN_STATE[data.run.status]
    : data.rethrows
      ? { text: 'rethrows', className: 'bg-red-100 text-red-800' }
      : undefined

// Footprints are estimated from text length like nodeDimensions does,
// erring wide: 9px text runs about 5.4px a character (6px in mono), 8px
// uppercase badges about 5.6px.
const monoWidth = (text: string) => Math.ceil(text.length * 6) + 8
const textWidth = (text: string) => Math.ceil(text.length * 5.4)
const badgeWidth = (text?: string) => (text ? Math.ceil(text.length * 5.6) + 8 : 0)

/**
 * The narrowest a handler box can be without clipping its header
 * (toggle, icon, "On", the failure, the handler name and its badge).
 */
export const handlerHeaderWidth = (data: ExceptionHandlerData): number => {
  const label = data.run ? data.run.failureName : data.catchLabel
  const gaps = 4 * 6
  // Past this the name truncates (its full text is the tooltip) instead of stretching the box.
  const name = Math.min(textWidth(data.handlerSpecName), 240)
  return 16 + 12 + 10 + 14 + monoWidth(label) + name + badgeWidth(handlerBadge(data)?.text) + gaps
}

export const CHIP_HEIGHT = 22

/** A collapsed handler: icon, the failure, its badge and the expand toggle. */
export const chipFootprint = (data: ExceptionHandlerData): { w: number; h: number } => {
  const label = data.run ? data.run.failureName : data.catchLabel
  const badge = badgeWidth(handlerBadge(data)?.text)
  return { w: 12 + 10 + monoWidth(label) + (badge ? badge + 4 : 0) + 12 + 3 * 4, h: CHIP_HEIGHT }
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
