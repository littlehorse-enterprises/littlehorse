import { ExponentialBackoffRetryPolicy } from '../proto/common_wfspec'
import { LHPath_Selector } from '../proto/common_wfspec'
import { LHErrorType } from '../proto/common_enums'
import type { WorkflowThread, ThreadFunc } from './WorkflowThread'
import type { WfRunVariable } from './variables'

/** Reference to the output of a node, usable as an input to later nodes. */
export class NodeOutput {
  jsonPathStr?: string
  readonly lhPath: LHPath_Selector[] = []

  constructor(
    readonly nodeName: string,
    readonly parent: WorkflowThread
  ) {}

  jsonPath(path: string): NodeOutput {
    if (this.jsonPathStr !== undefined) {
      throw new Error('Cannot use jsonPath() twice on same node!')
    }
    const out = new NodeOutput(this.nodeName, this.parent)
    out.jsonPathStr = path
    return out
  }

  get(field: string): NodeOutput {
    if (this.jsonPathStr !== undefined) {
      throw new Error('Cannot use jsonPath() and get() on same var!')
    }
    const out = new NodeOutput(this.nodeName, this.parent)
    out.lhPath.push({ selectorType: { oneofKind: 'key', key: field } })
    return out
  }
}

export class TaskNodeOutput extends NodeOutput {
  withRetries(retries: number): TaskNodeOutput {
    this.parent.overrideTaskRetries(this, retries)
    return this
  }

  withExponentialBackoff(policy: ExponentialBackoffRetryPolicy): TaskNodeOutput {
    this.parent.overrideTaskExponentialBackoffPolicy(this, policy)
    return this
  }

  timeout(timeoutSeconds: number): TaskNodeOutput {
    this.parent.addTimeoutToTaskNode(this, timeoutSeconds)
    return this
  }
}

export class UserTaskOutput extends NodeOutput {
  withNotes(notes: unknown): UserTaskOutput {
    this.parent.setUserTaskNotes(this, notes)
    return this
  }

  withOnCancellationException(exceptionName: unknown): UserTaskOutput {
    this.parent.setUserTaskOnCancellationException(this, exceptionName)
    return this
  }
}

export class ExternalEventNodeOutput extends NodeOutput {
  constructor(
    nodeName: string,
    readonly externalEventDefName: string,
    parent: WorkflowThread
  ) {
    super(nodeName, parent)
  }

  timeout(timeoutSeconds: number): ExternalEventNodeOutput {
    this.parent.addTimeoutToExtEvtNode(this, timeoutSeconds)
    return this
  }

  withCorrelationId(correlationId: unknown, maskCorrelationKey?: boolean): ExternalEventNodeOutput {
    this.parent.addCorrelationIdToExtEvtNode(this, correlationId, maskCorrelationKey)
    return this
  }
}

export class WaitForConditionNodeOutput extends NodeOutput {}

export class WaitForThreadsNodeOutput extends NodeOutput {
  handleExceptionOnChild(exceptionName: string | null, handler: ThreadFunc): WaitForThreadsNodeOutput {
    this.parent.addPerThreadFailureHandler(this, 'exn-handler', exceptionName ?? 'FAILURE_TYPE_EXCEPTION', {
      exceptionName: exceptionName ?? undefined,
      anyExceptions: exceptionName === null || exceptionName === undefined,
      handler,
    })
    return this
  }

  handleErrorOnChild(error: LHErrorType | null, handler: ThreadFunc): WaitForThreadsNodeOutput {
    const errorName = error === null || error === undefined ? undefined : LHErrorType[error]
    this.parent.addPerThreadFailureHandler(this, 'error-handler', errorName ?? 'FAILURE_TYPE_ERROR', {
      exceptionName: errorName,
      anyErrors: errorName === undefined,
      handler,
    })
    return this
  }

  handleAnyFailureOnChild(handler: ThreadFunc): WaitForThreadsNodeOutput {
    this.parent.addPerThreadFailureHandler(this, 'failure-handler', 'ANY_FAILURE', { handler })
    return this
  }
}

/** Handle to a child thread spawned with spawnThread(). */
export class SpawnedThread {
  constructor(
    readonly parent: WorkflowThread,
    readonly childThreadName: string,
    readonly internalThreadVar: WfRunVariable
  ) {}

  getThreadNumberVariable(): WfRunVariable {
    return this.internalThreadVar
  }
}

/** Handle to one-or-more spawned threads that can be waited for. */
export type SpawnedThreads = FixedSpawnedThreads | SpawnedThreadsIterator

export class FixedSpawnedThreads {
  readonly kind = 'fixed'
  readonly threads: SpawnedThread[]

  constructor(...threads: SpawnedThread[]) {
    this.threads = threads
  }
}

export class SpawnedThreadsIterator {
  readonly kind = 'iterator'

  constructor(readonly internalStartedThreadVar: WfRunVariable) {}
}

export function spawnedThreadsOf(...threads: SpawnedThread[]): SpawnedThreads {
  return new FixedSpawnedThreads(...threads)
}

/** Handle to a child workflow started with runWf(). */
export class SpawnedChildWf {
  constructor(
    readonly sourceNodeName: string,
    readonly thread: WorkflowThread
  ) {}
}
