'use server'

import { TaskDefData } from '@/types'
import { lhClient } from '../lhClient'

export async function getTaskDefs(tenantId: string, taskDefNames: string[]): Promise<TaskDefData[]> {
  const client = await lhClient({ tenantId })
  const taskDefMap = new Map<string, TaskDefData>()

  await Promise.all(
    taskDefNames.map(async name => {
      if (!taskDefMap.has(name)) {
        const taskDef = await client.getTaskDef({ name })
        if (!taskDef) return

        taskDefMap.set(name, {
          name,
          createdAt: taskDef.createdAt ? new Date(taskDef.createdAt) : undefined,
        })
      }
    })
  )

  return Array.from(taskDefMap.values())
}
