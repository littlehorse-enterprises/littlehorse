import {
  bucketStartMs,
  enumerateBucketStarts,
  formatBucketLabel,
} from '@/app/(authenticated)/[tenantId]/(diagram)/wfSpec/[...props]/components/metrics/metricsData'
import { toDate } from '@/app/utils'
import { MetricWindow, QuotaUsageMetrics } from 'littlehorse-client/proto'

export type QuotaUsageBucket = {
  time: string
  timestamp: number
  usage: QuotaUsageMetrics
}

export type QuotaCountDataPoint = {
  time: string
  timestamp: number
  observed: number
  throttled: number
}

export type QuotaThrottleDataPoint = {
  time: string
  timestamp: number
  avgThrottleMs: number
}

export type QuotaUsageSummary = {
  observed: number
  throttled: number
  throttleRate: number
  totalThrottleTimeMs: number
}

export function mergeQuotaUsageGroup(parts: QuotaUsageMetrics[]): QuotaUsageMetrics {
  let observed = 0
  let throttled = 0
  let totalThrottleTimeMs = 0
  for (const p of parts) {
    observed += p.requestsObserved
    throttled += p.requestsThrottled
    totalThrottleTimeMs += Number(p.totalThrottleTimeMs)
  }
  return {
    requestsObserved: observed,
    requestsThrottled: throttled,
    totalThrottleTimeMs: String(totalThrottleTimeMs),
  }
}

export const EMPTY_QUOTA_USAGE: QuotaUsageMetrics = mergeQuotaUsageGroup([])

export function parseQuotaWindows(windows: MetricWindow[]): { ts: number; usage: QuotaUsageMetrics }[] {
  return windows
    .filter(w => w.metric?.oneofKind === 'quotaUsage')
    .map(w => ({
      ts: toDate(w.id?.windowStart)?.getTime() ?? 0,
      usage: (w.metric as { oneofKind: 'quotaUsage'; quotaUsage: QuotaUsageMetrics }).quotaUsage,
    }))
    .sort((a, b) => a.ts - b.ts)
}

/**
 * Buckets sparse metric windows onto the displayed time axis, zero-filling minutes with no
 * traffic. Chart series and summary tiles both derive from this so they cannot disagree.
 */
export function bucketQuotaUsage(
  windows: MetricWindow[],
  bucketMinutes: number,
  rangeMinutes: number,
  rangeStartMs: number,
  rangeEndMs: number
): QuotaUsageBucket[] {
  const bucketMs = bucketMinutes * 60 * 1000
  const groups = new Map<number, QuotaUsageMetrics[]>()
  for (const { ts, usage } of parseQuotaWindows(windows)) {
    const key = bucketStartMs(ts, bucketMs)
    groups.set(key, [...(groups.get(key) ?? []), usage])
  }
  return enumerateBucketStarts(rangeStartMs, rangeEndMs, bucketMs).map(timestamp => ({
    time: formatBucketLabel(timestamp, bucketMinutes, rangeMinutes),
    timestamp,
    usage: groups.has(timestamp) ? mergeQuotaUsageGroup(groups.get(timestamp)!) : EMPTY_QUOTA_USAGE,
  }))
}

export function toQuotaCountData(buckets: QuotaUsageBucket[]): QuotaCountDataPoint[] {
  return buckets.map(({ time, timestamp, usage }) => ({
    time,
    timestamp,
    observed: usage.requestsObserved,
    throttled: usage.requestsThrottled,
  }))
}

export function toQuotaThrottleData(buckets: QuotaUsageBucket[]): QuotaThrottleDataPoint[] {
  return buckets.map(({ time, timestamp, usage }) => ({
    time,
    timestamp,
    avgThrottleMs:
      usage.requestsThrottled > 0 ? Math.round(Number(usage.totalThrottleTimeMs) / usage.requestsThrottled) : 0,
  }))
}

export function summarizeQuotaUsage(buckets: QuotaUsageBucket[]): QuotaUsageSummary {
  const merged = mergeQuotaUsageGroup(buckets.map(b => b.usage))
  return {
    observed: merged.requestsObserved,
    throttled: merged.requestsThrottled,
    throttleRate: merged.requestsObserved > 0 ? merged.requestsThrottled / merged.requestsObserved : 0,
    totalThrottleTimeMs: Number(merged.totalThrottleTimeMs),
  }
}
