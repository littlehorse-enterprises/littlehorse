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

export const utcToLocalDateTime = (utcISODateTime: string): string =>
  new Date(Date.parse(utcISODateTime)).toLocaleString(undefined, { hour12: false, timeZoneName: 'short' })

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
