import { PutCheckpointResponse_FlowControlContinue, ScheduledTask } from '../proto/service'
import { Timestamp } from '../proto/google/protobuf/timestamp'
import { WfRunId, TaskRunId, NodeRunId } from '../proto/object_id'
import { UserTaskTriggerReference } from '../proto/user_tasks'
import type { LHPublicClient } from '../client'
import { extractVariableValue, toVariableValue } from './variableMapping'
import { LHTypeAdapterRegistry } from '../common/typeAdapters'

/**
 * Collects logs produced inside a checkpointed operation; they are stored
 * with the checkpoint rather than the TaskRun.
 */
export class CheckpointContext {
  private logOutput = ''

  log(message: string): void {
    if (this.logOutput.length > 0) this.logOutput += '\n'
    this.logOutput += message
  }

  getLogOutput(): string {
    return this.logOutput
  }
}

/**
 * Context object provided to task functions during execution. Contains metadata
 * about the current task run and allows logging.
 */
export class WorkerContext {
  private logOutput: string = ''
  private readonly task: ScheduledTask
  private readonly client?: LHPublicClient
  /** How many checkpoints this attempt has reached so far. */
  private checkpointsSoFar = 0

  constructor(
    task: ScheduledTask,
    client?: LHPublicClient,
    private readonly typeAdapters?: LHTypeAdapterRegistry
  ) {
    this.task = task
    this.client = client
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
    return this.task.createdAt ? Timestamp.toDate(this.task.createdAt).toISOString() : undefined
  }

  /**
   * Returns the NodeRunId if this task was triggered by a TASK node.
   */
  getNodeRunId(): NodeRunId | undefined {
    if (this.task.source?.taskRunSource?.oneofKind === 'taskNode') {
      return this.task.source.taskRunSource.taskNode.nodeRunId
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
   * For a User Task reminder TaskRun, the user the UserTaskRun is assigned to.
   * Undefined when this is not a reminder task, or it has no assigned user.
   */
  getUserId(): string | undefined {
    return this.userTaskTrigger()?.userId
  }

  /**
   * For a User Task reminder TaskRun, the group the UserTaskRun is assigned
   * to. Undefined when this is not a reminder task, or it has no group.
   */
  getUserGroup(): string | undefined {
    return this.userTaskTrigger()?.userGroup
  }

  private userTaskTrigger(): UserTaskTriggerReference | undefined {
    const source = this.task.source?.taskRunSource
    return source?.oneofKind === 'userTaskTrigger' ? source.userTaskTrigger : undefined
  }

  /**
   * Runs `operation` once across all attempts of this TaskRun.
   *
   * On a retry the server reports how many checkpoints the previous attempt
   * reached (`totalObservedCheckpoints`); calls up to that count are replayed
   * from the stored value instead of being re-executed, so side effects that
   * already happened — charging a card, sending a message — are not repeated.
   * Mirrors Java's WorkerContext#executeAndCheckpoint.
   */
  async executeAndCheckpoint<T>(operation: (ctx: CheckpointContext) => T | Promise<T>): Promise<T> {
    if (this.client === undefined) {
      throw new Error('executeAndCheckpoint requires a server connection; none was provided to this WorkerContext')
    }

    if (this.checkpointsSoFar < this.task.totalObservedCheckpoints) {
      const checkpointNumber = this.checkpointsSoFar++
      const checkpoint = await this.client.getCheckpoint({
        taskRun: this.task.taskRunId,
        checkpointNumber,
      })
      return extractVariableValue(checkpoint.value) as T
    }

    const checkpointContext = new CheckpointContext()
    const result = await operation(checkpointContext)

    const response = await this.client.putCheckpoint({
      taskRunId: this.task.taskRunId,
      taskAttempt: this.task.attemptNumber,
      value: toVariableValue(result, undefined, this.typeAdapters),
      logs: checkpointContext.getLogOutput(),
    })
    this.checkpointsSoFar++

    if (response.flowControlContinueType !== PutCheckpointResponse_FlowControlContinue.CONTINUE_TASK) {
      throw new Error('Halting execution because the server told us to.')
    }
    return result
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
