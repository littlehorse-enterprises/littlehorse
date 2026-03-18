'use server'

import { lhClient } from '@/app/lhClient'
import { InactiveThreadRun, WfRunId } from 'littlehorse-client/proto'

type Props = {
  wfRunId: WfRunId
  threadRunNumber: number
  tenantId: string
}

export const getArchivedThreadRun = async ({
  wfRunId,
  threadRunNumber,
  tenantId,
}: Props): Promise<InactiveThreadRun | undefined> => {
  const client = await lhClient({ tenantId })
  try {
    const archivedThreadRun = await client.getInactiveThreadRun({
      wfRunId,
      threadRunNumber,
    })
    return archivedThreadRun
  } catch (error) {
    // ThreadRun might not be archived or might not exist
    return undefined
  }
}
