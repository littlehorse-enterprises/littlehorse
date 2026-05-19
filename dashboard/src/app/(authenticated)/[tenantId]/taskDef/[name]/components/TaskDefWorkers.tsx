'use client'

import { getTaskWorkerGroup } from '@/app/(authenticated)/[tenantId]/taskDef/[name]/actions/getTaskWorkerGroup'
import { utcToLocalDateTime } from '@/app/utils'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { useQuery } from '@tanstack/react-query'
import { LHHostInfo, TaskWorkerMetadata } from 'littlehorse-client/proto'
import { RefreshCwIcon } from 'lucide-react'
import { useParams } from 'next/navigation'
import { FC } from 'react'

type Props = {
  taskDefName: string
  isRefreshing: boolean
  onRefresh: () => void
}

const formatHosts = (hosts: LHHostInfo[]) =>
  hosts.length === 0 ? '—' : hosts.map(h => `${h.host}:${h.port}`).join(', ')

type WorkerRow = {
  clientId: string
  metadata: TaskWorkerMetadata
}

export const TaskDefWorkers: FC<Props> = ({ taskDefName, isRefreshing, onRefresh }) => {
  const tenantId = useParams().tenantId as string

  const { data, isPending } = useQuery({
    queryKey: ['taskWorkerGroup', tenantId, taskDefName],
    queryFn: () => getTaskWorkerGroup({ tenantId, taskDefName }),
    refetchInterval: 30_000,
  })

  const workers: WorkerRow[] = data
    ? Object.entries(data.taskWorkers).map(([clientId, metadata]) => ({ clientId, metadata }))
    : []

  workers.sort((a, b) => {
    const aTime = a.metadata.latestHeartbeat ? Date.parse(a.metadata.latestHeartbeat) : 0
    const bTime = b.metadata.latestHeartbeat ? Date.parse(b.metadata.latestHeartbeat) : 0
    return bTime - aTime
  })

  return (
    <Card>
      <CardHeader className="flex flex-row items-start justify-between space-y-0">
        <div>
          <CardTitle className="text-lg">Task workers</CardTitle>
          <CardDescription>
            Workers connected to this TaskDef. Each row is a client session identified by client ID.
          </CardDescription>
        </div>
        <button
          type="button"
          onClick={onRefresh}
          disabled={isRefreshing}
          className="inline-flex items-center gap-1.5 rounded-md px-2 py-1 text-sm text-muted-foreground transition-colors hover:bg-muted hover:text-foreground disabled:pointer-events-none disabled:opacity-60"
          aria-label="Refresh task workers"
        >
          <RefreshCwIcon className={`h-4 w-4 ${isRefreshing ? 'animate-spin' : ''}`} />
        </button>
      </CardHeader>
      <CardContent>
        {isPending || isRefreshing ? (
          <div className="flex min-h-[120px] items-center justify-center">
            <RefreshCwIcon className="h-6 w-6 animate-spin text-blue-500" />
          </div>
        ) : workers.length === 0 ? (
          <p className="py-6 text-center text-sm text-muted-foreground">
            No task workers are connected. Start a worker that polls this TaskDef to see it here.
          </p>
        ) : (
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead scope="col">Client ID</TableHead>
                <TableHead scope="col">Worker ID</TableHead>
                <TableHead scope="col">Last heartbeat</TableHead>
                <TableHead scope="col">Hosts</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {workers.map(({ clientId, metadata }) => (
                <TableRow key={clientId}>
                  <TableCell className="font-mono text-sm">{clientId}</TableCell>
                  <TableCell className="font-mono text-sm">{metadata.taskWorkerId || '—'}</TableCell>
                  <TableCell>
                    {metadata.latestHeartbeat ? utcToLocalDateTime(metadata.latestHeartbeat) : '—'}
                  </TableCell>
                  <TableCell className="font-mono text-sm">{formatHosts(metadata.hosts)}</TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        )}
      </CardContent>
    </Card>
  )
}
