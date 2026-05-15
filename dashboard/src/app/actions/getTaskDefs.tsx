'use server'

import { uniqueInOrder } from '@/app/utils'
import { TaskDefData } from '@/types'
import { lhClient } from '../lhClient'

export async function getTaskDefs(tenantId: string, taskDefNames: string[]): Promise<TaskDefData[]> {
  const client = await lhClient({ tenantId })
  const uniqueOrdered = uniqueInOrder(taskDefNames)

  const taskDefMap = new Map<string, TaskDefData>()
  await Promise.all(
    uniqueOrdered.map(async name => {
      const taskDef = await client.getTaskDef({ name })
      if (!taskDef) return

      taskDefMap.set(name, {
        name,
        createdAt: taskDef.createdAt ? new Date(taskDef.createdAt) : undefined,
      })
    })
  )

  return uniqueOrdered.map(name => taskDefMap.get(name)).filter((row): row is TaskDefData => row != null)
}
