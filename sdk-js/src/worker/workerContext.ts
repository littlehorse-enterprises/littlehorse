import { ScheduledTask } from '../proto/service'
import { WfRunId, TaskRunId, NodeRunId } from '../proto/object_id'

/**
 * Context object provided to task functions during execution. Contains metadata
 * about the current task run and allows logging.
 */
export class WorkerContext {
  private logOutput: string = ''
  private readonly task: ScheduledTask

  constructor(task: ScheduledTask) {
    this.task = task
  }

  /**
   * Returns the WfRunId of the WfRun that triggered this TaskRun.
   */
  getWfRunId(): WfRunId | undefined {
    return this.task.taskRunId?.wfRunId
  }

  /**
   * Returns the TaskRunId of the current TaskRun.
   */
  getTaskRunId(): TaskRunId | undefined {
    return this.task.taskRunId
  }

  /**
   * Returns the attempt number of the current TaskRun (0-indexed).
   * 0 means this is the first attempt; 1 means first retry, etc.
   */
  getAttemptNumber(): number {
    return this.task.attemptNumber
  }

  /**
   * Returns the time at which this TaskRun was scheduled.
   */
  getScheduledTime(): string | undefined {
    return this.task.createdAt
  }

  /**
   * Returns the NodeRunId if this task was triggered by a TASK node.
   */
  getNodeRunId(): NodeRunId | undefined {
    if (this.task.source?.taskRunSource?.$case === 'taskNode') {
      return this.task.source.taskRunSource.value.nodeRunId
    }
    return undefined
  }

  /**
   * Returns a deterministic idempotency key derived from the TaskRunId.
   */
  getIdempotencyKey(): string | undefined {
    const taskRunId = this.task.taskRunId
    if (!taskRunId) return undefined
    const wfRunId = taskRunId.wfRunId?.id ?? ''
    return `${wfRunId}/${taskRunId.taskGuid}`
  }

  /**
   * Appends a log message. Logs are sent back to the server with the task result.
   */
  log(message: string): void {
    if (this.logOutput.length > 0) {
      this.logOutput += '\n'
    }
    this.logOutput += message
  }

  /**
   * Returns the accumulated log output.
   */
  getLogOutput(): string {
    return this.logOutput
  }
}
