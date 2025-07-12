'use server'
import { lhClient } from '@/app/lhClient'
import { WithTenant } from '@/types'
import { ExternalEventId, NodeRun } from 'littlehorse-client/proto'

export type WaitForConditionRequestProps = WithTenant & { nodeRun: NodeRun }
export const getWaitForCondition = async ({ tenantId, nodeRun }: WaitForConditionRequestProps) => {
  const client = await lhClient({ tenantId })
  const wfSpec = await client.getWfSpec(nodeRun.wfSpecId ?? {})
  const waitForCondition = wfSpec.threadSpecs[nodeRun.threadSpecName].nodes[nodeRun.nodeName]
  return waitForCondition
}
