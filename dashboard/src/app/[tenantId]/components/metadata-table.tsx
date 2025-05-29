"use client"
import { Eye } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { Badge } from "@/components/ui/badge"
import { useRouter } from "next/navigation"
import { Ids, SearchResponse } from "@/actions/search"
import { SearchType } from "@/types/search"
import { UserTaskDefId, WfSpecId } from "littlehorse-client/proto"

interface MetadataTableProps {
    data: SearchResponse[] | undefined
    activeTab: SearchType
    isLoading: boolean
}

export function MetadataTable({ data, activeTab, isLoading }: MetadataTableProps) {
    const router = useRouter()

    function handleRowClick(name: string, majorVersion?: number, revision?: number) {
        if (activeTab === "WfSpec")
            router.push(`/diagram/${name}?version=${majorVersion}.${revision}`)
        else if (activeTab === "TaskDef")
            router.push(`/taskdef/${name}`)
        else if (activeTab === "UserTaskDef")
            router.push(`/usertaskdef/${name}`)
        else if (activeTab === "ExternalEventDef")
            router.push(`/externaleventdef/${name}`)
    }

    return (
        <Table>
            <TableHeader>
                <TableRow>
                    <TableHead>Name</TableHead>
                    {(activeTab === "WfSpec" || activeTab === "UserTaskDef") && <TableHead>Version</TableHead>}
                    <TableHead>Actions</TableHead>
                </TableRow>
            </TableHeader>
            <TableBody>
                {isLoading ? (
                    <TableRow>
                        <TableCell colSpan={4} className="text-center py-8 text-muted-foreground">
                            Loading...
                        </TableCell>
                    </TableRow>
                ) : data?.[0]?.results.length === 0 ? (
                    <TableRow>
                        <TableCell colSpan={4} className="text-center py-8 text-muted-foreground">
                            No metadata found
                        </TableCell>
                    </TableRow>
                ) : (
                    data?.map((item) => (
                        item.results.map((result: Ids) => (
                            <TableRow
                                key={result.name}
                                className="cursor-pointer hover:bg-muted/50"
                                onClick={() => isOfType<WfSpecId>(activeTab === "WfSpec", result) ? handleRowClick(result.name, result.majorVersion, result.revision) : handleRowClick(result.name)}
                            >
                                <TableCell className="font-medium">{result.name}</TableCell>
                                {isOfType<WfSpecId>(activeTab === "WfSpec", result) && <TableCell>
                                    <Badge variant="outline" className="font-mono">
                                        {result.majorVersion}.{result.revision}
                                    </Badge>
                                </TableCell>
                                }
                                {isOfType<UserTaskDefId>(activeTab === "UserTaskDef", result) && <TableCell>
                                    <Badge variant="outline" className="font-mono">
                                        {result.version}
                                    </Badge>
                                </TableCell>
                                }
                                <TableCell>
                                    <Button variant="ghost" size="sm" className="flex items-center gap-1">
                                        <Eye className="h-4 w-4" />
                                        View
                                    </Button>
                                </TableCell>
                            </TableRow>
                        ))
                    ))
                )}
            </TableBody>
        </Table>
    )
}

function isOfType<T>(conditional: boolean, obj: unknown): obj is T {
    return conditional
} 