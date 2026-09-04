import {
  bucketStartMs,
  enumerateBucketStarts,
  formatBucketLabel,
} from '@/app/(authenticated)/[tenantId]/(diagram)/wfSpec/[...props]/components/metrics/metricsData'
import { toDate } from '@/app/utils'
import { MetricWindow, QuotaUsageMetrics } from 'littlehorse-client/proto'

export type QuotaCountDataPoint = {
  time: string
  timestamp: number
  observed: number
  throttled: number
}

export type QuotaThrottleDataPoint = {
  time: string
  timestamp: number
  totalThrottleMs: number
  avgThrottleMs: number
}

export type QuotaUsageSummary = {
  observed: number
  throttled: number
  throttleRate: number
  totalThrottleTimeMs: number
}

export const EMPTY_QUOTA_USAGE: QuotaUsageMetrics = {
  requestsObserved: 0,
  requestsThrottled: 0,
  totalThrottleTimeMs: '0',
}

export function parseQuotaWindows(windows: MetricWindow[]): { ts: number; usage: QuotaUsageMetrics }[] {
  return windows
    .filter(w => w.metric?.oneofKind === 'quotaUsage')
    .map(w => ({
      ts: toDate(w.id?.windowStart)?.getTime() ?? 0,
      usage: (w.metric as { oneofKind: 'quotaUsage'; quotaUsage: QuotaUsageMetrics }).quotaUsage,
    }))
    .sort((a, b) => a.ts - b.ts)
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

function aggregateQuotaByBucket(
  points: { ts: number; usage: QuotaUsageMetrics }[],
  bucketMs: number
): Map<number, QuotaUsageMetrics> {
  const groups = new Map<number, QuotaUsageMetrics[]>()
  for (const p of points) {
    const k = bucketStartMs(p.ts, bucketMs)
    const arr = groups.get(k) ?? []
    arr.push(p.usage)
    groups.set(k, arr)
  }
  const merged = new Map<number, QuotaUsageMetrics>()
  groups.forEach((usages, bs) => {
    merged.set(bs, mergeQuotaUsageGroup(usages))
  })
  return merged
}

export function transformToQuotaCountData(
  windows: MetricWindow[],
  bucketMinutes: number,
  rangeMinutes: number,
  rangeStartMs: number,
  rangeEndMs: number
): QuotaCountDataPoint[] {
  const bucketMs = bucketMinutes * 60 * 1000
  const byBucket = aggregateQuotaByBucket(parseQuotaWindows(windows), bucketMs)
  return enumerateBucketStarts(rangeStartMs, rangeEndMs, bucketMs).map(bucketStart => {
    const usage = byBucket.get(bucketStart) ?? EMPTY_QUOTA_USAGE
    return {
      time: formatBucketLabel(bucketStart, bucketMinutes, rangeMinutes),
      timestamp: bucketStart,
      observed: usage.requestsObserved,
      throttled: usage.requestsThrottled,
    }
  })
}

export function transformToQuotaThrottleData(
  windows: MetricWindow[],
  bucketMinutes: number,
  rangeMinutes: number,
  rangeStartMs: number,
  rangeEndMs: number
): QuotaThrottleDataPoint[] {
  const bucketMs = bucketMinutes * 60 * 1000
  const byBucket = aggregateQuotaByBucket(parseQuotaWindows(windows), bucketMs)
  return enumerateBucketStarts(rangeStartMs, rangeEndMs, bucketMs).map(bucketStart => {
    const usage = byBucket.get(bucketStart) ?? EMPTY_QUOTA_USAGE
    const totalThrottleMs = Number(usage.totalThrottleTimeMs)
    return {
      time: formatBucketLabel(bucketStart, bucketMinutes, rangeMinutes),
      timestamp: bucketStart,
      totalThrottleMs,
      avgThrottleMs: usage.requestsThrottled > 0 ? Math.round(totalThrottleMs / usage.requestsThrottled) : 0,
    }
  })
}

export function summarizeQuotaUsage(windows: MetricWindow[]): QuotaUsageSummary {
  const merged = mergeQuotaUsageGroup(parseQuotaWindows(windows).map(p => p.usage))
  return {
    observed: merged.requestsObserved,
    throttled: merged.requestsThrottled,
    throttleRate: merged.requestsObserved > 0 ? merged.requestsThrottled / merged.requestsObserved : 0,
    totalThrottleTimeMs: Number(merged.totalThrottleTimeMs),
  }
}

export function formatDurationMs(ms: number): string {
  if (ms < 1000) return `${ms}ms`
  if (ms < 60_000) return `${(ms / 1000).toFixed(1)}s`
  if (ms < 3_600_000) {
    const minutes = Math.floor(ms / 60_000)
    const seconds = Math.round((ms % 60_000) / 1000)
    return seconds > 0 ? `${minutes}m ${seconds}s` : `${minutes}m`
  }
  const hours = Math.floor(ms / 3_600_000)
  const minutes = Math.round((ms % 3_600_000) / 60_000)
  return minutes > 0 ? `${hours}h ${minutes}m` : `${hours}h`
}
