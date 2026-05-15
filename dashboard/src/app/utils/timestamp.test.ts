import {
  buildRfc3339Utc,
  normalizeUtcTimestampString,
  parseRfc3339Utc,
  rfc3339ToUnixMillis,
  unixMillisToRfc3339,
} from './timestamp'

describe('timestamp RFC3339 helpers', () => {
  it('round-trips UTC with nanoseconds', () => {
    const d = new Date(Date.UTC(2024, 5, 15, 14, 30, 45, 123))
    const nanos = 123456789
    const s = buildRfc3339Utc(d, nanos)
    expect(s).toMatch(/2024-06-15T14:30:45\.123456789Z$/)
    const parsed = parseRfc3339Utc(s)
    expect(parsed).not.toBeNull()
    expect(parsed!.nanoseconds).toBe(123456789)
  })

  it('normalizes loose date input to RFC3339', () => {
    const out = normalizeUtcTimestampString('2024-01-02T03:04:05.000000001Z')
    expect(out).toBe('2024-01-02T03:04:05.000000001Z')
  })
})

describe('unix millis helpers', () => {
  it('converts unix millis to RFC3339 preserving millisecond precision', () => {
    const ms = Date.UTC(2024, 5, 15, 14, 30, 45, 123)
    const rfc = unixMillisToRfc3339(ms)
    expect(rfc).toBe('2024-06-15T14:30:45.123000000Z')
  })

  it('converts unix millis with zero ms to RFC3339', () => {
    const ms = Date.UTC(2024, 0, 1, 0, 0, 0, 0)
    const rfc = unixMillisToRfc3339(ms)
    expect(rfc).toBe('2024-01-01T00:00:00.000000000Z')
  })

  it('round-trips RFC3339 through unix millis', () => {
    const original = '2024-06-15T14:30:45.123000000Z'
    const ms = rfc3339ToUnixMillis(original)
    expect(ms).not.toBeNull()
    expect(ms).toBe(Date.UTC(2024, 5, 15, 14, 30, 45, 123))
  })

  it('returns null for invalid RFC3339 input', () => {
    expect(rfc3339ToUnixMillis('not-a-date')).toBeNull()
  })
})
