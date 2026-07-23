import { AllowedUpdateType, PutWfSpecRequest } from '../proto/service'
import { ThreadRetentionPolicy, WfSpec_ParentWfSpecReference, WorkflowRetentionPolicy } from '../proto/wf_spec'
import { ExponentialBackoffRetryPolicy, VariableMutationType } from '../proto/common_wfspec'
import { WorkflowThread, ThreadFunc } from './WorkflowThread'
import type { WfRunVariable } from './variables'

/**
 * Represents a WfSpec (mirrors Java Workflow/WorkflowImpl). Calling
 * compileWorkflow() runs the thread functions exactly once to build the
 * PutWfSpecRequest proto — the workflow itself executes on the server.
 */
export class Workflow {
  private compiled?: PutWfSpecRequest
  private readonly threadFuncs: Array<{ name: string; func: ThreadFunc }> = []
  private readonly requiredTaskDefNames = new Set<string>()
  private readonly requiredExternalEventDefNames = new Set<string>()
  private readonly requiredChildWfSpecNames = new Set<string>()
  private readonly requiredWorkflowEventDefNames = new Set<string>()

  readonly threadsStack: WorkflowThread[] = []
  defaultTaskTimeout?: number
  defaultSimpleRetries = 0
  defaultExponentialBackoff?: ExponentialBackoffRetryPolicy
  defaultThreadRetentionPolicy?: ThreadRetentionPolicy
  private wfRetentionPolicy?: WorkflowRetentionPolicy
  private parentWfSpecName?: string
  private allowedUpdates = AllowedUpdateType.ALL_UPDATES

  private constructor(
    readonly name: string,
    private readonly entrypointThread: ThreadFunc
  ) {}

  static newWorkflow(name: string, entrypointThreadFunc: ThreadFunc): Workflow {
    return new Workflow(name, entrypointThreadFunc)
  }

  compileWorkflow(): PutWfSpecRequest {
    if (this.compiled !== undefined) return this.compiled

    const spec = PutWfSpecRequest.create({ name: this.name, allowedUpdates: this.allowedUpdates })
    spec.entrypointThreadName = this.addSubThread('entrypoint', this.entrypointThread)

    while (this.threadFuncs.length > 0) {
      const { name: funcName, func } = this.threadFuncs.shift()!
      const thread = new WorkflowThread(this.name, this, func)
      spec.threadSpecs[funcName] = thread.buildSpec()
    }

    if (this.wfRetentionPolicy !== undefined) {
      spec.retentionPolicy = this.wfRetentionPolicy
    }
    if (this.parentWfSpecName !== undefined) {
      spec.parentWfSpec = WfSpec_ParentWfSpecReference.create({ wfSpecName: this.parentWfSpecName })
    }

    this.compiled = spec
    return spec
  }

  compileWfToJson(): string {
    return PutWfSpecRequest.toJsonString(this.compileWorkflow(), { prettySpaces: 2 })
  }

  addSubThread(subThreadName: string, subThreadFunc: ThreadFunc): string {
    if (this.threadFuncs.some(pair => pair.name === subThreadName)) {
      throw new Error(`Thread ${subThreadName} already exists`)
    }
    this.threadFuncs.push({ name: subThreadName, func: subThreadFunc })
    return subThreadName
  }

  /** Applies a mutation on the innermost active thread (used by WfRunVariable.assign). */
  mutateOnActiveThread(variable: WfRunVariable, type: VariableMutationType, rhs: unknown): void {
    const top = this.threadsStack[this.threadsStack.length - 1]
    const activeThread = top !== undefined && top.isActive ? top : variable.parent
    activeThread.mutate(variable, type, rhs)
  }

  setParent(parentWfSpecName: string): void {
    this.parentWfSpecName = parentWfSpecName
  }

  setDefaultTaskTimeout(timeoutSeconds: number): void {
    this.defaultTaskTimeout = timeoutSeconds
  }

  getDefaultTaskTimeout(): number | undefined {
    return this.defaultTaskTimeout
  }

  setDefaultTaskRetries(defaultSimpleRetries: number): void {
    if (defaultSimpleRetries < 0) {
      throw new Error('Cannot have negative retries!')
    }
    this.defaultSimpleRetries = defaultSimpleRetries
  }

  setDefaultTaskExponentialBackoffPolicy(defaultPolicy: ExponentialBackoffRetryPolicy): void {
    this.defaultExponentialBackoff = defaultPolicy
  }

  withRetentionPolicy(policy: WorkflowRetentionPolicy): Workflow {
    this.wfRetentionPolicy = policy
    return this
  }

  withDefaultThreadRetentionPolicy(policy: ThreadRetentionPolicy): Workflow {
    this.defaultThreadRetentionPolicy = policy
    return this
  }

  withUpdateType(allowedUpdateType: AllowedUpdateType): Workflow {
    this.allowedUpdates = allowedUpdateType
    return this
  }

  getName(): string {
    return this.name
  }

  addTaskDefName(taskDefName: string): void {
    this.requiredTaskDefNames.add(taskDefName)
  }

  addExternalEventDefName(eedName: string): void {
    this.requiredExternalEventDefNames.add(eedName)
  }

  addChildWfSpecName(childWfSpecName: string): void {
    this.requiredChildWfSpecNames.add(childWfSpecName)
  }

  addWorkflowEventDefName(name: string): void {
    this.requiredWorkflowEventDefNames.add(name)
  }

  getRequiredTaskDefNames(): Set<string> {
    this.compileWorkflow()
    return this.requiredTaskDefNames
  }

  getRequiredExternalEventDefNames(): Set<string> {
    this.compileWorkflow()
    return this.requiredExternalEventDefNames
  }

  getRequiredChildWfSpecNames(): Set<string> {
    this.compileWorkflow()
    return this.requiredChildWfSpecNames
  }

  getRequiredWorkflowEventDefNames(): Set<string> {
    this.compileWorkflow()
    return this.requiredWorkflowEventDefNames
  }
}
