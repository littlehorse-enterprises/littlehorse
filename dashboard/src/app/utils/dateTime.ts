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

export type StartTimeWindow = { latestStart: string; earliestStart: string } | undefined

export function computeStartTimeWindow(windowMinutes: number): StartTimeWindow {
  if (windowMinutes === -1) return undefined
  const now = new Date()
  const latestStart = now.toISOString()
  const earliestStart = new Date(now.getTime() - windowMinutes * 6e4).toISOString()
  return { latestStart, earliestStart }
}
