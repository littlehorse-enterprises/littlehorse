import { parseExpression } from 'cron-parser'

import { FUTURE_TIME_RANGES } from '@/app/constants'
export function getCronTimeWindow(cronExpression: string): number | undefined {
  try {
    const interval = parseExpression(cronExpression)
    const nextExecution = interval.next().toDate()
    const now = new Date()
    const minutesUntil = (nextExecution.getTime() - now.getTime()) / (1000 * 60)

    const timeWindow = FUTURE_TIME_RANGES.find(range => minutesUntil <= range.value)

    return timeWindow?.value ?? undefined
  } catch (error) {
    console.error('Invalid cron expression:', error)
    return undefined
  }
}
