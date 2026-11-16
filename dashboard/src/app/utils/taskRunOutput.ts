import {
  InlineStructBuilder,
  NodeRun,
  TaskAttempt,
  TaskRunId,
  VariableAssignment,
  VariableValue,
} from 'littlehorse-client/proto'
import { WorkflowDefinition } from '@/types'

const collectNodeOutputNames = (assignment: VariableAssignment | undefined, names: Set<string>): void => {
  if (!assignment) return
  const source = assignment.source
  switch (source.oneofKind) {
    case 'nodeOutput':
      names.add(source.nodeOutput.nodeName)
      break
    case 'expression':
      collectNodeOutputNames(source.expression.lhs, names)
      collectNodeOutputNames(source.expression.rhs, names)
      break
    case 'formatString':
      collectNodeOutputNames(source.formatString.format, names)
      source.formatString.args.forEach(arg => collectNodeOutputNames(arg, names))
      break
    case 'sizeOf':
      collectNodeOutputNames(source.sizeOf.operand, names)
      break
    case 'mapBuilder':
      source.mapBuilder.entries.forEach(entry => {
        collectNodeOutputNames(entry.key, names)
        collectNodeOutputNames(entry.value, names)
      })
      break
    case 'structBuilder':
      collectStructNodeOutputNames(source.structBuilder.value, names)
      break
  }
  if (assignment.path.oneofKind === 'lhPath') {
    assignment.path.lhPath.path.forEach(selector => {
      if (selector.selectorType.oneofKind === 'dynamic') {
        collectNodeOutputNames(selector.selectorType.dynamic, names)
      }
    })
  }
}

const collectStructNodeOutputNames = (builder: InlineStructBuilder | undefined, names: Set<string>): void => {
  for (const { structValue } of Object.values(builder?.fields ?? {})) {
    if (structValue.oneofKind === 'simpleValue') collectNodeOutputNames(structValue.simpleValue, names)
    if (structValue.oneofKind === 'subStructure') collectStructNodeOutputNames(structValue.subStructure, names)
  }
}

/** Only condition operands need runtime outputs. Node-output references use the latest execution in their thread. */
export const getConditionTaskRunIds = (wfSpec: WorkflowDefinition, nodeRuns: NodeRun[]): TaskRunId[] => {
  const referencedNodes = new Map<string, Set<string>>()
  for (const [threadName, thread] of Object.entries(wfSpec.threadSpecs)) {
    const names = new Set<string>()
    for (const node of Object.values(thread.nodes)) {
      for (const { edgeCondition } of node.outgoingEdges) {
        if (edgeCondition.oneofKind === 'legacyCondition') {
          collectNodeOutputNames(edgeCondition.legacyCondition.left, names)
          collectNodeOutputNames(edgeCondition.legacyCondition.right, names)
        } else if (edgeCondition.oneofKind === 'condition') {
          collectNodeOutputNames(edgeCondition.condition, names)
        }
      }
    }
    referencedNodes.set(threadName, names)
  }

  const latestNodeRuns = new Map<string, NodeRun>()
  for (const nodeRun of nodeRuns) {
    if (!nodeRun.id || !referencedNodes.get(nodeRun.threadSpecName)?.has(nodeRun.nodeName)) continue
    const key = JSON.stringify([nodeRun.id.threadRunNumber, nodeRun.nodeName])
    const previous = latestNodeRuns.get(key)
    if (!previous || nodeRun.id.position > previous.id!.position) latestNodeRuns.set(key, nodeRun)
  }
  const ids = new Map<string, TaskRunId>()
  for (const nodeRun of latestNodeRuns.values()) {
    const id = nodeRun.nodeType.oneofKind === 'task' ? nodeRun.nodeType.task.taskRunId : undefined
    if (id) ids.set(id.taskGuid, id)
  }
  return Array.from(ids.values())
}

export const taskAttemptOutput = (attempts: TaskAttempt[]): VariableValue | undefined => {
  for (let i = attempts.length - 1; i >= 0; i--) {
    const result = attempts[i].result
    if (result.oneofKind === 'output') return result.output
  }
  return undefined
}

export const buildNodeOutputValuesFromNodeRuns = (
  nodeRuns: NodeRun[],
  taskRunsByGuid: Map<string, { attempts: TaskAttempt[] }>
): Record<string, VariableValue> => {
  const values: Record<string, VariableValue> = {}
  for (const nodeRun of nodeRuns) {
    const taskGuid = nodeRun.nodeType.oneofKind === 'task' ? nodeRun.nodeType.task.taskRunId?.taskGuid : undefined
    if (!taskGuid || !nodeRun.nodeName) continue
    const taskRun = taskRunsByGuid.get(taskGuid)
    const output = taskRun ? taskAttemptOutput(taskRun.attempts) : undefined
    if (output) values[nodeRun.nodeName] = output
  }
  return values
}
