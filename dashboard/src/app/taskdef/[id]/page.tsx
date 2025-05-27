"use client"

import { useState } from "react"
import { useParams } from "next/navigation"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Table, TableHeader, TableRow, TableHead, TableBody, TableCell } from "@/components/ui/table"
import { Badge } from "@/components/ui/badge"
import Link from "next/link"
import { Button } from "@/components/ui/button"
import {
    MetadataDetailPage,
    TabItem,
    OverviewCard,
    DateRangeFilter,
    StatusFilter,
    InputFilter
} from "@/components/metadata"
import { DateRange } from "react-day-picker"
import { TablePagination } from "@/components/ui/table-pagination"
import { Separator } from "@/components/ui/separator"
import { MetadataHeader } from "@/components/metadata/MetadataHeader"
import { lhClient } from "@/lib/lhClient"
import { TaskDef, TaskStatus, VariableDef, WfSpecId, WfSpecIdList } from "littlehorse-client/proto"
import useSWR from "swr"
import useSWRInfinite from "swr/infinite"
import { SEARCH_DEFAULT_LIMIT } from "@/lib/constants"
import { search, SearchResponse, WfSpecList } from "@/actions/searchAction"
import { PaginatedWfSpecList, searchWfSpecs } from "@/actions/searchWfSpec"
import { PaginatedTaskRunList, runDetails, searchTaskRun } from "@/actions/searchTaskRun"
import { getTaskDef } from "@/actions/getTaskDef"

function Section({
    title,
    children,
    className = "",
    titleClassName = ""
}: {
    title: string;
    children: React.ReactNode;
    className?: string;
    titleClassName?: string;
}) {
    return (
        <div className={`${className}`}>
            <h2 className={`text-xl font-semibold mb-4 ${titleClassName}`}>{title}</h2>
            <div className="px-1">{children}</div>
        </div>
    );
}

// Function to get TaskDef details
// Using React Query for data fetching



export default function TaskDefDetailsPage() {
    const params = useParams()
    const taskDefName = params.id as string
    const tenantId = "default"

    const [statusFilter, setStatusFilter] = useState<TaskStatus | undefined>()
    const [dateRange, setDateRange] = useState<DateRange | undefined>(undefined)
    const [searchQuery, setSearchQuery] = useState("")
    const [wfSpecsLimit, setWfSpecsLimit] = useState(SEARCH_DEFAULT_LIMIT)

    // TaskDef
    const { data: taskDef, isLoading, error } = useSWR<TaskDef>(
        ['taskDef', taskDefName],
        async () => {
            return await getTaskDef(tenantId, { name: taskDefName })
        },
    );


    // WfSpec Usage
    const getWfSpecListKey = (pageIndex: number, previousPageData: PaginatedWfSpecList | null) => {
        if (previousPageData && !previousPageData.bookmark) return null // reached the end
        return ["default", wfSpecsLimit, taskDefName, previousPageData?.bookmark]
    }
    const { data: wfSpecsData } = useSWRInfinite<PaginatedWfSpecList>(getWfSpecListKey, async key => {
        const [, limit, , bookmark] = key
        return searchWfSpecs({ tenantId, limit, taskDefName, bookmarkAsString: bookmark })
    })
    const wfSpecs = wfSpecsData?.flatMap(page => page.results.map(result => result))

    // TaskRuns
    const getTaskRunListKey = (pageIndex: number, previousPageData: PaginatedTaskRunList | null): {
        tenantId: string,
        status: TaskStatus | undefined,
        limit: number,
        createdAfter: string | undefined,
        createdBefore: string | undefined,
        bookmark: string | undefined
    } | null => {
        if (previousPageData && !previousPageData.bookmark) return null // reached the end
        return {
            tenantId,
            status: statusFilter,
            limit: wfSpecsLimit,
            createdAfter: dateRange?.from?.toISOString(),
            createdBefore: dateRange?.to?.toISOString(),
            bookmark: previousPageData?.bookmarkAsString
        }
    }
    const { data: taskRunData } = useSWRInfinite<PaginatedTaskRunList>(getTaskRunListKey, async (key: ReturnType<typeof getTaskRunListKey>) => {
        const { tenantId, status, limit, bookmark } = key ?? {}
        return searchTaskRun({
            taskDefName: taskDefName,
            bookmarkAsString: bookmark,
            limit,
            status,
            tenantId,
            earliestStart: dateRange?.from?.toISOString(),
            latestStart: dateRange?.to?.toISOString(),
        })
    })


    // Filter task runs
    // const filteredTaskRuns = taskRuns.filter(function (run: any) {
    //     // Apply status filter if selected
    //     if (statusFilter && run.status !== statusFilter) {
    //         return false
    //     }

    //     // Apply date range filter if selected
    //     if (dateRange) {
    //         const runDate = new Date(run.createdAt)
    //         if (dateRange.from && runDate < dateRange.from) {
    //             return false
    //         }
    //         if (dateRange.to && runDate > dateRange.to) {
    //             return false
    //         }
    //     }

    //     // Apply search query to WfRunId or TaskGuid
    //     if (searchQuery && !run.wfRunId.toLowerCase().includes(searchQuery.toLowerCase()) &&
    //         !run.taskGuid.toLowerCase().includes(searchQuery.toLowerCase())) {
    //         return false
    //     }

    //     return true
    // })

    // Calculate pagination
    // const paginatedTaskRuns = filteredTaskRuns.slice(
    //     (currentPage - 1) * itemsPerPage,
    //     currentPage * itemsPerPage
    // )

    // Handle pagination navigation
    // function handlePageChange(page: number) {
    //     setCurrentPage(page)
    // }

    // Function to get status badge color
    function getStatusBadgeVariant(status: string) {
        switch (status) {
            case "COMPLETED":
                return "default"
            case "RUNNING":
                return "secondary"
            case "ERROR":
                return "destructive"
            default:
                return "outline"
        }
    }

    // Status filter options
    return (
        <div className="container mx-auto py-6">
            {/* Header */}
            <MetadataHeader
                title={taskDef?.id?.name ?? ""}
                type="TaskDef"
                backUrl="/dashboard?tab=task-def"
            />

            <Separator className="my-3" />

            {/* Overview Section */}
            <div className="flex flex-col md:flex-row">
                <div className="md:flex-1">
                    <Section title="Input Variables" className="bg-card rounded-lg p-4">
                        <div className="space-y-2 grid grid-rows-3 grid-flow-col">
                            {taskDef?.inputVars.map((variable: VariableDef, index: number) => (
                                <div key={index} className="font-mono flex items-center">
                                    <Badge>
                                        {variable.type}
                                    </Badge>
                                    <span className="text-sm">{variable.name}</span>
                                </div>
                            ))}
                        </div>
                    </Section>
                </div>

                <div className="hidden md:flex items-center mx-4">
                    <Separator orientation="vertical" className="h-40" />
                </div>

                <div className="md:flex-1">
                    <Section title="Output Type" className="bg-card rounded-lg p-4">
                        <div className="font-mono flex items-center">
                            <Badge variant="default" className="mr-2">
                                object
                            </Badge>
                            <span className="text-sm">{taskDef?.returnType?.returnType?.type}</span>
                        </div>
                    </Section>
                </div>
            </div >

            <Separator className="my-3" />

            {/* WfSpec Usage and Task Runs in flex row */}
            <div className="flex flex-col md:flex-row gap-6">
                {/* WfSpec Usage Section */}
                <div className="md:w-1/5">
                    <Section title="WfSpec Usage" className="bg-card rounded-lg p-2" titleClassName="text-base mb-2">
                        <div className="space-y-2">
                            {wfSpecs?.length === 0 ? (
                                <p className="text-muted-foreground text-sm">No workflow specifications found.</p>
                            ) : (
                                <ul className="space-y-1">
                                    {wfSpecs?.map(function (wfSpec: WfSpecId) {
                                        return (
                                            <Link key={wfSpec.name} href={`/diagram?id=${wfSpec.name}`} className="block">
                                                <li className="flex flex-col items-center justify-between p-2 border rounded-lg cursor-pointer hover:bg-muted transition-colors">
                                                    <p className="font-medium text-sm">{wfSpec.name}</p>
                                                    <div className="flex items-center justify-start w-full ml-5">
                                                        <div className="flex items-center gap-1 mt-0.5">
                                                            <Badge variant="outline" className="font-mono text-xs px-2 py-0.5">
                                                                {wfSpec.majorVersion}.{wfSpec.revision}
                                                            </Badge>
                                                            <p className="font-mono text-xs px-2 py-0.5">View Diagram</p>
                                                        </div>
                                                    </div>
                                                </li>
                                            </Link>
                                        )
                                    })}
                                </ul>
                            )}
                        </div>
                    </Section>
                </div>

                <div className="hidden md:flex h-auto self-stretch">
                    <Separator orientation="vertical" className="h-full mx-4" />
                </div>

                {/* Task Runs Section */}
                <div className="md:w-4/5">
                    <Section title="Related Task Runs">
                        <Table>
                            <TableHeader>
                                <TableRow>
                                    <TableHead>
                                        <InputFilter
                                            label="WfRun ID"
                                            value={searchQuery}
                                            onChange={setSearchQuery}
                                            placeholder="Search..."
                                        />
                                    </TableHead>
                                    <TableHead>Task GUID</TableHead>
                                    <TableHead>
                                        <StatusFilter
                                            label="Status"
                                            value={statusFilter}
                                            options={Object.values(TaskStatus).map(status => ({
                                                value: status,
                                                label: status
                                            }))}
                                            onChange={(value: string | undefined) => {
                                                setStatusFilter(value as TaskStatus)
                                            }}
                                        />
                                    </TableHead>
                                    <TableHead>
                                        <DateRangeFilter
                                            label="Created At"
                                            dateRange={dateRange}
                                            onDateRangeChange={setDateRange}
                                        />
                                    </TableHead>
                                </TableRow>
                            </TableHeader>
                            <TableBody>
                                {taskRunData?.flat().every(page => page.resultsWithDetails.length === 0) ? (
                                    <TableRow>
                                        <TableCell colSpan={4} className="text-center py-8 text-muted-foreground">
                                            No task runs found matching your filters
                                        </TableCell>
                                    </TableRow>
                                ) : (
                                    taskRunData?.flatMap(page => page.resultsWithDetails).map(function (run: runDetails) {
                                        return (
                                            <TableRow key={run.taskRun.id?.taskGuid}>
                                                <TableCell className="font-mono">
                                                    {run.taskRun.id?.wfRunId?.id}
                                                </TableCell>
                                                <TableCell className="font-mono">
                                                    {run.taskRun.id?.taskGuid}
                                                </TableCell>
                                                <TableCell>
                                                    <Badge variant={getStatusBadgeVariant(run.taskRun.status) as any}>
                                                        {run.taskRun.status}
                                                    </Badge>
                                                </TableCell>
                                                <TableCell>
                                                    {run.taskRun.scheduledAt ? new Date(run.taskRun.scheduledAt).toLocaleString() : "N/A"}
                                                </TableCell>


                                            </TableRow>
                                        )
                                    })
                                )}
                            </TableBody>
                        </Table>
                        {/* <TablePagination
                            currentPage={currentPage}
                            totalItems={taskRunData?.length}
                            itemsPerPage={itemsPerPage}
                            onPageChange={handlePageChange}
                        /> */}
                    </Section>
                </div>
            </div>
        </div >
    )
} 