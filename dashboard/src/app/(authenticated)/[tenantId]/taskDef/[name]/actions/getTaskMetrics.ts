'use server'

import { listMetricsChunked } from '@/app/actions/listMetricsChunked'
import { lhClient } from '@/app/lhClient'
import { WithTenant } from '@/types'
import { MetricsList, TaskDefId } from 'littlehorse-client/proto'

type GetTaskMetricsProps = {
  taskDefId: TaskDefId
  windowStart?: string
  windowEnd?: string
} & WithTenant

export const getTaskMetrics = async ({
  taskDefId,
  windowStart,
  windowEnd,
  tenantId,
}: GetTaskMetricsProps): Promise<MetricsList> => {
  const client = await lhClient({ tenantId })
  return listMetricsChunked(
    (start, end) => client.listTaskMetrics({ taskDef: taskDefId, windowStart: start, windowEnd: end }),
    windowStart,
    windowEnd
  )
}
