'use server'
import { listMetricsChunked } from '@/app/actions/listMetricsChunked'
import { lhClient } from '@/app/lhClient'
import { WithTenant } from '@/types'
import { MetricsList, WfSpecId } from 'littlehorse-client/proto'

type GetWfMetricsProps = {
  wfSpecId: WfSpecId
  windowStart?: string
  windowEnd?: string
} & WithTenant

export const getWfMetrics = async ({
  wfSpecId,
  windowStart,
  windowEnd,
  tenantId,
}: GetWfMetricsProps): Promise<MetricsList> => {
  const client = await lhClient({ tenantId })
  return listMetricsChunked(
    (start, end) => client.listWfMetrics({ wfSpec: wfSpecId, windowStart: start, windowEnd: end }),
    windowStart,
    windowEnd
  )
}
