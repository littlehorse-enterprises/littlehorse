/** UTC RFC3339 with optional fractional seconds (maps to protobuf Timestamp JSON). */
const RFC3339_UTC = /^(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2}):(\d{2})(\.(\d{1,9}))?Z$/

function fractionalDigitsToNanos(fracDigits: string): number {
  if (!fracDigits) return 0
  const padded = fracDigits.padEnd(9, '0').slice(0, 9)
  return Math.round(parseFloat(`0.${padded}`) * 1e9)
}

/**
 * Builds an RFC3339 UTC string with nanosecond precision in the fractional component.
 */
export function buildRfc3339Utc(date: Date, nanoseconds: number): string {
  const ns = Math.min(999_999_999, Math.max(0, Math.floor(nanoseconds)))
  const y = date.getUTCFullYear()
  const mo = String(date.getUTCMonth() + 1).padStart(2, '0')
  const d = String(date.getUTCDate()).padStart(2, '0')
  const h = String(date.getUTCHours()).padStart(2, '0')
  const mi = String(date.getUTCMinutes()).padStart(2, '0')
  const s = String(date.getUTCSeconds()).padStart(2, '0')
  const frac = ns.toString().padStart(9, '0')
  return `${y}-${mo}-${d}T${h}:${mi}:${s}.${frac}Z`
}

export function parseRfc3339Utc(input: string): { date: Date; nanoseconds: number } | null {
  const trimmed = input.trim()
  const m = trimmed.match(RFC3339_UTC)
  if (m) {
    const [, y, mo, d, h, mi, sec, , fracRaw] = m
    const nanoseconds = fractionalDigitsToNanos(fracRaw ?? '')
    const ms = Math.floor(nanoseconds / 1_000_000)
    const date = new Date(Date.UTC(+y, +mo - 1, +d, +h, +mi, +sec, ms))
    return { date, nanoseconds }
  }
  const parsed = Date.parse(trimmed)
  if (Number.isNaN(parsed)) return null
  const date = new Date(parsed)
  return { date, nanoseconds: date.getUTCMilliseconds() * 1_000_000 }
}

export function unixMillisToRfc3339(ms: number): string {
  const date = new Date(ms)
  const nanos = (ms % 1000) * 1_000_000
  return buildRfc3339Utc(date, nanos)
}

export function rfc3339ToUnixMillis(rfc: string): number | null {
  const parsed = parseRfc3339Utc(rfc)
  if (!parsed) return null
  return parsed.date.getTime()
}

export function normalizeUtcTimestampString(value: string): string {
  const trimmed = value.trim()
  if (!trimmed) throw new Error('Empty timestamp')
  const parsed = parseRfc3339Utc(trimmed)
  if (parsed) return buildRfc3339Utc(parsed.date, parsed.nanoseconds)
  const d = new Date(trimmed)
  if (Number.isNaN(d.getTime())) throw new Error('Invalid timestamp')
  return buildRfc3339Utc(d, d.getUTCMilliseconds() * 1_000_000)
}
