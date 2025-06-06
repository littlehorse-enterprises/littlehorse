"use client"

import { getWfRunsFromIds } from "@/actions/getWfRunsFromIds"
import { DataTable } from "@/components/ui/data-table"
import { DropdownFilter } from "@/components/ui/dropdown-filter"
import { FilterResetButton } from "@/components/ui/filter-reset-button"
import { useExecuteRPCWithSWR } from "@/hooks/useExecuteRPCWithSWR"
import {
  MINUTES_TO_TIME_RANGE,
  STATUS_OPTIONS,
  TIME_RANGE_MINUTES,
  TIME_RANGE_OPTIONS
} from "@/lib/constants"
import type { ColumnDef } from "@tanstack/react-table"
import { WfRun } from "littlehorse-client/proto"
import { CheckCircle, Clock, Loader2, XCircle } from "lucide-react"
import { useParams, useRouter } from "next/navigation"
import { useEffect, useMemo, useState } from "react"

export default function WfRunTab() {
    const router = useRouter()
    const tenantId = useParams().tenantId as string
    const wfSpecName = useParams().wfSpecName as string
    const [statusFilter, setStatusFilter] = useState<string[]>([])
    const [timeRangeFilter, setTimeRangeFilter] = useState<number | null>(null)
    const [wfRuns, setWfRuns] = useState<WfRun[]>([])

    const { data: wfRunsData } = useExecuteRPCWithSWR("searchWfRun", {
        wfSpecName,
        variableFilters: []
    })

    // Fetch workflow runs when wfRunsData changes
    useEffect(() => {
        async function fetchWfRuns() {
            if (wfRunsData?.results && wfRunsData.results.length > 0) {
                const runs = await getWfRunsFromIds(wfRunsData.results, tenantId)
                setWfRuns(runs)
            } else {
                setWfRuns([])
            }
        }

        fetchWfRuns()
    }, [wfRunsData, tenantId])

    // Compute the current time range string value based on minutes
    const currentTimeRange = useMemo(() => {
        return timeRangeFilter === null ? "all" : MINUTES_TO_TIME_RANGE[timeRangeFilter] || "all";
    }, [timeRangeFilter]);

    // Handle status filter changes
    const handleStatusFilterChange = (values: string | string[]) => {
        if (Array.isArray(values)) {
            setStatusFilter(values)
        }
    }

    // Handle time range filter changes
    const handleTimeRangeFilterChange = (value: string | string[]) => {
        if (typeof value === "string") {
            setTimeRangeFilter(TIME_RANGE_MINUTES[value] ?? null)
        }
    }

    // Reset all filters
    const resetFilters = () => {
        setStatusFilter([])
        setTimeRangeFilter(null)
    }

    // Filter data based on status and time range filters
    const filteredData = useMemo(() => {
        let filtered = wfRuns

        // Apply status filter
        if (statusFilter.length) {
            filtered = filtered?.filter((run) => statusFilter.includes(run.status))
        }

        // Apply time range filter
        if (timeRangeFilter !== null) {
            const cutoffTime = new Date(Date.now() - timeRangeFilter * 60 * 1000).getTime()
            filtered = filtered.filter((run) => {
                if (!run.startTime) return false
                const startTime = new Date(run.startTime).getTime()
                return startTime >= cutoffTime
            })
        }

        return filtered
    }, [wfRuns, statusFilter, timeRangeFilter])

    // Get status icon for workflow run
    const getStatusIcon = (status: string) => {
        switch (status) {
            case "COMPLETED":
                return <CheckCircle className="h-3 w-3 text-green-500" />
            case "FAILED":
                return <XCircle className="h-3 w-3 text-red-500" />
            case "RUNNING":
                return <Loader2 className="h-3 w-3 text-blue-500 animate-spin" />
            case "PENDING":
                return <Clock className="h-3 w-3 text-[#656565]" />
            default:
                return null
        }
    }

    // Define columns for the workflow runs table
    const columns: ColumnDef<WfRun>[] = [
        {
            accessorKey: "id",
            header: "ID",
            cell: ({ row }) => {
                const id = row.getValue("id");
                return <span className="font-mono text-xs">
                    {typeof id === 'object' && id !== null && 'id' in id
                        ? (id as { id: string }).id
                        : String(id)}
                </span>;
            },
        },
        {
            accessorKey: "status",
            header: () => (
                <div data-interactive="true" onClick={(e) => e.stopPropagation()}>
                    <DropdownFilter
                        title="Status"
                        labelText="Filter by status"
                        options={STATUS_OPTIONS}
                        selectionMode="multiple"
                        onFilterChange={handleStatusFilterChange}
                        initialSelected={statusFilter}
                        currentValue={statusFilter}
                    />
                </div>
            ),
            cell: ({ row }) => {
                const status = row.getValue("status") as string
                const icon = getStatusIcon(status)

                return (
                    <div className="flex items-center">
                        {icon}
                        <span className="ml-1">{status}</span>
                    </div>
                )
            },
        },
        {
            accessorKey: "startTime",
            header: () => (
                <div data-interactive="true" onClick={(e) => e.stopPropagation()}>
                    <DropdownFilter
                        title="Started"
                        labelText="Filter by time range"
                        options={TIME_RANGE_OPTIONS}
                        selectionMode="single"
                        visualStyle="checkbox"
                        onFilterChange={handleTimeRangeFilterChange}
                        initialSelected="all"
                        currentValue={currentTimeRange}
                        icon={<Clock className="h-3.5 w-3.5 text-muted-foreground/70" />}
                    />
                </div>
            ),
            cell: ({ row }) => {
                const startTime = row.getValue("startTime")
                if (!startTime) return "-"
                const date = new Date(startTime as string)
                return date.toLocaleString(undefined, {
                    month: "numeric",
                    day: "numeric",
                    year: "2-digit",
                    hour: "numeric",
                    minute: "2-digit",
                    hour12: true,
                })
            },
        },
    ]

    const hasActiveFilters = statusFilter.length > 0 || timeRangeFilter !== null
    if (!wfRuns) return null

    return (
        <div className="flex-1 overflow-y-auto p-2">
            <div className="flex items-center justify-between mb-2">
                <div className="text-xs text-[#656565]">
                    Showing {filteredData?.length} of {wfRuns?.length} workflow runs
                    {(statusFilter.length > 0 || timeRangeFilter !== null) && (
                        <span className="ml-1">
                            (filtered by
                            {statusFilter.length > 0 && (
                                <span>
                                    {" "}
                                    status:{" "}
                                    {statusFilter
                                        .map((status) => {
                                            const option = STATUS_OPTIONS.find((opt) => opt.value === status)
                                            return option ? option.label : status
                                        })
                                        .join(", ")}
                                </span>
                            )}
                            {timeRangeFilter !== null && (
                                <span>
                                    {statusFilter.length > 0 ? "," : ""} time: last{" "}
                                    {timeRangeFilter === 1440 ? "24 hours" :
                                        timeRangeFilter === 10080 ? "7 days" :
                                            timeRangeFilter === 43200 ? "30 days" :
                                                timeRangeFilter >= 60 ? `${timeRangeFilter / 60} hour${timeRangeFilter === 60 ? "" : "s"}` :
                                                    `${timeRangeFilter} minutes`}
                                </span>
                            )}
                            )
                        </span>
                    )}
                </div>
                <FilterResetButton onReset={resetFilters} isActive={hasActiveFilters} />
            </div>

            <DataTable columns={columns} data={filteredData} idField="id" onRowClick={(wfRun: WfRun) => {
                router.push(`/${tenantId}/diagram/${wfRun.wfSpecId?.name}/${wfRun.wfSpecId?.majorVersion}.${wfRun.wfSpecId?.revision}?wfRunId=${wfRun.id?.id}`)
            }} />
        </div>
    )
} 