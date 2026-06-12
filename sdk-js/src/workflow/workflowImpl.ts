import { AllowedUpdateType, PutWfSpecRequest } from '../proto/service'
import { WorkflowThreadImpl } from './internal/workflowThreadImpl'
import type { ThreadFunc } from './threadFunc'
import type { Workflow } from './workflow'

export class WorkflowImpl implements Workflow {
  private compiled: PutWfSpecRequest | undefined

  constructor(
    readonly name: string,
    private readonly entrypoint: ThreadFunc
  ) {}

  compile(): PutWfSpecRequest {
    if (this.compiled === undefined) {
      const thread = new WorkflowThreadImpl(this.name, this.entrypoint)
      this.compiled = PutWfSpecRequest.fromPartial({
        name: this.name,
        entrypointThreadName: 'entrypoint',
        threadSpecs: { entrypoint: thread.threadSpec },
        allowedUpdates: AllowedUpdateType.ALL_UPDATES,
      })
    }
    return this.compiled
  }
}
