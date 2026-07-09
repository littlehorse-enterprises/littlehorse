'use server'

import { toDate, uniqueInOrder } from '@/app/utils'
import { TaskDefData } from '@/types'
import { TaskStatus } from 'littlehorse-client/proto'
import { ClientError, Status } from 'nice-grpc-common'
import { lhClient } from '../lhClient'

type LhClient = Awaited<ReturnType<typeof lhClient>>

export type TaskDefStats = {
  connectedWorkers: number | null
  queueDepth: number | null
}

async function fetchConnectedWorkers(client: LhClient, name: string): Promise<number | null> {
  try {
    const group = await client.getTaskWorkerGroup({ name })
    return group ? Object.keys(group.taskWorkers).length : 0
  } catch (error) {
    if (error instanceof ClientError && error.code === Status.NOT_FOUND) return 0
    return null
  }
}

async function fetchQueueDepth(client: LhClient, name: string): Promise<number | null> {
  if (typeof client.countTaskRun !== 'function') {
    return null
  }

  const request = { taskDefName: name, status: TaskStatus.TASK_SCHEDULED }
  try {
    const response = await client.countTaskRun(request)
    return Number(response.value)
  } catch (error) {
    if (error instanceof ClientError && (error.code === Status.UNIMPLEMENTED || error.code === Status.NOT_FOUND)) {
      return null
    }
    throw error
  }
}

async function fetchTaskDefRow(client: LhClient, name: string): Promise<TaskDefData | null> {
  const taskDef = await client.getTaskDef({ name })
  if (!taskDef) return null

  return {
    name,
    createdAt: toDate(taskDef.createdAt),
    description: taskDef.description,
    inputVarCount: taskDef.inputVars.length,
    returnType: taskDef.returnType?.returnType?.definedType,
    connectedWorkers: null,
    queueDepth: null,
  }
}

export async function getTaskDefs(tenantId: string, taskDefNames: string[]): Promise<TaskDefData[]> {
  const client = await lhClient({ tenantId })
  const uniqueOrdered = uniqueInOrder(taskDefNames)

  const taskDefMap = new Map<string, TaskDefData>()
  await Promise.all(
    uniqueOrdered.map(async name => {
      const row = await fetchTaskDefRow(client, name)
      if (row) taskDefMap.set(name, row)
    })
  )

  return uniqueOrdered.map(name => taskDefMap.get(name)).filter((row): row is TaskDefData => row != null)
}

export async function getTaskDefStats(tenantId: string, taskDefNames: string[]): Promise<Record<string, TaskDefStats>> {
  const client = await lhClient({ tenantId })
  const uniqueOrdered = uniqueInOrder(taskDefNames)

  const statsEntries = await Promise.all(
    uniqueOrdered.map(async name => {
      const [connectedWorkers, queueDepth] = await Promise.all([
        fetchConnectedWorkers(client, name),
        fetchQueueDepth(client, name),
      ])
      return [name, { connectedWorkers, queueDepth }] as const
    })
  )

  return Object.fromEntries(statsEntries)
}
