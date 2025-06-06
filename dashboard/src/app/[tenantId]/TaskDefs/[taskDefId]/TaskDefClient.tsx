"use client"

import { useExecuteRPCWithSWR } from "@/hooks/useExecuteRPCWithSWR"
import { Badge } from "@littlehorse-enterprises/ui-library/badge"
import { Button } from "@littlehorse-enterprises/ui-library/button"
import { Card, CardContent, CardHeader, CardTitle } from "@littlehorse-enterprises/ui-library/card"
import { Input } from "@littlehorse-enterprises/ui-library/input"
import { Label } from "@littlehorse-enterprises/ui-library/label"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@littlehorse-enterprises/ui-library/select"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@littlehorse-enterprises/ui-library/table"
import { TaskDef, TaskStatus, WfSpecId } from "littlehorse-client/proto"
import { Activity, ArrowLeft, Clock, Filter, Hash, Type, Workflow, X } from "lucide-react"
import Link from "next/link"
import { useState } from "react"

interface TaskDefClientProps {
  taskDef: TaskDef
  wfSpecIds: WfSpecId[]
  tenantId: string
  taskDefId: string
}

export default function TaskDefClient({ 
  taskDef, 
  wfSpecIds,
  tenantId, 
  taskDefId 
}: TaskDefClientProps) {

  // Filter state
  const [filters, setFilters] = useState({
    status: undefined as TaskStatus | undefined,
    earliestStart: undefined as string | undefined,
    latestStart: undefined as string | undefined,
  })

  const [showFilters, setShowFilters] = useState(false)

  const { data: taskRuns } = useExecuteRPCWithSWR("searchTaskRun", {
    taskDefName: taskDefId,
    limit: 10,
    ...filters,
  });

  const hasActiveFilters = filters.status || filters.earliestStart || filters.latestStart

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
          Task Definition - Blueprint for executable tasks
        </p>
      </div>

      <div className="grid gap-6">
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

        {/* WfSpec Usage */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Workflow className="h-5 w-5" />
              WfSpec Usage
            </CardTitle>
          </CardHeader>
          <CardContent>
            {wfSpecIds.length > 0 ? (
              <div className="space-y-3">
                {wfSpecIds.map((wfSpecId, index) => (
                  <div key={index} className="flex items-center justify-between p-3 border rounded-lg">
                    <div className="flex items-center gap-3">
                      <Workflow className="h-4 w-4 text-muted-foreground" />
                      <div>
                        <Link
                          href={`/${tenantId}/diagram/${wfSpecId.name}/${wfSpecId.majorVersion}.${wfSpecId.revision}`}
                          className="font-mono font-medium hover:text-primary"
                        >
                          {wfSpecId.name}
                        </Link>
                        <Badge variant="secondary" className="ml-2 text-xs">
                          v{wfSpecId.majorVersion}.{wfSpecId.revision}
                        </Badge>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            ) : (
              <div className="text-center py-8 text-muted-foreground">
                No WfSpec are currently using this TaskDef
              </div>
            )}
          </CardContent>
        </Card>

        {/* Related Task Runs */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Activity className="h-5 w-5" />
              Related Task Runs
            </CardTitle>
            <div className="flex items-center gap-2">
              <Button
                variant="outline"
                size="sm"
                onClick={() => setShowFilters(!showFilters)}
                className="flex items-center gap-2"
              >
                <Filter className="h-4 w-4" />
                Filters
                {hasActiveFilters && (
                  <Badge variant="secondary" className="ml-1">
                    {Object.values(filters).filter(v => v !== undefined && v !== "").length}
                  </Badge>
                )}
              </Button>
              {hasActiveFilters && (
                <Button variant="ghost" size="sm" onClick={() => {
                   setFilters({
                    status: undefined,
                    earliestStart: undefined,
                    latestStart: undefined,
                  })
                }}>
                  <X className="h-4 w-4" />
                  Clear
                </Button>
              )}
            </div>
          </CardHeader>
          <CardContent>
            {/* Filter Controls */}
            {showFilters && (
              <div className="mb-6 p-4 border rounded-lg bg-muted/10">
                <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                  <div className="space-y-2">
                    <Label htmlFor="status-filter">Status</Label>
                    <Select
                      value={filters.status?.toString() || "ALL"}
                      onValueChange={(value) => 
                        setFilters(prev => ({ 
                          ...prev, 
                          status: value as TaskStatus
                        }))
                      }
                    >
                      <SelectTrigger>
                        <SelectValue placeholder="All statuses" />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="ALL">All statuses</SelectItem>
                        {Object.values(TaskStatus).filter(status => status !== TaskStatus.UNRECOGNIZED).map((status) => (
                          <SelectItem key={status} value={status}>
                            {status}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </div>
                  
                  <div className="space-y-2">
                    <Label htmlFor="earliest-start">Earliest Start</Label>
                    <Input
                      id="earliest-start"
                      type="datetime-local"
                      value={filters.earliestStart ?? ""}
                      onChange={(e) => 
                        setFilters(prev => ({ ...prev, earliestStart: e.target.value }))
                      }
                    />
                  </div>
                  
                  <div className="space-y-2">
                    <Label htmlFor="latest-start">Latest Start</Label>
                    <Input
                      id="latest-start"
                      type="datetime-local"
                      value={filters.latestStart ?? ""}
                      onChange={(e) => 
                        setFilters(prev => ({ ...prev, latestStart: e.target.value }))
                      }
                    />
                  </div>
                </div>
              </div>
            )}

            {!taskRuns ? (
              <div className="text-center py-8 text-muted-foreground">
                Loading task runs...
              </div>
            ) : taskRuns.results && taskRuns.results.length > 0 ? (
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>WfRun Id</TableHead>
                    <TableHead>Task GUID</TableHead>
                    <TableHead>Status</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {taskRuns.results.map((taskRunId, index) => (
                    <TableRow key={index}>
                      <TableCell>
                        {taskRunId.wfRunId!.id}
                      </TableCell>
                      <TableCell>
                        {taskRunId.taskGuid}
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            ) : (
              <div className="text-center py-8 text-muted-foreground">
                {hasActiveFilters 
                  ? "No task runs match the current filters"
                  : "No task runs found for this TaskDef"
                }
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  )
} 
