import type { ExponentialBackoffRetryPolicy } from '../proto/common_wfspec'
import type { NodeOutput } from './nodeOutput'

export interface TaskNodeOutput extends NodeOutput {
  withRetries(retries: number): TaskNodeOutput
  withTimeout(timeoutSeconds: number): TaskNodeOutput
  withExponentialBackoff(policy: ExponentialBackoffRetryPolicy): TaskNodeOutput
}
