'use server';

import { getClient } from '../lhConfig';
import type { PutWfSpecRequest, PutTaskDefRequest } from 'littlehorse-client/proto';
import { extractTasksInfo, createTaskDefRequest } from '../lib/utils';
import type { DeployWorkflowResult } from '../types';

async function deployTaskDef(taskDefRequest: PutTaskDefRequest) {
  try {
    const client = getClient();    
    await client.putTaskDef(taskDefRequest);
  } catch (error) {
    throw error;
  }
}

export async function deployWorkflow(spec: PutWfSpecRequest): Promise<DeployWorkflowResult> {
  try {
    const client = getClient();    
    const tasksInfo = extractTasksInfo(spec);    
    const taskDefRequests = tasksInfo.map(createTaskDefRequest);

    await Promise.all(
      taskDefRequests.map(request => deployTaskDef(request))
    );
    
    const result = await client.putWfSpec(spec);
    
    return {
      success: true,
      wfSpecId: result.id
    };
  } catch (error) {
    console.error('Failed to deploy workflow:', error);

    throw new Error(
      error instanceof Error ? error.message : 'Failed to deploy workflow'
    );
  }
}
