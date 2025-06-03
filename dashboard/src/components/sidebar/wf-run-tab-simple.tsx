"use client"

import { CheckCircle, XCircle, Loader2, Clock } from "lucide-react"
import { TIME_RANGES } from "@/components/flow/mock-data"
import { DataTable } from "@/components/ui/data-table"
import type { ColumnDef } from "@tanstack/react-table"
import { DropdownFilter, FilterOption } from "@/components/ui/dropdown-filter"
import { FilterResetButton } from "@/components/ui/filter-reset-button"
import { useState, useMemo } from "react"
import { WfRun } from "littlehorse-client/proto"
import { useRouter } from "next/navigation"

// Convert TIME_RANGES to FilterOption[] format
const TIME_RANGE_OPTIONS: FilterOption[] = TIME_RANGES.map(({ value, label }) => ({
    value,
    label
}))

// Map time range values to minutes
const TIME_RANGE_MINUTES: Record<string, number | null> = TIME_RANGES.reduce(
    (acc, { value, minutes }) => {
        acc[value] = minutes;
        return acc;
    },
    {} as Record<string, number | null>
);

// Map minutes to time range values
const MINUTES_TO_TIME_RANGE: Record<number | string, string> = Object.entries(TIME_RANGE_MINUTES).reduce(
    (acc, [key, value]) => {
        if (value !== null) {
            acc[value] = key;
        } else {
            acc["null"] = key;
        }
        return acc;
    },
    {} as Record<string, string>
);

interface WfRunTabProps {
    wfRuns?: WfRun[]
}

export default function WfRunTab({ wfRuns = [] }: WfRunTabProps) {
    const router = useRouter()
    const [statusFilter, setStatusFilter] = useState<string[]>([])
    const [timeRangeFilter, setTimeRangeFilter] = useState<number | null>(null)

    // Compute the current time range string value based on minutes
    const currentTimeRange = useMemo(() => {
        return timeRangeFilter === null ? "all" : MINUTES_TO_TIME_RANGE[timeRangeFilter] || "all";
    }, [timeRangeFilter]);

    // Status filter options
    const statusOptions: FilterOption[] = [
        {
            value: "COMPLETED",
            label: "Completed",
            icon: <CheckCircle className="h-3 w-3 text-green-500" />,
        },
        {
            value: "FAILED",
            label: "Failed",
            icon: <XCircle className="h-3 w-3 text-red-500" />,
        },
        {
            value: "RUNNING",
            label: "Running",
            icon: <Loader2 className="h-3 w-3 text-blue-500" />,
        },
        {
            value: "PENDING",
            label: "Pending",
            icon: <Clock className="h-3 w-3 text-[#656565]" />,
        },
    ]

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

    // Define columns for the workflow runs table
    const columns: ColumnDef<WfRun>[] = [
        {
            accessorKey: "id",
            header: "ID",
            cell: ({ row }) => {
                const id = row.getValue("id");
                // Check if the ID is an object with an id property and return its string representation
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
                        options={statusOptions}
                        selectionMode="multiple"
                        onFilterChange={handleStatusFilterChange}
                        initialSelected={statusFilter}
                        currentValue={statusFilter}
                    />
                </div>
            ),
            cell: ({ row }) => {
                const status = row.getValue("status") as string
                const icon = (() => {
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
                })()

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

    return (
        <div className="flex-1 overflow-y-auto p-2">
            <div className="flex items-center justify-between mb-2">
                <div className="text-xs text-[#656565]">
                    Showing {wfRuns?.length} workflow runs
                    {(statusFilter.length > 0 || timeRangeFilter !== null) && (
                        <span className="ml-1">
                            (filtered by
                            {statusFilter.length > 0 && (
                                <span>
                                    {" "}
                                    status:{" "}
                                    {statusFilter
                                        .map((status) => {
                                            const option = statusOptions.find((opt) => opt.value === status)
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

            <DataTable columns={columns} data={wfRuns} idField="id" onRowClick={(e: WfRun) => {
                console.log("e", e)
                console.log(e.wfSpecId?.name, e.wfSpecId?.majorVersion, e.wfSpecId?.revision)
                const x = `/diagram/${e.wfSpecId?.name}?version=${String(e.wfSpecId?.majorVersion)}.${String(e.wfSpecId?.revision)}&wfRunId=${e.id?.id}`
                console.log("x", x)
                router.push(x)
            }} />
        </div>
    )
} 