import type { ExponentialBackoffRetryPolicy } from '../../proto/common_wfspec'
import type { TaskNodeOutput } from '../taskNodeOutput'
import { NodeOutputImpl } from './nodeOutputImpl'
import type { WorkflowThreadImpl } from './workflowThreadImpl'

export class TaskNodeOutputImpl extends NodeOutputImpl implements TaskNodeOutput {
  constructor(nodeName: string, thread: WorkflowThreadImpl) {
    super(nodeName, thread)
  }

  withRetries(retries: number): TaskNodeOutput {
    this.thread.patchTaskNode(this.nodeName, t => {
      t.retries = retries
    })
    return this
  }

  withTimeout(timeoutSeconds: number): TaskNodeOutput {
    this.thread.patchTaskNode(this.nodeName, t => {
      t.timeoutSeconds = timeoutSeconds
    })
    return this
  }

  withExponentialBackoff(policy: ExponentialBackoffRetryPolicy): TaskNodeOutput {
    this.thread.patchTaskNode(this.nodeName, t => {
      t.exponentialBackoff = policy
    })
    return this
  }
}
