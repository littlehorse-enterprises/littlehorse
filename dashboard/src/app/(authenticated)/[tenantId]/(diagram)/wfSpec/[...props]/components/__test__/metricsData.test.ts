import { CountAndTiming, MetricWindow, WfMetrics } from 'littlehorse-client/proto'
import {
  mergeManyTimings,
  mergeWfMetricsGroup,
  bucketStartMs,
  enumerateBucketStarts,
  aggregateByBucket,
  fillBucketGaps,
  transformToCountData,
  transformToLatencyData,
  EMPTY_WF_METRICS,
  avgLatency,
  parseWorkflowWindows,
} from '../metricsData'

function ct(count: number, min: number, max: number, total: number): CountAndTiming {
  return { count, minLatencyMs: min, maxLatencyMs: max, totalLatencyMs: total }
}

function wfMetrics(overrides: Partial<WfMetrics> = {}): WfMetrics {
  return {
    started: undefined,
    runningToCompleted: undefined,
    runningToError: undefined,
    runningToException: undefined,
    runningToHalting: undefined,
    runningToHalted: undefined,
    haltingToHalted: undefined,
    haltedToRunning: undefined,
    ...overrides,
  }
}

function makeWindow(windowStartISO: string, wf: WfMetrics): MetricWindow {
  return {
    id: { windowStart: windowStartISO, id: undefined, tenantId: undefined },
    metric: { $case: 'workflow' as const, value: wf },
  }
}

// ─── mergeManyTimings ─────────────────────────────────────────────

describe('mergeManyTimings', () => {
  it('returns zeros for empty input', () => {
    expect(mergeManyTimings([])).toEqual(ct(0, 0, 0, 0))
  })

  it('returns zeros when all inputs are undefined', () => {
    expect(mergeManyTimings([undefined, undefined])).toEqual(ct(0, 0, 0, 0))
  })

  it('skips entries with count=0', () => {
    const result = mergeManyTimings([ct(0, 10, 20, 30), ct(5, 1, 50, 100)])
    expect(result).toEqual(ct(5, 1, 50, 100))
  })

  it('sums counts and totalLatencyMs, takes min of mins, max of maxes', () => {
    const result = mergeManyTimings([ct(3, 10, 100, 300), ct(7, 5, 200, 700)])
    expect(result).toEqual(ct(10, 5, 200, 1000))
  })

  it('handles a single entry', () => {
    expect(mergeManyTimings([ct(2, 50, 150, 200)])).toEqual(ct(2, 50, 150, 200))
  })

  it('handles mix of undefined and real entries', () => {
    const result = mergeManyTimings([undefined, ct(4, 20, 80, 240), undefined, ct(6, 10, 90, 360)])
    expect(result).toEqual(ct(10, 10, 90, 600))
  })
})

// ─── avgLatency ───────────────────────────────────────────────────

describe('avgLatency', () => {
  it('returns 0 for undefined', () => {
    expect(avgLatency(undefined)).toBe(0)
  })

  it('returns 0 when count is 0', () => {
    expect(avgLatency(ct(0, 0, 0, 0))).toBe(0)
  })

  it('computes rounded average', () => {
    expect(avgLatency(ct(3, 10, 100, 250))).toBe(83)
  })
})

// ─── mergeWfMetricsGroup ──────────────────────────────────────────

describe('mergeWfMetricsGroup', () => {
  it('returns all-zero metrics for empty input', () => {
    const result = mergeWfMetricsGroup([])
    expect(result.started).toEqual(ct(0, 0, 0, 0))
    expect(result.runningToCompleted).toEqual(ct(0, 0, 0, 0))
  })

  it('merges started and runningToCompleted across multiple WfMetrics', () => {
    const a = wfMetrics({ started: ct(5, 0, 0, 0), runningToCompleted: ct(3, 10, 50, 90) })
    const b = wfMetrics({ started: ct(8, 0, 0, 0), runningToCompleted: ct(7, 5, 100, 210) })
    const result = mergeWfMetricsGroup([a, b])
    expect(result.started).toEqual(ct(13, 0, 0, 0))
    expect(result.runningToCompleted).toEqual(ct(10, 5, 100, 300))
  })
})

// ─── bucketStartMs ────────────────────────────────────────────────

describe('bucketStartMs', () => {
  it('aligns to 5-minute epoch boundaries', () => {
    const fiveMin = 5 * 60 * 1000
    const ts = new Date('2026-04-07T10:03:00Z').getTime()
    const expected = new Date('2026-04-07T10:00:00Z').getTime()
    expect(bucketStartMs(ts, fiveMin)).toBe(expected)
  })

  it('aligns to 1-hour epoch boundaries', () => {
    const oneHour = 60 * 60 * 1000
    const ts = new Date('2026-04-07T10:45:00Z').getTime()
    const expected = new Date('2026-04-07T10:00:00Z').getTime()
    expect(bucketStartMs(ts, oneHour)).toBe(expected)
  })

  it('aligns day buckets to local midnight (not UTC midnight)', () => {
    const dayMs = 86_400_000
    const ts = new Date('2026-04-07T03:00:00Z').getTime()
    const result = bucketStartMs(ts, dayMs)
    const d = new Date(ts)
    const localMidnight = new Date(d.getFullYear(), d.getMonth(), d.getDate()).getTime()
    expect(result).toBe(localMidnight)
  })
})

// ─── enumerateBucketStarts ────────────────────────────────────────

describe('enumerateBucketStarts', () => {
  it('produces correct sub-day buckets', () => {
    const fiveMin = 5 * 60 * 1000
    const start = new Date('2026-04-07T10:00:00Z').getTime()
    const end = new Date('2026-04-07T10:14:59Z').getTime()
    const result = enumerateBucketStarts(start, end, fiveMin)
    expect(result).toEqual([
      new Date('2026-04-07T10:00:00Z').getTime(),
      new Date('2026-04-07T10:05:00Z').getTime(),
      new Date('2026-04-07T10:10:00Z').getTime(),
    ])
  })

  it('produces local-midnight-aligned day buckets', () => {
    const dayMs = 86_400_000
    const april5Local = new Date(2026, 3, 5).getTime()
    const april7_3pm = new Date(2026, 3, 7, 15, 0).getTime()
    const result = enumerateBucketStarts(april5Local, april7_3pm, dayMs)
    expect(result).toEqual([
      new Date(2026, 3, 5).getTime(),
      new Date(2026, 3, 6).getTime(),
      new Date(2026, 3, 7).getTime(),
    ])
  })

  it('returns single bucket when range fits in one', () => {
    const hourMs = 60 * 60 * 1000
    const start = new Date('2026-04-07T10:15:00Z').getTime()
    const end = new Date('2026-04-07T10:45:00Z').getTime()
    const result = enumerateBucketStarts(start, end, hourMs)
    expect(result).toEqual([new Date('2026-04-07T10:00:00Z').getTime()])
  })
})

// ─── aggregateByBucket ────────────────────────────────────────────

describe('aggregateByBucket', () => {
  it('groups windows into 5-minute buckets and sums counts', () => {
    const fiveMin = 5 * 60 * 1000
    const points = [
      { ts: new Date('2026-04-07T10:01:00Z').getTime(), wf: wfMetrics({ started: ct(3, 0, 0, 0) }) },
      { ts: new Date('2026-04-07T10:03:00Z').getTime(), wf: wfMetrics({ started: ct(7, 0, 0, 0) }) },
      { ts: new Date('2026-04-07T10:06:00Z').getTime(), wf: wfMetrics({ started: ct(2, 0, 0, 0) }) },
    ]
    const result = aggregateByBucket(points, fiveMin)
    const bucket1 = new Date('2026-04-07T10:00:00Z').getTime()
    const bucket2 = new Date('2026-04-07T10:05:00Z').getTime()

    expect(result.size).toBe(2)
    expect(result.get(bucket1)!.started!.count).toBe(10)
    expect(result.get(bucket2)!.started!.count).toBe(2)
  })
})

// ─── fillBucketGaps ───────────────────────────────────────────────

describe('fillBucketGaps', () => {
  it('fills missing buckets with zeros', () => {
    const fiveMin = 5 * 60 * 1000
    const start = new Date('2026-04-07T10:00:00Z').getTime()
    const end = new Date('2026-04-07T10:14:59Z').getTime()

    const byBucket = new Map<number, WfMetrics>()
    byBucket.set(start, wfMetrics({ started: ct(5, 0, 0, 0) }))

    const filled = fillBucketGaps(byBucket, start, end, fiveMin)
    expect(filled).toHaveLength(3)
    expect(filled[0].wf.started!.count).toBe(5)
    expect(filled[1].wf.started!.count).toBe(0)
    expect(filled[2].wf.started!.count).toBe(0)
  })
})

// ─── parseWorkflowWindows ─────────────────────────────────────────

describe('parseWorkflowWindows', () => {
  it('filters out non-workflow windows', () => {
    const wfWindow = makeWindow('2026-04-07T10:00:00Z', wfMetrics({ started: ct(1, 0, 0, 0) }))
    const taskWindow: MetricWindow = {
      id: { windowStart: '2026-04-07T10:00:00Z', id: undefined, tenantId: undefined },
      metric: undefined,
    }
    const result = parseWorkflowWindows([wfWindow, taskWindow])
    expect(result).toHaveLength(1)
    expect(result[0].wf.started!.count).toBe(1)
  })

  it('sorts by timestamp ascending', () => {
    const a = makeWindow('2026-04-07T10:04:00Z', wfMetrics())
    const b = makeWindow('2026-04-07T10:00:00Z', wfMetrics())
    const result = parseWorkflowWindows([a, b])
    expect(result[0].ts).toBeLessThan(result[1].ts)
  })
})

// ─── transformToCountData ─────────────────────────────────────────

describe('transformToCountData', () => {
  it('returns zero-filled timeline when no windows provided', () => {
    const start = new Date('2026-04-07T10:00:00Z').getTime()
    const end = new Date('2026-04-07T10:09:59Z').getTime()
    const result = transformToCountData([], 5, 60, start, end)
    expect(result).toHaveLength(2)
    expect(result[0].started).toBe(0)
    expect(result[0].completed).toBe(0)
    expect(result[1].started).toBe(0)
  })

  it('aggregates multiple windows into buckets', () => {
    const w1 = makeWindow(
      '2026-04-07T10:01:00Z',
      wfMetrics({
        started: ct(3, 0, 0, 0),
        runningToCompleted: ct(2, 10, 50, 80),
      })
    )
    const w2 = makeWindow(
      '2026-04-07T10:03:00Z',
      wfMetrics({
        started: ct(5, 0, 0, 0),
        runningToCompleted: ct(4, 5, 100, 200),
      })
    )

    const start = new Date('2026-04-07T10:00:00Z').getTime()
    const end = new Date('2026-04-07T10:04:59Z').getTime()
    const result = transformToCountData([w1, w2], 5, 60, start, end)
    expect(result).toHaveLength(1)
    expect(result[0].started).toBe(8)
    expect(result[0].completed).toBe(6)
  })

  it('consistent data between overlapping 24h and 7d ranges', () => {
    const april6_10am = '2026-04-06T10:00:00Z'
    const april6_14pm = '2026-04-06T14:00:00Z'
    const w1 = makeWindow(april6_10am, wfMetrics({ started: ct(100, 0, 0, 0) }))
    const w2 = makeWindow(april6_14pm, wfMetrics({ started: ct(200, 0, 0, 0) }))

    const dayBucket = 1440
    const april6LocalStart = new Date(2026, 3, 6).getTime()
    const april6LocalEnd = new Date(2026, 3, 6, 23, 59, 59).getTime()

    const result24h = transformToCountData([w1, w2], dayBucket, 1440, april6LocalStart, april6LocalEnd)
    const april5LocalStart = new Date(2026, 3, 1).getTime()
    const result7d = transformToCountData([w1, w2], dayBucket, 10080, april5LocalStart, april6LocalEnd)

    const april6bucket24h = result24h.find(p => p.timestamp === april6LocalStart)
    const april6bucket7d = result7d.find(p => p.timestamp === april6LocalStart)

    expect(april6bucket24h).toBeDefined()
    expect(april6bucket7d).toBeDefined()
    expect(april6bucket24h!.started).toBe(april6bucket7d!.started)
    expect(april6bucket24h!.started).toBe(300)
  })
})

// ─── transformToLatencyData ───────────────────────────────────────

describe('transformToLatencyData', () => {
  it('computes weighted average latency across merged windows', () => {
    const w1 = makeWindow(
      '2026-04-07T10:01:00Z',
      wfMetrics({
        runningToCompleted: ct(2, 10, 50, 80),
      })
    )
    const w2 = makeWindow(
      '2026-04-07T10:03:00Z',
      wfMetrics({
        runningToCompleted: ct(8, 5, 100, 320),
      })
    )

    const start = new Date('2026-04-07T10:00:00Z').getTime()
    const end = new Date('2026-04-07T10:04:59Z').getTime()
    const result = transformToLatencyData([w1, w2], 5, 60, start, end)

    expect(result).toHaveLength(1)
    expect(result[0].completedAvg).toBe(40) // (80+320)/10 = 40
    expect(result[0].completedMax).toBe(100)
  })

  it('returns zero latency for empty windows', () => {
    const start = new Date('2026-04-07T10:00:00Z').getTime()
    const end = new Date('2026-04-07T10:04:59Z').getTime()
    const result = transformToLatencyData([], 5, 60, start, end)
    expect(result).toHaveLength(1)
    expect(result[0].completedAvg).toBe(0)
    expect(result[0].completedMax).toBe(0)
    expect(result[0].errorAvg).toBe(0)
  })
})

// ─── EMPTY_WF_METRICS ────────────────────────────────────────────

describe('EMPTY_WF_METRICS', () => {
  it('has all zero counts', () => {
    expect(EMPTY_WF_METRICS.started!.count).toBe(0)
    expect(EMPTY_WF_METRICS.runningToCompleted!.count).toBe(0)
    expect(EMPTY_WF_METRICS.runningToError!.count).toBe(0)
    expect(EMPTY_WF_METRICS.runningToException!.count).toBe(0)
  })
})
