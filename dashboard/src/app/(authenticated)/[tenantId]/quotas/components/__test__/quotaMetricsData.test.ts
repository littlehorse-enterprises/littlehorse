import { MetricWindow, QuotaUsageMetrics, Timestamp } from 'littlehorse-client/proto'
import {
  EMPTY_QUOTA_USAGE,
  formatDurationMs,
  mergeQuotaUsageGroup,
  parseQuotaWindows,
  summarizeQuotaUsage,
  transformToQuotaCountData,
  transformToQuotaThrottleData,
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

describe('transformToQuotaCountData', () => {
  const rangeStartMs = Date.parse('2026-09-03T10:00:00Z')
  const rangeEndMs = Date.parse('2026-09-03T10:10:00Z')

  it('aggregates windows into buckets and zero-fills gaps', () => {
    const windows = [
      makeQuotaWindow('2026-09-03T10:00:00Z', qu(10, 1, 500)),
      makeQuotaWindow('2026-09-03T10:01:00Z', qu(20, 4, 2000)),
      makeQuotaWindow('2026-09-03T10:06:00Z', qu(7, 0, 0)),
    ]
    const data = transformToQuotaCountData(windows, 5, 10, rangeStartMs, rangeEndMs)
    expect(data).toHaveLength(3)
    expect(data[0]).toMatchObject({ timestamp: rangeStartMs, observed: 30, throttled: 5 })
    expect(data[1]).toMatchObject({ observed: 7, throttled: 0 })
    expect(data[2]).toMatchObject({ timestamp: rangeEndMs, observed: 0, throttled: 0 })
  })

  it('returns all-zero buckets when there are no windows', () => {
    const data = transformToQuotaCountData([], 5, 10, rangeStartMs, rangeEndMs)
    expect(data).toHaveLength(3)
    expect(data.every(d => d.observed === 0 && d.throttled === 0)).toBe(true)
  })
})

describe('transformToQuotaThrottleData', () => {
  const rangeStartMs = Date.parse('2026-09-03T10:00:00Z')
  const rangeEndMs = Date.parse('2026-09-03T10:05:00Z')

  it('computes total and average backoff per bucket', () => {
    const windows = [
      makeQuotaWindow('2026-09-03T10:00:00Z', qu(100, 4, 1000)),
      makeQuotaWindow('2026-09-03T10:01:00Z', qu(100, 6, 4000)),
    ]
    const data = transformToQuotaThrottleData(windows, 5, 5, rangeStartMs, rangeEndMs)
    expect(data[0]).toMatchObject({ totalThrottleMs: 5000, avgThrottleMs: 500 })
  })

  it('reports zero average when nothing was throttled', () => {
    const windows = [makeQuotaWindow('2026-09-03T10:00:00Z', qu(50, 0, 0))]
    const data = transformToQuotaThrottleData(windows, 5, 5, rangeStartMs, rangeEndMs)
    expect(data[0]).toMatchObject({ totalThrottleMs: 0, avgThrottleMs: 0 })
  })
})

describe('summarizeQuotaUsage', () => {
  it('returns zeros and a zero rate for no windows', () => {
    expect(summarizeQuotaUsage([])).toEqual({
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
    expect(summarizeQuotaUsage(windows)).toEqual({
      observed: 1000,
      throttled: 250,
      throttleRate: 0.25,
      totalThrottleTimeMs: 125_000,
    })
  })
})

describe('formatDurationMs', () => {
  it('formats milliseconds, seconds, minutes, and hours', () => {
    expect(formatDurationMs(0)).toBe('0ms')
    expect(formatDurationMs(999)).toBe('999ms')
    expect(formatDurationMs(1500)).toBe('1.5s')
    expect(formatDurationMs(90_000)).toBe('1m 30s')
    expect(formatDurationMs(120_000)).toBe('2m')
    expect(formatDurationMs(5_400_000)).toBe('1h 30m')
    expect(formatDurationMs(7_200_000)).toBe('2h')
  })
})
