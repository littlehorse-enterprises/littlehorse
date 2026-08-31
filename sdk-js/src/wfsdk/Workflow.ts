import { AllowedUpdateType, PutWfSpecRequest } from '../proto/service'
import { ThreadFunc, WorkflowThread } from './WorkflowThread'

/**
 * compileWorkflow() runs the thread function exactly once to build the
 * PutWfSpecRequest — the workflow itself executes on the server. Rules:
 * conformance/areas/wfsdk/rules.md.
 */
export class Workflow {
  private compiled?: PutWfSpecRequest

  private constructor(
    readonly name: string,
    private readonly entrypointThread: ThreadFunc
  ) {}

  static newWorkflow(name: string, entrypointThreadFunc: ThreadFunc): Workflow {
    return new Workflow(name, entrypointThreadFunc)
  }

  compileWorkflow(): PutWfSpecRequest {
    if (this.compiled !== undefined) return this.compiled

    // R6: ALL_UPDATES default, nothing else emitted unless a call set it.
    const spec = PutWfSpecRequest.create({ name: this.name, allowedUpdates: AllowedUpdateType.ALL_UPDATES })
    // R5: the entrypoint thread compiles into threadSpecs["entrypoint"].
    spec.threadSpecs['entrypoint'] = new WorkflowThread(this.entrypointThread).buildSpec()
    spec.entrypointThreadName = 'entrypoint'

    this.compiled = spec
    return spec
  }
}
