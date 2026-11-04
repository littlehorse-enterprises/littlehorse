'use client'

import { countScheduledTaskRun } from '@/app/(authenticated)/[tenantId]/taskDef/[name]/actions/countScheduledTaskRun'
import { getTaskWorkerGroup } from '@/app/(authenticated)/[tenantId]/taskDef/[name]/actions/getTaskWorkerGroup'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { useQuery } from '@tanstack/react-query'
import { LayersIcon, RefreshCwIcon, UsersIcon } from 'lucide-react'
import { useParams } from 'next/navigation'
import { FC } from 'react'

type Props = {
  taskDefName: string
  isRefreshing: boolean
}

const formatCount = (value: number) => value.toLocaleString()

export const TaskDefQueueDepthCard: FC<Props> = ({ taskDefName, isRefreshing }) => {
  const tenantId = useParams().tenantId as string

  const { data: queueDepth, isPending: queuePending } = useQuery({
    queryKey: ['taskDefQueueDepth', tenantId, taskDefName],
    queryFn: () => countScheduledTaskRun({ tenantId, taskDefName }),
    refetchInterval: 30_000,
  })

  const showQueueLoading = queuePending || isRefreshing

  return (
    <Card className="flex h-full min-h-0 flex-col">
      <CardHeader className="flex flex-row items-start justify-between space-y-0 pb-2">
        <div className="min-w-0 pr-2">
          <CardTitle className="text-base font-medium">Task queue depth</CardTitle>
          <CardDescription className="text-xs">TaskRuns waiting in TASK_SCHEDULED</CardDescription>
        </div>
        <LayersIcon className="h-5 w-5 shrink-0 text-muted-foreground" />
      </CardHeader>
      <CardContent className="mt-auto flex flex-1 flex-col justify-end">
        {showQueueLoading ? (
          <div className="flex min-h-9 items-center">
            <RefreshCwIcon className="h-6 w-6 animate-spin text-blue-500" />
          </div>
        ) : queueDepth?.status === 'ok' ? (
          <p className="text-3xl font-bold tabular-nums">{formatCount(queueDepth.count)}</p>
        ) : (
          <p className="text-sm text-muted-foreground">{queueDepth?.message ?? 'Unavailable'}</p>
        )}
      </CardContent>
    </Card>
  )
}

export const TaskDefConnectedWorkersCard: FC<Props> = ({ taskDefName, isRefreshing }) => {
  const tenantId = useParams().tenantId as string

  const { data: workerGroup, isPending: workersPending } = useQuery({
    queryKey: ['taskWorkerGroup', tenantId, taskDefName],
    queryFn: () => getTaskWorkerGroup({ tenantId, taskDefName }),
    refetchInterval: 30_000,
  })

  const connectedWorkers = workerGroup ? Object.keys(workerGroup.taskWorkers).length : 0
  const showWorkersLoading = workersPending || isRefreshing

  return (
    <Card className="flex h-full min-h-0 flex-col">
      <CardHeader className="flex flex-row items-start justify-between space-y-0 pb-2">
        <div className="min-w-0 pr-2">
          <CardTitle className="text-base font-medium">Connected workers</CardTitle>
          <CardDescription className="text-xs">Task workers polling this TaskDef</CardDescription>
        </div>
        <UsersIcon className="h-5 w-5 shrink-0 text-muted-foreground" />
      </CardHeader>
      <CardContent className="mt-auto flex flex-1 flex-col justify-end">
        {showWorkersLoading ? (
          <div className="flex min-h-9 items-center">
            <RefreshCwIcon className="h-6 w-6 animate-spin text-blue-500" />
          </div>
        ) : (
          <p className="text-3xl font-bold tabular-nums">{formatCount(connectedWorkers)}</p>
        )}
      </CardContent>
    </Card>
  )
}
