import { NodeRun, TaskAttempt, VariableValue } from 'littlehorse-client/proto'

export const taskAttemptOutput = (attempts: TaskAttempt[]): VariableValue | undefined => {
  for (let i = attempts.length - 1; i >= 0; i--) {
    const attempt = attempts[i]
    if (attempt.result?.$case === 'output' && attempt.result.value) {
      return attempt.result.value
    }
  }
  return undefined
}

export const buildNodeOutputValuesFromNodeRuns = (
  nodeRuns: NodeRun[],
  taskRunsByGuid: Map<string, { attempts: TaskAttempt[] }>
): Record<string, VariableValue> => {
  const values: Record<string, VariableValue> = {}
  for (const nodeRun of nodeRuns) {
    const taskGuid =
      nodeRun.nodeType?.$case === 'task' ? nodeRun.nodeType.value.taskRunId?.taskGuid : undefined
    if (!taskGuid || !nodeRun.nodeName) continue
    const taskRun = taskRunsByGuid.get(taskGuid)
    const output = taskRun ? taskAttemptOutput(taskRun.attempts) : undefined
    if (output) values[nodeRun.nodeName] = output
  }
  return values
}
