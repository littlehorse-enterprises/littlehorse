'use server'

import { getClient } from '@/lhConfig'
import type { PutWfSpecRequest, PutTaskDefRequest } from 'littlehorse-client/proto'
import { extractTasksInfo, createTaskDefRequest } from '../lib/utils'
import type { DeployWorkflowResult } from '../types'

async function deployTaskDef(taskDefRequest: PutTaskDefRequest, tenantId: string, accessToken?: string) {
  try {
    const client = getClient({ tenantId, accessToken })
    await client.putTaskDef(taskDefRequest)
  } catch (error) {
    throw error
  }
}

export async function deployWorkflow(
  spec: PutWfSpecRequest,
  tenantId: string,
  accessToken?: string
): Promise<DeployWorkflowResult> {
  try {
    const client = getClient({ tenantId, accessToken })
    const tasksInfo = extractTasksInfo(spec)
    const taskDefRequests = tasksInfo.map(createTaskDefRequest)

    await Promise.all(taskDefRequests.map(request => deployTaskDef(request, tenantId, accessToken)))

    const result = await client.putWfSpec(spec)

    return {
      success: true,
      wfSpecId: result.id,
    }
  } catch (error) {
    console.error('Failed to deploy workflow:', error)

    throw new Error(error instanceof Error ? error.message : 'Failed to deploy workflow')
  }
}
