import { Timestamp, UserTaskEvent } from 'littlehorse-client/proto'

/**
 * A value that can represent a point in time. With `@protobuf-ts`, protobuf
 * `google.protobuf.Timestamp` fields are represented as a `Timestamp` message
 * (`{ seconds, nanos }`) rather than a `Date`/`string`, so date helpers accept
 * any of these representations.
 */
export type DateLike = string | number | Date | Timestamp | undefined | null

const isProtoTimestamp = (value: unknown): value is Timestamp =>
  typeof value === 'object' && value !== null && 'seconds' in value && 'nanos' in value

/**
 * Normalizes any supported time representation into a `Date`, or `undefined`
 * when the input is missing or invalid.
 */
export const toDate = (input: DateLike): Date | undefined => {
  if (input === undefined || input === null) return undefined
  if (input instanceof Date) return Number.isNaN(input.getTime()) ? undefined : input
  if (typeof input === 'number') return new Date(input)
  if (isProtoTimestamp(input)) return Timestamp.toDate(input)
  const parsed = new Date(input)
  return Number.isNaN(parsed.getTime()) ? undefined : parsed
}

export const formatDate = (date?: Date | number) => {
  return new Intl.DateTimeFormat('en-US', {
    year: 'numeric',
    month: 'numeric',
    day: 'numeric',
    hour: 'numeric',
    minute: 'numeric',
    second: 'numeric',
    hour12: false,
  }).format(date)
}
// Normalize and sort events by their `time` field.

export const getEventTime = (e: UserTaskEvent) => {
  return toDate(e.time)?.getTime() ?? 0
}
/**
 * Human-friendly date/time like: "April 2, 2022 at 11:29 am"
 * Accepts a Date, numeric timestamp, ISO string, or proto Timestamp.
 * Returns empty string for invalid input.
 */
export const formatDateReadable = (dateInput?: DateLike): string => {
  const dt = toDate(dateInput)
  if (!dt) return ''

  const formattedDate = dt.toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  })

  const formattedTime = dt
    .toLocaleTimeString('en-US', {
      hour: 'numeric',
      minute: '2-digit',
      hour12: true,
    })
    .toLowerCase()

  return `  ${formattedDate} at ${formattedTime}`
}

export const utcToLocalDateTime = (utcISODateTime: DateLike): string => {
  const date = toDate(utcISODateTime)
  if (!date) return ''
  return date.toLocaleString(undefined, { hour12: false, timeZoneName: 'short' })
}

export const localDateTimeToUTCIsoString = (localDateTime: string): string => new Date(localDateTime).toISOString()

export const formatTime = (seconds: number | undefined): string => {
  if (!seconds) return ''
  const minute = 60
  const hour = 60 * minute
  const day = 24 * hour
  const year = 365 * day

  const years = Math.floor(seconds / year)
  seconds %= year
  const days = Math.floor(seconds / day)
  seconds %= day
  const hours = Math.floor(seconds / hour)
  seconds %= hour
  const minutes = Math.floor(seconds / minute)
  seconds %= minute

  const parts: string[] = []
  if (years > 0) parts.push(`${years}y`)
  if (days > 0) parts.push(`${days}d`)
  if (hours > 0) parts.push(`${hours}h`)
  if (minutes > 0) parts.push(`${minutes}m`)
  if (seconds > 0 || parts.length === 0) parts.push(`${seconds}s`)

  return parts.join(' ')
}

export function formatDuration(durationMs: number) {
  // Negative durations don't make sense, so we clamp them at 0
  durationMs = Math.max(durationMs, 0)

  let durationDisplay
  if (durationMs < 1000) {
    durationDisplay = `${durationMs} ms`
  } else if (durationMs < 60000) {
    durationDisplay = `${(durationMs / 1000).toFixed(2)} s`
  } else if (durationMs < 3600000) {
    durationDisplay = `${(durationMs / 60000).toFixed(2)} min`
  } else {
    durationDisplay = `${(durationMs / 3600000).toFixed(2)} h`
  }
  return durationDisplay
}

export type StartTimeWindow = { latestStart: string; earliestStart: string } | undefined

export function computeStartTimeWindow(windowMinutes: number): StartTimeWindow {
  if (windowMinutes === -1) return undefined
  const now = new Date()
  const latestStart = now.toISOString()
  const earliestStart = new Date(now.getTime() - windowMinutes * 6e4).toISOString()
  return { latestStart, earliestStart }
}
