import type { ZodTypeAny } from 'zod'
import { ExponentialBackoffRetryPolicy } from '../proto/common_wfspec'
import { LHPath_Selector } from '../proto/common_wfspec'
import { LHErrorType } from '../proto/common_enums'
import { CorrelatedEventConfig } from '../proto/external_event'
import { PutExternalEventDefRequest, PutWorkflowEventDefRequest } from '../proto/service'
import { ReturnType } from '../proto/type_definition'
import { zodToTypeDef } from '../worker/zodSchema'
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
  /** Payload schema declared via registeredAs(); drives auto-registration. */
  payloadSchema?: ZodTypeAny
  correlatedEventConfig?: CorrelatedEventConfig
  private registered = false

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
    // Java attaches a default CorrelatedEventConfig as soon as a correlation
    // id is set, unless one was configured explicitly.
    this.correlatedEventConfig ??= CorrelatedEventConfig.create()
    return this
  }

  /**
   * Declares the event payload type so the ExternalEventDef can be registered
   * alongside the WfSpec. Java takes a Class<?>; the JS equivalent is a zod
   * schema, matching how the worker declares task/struct schemas.
   */
  registeredAs(payloadSchema: ZodTypeAny): ExternalEventNodeOutput {
    this.payloadSchema = payloadSchema
    if (!this.registered) {
      this.registered = true
      this.parent.workflow.addExternalEventDefToRegister(this)
    }
    return this
  }

  withCorrelatedEventConfig(config: CorrelatedEventConfig): ExternalEventNodeOutput {
    this.correlatedEventConfig = config
    return this
  }

  toPutExternalEventDefRequest(): PutExternalEventDefRequest {
    return PutExternalEventDefRequest.create({
      name: this.externalEventDefName,
      contentType: schemaToReturnType(this.payloadSchema),
      ...(this.correlatedEventConfig !== undefined && { correlatedEventConfig: this.correlatedEventConfig }),
    })
  }
}

/**
 * Result of registerInterruptHandler(); mirrors Java's InterruptHandler.
 * withEventType registers an ExternalEventDef for the interrupt with the
 * given payload schema (Java takes a Class; the JS equivalent is zod).
 */
export class InterruptHandler {
  private eventTypeRegistered = false

  constructor(
    readonly interruptName: string,
    private readonly parent: WorkflowThread
  ) {}

  withEventType(payloadSchema: ZodTypeAny | null): void {
    if (this.eventTypeRegistered) {
      throw new Error(`Interrupt event type already registered: ${this.interruptName}`)
    }
    this.eventTypeRegistered = true
    const name = this.interruptName
    this.parent.workflow.addExternalEventDefToRegister({
      toPutExternalEventDefRequest: () =>
        PutExternalEventDefRequest.create({
          name,
          contentType: schemaToReturnType(payloadSchema ?? undefined),
        }),
    })
  }
}

/** Result of throwEvent(); allows declaring the payload type. */
export class ThrowEventNodeOutput {
  payloadSchema?: ZodTypeAny
  private registered = false

  constructor(
    readonly workflowEventDefName: string,
    readonly parent: WorkflowThread
  ) {}

  /** See ExternalEventNodeOutput#registeredAs — takes a zod schema, not a class. */
  registeredAs(payloadSchema: ZodTypeAny): ThrowEventNodeOutput {
    this.payloadSchema = payloadSchema
    if (!this.registered) {
      this.registered = true
      this.parent.workflow.addWorkflowEventDefToRegister(this)
    }
    return this
  }

  toPutWorkflowEventDefRequest(): PutWorkflowEventDefRequest {
    return PutWorkflowEventDefRequest.create({
      name: this.workflowEventDefName,
      contentType: schemaToReturnType(this.payloadSchema),
    })
  }
}

/**
 * Mirrors Java BuilderUtil#javaTypeToReturnType: an absent payload type means
 * a void event, so `return_type` is left unset.
 */
function schemaToReturnType(schema?: ZodTypeAny): ReturnType {
  if (schema === undefined) {
    return ReturnType.create()
  }
  return ReturnType.create({ returnType: zodToTypeDef(schema) })
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
