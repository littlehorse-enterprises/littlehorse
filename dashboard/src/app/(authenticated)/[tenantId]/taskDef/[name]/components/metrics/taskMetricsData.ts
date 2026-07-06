import { MetricWindow, TaskMetrics } from 'littlehorse-client/proto'
import { toDate } from '@/app/utils'
import {
  avgLatency,
  bucketStartMs,
  CountDataPoint,
  enumerateBucketStarts,
  formatBucketLabel,
  LatencyDataPoint,
  mergeManyTimings,
  PieDataPoint,
} from '@/app/(authenticated)/[tenantId]/(diagram)/wfSpec/[...props]/components/metrics/metricsData'

export type { CountDataPoint, LatencyDataPoint, PieDataPoint }

export function mergeTaskMetricsGroup(tasks: TaskMetrics[]): TaskMetrics {
  return {
    taskrunCreatedToCompleted: mergeManyTimings(tasks.map(t => t.taskrunCreatedToCompleted)),
    taskrunCreatedToError: mergeManyTimings(tasks.map(t => t.taskrunCreatedToError)),
    taskrunCreatedToException: mergeManyTimings(tasks.map(t => t.taskrunCreatedToException)),
    taskattemptPendingToScheduled: mergeManyTimings(tasks.map(t => t.taskattemptPendingToScheduled)),
    taskattemptScheduledToRunning: mergeManyTimings(tasks.map(t => t.taskattemptScheduledToRunning)),
    taskattemptRunningToError: mergeManyTimings(tasks.map(t => t.taskattemptRunningToError)),
    taskattemptRunningToSuccess: mergeManyTimings(tasks.map(t => t.taskattemptRunningToSuccess)),
    taskattemptRunningToException: mergeManyTimings(tasks.map(t => t.taskattemptRunningToException)),
  }
}

export const EMPTY_TASK_METRICS: TaskMetrics = mergeTaskMetricsGroup([])

export function parseTaskWindows(windows: MetricWindow[]): { ts: number; task: TaskMetrics }[] {
  return windows
    .filter(w => w.metric?.oneofKind === 'task')
    .map(w => ({
      ts: toDate(w.id?.windowStart)?.getTime() ?? 0,
      task: (w.metric as { oneofKind: 'task'; task: TaskMetrics }).task,
    }))
    .sort((a, b) => a.ts - b.ts)
}

export function aggregateTaskByBucket(
  points: { ts: number; task: TaskMetrics }[],
  bucketMs: number
): Map<number, TaskMetrics> {
  const groups = new Map<number, TaskMetrics[]>()
  for (const p of points) {
    const k = bucketStartMs(p.ts, bucketMs)
    const arr = groups.get(k) ?? []
    arr.push(p.task)
    groups.set(k, arr)
  }
  const merged = new Map<number, TaskMetrics>()
  groups.forEach((tasks, bs) => {
    merged.set(bs, mergeTaskMetricsGroup(tasks))
  })
  return merged
}

function fillTaskBucketGaps(
  byBucket: Map<number, TaskMetrics>,
  rangeStartMs: number,
  rangeEndMs: number,
  bucketMs: number
): { bucketStart: number; task: TaskMetrics }[] {
  return enumerateBucketStarts(rangeStartMs, rangeEndMs, bucketMs).map(bucketStart => ({
    bucketStart,
    task: byBucket.get(bucketStart) ?? EMPTY_TASK_METRICS,
  }))
}

export function transformTaskToCountData(
  windows: MetricWindow[],
  bucketMinutes: number,
  rangeMinutes: number,
  rangeStartMs: number,
  rangeEndMs: number
): CountDataPoint[] {
  const bucketMs = bucketMinutes * 60 * 1000
  const byBucket = aggregateTaskByBucket(parseTaskWindows(windows), bucketMs)
  const filled = fillTaskBucketGaps(byBucket, rangeStartMs, rangeEndMs, bucketMs)
  return filled.map(({ bucketStart, task }) => ({
    time: formatBucketLabel(bucketStart, bucketMinutes, rangeMinutes),
    timestamp: bucketStart,
    started: task.taskattemptScheduledToRunning?.count ?? 0,
    completed: task.taskrunCreatedToCompleted?.count ?? 0,
    error: task.taskrunCreatedToError?.count ?? 0,
    exception: task.taskrunCreatedToException?.count ?? 0,
  }))
}

export function transformTaskToLatencyData(
  windows: MetricWindow[],
  bucketMinutes: number,
  rangeMinutes: number,
  rangeStartMs: number,
  rangeEndMs: number
): LatencyDataPoint[] {
  const bucketMs = bucketMinutes * 60 * 1000
  const byBucket = aggregateTaskByBucket(parseTaskWindows(windows), bucketMs)
  const filled = fillTaskBucketGaps(byBucket, rangeStartMs, rangeEndMs, bucketMs)
  return filled.map(({ bucketStart, task }) => ({
    time: formatBucketLabel(bucketStart, bucketMinutes, rangeMinutes),
    timestamp: bucketStart,
    completedAvg: avgLatency(task.taskrunCreatedToCompleted),
    completedMax: Number(task.taskrunCreatedToCompleted?.maxLatencyMs ?? 0),
    errorAvg: avgLatency(task.taskrunCreatedToError),
    errorMax: Number(task.taskrunCreatedToError?.maxLatencyMs ?? 0),
  }))
}

export function transformTaskToPieData(windows: MetricWindow[], viewMode: 'count' | 'latency'): PieDataPoint[] {
  const parsed = parseTaskWindows(windows)
  const merged = mergeTaskMetricsGroup(parsed.map(p => p.task))

  if (viewMode === 'count') {
    return [
      { name: 'Started', value: merged.taskattemptScheduledToRunning?.count ?? 0, fill: 'hsl(221, 83%, 53%)' },
      { name: 'Completed', value: merged.taskrunCreatedToCompleted?.count ?? 0, fill: 'hsl(142, 71%, 45%)' },
      { name: 'Error', value: merged.taskrunCreatedToError?.count ?? 0, fill: 'hsl(0, 84%, 60%)' },
      { name: 'Exception', value: merged.taskrunCreatedToException?.count ?? 0, fill: 'hsl(38, 92%, 50%)' },
    ].filter(d => d.value > 0)
  }

  return [
    { name: 'Completed (avg)', value: avgLatency(merged.taskrunCreatedToCompleted), fill: 'hsl(142, 71%, 45%)' },
    { name: 'Error (avg)', value: avgLatency(merged.taskrunCreatedToError), fill: 'hsl(0, 84%, 60%)' },
  ].filter(d => d.value > 0)
}
