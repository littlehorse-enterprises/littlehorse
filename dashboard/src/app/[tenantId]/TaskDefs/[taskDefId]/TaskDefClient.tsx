"use client"

import { executeRpc } from "@/actions/executeRPC"
import { useExecuteRPCWithSWR } from "@/hooks/useExecuteRPCWithSWR"
import { SEARCH_LIMIT_DEFAULT, SEARCH_LIMITS } from "@/utils/ui/constants"
import { Badge } from "@littlehorse-enterprises/ui-library/badge"
import { Card, CardContent, CardHeader, CardTitle } from "@littlehorse-enterprises/ui-library/card"
import { Label } from "@littlehorse-enterprises/ui-library/label"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@littlehorse-enterprises/ui-library/select"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@littlehorse-enterprises/ui-library/table"
import { TaskDef } from "littlehorse-client/proto"
import { Activity, ArrowLeft, Clock, Hash, Loader2, Type } from "lucide-react"
import Link from "next/link"
import { useParams } from "next/navigation"
import { useState } from "react"

interface TaskDefClientProps {
  taskDef: TaskDef
}

export default function TaskDefClient({ 
  taskDef, 
}: TaskDefClientProps) {
  const { taskDefId, tenantId } = useParams<{ taskDefId: string; tenantId: string }>()
  
  const [limit, setLimit] = useState(SEARCH_LIMIT_DEFAULT);

  const getKey = (pageIndex: number, previousData: Awaited<ReturnType<typeof executeRpc<"searchTaskRun">>> | null) => {
    if (previousData && !previousData.bookmark) return null // reached the end
    return ['searchTaskRun', tenantId, limit, taskDefId, previousData?.bookmark] as const;
  }

  const { data: taskRuns } = useExecuteRPCWithSWR("searchTaskRun", {
    taskDefName: taskDefId,
    limit,
  });

  return (
    <div className="container mx-auto py-6">
      {/* Header */}
      <div className="mb-6 flex items-center gap-4">
        <Link href={`/${tenantId}`} className="flex items-center gap-2 text-muted-foreground hover:text-foreground">
          <ArrowLeft className="h-4 w-4" />
          Back to Metadata
        </Link>
      </div>

      <div className="mb-8">
        <h1 className="text-4xl font-bold flex items-center gap-3">
          <Hash className="h-8 w-8" />
          {taskDef.id?.name}
        </h1>
        <p className="text-muted-foreground mt-2">
          Task Definition
        </p>
      </div>

      <div className="grid gap-6">
        {/* Related Task Runs */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Activity className="h-5 w-5" />
              Related Task Runs
            </CardTitle>
          </CardHeader>
          <CardContent>
              <>
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>WfRun Id</TableHead>
                      <TableHead>GUID</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {!taskRuns ?(
                      <TableRow>
                        <TableCell colSpan={2} className="text-center py-8 text-muted-foreground">
                          <Loader2 className="inline animate-spin" />
                        </TableCell>
                      </TableRow>
                    ): taskRuns.results.length === 0 ?(<TableRow>
                        <TableCell colSpan={2} className="text-center py-8 text-muted-foreground">
                          No TaskRuns found for this TaskDef
                        </TableCell>
                      </TableRow>) :(
                      taskRuns.results.map((taskRunId, index) => (
                        <TableRow key={index}>
                          <TableCell>
                            {taskRunId.wfRunId && taskRunId.wfRunId.id}
                          </TableCell>
                          <TableCell>
                            {taskRunId.taskGuid}
                          </TableCell>
                        </TableRow>
                      ))
                    )}
                  </TableBody>
                </Table>
                {/* Limit dropdown */}
                <div className="mt-4 flex items-center justify-end">
                  <Label className="mr-2 text-sm">Limit:</Label>
                  <Select
                    value={limit.toString()}
                    onValueChange={value => setLimit(Number(value) as typeof SEARCH_LIMITS[number])}
                  >
                    <SelectTrigger className="w-fit">
                      <SelectValue placeholder="Items per load" />
                    </SelectTrigger>
                    <SelectContent>
                      {SEARCH_LIMITS.map(limit => (
                        <SelectItem key={limit} value={limit.toString()}>
                          {limit}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>
              </>
          </CardContent>
        </Card>

        {/* Basic Information */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Clock className="h-5 w-5" />
              Basic Information
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <p className="text-sm font-medium text-muted-foreground">Name</p>
                <p className="text-lg font-mono">{taskDef.id?.name}</p>
              </div>
              <div>
                <p className="text-sm font-medium text-muted-foreground">Created At</p>
                <p className="text-lg">
                  {taskDef.createdAt 
                    ? new Date(taskDef.createdAt).toLocaleString()
                    : "N/A"
                  }
                </p>
              </div>
            </div>
          </CardContent>
        </Card>

        {/* Input Variables */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Type className="h-5 w-5" />
              Input Variables
            </CardTitle>
          </CardHeader>
          <CardContent>
            {taskDef.inputVars && taskDef.inputVars.length > 0 ? (
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Name</TableHead>
                    <TableHead>Type</TableHead>
                    <TableHead>Default</TableHead>
                    <TableHead>Masked</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {taskDef.inputVars.map((variable, index) => (
                    <TableRow key={index}>
                      <TableCell className="font-mono font-medium">
                        {variable.name}
                      </TableCell>
                      <TableCell>
                        <Badge variant="outline" className="font-mono">
                          {variable.type || "UNKNOWN"}
                        </Badge>
                      </TableCell>
                      <TableCell className="font-mono text-sm">
                        {variable.defaultValue 
                          ? JSON.stringify(variable.defaultValue).slice(0, 50) + "..."
                          : "None"
                        }
                      </TableCell>
                      <TableCell>
                        {
                          variable.maskedValue ? "Yes" : "No"
                        }
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            ) : (
              <div className="text-center py-8 text-muted-foreground">
                No input variables defined for this task
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  )
} 
