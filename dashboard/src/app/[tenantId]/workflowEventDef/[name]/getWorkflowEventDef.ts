'use server'

import { lhClient } from '@/app/lhClient'
import { WorkflowEventDef, WorkflowEventDefId } from 'littlehorse-client/proto'

export const getWorkflowEventDef = async (tenantId: string, request: WorkflowEventDefId): Promise<WorkflowEventDef> => {
  const client = await lhClient({ tenantId })

  return client.getWorkflowEventDef(request)
}
