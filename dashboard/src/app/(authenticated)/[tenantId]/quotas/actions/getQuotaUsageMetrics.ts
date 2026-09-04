'use server'
import { lhClient } from '@/app/lhClient'
import { WithTenant } from '@/types'
import { MetricsList, MetricWindow, QuotaId, Timestamp } from 'littlehorse-client/proto'

const toTimestamp = (value?: string): Timestamp | undefined => (value ? Timestamp.fromDate(new Date(value)) : undefined)

type GetQuotaUsageMetricsProps = {
  quotaId: QuotaId
  windowStart?: string
  windowEnd?: string
} & WithTenant

const CHUNK_MINUTES = 90
const MAX_CONCURRENT = 8

export const getQuotaUsageMetrics = async ({
  quotaId,
  windowStart,
  windowEnd,
  tenantId,
}: GetQuotaUsageMetricsProps): Promise<MetricsList> => {
  const client = await lhClient({ tenantId })

  if (!windowStart || !windowEnd) {
    return client.listQuotaUsageMetrics({
      quotaId,
      windowStart: toTimestamp(windowStart),
      windowEnd: toTimestamp(windowEnd),
    })
  }

  const startMs = new Date(windowStart).getTime()
  const endMs = new Date(windowEnd).getTime()
  const rangeMinutes = (endMs - startMs) / 60_000

  if (rangeMinutes <= CHUNK_MINUTES) {
    return client.listQuotaUsageMetrics({
      quotaId,
      windowStart: toTimestamp(windowStart),
      windowEnd: toTimestamp(windowEnd),
    })
  }

  const chunks: { start: string; end: string }[] = []
  let cursor = startMs
  while (cursor < endMs) {
    const chunkEnd = Math.min(cursor + CHUNK_MINUTES * 60_000, endMs)
    chunks.push({
      start: new Date(cursor).toISOString(),
      end: new Date(chunkEnd).toISOString(),
    })
    cursor = chunkEnd
  }

  const allWindows: MetricWindow[] = []
  for (let i = 0; i < chunks.length; i += MAX_CONCURRENT) {
    const batch = chunks.slice(i, i + MAX_CONCURRENT)
    const results = await Promise.all(
      batch.map(c =>
        client.listQuotaUsageMetrics({ quotaId, windowStart: toTimestamp(c.start), windowEnd: toTimestamp(c.end) })
      )
    )
    for (const r of results) {
      allWindows.push(...(r.windows ?? []))
    }
  }

  return { windows: allWindows }
}
