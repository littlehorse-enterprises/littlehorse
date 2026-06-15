import type { Client } from 'nice-grpc'
import { LittleHorseDefinition, PutWfSpecRequest } from '../proto/service'
import type { WfSpec } from '../proto/wf_spec'
import type { ThreadFunc } from './threadFunc'
import { WorkflowImpl } from './workflowImpl'

export interface Workflow {
  readonly name: string
  compile(): PutWfSpecRequest
}

export namespace Workflow {
  export function newWorkflow(name: string, entrypoint: ThreadFunc): Workflow {
    return new WorkflowImpl(name, entrypoint)
  }

  export async function registerWfSpec(
    workflow: Workflow,
    client: Client<typeof LittleHorseDefinition>
  ): Promise<WfSpec> {
    return client.putWfSpec(workflow.compile())
  }
}
