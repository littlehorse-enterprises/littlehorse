import { MetricWindow, QuotaUsageMetrics, Timestamp } from 'littlehorse-client/proto'
import {
  bucketQuotaUsage,
  EMPTY_QUOTA_USAGE,
  mergeQuotaUsageGroup,
  parseQuotaWindows,
  summarizeQuotaUsage,
  toQuotaCountData,
  toQuotaThrottleData,
} from '../quotaMetricsData'

function qu(observed: number, throttled: number, totalThrottleMs: number): QuotaUsageMetrics {
  return {
    requestsObserved: observed,
    requestsThrottled: throttled,
    totalThrottleTimeMs: String(totalThrottleMs),
  }
}

function makeQuotaWindow(windowStartISO: string, usage: QuotaUsageMetrics): MetricWindow {
  return {
    id: {
      windowStart: Timestamp.fromDate(new Date(windowStartISO)),
      id: { oneofKind: undefined },
      tenantId: undefined,
    },
    metric: { oneofKind: 'quotaUsage', quotaUsage: usage },
  }
}

function makeWorkflowWindow(windowStartISO: string): MetricWindow {
  return {
    id: {
      windowStart: Timestamp.fromDate(new Date(windowStartISO)),
      id: { oneofKind: undefined },
      tenantId: undefined,
    },
    metric: {
      oneofKind: 'workflow',
      workflow: {
        started: undefined,
        runningToCompleted: undefined,
        runningToError: undefined,
        runningToException: undefined,
        runningToHalting: undefined,
        runningToHalted: undefined,
        haltingToHalted: undefined,
        haltedToRunning: undefined,
      },
    },
  }
}

describe('parseQuotaWindows', () => {
  it('keeps only quotaUsage windows', () => {
    const windows = [makeWorkflowWindow('2026-09-03T10:00:00Z'), makeQuotaWindow('2026-09-03T10:01:00Z', qu(5, 1, 500))]
    const parsed = parseQuotaWindows(windows)
    expect(parsed).toHaveLength(1)
    expect(parsed[0].usage).toEqual(qu(5, 1, 500))
  })

  it('sorts windows by start time', () => {
    const windows = [
      makeQuotaWindow('2026-09-03T10:05:00Z', qu(2, 0, 0)),
      makeQuotaWindow('2026-09-03T10:01:00Z', qu(1, 0, 0)),
    ]
    const parsed = parseQuotaWindows(windows)
    expect(parsed.map(p => p.usage.requestsObserved)).toEqual([1, 2])
  })
})

describe('mergeQuotaUsageGroup', () => {
  it('returns zeros for empty input', () => {
    expect(mergeQuotaUsageGroup([])).toEqual(EMPTY_QUOTA_USAGE)
  })

  it('sums counts and throttle time across parts', () => {
    const merged = mergeQuotaUsageGroup([qu(10, 2, 1000), qu(5, 3, 2500)])
    expect(merged).toEqual(qu(15, 5, 3500))
  })
})

const countData = (windows: MetricWindow[], bucketMinutes: number, rangeMinutes: number, s: number, e: number) =>
  toQuotaCountData(bucketQuotaUsage(windows, bucketMinutes, rangeMinutes, s, e))

const throttleData = (windows: MetricWindow[], bucketMinutes: number, rangeMinutes: number, s: number, e: number) =>
  toQuotaThrottleData(bucketQuotaUsage(windows, bucketMinutes, rangeMinutes, s, e))

describe('bucketQuotaUsage / toQuotaCountData', () => {
  const rangeStartMs = Date.parse('2026-09-03T10:00:00Z')
  const rangeEndMs = Date.parse('2026-09-03T10:10:00Z')

  it('aggregates windows into buckets and zero-fills gaps', () => {
    const windows = [
      makeQuotaWindow('2026-09-03T10:00:00Z', qu(10, 1, 500)),
      makeQuotaWindow('2026-09-03T10:01:00Z', qu(20, 4, 2000)),
      makeQuotaWindow('2026-09-03T10:06:00Z', qu(7, 0, 0)),
    ]
    const data = countData(windows, 5, 10, rangeStartMs, rangeEndMs)
    expect(data).toHaveLength(3)
    expect(data[0]).toMatchObject({ timestamp: rangeStartMs, observed: 30, throttled: 5 })
    expect(data[1]).toMatchObject({ observed: 7, throttled: 0 })
    expect(data[2]).toMatchObject({ timestamp: rangeEndMs, observed: 0, throttled: 0 })
  })

  it('returns all-zero buckets when there are no windows', () => {
    const data = countData([], 5, 10, rangeStartMs, rangeEndMs)
    expect(data).toHaveLength(3)
    expect(data.every(d => d.observed === 0 && d.throttled === 0)).toBe(true)
  })
})

describe('toQuotaThrottleData', () => {
  const rangeStartMs = Date.parse('2026-09-03T10:00:00Z')
  const rangeEndMs = Date.parse('2026-09-03T10:05:00Z')

  it('computes total and average backoff per bucket', () => {
    const windows = [
      makeQuotaWindow('2026-09-03T10:00:00Z', qu(100, 4, 1000)),
      makeQuotaWindow('2026-09-03T10:01:00Z', qu(100, 6, 4000)),
    ]
    const data = throttleData(windows, 5, 5, rangeStartMs, rangeEndMs)
    expect(data[0]).toMatchObject({ avgThrottleMs: 500 })
  })

  it('reports zero average when nothing was throttled', () => {
    const windows = [makeQuotaWindow('2026-09-03T10:00:00Z', qu(50, 0, 0))]
    const data = throttleData(windows, 5, 5, rangeStartMs, rangeEndMs)
    expect(data[0]).toMatchObject({ avgThrottleMs: 0 })
  })
})

describe('summarizeQuotaUsage', () => {
  const rangeStart = Date.parse('2026-09-03T10:00:00Z')
  const rangeEnd = Date.parse('2026-09-03T10:10:00Z')

  it('returns zeros and a zero rate for no windows', () => {
    expect(summarizeQuotaUsage(bucketQuotaUsage([], 5, 10, rangeStart, rangeEnd))).toEqual({
      observed: 0,
      throttled: 0,
      throttleRate: 0,
      totalThrottleTimeMs: 0,
    })
  })

  it('totals usage across windows and derives the throttle rate', () => {
    const windows = [
      makeQuotaWindow('2026-09-03T10:00:00Z', qu(600, 100, 50_000)),
      makeQuotaWindow('2026-09-03T10:01:00Z', qu(400, 150, 75_000)),
    ]
    expect(summarizeQuotaUsage(bucketQuotaUsage(windows, 5, 10, rangeStart, rangeEnd))).toEqual({
      observed: 1000,
      throttled: 250,
      throttleRate: 0.25,
      totalThrottleTimeMs: 125_000,
    })
  })
})

describe('summary and chart consistency', () => {
  it('tiles total exactly what the chart buckets show', () => {
    const rangeStart = Date.parse('2026-09-03T10:02:30Z')
    const rangeEnd = Date.parse('2026-09-03T10:12:30Z')
    const windows = [
      makeQuotaWindow('2026-09-03T10:01:00Z', qu(999, 999, 999)),
      makeQuotaWindow('2026-09-03T10:06:00Z', qu(10, 4, 2000)),
      makeQuotaWindow('2026-09-03T10:07:00Z', qu(20, 6, 4000)),
    ]
    const buckets = bucketQuotaUsage(windows, 5, 10, rangeStart, rangeEnd)
    const summary = summarizeQuotaUsage(buckets)
    const charted = toQuotaCountData(buckets)

    expect(summary.observed).toBe(charted.reduce((n, p) => n + p.observed, 0))
    expect(summary.throttled).toBe(charted.reduce((n, p) => n + p.throttled, 0))
    expect(summary.observed).toBe(30)
  })
})
