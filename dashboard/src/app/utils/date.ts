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

export const utcToLocalDateTime = (utcISODateTime: string) =>
  new Date(Date.parse(utcISODateTime)).toLocaleString(undefined, { hour12: false, timeZoneName: 'short' })
