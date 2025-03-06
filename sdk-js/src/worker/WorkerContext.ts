import { ScheduledTask } from '../proto/service';
import { UserTaskTriggerReference } from '../proto/user_tasks';

/**
 * WorkerContext provides runtime information about the WfRun and NodeRun
 * being executed by the Task Worker.
 */
export class WorkerContext {
  private stderr: string = '';
  private task: ScheduledTask;
  private scheduleTime: Date;
  
  constructor(task: ScheduledTask, scheduleTime: Date = new Date()) {
    this.task = task;
    this.scheduleTime = scheduleTime;
  }
  
  /**
   * Returns the ID of the WfRun for the NodeRun that's being executed.
   */
  getWfRunId(): string | undefined {
    const source = this.task.source;
    if (source?.taskNode) {
      return source.taskNode.nodeRunId?.wfRunId?.id;
    } else if (source?.userTaskTrigger) {
      return source.userTaskTrigger.nodeRunId?.wfRunId?.id;
    }
  }
  
  /**
   * Returns the NodeRun ID for the Task that was scheduled.
   */
  getNodeRunId() {
    const source = this.task.source;
    if (source?.taskNode) {
      return source.taskNode.nodeRunId;
    }
    if (source?.userTaskTrigger) {
      return source.userTaskTrigger.nodeRunId;
    }
  }
  
  /**
   * Returns the attempt number of the NodeRun being executed.
   * First attempt is 0, first retry is 1, etc.
   */
  getAttemptNumber(): number {
    return this.task.attemptNumber;
  }
  
  /**
   * Returns the time at which the task was scheduled.
   */
  getScheduledTime(): Date {
    return this.scheduleTime;
  }
  
  /**
   * Appends information to the log output for this NodeRun.
   */
  log(thing: any): void {
    if (thing !== null && thing !== undefined) {
      this.stderr += thing.toString();
    } else {
      this.stderr += 'null';
    }
  }
  
  /**
   * Returns the current log output.
   */
  getLogOutput(): string {
    return this.stderr;
  }
  
  /**
   * Returns the TaskRunId of this TaskRun.
   */
  getTaskRunId(): string | undefined {
    return this.task.taskRunId?.taskGuid;
  }
  
  /**
   * If this TaskRun is a User Task Reminder TaskRun, returns the UserId
   * of the user the UserTask is assigned to. Returns null otherwise.
   */
  getUserId(): string | null {
    const userTaskTrigger = this.getUserTaskTrigger();
    if (!userTaskTrigger || !userTaskTrigger.userId) return null;
    return userTaskTrigger.userId;
  }
  
  /**
   * If this TaskRun is a User Task Reminder TaskRun, returns the UserGroup
   * the UserTask is assigned to. Returns null otherwise.
   */
  getUserGroup(): string | null {
    const userTaskTrigger = this.getUserTaskTrigger();
    if (!userTaskTrigger || !userTaskTrigger.userGroup) return null;
    return userTaskTrigger.userGroup;
  }
  
  /**
   * Returns an idempotency key that can be used to make calls to upstream APIs
   * idempotent across TaskRun retries.
   */
  getIdempotencyKey(): string {
    if (!this.task.taskRunId) {
      throw new Error('TaskRunId is not set');
    }
    if (!this.task.taskRunId.wfRunId) {
      throw new Error('WfRunId is not set');
    }
    return `${this.task.taskRunId.wfRunId.id}/${this.task.taskRunId.taskGuid}`;
  }
  
  private getUserTaskTrigger(): UserTaskTriggerReference | null {
    const source = this.task.source;
    if (source?.userTaskTrigger) {
      return source.userTaskTrigger;
    }
    return null;
  }
} 