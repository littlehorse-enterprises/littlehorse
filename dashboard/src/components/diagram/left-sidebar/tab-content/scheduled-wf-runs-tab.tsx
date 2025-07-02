'use client'

import { getScheduledWfRunsFromIds } from '@/actions/getScheduledWfRunsFromIds'
import { DataTable } from '@/components/ui/data-table'
import { useExecuteRPCWithSWR } from '@/hooks/useExecuteRPCWithSWR'
import { formatDateTimeWithMs } from '@/utils/ui/status-utils'
import type { ColumnDef } from '@tanstack/react-table'
import { ScheduledWfRun } from 'littlehorse-client/proto'
import { Clock } from 'lucide-react'
import { useParams } from 'next/navigation'
import { useEffect, useState } from 'react'

export default function ScheduledWfRunsTab() {
  const tenantId = useParams().tenantId as string
  const [scheduledWfRuns, setScheduledWfRuns] = useState<ScheduledWfRun[]>([])

  const { data: scheduledWfRunsData } = useExecuteRPCWithSWR('searchScheduledWfRun', {
    wfSpecName: 'example-basic',
  })

  // Fetch scheduled workflow runs when scheduledWfRunsData changes
  useEffect(() => {
    async function fetchScheduledWfRuns() {
      if (scheduledWfRunsData?.results && scheduledWfRunsData.results.length > 0) {
        const runs = await getScheduledWfRunsFromIds(scheduledWfRunsData.results, tenantId)
        setScheduledWfRuns(runs)
      } else {
        setScheduledWfRuns([])
      }
    }

    fetchScheduledWfRuns()
  }, [scheduledWfRunsData, tenantId])

  // Define columns for the scheduled workflow runs table
  const columns: ColumnDef<ScheduledWfRun>[] = [
    {
      accessorKey: 'id',
      header: 'ID',
      cell: ({ row }) => {
        const id = row.getValue('id')
        return (
          <span className="font-mono text-xs">
            {typeof id === 'object' && id !== null && 'id' in id ? (id as { id: string }).id : String(id)}
          </span>
        )
      },
    },
    {
      accessorKey: 'cronExpression',
      header: 'CRON',
      cell: ({ row }) => <span className="font-mono text-xs">{row.getValue('cronExpression')}</span>,
    },
    {
      accessorKey: 'createdAt',
      header: 'Created',
      cell: ({ row }) => {
        const createdAt = row.getValue('createdAt')
        if (!createdAt) return '-'
        return (
          <div className="flex items-center">
            <Clock className="mr-1 h-3 w-3 text-[#656565]" />
            <span>{formatDateTimeWithMs(createdAt)}</span>
          </div>
        )
      },
    },
  ]

  return (
    <div className="flex-1 overflow-y-auto p-2">
      <div className="mb-2 text-xs text-[#656565]">Showing {scheduledWfRuns.length} scheduled workflow runs</div>

      <DataTable columns={columns} data={scheduledWfRuns} idField="id" />
    </div>
  )
}
