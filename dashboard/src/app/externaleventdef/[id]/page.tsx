"use client"

import { useState } from "react"
import { useParams } from "next/navigation"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Table, TableHeader, TableRow, TableHead, TableBody, TableCell } from "@/components/ui/table"
import { Check } from "lucide-react"
import { isAfter, isBefore } from "date-fns"
import { DateRange } from "react-day-picker"
import {
    MetadataDetailPage,
    TabItem,
    OverviewCard,
    WorkflowSpecUsage,
    DateRangeFilter,
    CheckboxFilter
} from "@/components/metadata"
import { TablePagination } from "@/components/ui/table-pagination"
import { Separator } from "@/components/ui/separator"
import { MetadataHeader } from "@/components/metadata/MetadataHeader"

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

// Mock data for externalEventDef details
const getExternalEventDefDetails = (id: string) => {
    return {
        id,
        name: "PaymentProcessed",
        versions: ["1.0.0", "1.1.0"],
    }
}

// Mock data for WfSpecs that use this ExternalEventDef
const getWfSpecUsage = (externalEventDefId: string) => {
    return [
        { id: "wf1", name: "event-handler-workflow", version: "v1.0" },
        { id: "wf2", name: "notification-system", version: "v0.2" }
    ]
}

// Mock data for related External Events
const getRelatedExternalEvents = () => {
    return [
        {
            id: "evt1",
            wfRunId: "wfr-123456abcdef",
            guid: "evt-abcdef123456",
            triggeredDate: "2023-09-15T10:30:00",
            isClaimed: true,
        },
        {
            id: "evt2",
            wfRunId: "wfr-789012ghijkl",
            guid: "evt-ghijkl789012",
            triggeredDate: "2023-09-14T14:45:00",
            isClaimed: false,
        },
        {
            id: "evt3",
            wfRunId: "wfr-345678mnopqr",
            guid: "evt-mnopqr345678",
            triggeredDate: "2023-09-13T09:15:00",
            isClaimed: true,
        }
    ]
}

export default function ExternalEventDefDetailsPage() {
    const params = useParams()
    const externalEventDefId = params.id as string

    const [dateRange, setDateRange] = useState<DateRange | undefined>(undefined)
    const [isClaimedFilter, setIsClaimedFilter] = useState<boolean | null>(null)
    // Pagination state
    const [currentPage, setCurrentPage] = useState(1)
    const itemsPerPage = 2

    const externalEventDef = getExternalEventDefDetails(externalEventDefId)
    const wfSpecUsage = getWfSpecUsage(externalEventDefId)
    const externalEvents = getRelatedExternalEvents()

    // Filter external events
    const filteredExternalEvents = externalEvents.filter(event => {
        // Apply date range filter if selected
        if (dateRange) {
            const eventDate = new Date(event.triggeredDate)
            if (dateRange.from && isBefore(eventDate, dateRange.from)) {
                return false
            }
            if (dateRange.to && isAfter(eventDate, dateRange.to)) {
                return false
            }
        }

        // Apply claimed filter if selected
        if (isClaimedFilter !== null && event.isClaimed !== isClaimedFilter) {
            return false
        }

        return true
    })

    // Calculate pagination
    const paginatedExternalEvents = filteredExternalEvents.slice(
        (currentPage - 1) * itemsPerPage,
        currentPage * itemsPerPage
    )

    // Handle pagination navigation
    const handlePageChange = (page: number) => {
        setCurrentPage(page)
    }

    return (
        <div className="container mx-auto py-6">
            {/* Header */}
            <MetadataHeader
                title={externalEventDef.name}
                type="ExternalEventDef"
                version={externalEventDef.versions}
                backUrl="/dashboard?tab=external-event-def"
            />

            <Separator className="my-3" />

            {/* Related External Events Section */}
            <Section title="Related External Events">
                <Table>
                    <TableHeader>
                        <TableRow>
                            <TableHead>
                                <div className="flex flex-col gap-2">
                                    <span>WfRun Id</span>
                                </div>
                            </TableHead>
                            <TableHead>
                                <div className="flex flex-col gap-2">
                                    <span>GUID</span>
                                </div>
                            </TableHead>
                            <TableHead>
                                <DateRangeFilter
                                    label="Triggered Date"
                                    dateRange={dateRange}
                                    onDateRangeChange={setDateRange}
                                />
                            </TableHead>
                            <TableHead>
                                <CheckboxFilter
                                    label="Is Claimed"
                                    value={isClaimedFilter}
                                    labelTrue="Claimed"
                                    labelFalse="Not Claimed"
                                    onChange={setIsClaimedFilter}
                                />
                            </TableHead>
                        </TableRow>
                    </TableHeader>
                    <TableBody>
                        {paginatedExternalEvents.length === 0 ? (
                            <TableRow>
                                <TableCell colSpan={4} className="text-center py-8 text-muted-foreground">
                                    No data
                                </TableCell>
                            </TableRow>
                        ) : (
                            paginatedExternalEvents.map((event) => (
                                <TableRow key={event.id}>
                                    <TableCell className="font-mono">
                                        {event.wfRunId}
                                    </TableCell>
                                    <TableCell className="font-mono">
                                        {event.guid}
                                    </TableCell>
                                    <TableCell>
                                        {new Date(event.triggeredDate).toLocaleString()}
                                    </TableCell>
                                    <TableCell className="text-center">
                                        {event.isClaimed && <Check className="h-4 w-4 mx-auto text-green-600" />}
                                    </TableCell>
                                </TableRow>
                            ))
                        )}
                    </TableBody>
                </Table>
                <TablePagination
                    currentPage={currentPage}
                    totalItems={filteredExternalEvents.length}
                    itemsPerPage={itemsPerPage}
                    onPageChange={handlePageChange}
                />
            </Section>
        </div>
    )
} 