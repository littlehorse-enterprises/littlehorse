"use client"

import { useState } from "react"
import { useParams, useRouter } from "next/navigation"
import { Table, TableHeader, TableRow, TableHead, TableBody, TableCell } from "@/components/ui/table"
import { Badge } from "@/components/ui/badge"
import { ArrowLeft, ChevronLeft, ChevronRight } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Separator } from "@/components/ui/separator"
import {
    WorkflowSpecUsage,
    DateRangeFilter,
    StatusFilter,
    InputFilter,
    MetadataHeader
} from "@/components/metadata"
import { DateRange } from "react-day-picker"
import {
    Carousel,
    CarouselContent,
    CarouselItem,
    CarouselNext,
    CarouselPrevious,
} from "@/components/ui/carousel"
import { TablePagination } from "@/components/ui/table-pagination"

// Mock data for UserTaskDef details
interface Field {
    name: string
    display: string
    type: string
    required: boolean
    description: string
}

const getUserTaskDefDetails = (id: string) => {
    return {
        id,
        name: "customer-support-ticket",
        version: "0.1.3",
        versions: ["0.1.3", "0.1.2", "0.1.1", "0.1.0"],
        fields: [
            {
                name: "ticket_id",
                display: "Ticket ID",
                type: "STRING",
                required: true,
                description: "The unique identifier for the support ticket"
            },
            {
                name: "customer_name",
                display: "Customer Name",
                type: "STRING",
                required: true,
                description: "Full name of the customer"
            },
            {
                name: "issue_description",
                display: "Issue Description",
                type: "TEXT",
                required: true,
                description: "Detailed description of the customer's issue"
            },
            {
                name: "priority",
                display: "Priority",
                type: "ENUM",
                required: true,
                description: "Priority level of the ticket (Low, Medium, High, Critical)"
            },
            {
                name: "resolution_notes",
                display: "Resolution Notes",
                type: "TEXT",
                required: false,
                description: "Notes added by support staff about the resolution"
            }
        ]
    }
}

// Mock data for WfSpecs that use this UserTaskDef
const getWfSpecUsage = (userTaskDefId: string) => {
    return [
        { id: "wf1", name: "customer-support-workflow", version: "v1.0" },
        { id: "wf2", name: "escalation-process", version: "v2.1" }
    ]
}

// Mock data for related user task runs
const getRelatedUserTaskRuns = () => {
    return [
        {
            id: "utr1",
            wfRunId: "wfr-aa112233",
            userTaskGuid: "utg-1122aabb",
            userId: "alice@example.com",
            userGroup: "support-team-a",
            status: "ASSIGNED",
            creationDate: "2023-06-12T10:30:15Z"
        },
        {
            id: "utr2",
            wfRunId: "wfr-bb223344",
            userTaskGuid: "utg-2233bbcc",
            userId: "bob@example.com",
            userGroup: "support-team-b",
            status: "DONE",
            creationDate: "2023-06-11T14:22:33Z"
        },
        {
            id: "utr3",
            wfRunId: "wfr-cc334455",
            userTaskGuid: "utg-3344ccdd",
            userId: null,
            userGroup: "support-management",
            status: "UNASSIGNED",
            creationDate: "2023-06-10T09:15:40Z"
        },
        {
            id: "utr4",
            wfRunId: "wfr-dd445566",
            userTaskGuid: "utg-4455ddee",
            userId: "charlie@example.com",
            userGroup: "support-team-a",
            status: "CANCELLED",
            creationDate: "2023-06-09T16:45:20Z"
        }
    ]
}

// Simple section component
const Section = ({
    title,
    children,
    className = "",
    titleClassName = ""
}: {
    title: string;
    children: React.ReactNode;
    className?: string;
    titleClassName?: string;
}) => (
    <div className={`${className}`}>
        <h2 className={`text-xl font-semibold mb-4 ${titleClassName}`}>{title}</h2>
        <div className="px-1">{children}</div>
    </div>
);

export default function UserTaskDefDetailsPage() {
    const router = useRouter()
    const params = useParams()
    const userTaskDefId = params.id as string

    const [statusFilter, setStatusFilter] = useState<string | null>(null)
    const [userIdFilter, setUserIdFilter] = useState("")
    const [userGroupFilter, setUserGroupFilter] = useState("")
    const [dateRange, setDateRange] = useState<DateRange | undefined>(undefined)
    // Pagination state
    const [currentPage, setCurrentPage] = useState(1)
    const itemsPerPage = 2

    const userTaskDef = getUserTaskDefDetails(userTaskDefId)
    const wfSpecUsage = getWfSpecUsage(userTaskDefId)
    const userTaskRuns = getRelatedUserTaskRuns()

    // Filter user task runs
    const filteredUserTaskRuns = userTaskRuns.filter(run => {
        // Apply status filter if selected
        if (statusFilter && run.status !== statusFilter) {
            return false
        }

        // Apply user id filter if specified
        if (userIdFilter && (!run.userId || !run.userId.toLowerCase().includes(userIdFilter.toLowerCase()))) {
            return false
        }

        // Apply user group filter if specified
        if (userGroupFilter && (!run.userGroup || !run.userGroup.toLowerCase().includes(userGroupFilter.toLowerCase()))) {
            return false
        }

        // Apply date range filter if selected
        if (dateRange) {
            const runDate = new Date(run.creationDate)
            if (dateRange.from && runDate < dateRange.from) {
                return false
            }
            if (dateRange.to && runDate > dateRange.to) {
                return false
            }
        }

        return true
    })

    // Calculate pagination
    const totalPages = Math.ceil(filteredUserTaskRuns.length / itemsPerPage)
    const paginatedUserTaskRuns = filteredUserTaskRuns.slice(
        (currentPage - 1) * itemsPerPage,
        currentPage * itemsPerPage
    )

    // Handle pagination navigation
    const handlePageChange = (page: number) => {
        if (page < 1 || page > totalPages) return
        setCurrentPage(page)
    }

    // Function to get status badge color
    const getStatusBadgeVariant = (status: string) => {
        switch (status) {
            case "ASSIGNED":
                return "info"
            case "UNASSIGNED":
                return "secondary"
            case "DONE":
                return "success"
            case "CANCELLED":
                return "destructive"
            default:
                return "default"
        }
    }

    // Status filter options
    const statusOptions = [
        { value: "ASSIGNED", label: "Assigned" },
        { value: "UNASSIGNED", label: "Unassigned" },
        { value: "DONE", label: "Done" },
        { value: "CANCELLED", label: "Cancelled" }
    ]

    return (
        <div className="container mx-auto py-6">
            {/* Header */}
            <MetadataHeader
                title={userTaskDef.name}
                type="UserTaskDef"
                version={userTaskDef.versions}
                backUrl="/dashboard?tab=user-task-def"
            />

            <Separator className="my-3" />

            {/* Fields Section */}
            <Section title="Fields">
                <div className="relative mx-12">
                    <Carousel
                        opts={{
                            align: "start",
                            loop: false,
                        }}
                        className="w-full"
                    >
                        <CarouselContent className="px-2">
                            {userTaskDef.fields.map((field, index) => (
                                <CarouselItem key={index} className="basis-full sm:basis-1/2 md:basis-1/3 lg:basis-1/4">
                                    <div className="p-1">
                                        <div className="bg-muted/30 rounded-md p-3 h-full">
                                            <div className="flex items-center justify-between mb-1.5">
                                                <span className="text-pink-500 font-mono text-sm">{field.name}</span>
                                                <Badge className="text-xs px-1.5 py-0">{field.type}</Badge>
                                            </div>
                                            <div className="text-gray-500 text-xs mb-1.5">
                                                display: {field.display}
                                            </div>
                                            <div className="flex items-center gap-1 mb-1.5">
                                                {field.required && <Badge variant="secondary" className="text-xs py-0 h-5">Required</Badge>}
                                            </div>
                                            <p className="text-xs text-muted-foreground line-clamp-2 hover:line-clamp-none">
                                                {field.description}
                                            </p>
                                        </div>
                                    </div>
                                </CarouselItem>
                            ))}
                        </CarouselContent>
                        <CarouselPrevious className="absolute -left-10 top-1/2 -translate-y-1/2 h-9 w-9" />
                        <CarouselNext className="absolute -right-10 top-1/2 -translate-y-1/2 h-9 w-9" />
                    </Carousel>
                </div>
            </Section>

            <Separator className="my-3" />

            {/* Flex layout for WfSpec Usage and UserTaskRuns */}

            {/* Related UserTaskRuns Section */}
            <Section title="Related UserTaskRuns">
                <Table>
                    <TableHeader>
                        <TableRow>
                            <TableHead>WfRun Id</TableHead>
                            <TableHead>
                                <InputFilter
                                    label="User Id"
                                    value={userIdFilter}
                                    onChange={setUserIdFilter}
                                    placeholder="Filter..."
                                />
                            </TableHead>
                            <TableHead>
                                <InputFilter
                                    label="User Group"
                                    value={userGroupFilter}
                                    onChange={setUserGroupFilter}
                                    placeholder="Filter..."
                                />
                            </TableHead>
                            <TableHead>
                                <StatusFilter
                                    label="Status"
                                    value={statusFilter}
                                    options={statusOptions}
                                    onChange={setStatusFilter}
                                />
                            </TableHead>
                            <TableHead>
                                <DateRangeFilter
                                    label="Creation Date"
                                    dateRange={dateRange}
                                    onDateRangeChange={setDateRange}
                                />
                            </TableHead>
                        </TableRow>
                    </TableHeader>
                    <TableBody>
                        {paginatedUserTaskRuns.length === 0 ? (
                            <TableRow>
                                <TableCell colSpan={5} className="text-center py-8 text-muted-foreground">
                                    No data
                                </TableCell>
                            </TableRow>
                        ) : (
                            paginatedUserTaskRuns.map((run) => (
                                <TableRow key={run.id}>
                                    <TableCell className="font-mono">
                                        {run.wfRunId}
                                    </TableCell>
                                    <TableCell>
                                        {run.userId || "-"}
                                    </TableCell>
                                    <TableCell>
                                        {run.userGroup || "-"}
                                    </TableCell>
                                    <TableCell>
                                        <Badge variant={getStatusBadgeVariant(run.status) as any}>
                                            {run.status}
                                        </Badge>
                                    </TableCell>
                                    <TableCell>
                                        {new Date(run.creationDate).toLocaleString()}
                                    </TableCell>
                                </TableRow>
                            ))
                        )}
                    </TableBody>
                </Table>

                {/* Pagination Controls */}
                <TablePagination
                    currentPage={currentPage}
                    totalItems={filteredUserTaskRuns.length}
                    itemsPerPage={itemsPerPage}
                    onPageChange={handlePageChange}
                />
            </Section>
        </div>
    )
} 