import { MetricsList, MetricWindow, Timestamp } from 'littlehorse-client/proto'

/** Wider ranges are split so no single scan spans more than this. */
const CHUNK_MINUTES = 90
const MAX_CONCURRENT = 8

export const toTimestamp = (value?: string): Timestamp | undefined =>
  value ? Timestamp.fromDate(new Date(value)) : undefined

type MetricsScan = (windowStart?: Timestamp, windowEnd?: Timestamp) => Promise<MetricsList>

/**
 * Runs a metrics scan over a time range, splitting ranges wider than CHUNK_MINUTES into
 * bounded concurrent scans so one request cannot ask the server for an unbounded window.
 */
export const listMetricsChunked = async (
  scan: MetricsScan,
  windowStart?: string,
  windowEnd?: string
): Promise<MetricsList> => {
  if (!windowStart || !windowEnd) {
    return scan(toTimestamp(windowStart), toTimestamp(windowEnd))
  }

  const startMs = new Date(windowStart).getTime()
  const endMs = new Date(windowEnd).getTime()

  if ((endMs - startMs) / 60_000 <= CHUNK_MINUTES) {
    return scan(toTimestamp(windowStart), toTimestamp(windowEnd))
  }

  const chunks: { start: string; end: string }[] = []
  let cursor = startMs
  while (cursor < endMs) {
    const chunkEnd = Math.min(cursor + CHUNK_MINUTES * 60_000, endMs)
    chunks.push({ start: new Date(cursor).toISOString(), end: new Date(chunkEnd).toISOString() })
    cursor = chunkEnd
  }

  const allWindows: MetricWindow[] = []
  for (let i = 0; i < chunks.length; i += MAX_CONCURRENT) {
    const results = await Promise.all(
      chunks.slice(i, i + MAX_CONCURRENT).map(c => scan(toTimestamp(c.start), toTimestamp(c.end)))
    )
    for (const r of results) {
      allWindows.push(...(r.windows ?? []))
    }
  }

  return { windows: allWindows }
}
