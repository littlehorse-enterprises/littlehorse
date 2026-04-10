import { CountAndTiming, MetricWindow, WfMetrics } from 'littlehorse-client/proto'

export type CountDataPoint = {
  time: string
  timestamp: number
  started: number
  completed: number
  error: number
  exception: number
}

export type LatencyDataPoint = {
  time: string
  timestamp: number
  completedAvg: number
  completedMax: number
  errorAvg: number
  errorMax: number
}

export function mergeManyTimings(parts: (CountAndTiming | undefined)[]): CountAndTiming {
  let count = 0
  let totalLatencyMs = 0
  let minLatencyMs = Number.POSITIVE_INFINITY
  let maxLatencyMs = 0
  for (const p of parts) {
    if (!p || p.count === 0) continue
    count += p.count
    totalLatencyMs += p.totalLatencyMs
    minLatencyMs = Math.min(minLatencyMs, p.minLatencyMs)
    maxLatencyMs = Math.max(maxLatencyMs, p.maxLatencyMs)
  }
  return {
    count,
    totalLatencyMs,
    minLatencyMs: minLatencyMs === Number.POSITIVE_INFINITY ? 0 : minLatencyMs,
    maxLatencyMs,
  }
}

export function mergeWfMetricsGroup(wfs: WfMetrics[]): WfMetrics {
  return {
    started: mergeManyTimings(wfs.map(w => w.started)),
    runningToCompleted: mergeManyTimings(wfs.map(w => w.runningToCompleted)),
    runningToError: mergeManyTimings(wfs.map(w => w.runningToError)),
    runningToException: mergeManyTimings(wfs.map(w => w.runningToException)),
    runningToHalting: mergeManyTimings(wfs.map(w => w.runningToHalting)),
    runningToHalted: mergeManyTimings(wfs.map(w => w.runningToHalted)),
    haltingToHalted: mergeManyTimings(wfs.map(w => w.haltingToHalted)),
    haltedToRunning: mergeManyTimings(wfs.map(w => w.haltedToRunning)),
  }
}

const DAY_MS = 86_400_000

export function bucketStartMs(ts: number, bucketMs: number): number {
  if (bucketMs >= DAY_MS) {
    const d = new Date(ts)
    return new Date(d.getFullYear(), d.getMonth(), d.getDate()).getTime()
  }
  return Math.floor(ts / bucketMs) * bucketMs
}

export function parseWorkflowWindows(windows: MetricWindow[]): { ts: number; wf: WfMetrics }[] {
  return windows
    .filter(w => w.metric?.$case === 'workflow')
    .map(w => ({
      ts: new Date(w.id?.windowStart ?? 0).getTime(),
      wf: w.metric!.value as WfMetrics,
    }))
    .sort((a, b) => a.ts - b.ts)
}

export function aggregateByBucket(points: { ts: number; wf: WfMetrics }[], bucketMs: number): Map<number, WfMetrics> {
  const groups = new Map<number, WfMetrics[]>()
  for (const p of points) {
    const k = bucketStartMs(p.ts, bucketMs)
    const arr = groups.get(k) ?? []
    arr.push(p.wf)
    groups.set(k, arr)
  }
  const merged = new Map<number, WfMetrics>()
  for (const [bs, wfs] of groups) {
    merged.set(bs, mergeWfMetricsGroup(wfs))
  }
  return merged
}

export const EMPTY_WF_METRICS: WfMetrics = mergeWfMetricsGroup([])

export function enumerateBucketStarts(rangeStartMs: number, rangeEndMs: number, bucketMs: number): number[] {
  if (bucketMs >= DAY_MS) {
    const out: number[] = []
    const d = new Date(rangeStartMs)
    let current = new Date(d.getFullYear(), d.getMonth(), d.getDate()).getTime()
    if (current < rangeStartMs) {
      const next = new Date(current)
      next.setDate(next.getDate() + 1)
      current = next.getTime()
    }
    while (current <= rangeEndMs) {
      out.push(current)
      const next = new Date(current)
      next.setDate(next.getDate() + 1)
      current = next.getTime()
    }
    return out
  }
  const first = Math.ceil(rangeStartMs / bucketMs) * bucketMs
  const last = Math.floor(rangeEndMs / bucketMs) * bucketMs
  const out: number[] = []
  for (let t = first; t <= last; t += bucketMs) {
    out.push(t)
  }
  return out
}

export function fillBucketGaps(
  byBucket: Map<number, WfMetrics>,
  rangeStartMs: number,
  rangeEndMs: number,
  bucketMs: number
): { bucketStart: number; wf: WfMetrics }[] {
  return enumerateBucketStarts(rangeStartMs, rangeEndMs, bucketMs).map(bucketStart => ({
    bucketStart,
    wf: byBucket.get(bucketStart) ?? EMPTY_WF_METRICS,
  }))
}

export function formatBucketLabel(bucketStartMs: number, bucketMinutes: number, rangeMinutes: number): string {
  const d = new Date(bucketStartMs)
  if (bucketMinutes >= 1440) {
    return d.toLocaleDateString([], { month: 'short', day: 'numeric', year: 'numeric' })
  }
  if (bucketMinutes >= 60) {
    return d.toLocaleString([], { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' })
  }
  if (rangeMinutes <= 24 * 60) {
    return d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
  }
  return d.toLocaleString([], { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' })
}

export function avgLatency(ct: CountAndTiming | undefined): number {
  if (!ct || ct.count === 0) return 0
  return Math.round(ct.totalLatencyMs / ct.count)
}

export function transformToCountData(
  windows: MetricWindow[],
  bucketMinutes: number,
  rangeMinutes: number,
  rangeStartMs: number,
  rangeEndMs: number
): CountDataPoint[] {
  const bucketMs = bucketMinutes * 60 * 1000
  const byBucket = aggregateByBucket(parseWorkflowWindows(windows), bucketMs)
  const filled = fillBucketGaps(byBucket, rangeStartMs, rangeEndMs, bucketMs)
  return filled.map(({ bucketStart, wf }) => ({
    time: formatBucketLabel(bucketStart, bucketMinutes, rangeMinutes),
    timestamp: bucketStart,
    started: wf.started?.count ?? 0,
    completed: wf.runningToCompleted?.count ?? 0,
    error: wf.runningToError?.count ?? 0,
    exception: wf.runningToException?.count ?? 0,
  }))
}

export function transformToLatencyData(
  windows: MetricWindow[],
  bucketMinutes: number,
  rangeMinutes: number,
  rangeStartMs: number,
  rangeEndMs: number
): LatencyDataPoint[] {
  const bucketMs = bucketMinutes * 60 * 1000
  const byBucket = aggregateByBucket(parseWorkflowWindows(windows), bucketMs)
  const filled = fillBucketGaps(byBucket, rangeStartMs, rangeEndMs, bucketMs)
  return filled.map(({ bucketStart, wf }) => ({
    time: formatBucketLabel(bucketStart, bucketMinutes, rangeMinutes),
    timestamp: bucketStart,
    completedAvg: avgLatency(wf.runningToCompleted),
    completedMax: wf.runningToCompleted?.maxLatencyMs ?? 0,
    errorAvg: avgLatency(wf.runningToError),
    errorMax: wf.runningToError?.maxLatencyMs ?? 0,
  }))
}

export type PieDataPoint = {
  name: string
  value: number
  fill: string
}

export function transformToPieData(windows: MetricWindow[], viewMode: 'count' | 'latency'): PieDataPoint[] {
  const parsed = parseWorkflowWindows(windows)
  const allWfs = parsed.map(p => p.wf)
  const merged = mergeWfMetricsGroup(allWfs)

  if (viewMode === 'count') {
    return [
      { name: 'Started', value: merged.started?.count ?? 0, fill: 'hsl(221, 83%, 53%)' },
      { name: 'Completed', value: merged.runningToCompleted?.count ?? 0, fill: 'hsl(142, 71%, 45%)' },
      { name: 'Error', value: merged.runningToError?.count ?? 0, fill: 'hsl(0, 84%, 60%)' },
      { name: 'Exception', value: merged.runningToException?.count ?? 0, fill: 'hsl(38, 92%, 50%)' },
    ].filter(d => d.value > 0)
  }

  return [
    { name: 'Completed (avg)', value: avgLatency(merged.runningToCompleted), fill: 'hsl(142, 71%, 45%)' },
    { name: 'Error (avg)', value: avgLatency(merged.runningToError), fill: 'hsl(0, 84%, 60%)' },
  ].filter(d => d.value > 0)
}
