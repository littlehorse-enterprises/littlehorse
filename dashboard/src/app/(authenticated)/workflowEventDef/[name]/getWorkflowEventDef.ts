'use server'

import { lhClient } from '@/app/lhClient'
import { WorkflowEventDef, WorkflowEventDefId } from 'littlehorse-client/proto'
import { cookies } from 'next/headers'

export const getWorkflowEventDef = async (request: WorkflowEventDefId): Promise<WorkflowEventDef> => {
  const tenantId = cookies().get('tenantId')?.value
  const client = await lhClient({ tenantId })

  return client.getWorkflowEventDef(request);
}
