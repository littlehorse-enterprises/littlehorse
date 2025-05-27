"use client"

import { Clock } from "lucide-react"
import { mockScheduledWorkflowRuns } from "@/components/flow/mock-data"
import { DataTable } from "@/components/ui/data-table"
import type { ColumnDef } from "@tanstack/react-table"

export default function ScheduledWfRunsTab() {
  // Define columns for the scheduled workflow runs table
  const columns: ColumnDef<(typeof mockScheduledWorkflowRuns)[0]>[] = [
    {
      accessorKey: "id",
      header: "ID",
      cell: ({ row }) => <span className="font-mono text-xs">{row.getValue("id")}</span>,
    },
    {
      accessorKey: "cronExpression",
      header: "CRON",
      cell: ({ row }) => <span className="font-mono text-xs">{row.getValue("cronExpression")}</span>,
    },
    {
      accessorKey: "nextRunTime",
      header: "Next Run",
      cell: ({ row }) => {
        const nextRunDate = new Date(row.getValue("nextRunTime"))
        return (
          <div className="flex items-center">
            <Clock className="h-3 w-3 text-[#656565] mr-1" />
            <span>
              {nextRunDate.toLocaleString(undefined, {
                month: "short",
                day: "numeric",
                hour: "numeric",
                minute: "2-digit",
                hour12: true,
              })}
            </span>
          </div>
        )
      },
    },
  ]

  return (
    <div className="flex-1 overflow-y-auto p-2">
      <div className="text-xs text-[#656565] mb-2">
        Showing {mockScheduledWorkflowRuns.length} scheduled workflow runs
      </div>

      <DataTable columns={columns} data={mockScheduledWorkflowRuns} idField="id" />
    </div>
  )
}
