'use server'
import { lhClient } from '@/app/lhClient'
import { WithTenant } from '@/types'
import { WorkflowEventId } from 'littlehorse-client/proto'

export type WorkflowEventRequestProps = WorkflowEventId & WithTenant
export const getWorkflowEvent = async ({ tenantId, ...req }: WorkflowEventRequestProps) => {
  const client = await lhClient({ tenantId })
  return client.getWorkflowEvent(req)
}
