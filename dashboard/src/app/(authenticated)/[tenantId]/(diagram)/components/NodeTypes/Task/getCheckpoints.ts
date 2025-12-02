'use server'
import { lhClient } from '@/app/lhClient'
import { WithTenant } from '@/types'
import { Checkpoint, TaskRunId } from 'littlehorse-client/proto'

export type GetCheckpointsRequestProps = { taskRunId: TaskRunId; totalCheckpoints: number } & WithTenant

export const getCheckpoints = async ({
  tenantId,
  taskRunId,
  totalCheckpoints,
}: GetCheckpointsRequestProps): Promise<Checkpoint[]> => {
  const client = await lhClient({ tenantId })
  const checkpoints: Checkpoint[] = []

  for (let i = 0; i < totalCheckpoints; i++) {
    const checkpoint = await client.getCheckpoint({ taskRun: taskRunId, checkpointNumber: i })
    checkpoints.push(checkpoint)
  }

  return checkpoints
}
