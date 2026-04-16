/** UTC RFC3339 with optional fractional seconds (maps to protobuf Timestamp JSON). */
const RFC3339_UTC = /^(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2}):(\d{2})(\.(\d{1,9}))?Z$/

const TIME_RE = /^(\d{1,2}):(\d{1,2}):(\d{1,2})(?:\.(\d{1,9}))?$/

export type UtcParts = { y: number; mo: number; day: number; h: number; mi: number; s: number; ms: number }

export function utcParts(d: Date): UtcParts {
  return {
    y: d.getUTCFullYear(),
    mo: d.getUTCMonth(),
    day: d.getUTCDate(),
    h: d.getUTCHours(),
    mi: d.getUTCMinutes(),
    s: d.getUTCSeconds(),
    ms: d.getUTCMilliseconds(),
  }
}

export function utcDateFromParts(y: number, mo: number, day: number, h: number, mi: number, s: number, ms: number): Date {
  return new Date(Date.UTC(y, mo, day, h, mi, s, ms))
}

export const msFromNanos = (nanoseconds: number) => Math.floor(nanoseconds / 1_000_000)

export function utcTimeWithNanos(d: Date, nanos: number): string {
  const p = utcParts(d)
  const hms = `${String(p.h).padStart(2, '0')}:${String(p.mi).padStart(2, '0')}:${String(p.s).padStart(2, '0')}`
  if (nanos === 0) return hms
  const subSecondNanos = nanos % 1_000_000_000
  const frac = subSecondNanos.toString().padStart(9, '0').replace(/0+$/, '')
  return frac ? `${hms}.${frac}` : hms
}

export function parseTimeWithNanos(raw: string): { h: number; mi: number; s: number; nanos: number } | null {
  const m = raw.trim().match(TIME_RE)
  if (!m) return null
  const [, hStr, miStr, sStr, fracStr] = m
  const h = parseInt(hStr, 10)
  const mi = parseInt(miStr, 10)
  const s = parseInt(sStr, 10)
  if (h > 23 || mi > 59 || s > 59) return null
  let nanos = 0
  if (fracStr) {
    const padded = fracStr.padEnd(9, '0').slice(0, 9)
    nanos = parseInt(padded, 10)
  }
  return { h, mi, s, nanos }
}

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
  const p = utcParts(date)
  const mo = String(p.mo + 1).padStart(2, '0')
  const d = String(p.day).padStart(2, '0')
  const h = String(p.h).padStart(2, '0')
  const mi = String(p.mi).padStart(2, '0')
  const s = String(p.s).padStart(2, '0')
  const frac = ns.toString().padStart(9, '0')
  return `${p.y}-${mo}-${d}T${h}:${mi}:${s}.${frac}Z`
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
