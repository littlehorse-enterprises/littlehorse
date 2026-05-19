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
  onRefresh: () => void
}

const formatCount = (value: number) => value.toLocaleString()

export const TaskDefOverview: FC<Props> = ({ taskDefName, isRefreshing, onRefresh }) => {
  const tenantId = useParams().tenantId as string

  const { data: queueDepth, isPending: queuePending } = useQuery({
    queryKey: ['taskDefQueueDepth', tenantId, taskDefName],
    queryFn: () => countScheduledTaskRun({ tenantId, taskDefName }),
    refetchInterval: 30_000,
  })

  const { data: workerGroup, isPending: workersPending } = useQuery({
    queryKey: ['taskWorkerGroup', tenantId, taskDefName],
    queryFn: () => getTaskWorkerGroup({ tenantId, taskDefName }),
    refetchInterval: 30_000,
  })

  const connectedWorkers = workerGroup ? Object.keys(workerGroup.taskWorkers).length : 0
  const showQueueLoading = queuePending || isRefreshing
  const showWorkersLoading = workersPending || isRefreshing

  return (
    <section className="space-y-3">
      <div className="flex items-center justify-between">
        <h2 className="text-lg font-semibold">Overview</h2>
        <button
          type="button"
          onClick={onRefresh}
          disabled={isRefreshing}
          className="inline-flex items-center gap-1.5 rounded-md px-2 py-1 text-sm text-muted-foreground transition-colors hover:bg-muted hover:text-foreground disabled:pointer-events-none disabled:opacity-60"
          aria-label="Refresh overview"
        >
          <RefreshCwIcon className={`h-4 w-4 ${isRefreshing ? 'animate-spin' : ''}`} />
          Refresh
        </button>
      </div>
      <div className="grid gap-4 sm:grid-cols-2">
        <Card>
          <CardHeader className="flex flex-row items-start justify-between space-y-0 pb-2">
            <div>
              <CardTitle className="text-base font-medium">Task queue depth</CardTitle>
              <CardDescription>TaskRuns waiting in TASK_SCHEDULED</CardDescription>
            </div>
            <LayersIcon className="h-5 w-5 text-muted-foreground" />
          </CardHeader>
          <CardContent>
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

        <Card>
          <CardHeader className="flex flex-row items-start justify-between space-y-0 pb-2">
            <div>
              <CardTitle className="text-base font-medium">Connected workers</CardTitle>
              <CardDescription>Task workers polling this TaskDef</CardDescription>
            </div>
            <UsersIcon className="h-5 w-5 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            {showWorkersLoading ? (
              <div className="flex min-h-9 items-center">
                <RefreshCwIcon className="h-6 w-6 animate-spin text-blue-500" />
              </div>
            ) : (
              <p className="text-3xl font-bold tabular-nums">{formatCount(connectedWorkers)}</p>
            )}
          </CardContent>
        </Card>
      </div>
    </section>
  )
}
