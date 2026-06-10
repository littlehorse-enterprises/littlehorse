'use server'

import { lhClient } from '@/app/lhClient'
import { WithTenant } from '@/types'
import { MetricsList, MetricWindow, TaskDefId } from 'littlehorse-client/proto'

type GetTaskMetricsProps = {
  taskDefId: TaskDefId
  windowStart?: string
  windowEnd?: string
} & WithTenant

const CHUNK_MINUTES = 90
const MAX_CONCURRENT = 8

export const getTaskMetrics = async ({
  taskDefId,
  windowStart,
  windowEnd,
  tenantId,
}: GetTaskMetricsProps): Promise<MetricsList> => {
  const client = await lhClient({ tenantId })

  if (!windowStart || !windowEnd) {
    return client.listTaskMetrics({ taskDef: taskDefId, windowStart, windowEnd })
  }

  const startMs = new Date(windowStart).getTime()
  const endMs = new Date(windowEnd).getTime()
  const rangeMinutes = (endMs - startMs) / 60_000

  if (rangeMinutes <= CHUNK_MINUTES) {
    return client.listTaskMetrics({ taskDef: taskDefId, windowStart, windowEnd })
  }

  const chunks: { start: string; end: string }[] = []
  let cursor = startMs
  while (cursor < endMs) {
    const chunkEnd = Math.min(cursor + CHUNK_MINUTES * 60_000, endMs)
    chunks.push({
      start: new Date(cursor).toISOString(),
      end: new Date(chunkEnd).toISOString(),
    })
    cursor = chunkEnd
  }

  const allWindows: MetricWindow[] = []
  for (let i = 0; i < chunks.length; i += MAX_CONCURRENT) {
    const batch = chunks.slice(i, i + MAX_CONCURRENT)
    const results = await Promise.all(
      batch.map(c => client.listTaskMetrics({ taskDef: taskDefId, windowStart: c.start, windowEnd: c.end }))
    )
    for (const r of results) {
      allWindows.push(...(r.windows ?? []))
    }
  }

  return { windows: allWindows }
}
