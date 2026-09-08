'use server'
import { listMetricsChunked } from '@/app/actions/listMetricsChunked'
import { lhClient } from '@/app/lhClient'
import { WithTenant } from '@/types'
import { MetricsList, QuotaId } from 'littlehorse-client/proto'

type GetQuotaUsageMetricsProps = {
  quotaId: QuotaId
  windowStart?: string
  windowEnd?: string
} & WithTenant

export const getQuotaUsageMetrics = async ({
  quotaId,
  windowStart,
  windowEnd,
  tenantId,
}: GetQuotaUsageMetricsProps): Promise<MetricsList> => {
  const client = await lhClient({ tenantId })
  return listMetricsChunked(
    (start, end) => client.listQuotaUsageMetrics({ quotaId, windowStart: start, windowEnd: end }),
    windowStart,
    windowEnd
  )
}
