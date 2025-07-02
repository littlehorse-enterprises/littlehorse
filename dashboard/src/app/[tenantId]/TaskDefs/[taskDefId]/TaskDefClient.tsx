'use client'

import { searchTaskRun } from '@/actions/searchTaskRun'
import LinkWithTenant from '@/components/link-with-tenant'
import { Pagination } from '@/components/ui/load-more-pagination'
import { SEARCH_LIMIT_DEFAULT, SEARCH_LIMITS } from '@/constants'
import { Badge } from '@littlehorse-enterprises/ui-library/badge'
import { Card, CardContent, CardHeader, CardTitle } from '@littlehorse-enterprises/ui-library/card'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@littlehorse-enterprises/ui-library/table'
import { TaskDef } from 'littlehorse-client/proto'
import { Activity, ArrowLeft, Clock, Hash, Loader2, Type } from 'lucide-react'
import { useParams } from 'next/navigation'
import { useState } from 'react'
import useSWRInfinite from 'swr/infinite'

interface TaskDefClientProps {
  taskDef: TaskDef
}

export default function TaskDefClient({ taskDef }: TaskDefClientProps) {
  const { taskDefId, tenantId } = useParams<{ taskDefId: string; tenantId: string }>()
  const [limit, setLimit] = useState(SEARCH_LIMIT_DEFAULT)

  const getKey = (pageIndex: number, previousPageData: Awaited<ReturnType<typeof searchTaskRun>> | null) => {
    if (previousPageData && !previousPageData.bookmark) return null // reached the end
    return ['searchTaskRun', tenantId, limit, taskDefId, previousPageData?.bookmark] as const
  }

  const {
    data: pages,
    size,
    setSize,
    isLoading: isDataLoading,
  } = useSWRInfinite(getKey, async key => {
    const [, tenantId, limit, taskDefName, bookmark] = key
    return searchTaskRun({
      taskDefName,
      tenantId,
      limit,
      bookmark,
    })
  })

  return (
    <div className="container mx-auto py-6">
      {/* Header */}
      <div className="mb-6 flex items-center gap-4">
        <LinkWithTenant href="/" className="text-muted-foreground hover:text-foreground flex items-center gap-2">
          <ArrowLeft className="h-4 w-4" />
          Back to Metadata
        </LinkWithTenant>
      </div>

      <div className="mb-8">
        <h1 className="flex items-center gap-3 text-4xl font-bold">
          <Hash className="h-8 w-8" />
          {taskDef.id?.name}
        </h1>
        <p className="text-muted-foreground mt-2">Task Definition</p>
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
                  {!pages || isDataLoading ? (
                    <TableRow>
                      <TableCell colSpan={2} className="text-muted-foreground py-8 text-center">
                        <Loader2 className="inline animate-spin" />
                      </TableCell>
                    </TableRow>
                  ) : pages.every(page => page.results.length === 0) ? (
                    <TableRow>
                      <TableCell colSpan={2} className="text-muted-foreground py-8 text-center">
                        No TaskRuns found for this TaskDef
                      </TableCell>
                    </TableRow>
                  ) : (
                    pages
                      .flatMap(page => page.results)
                      .map((taskRunId, index) => (
                        <TableRow key={index}>
                          <TableCell>{taskRunId.wfRunId && taskRunId.wfRunId.id}</TableCell>
                          <TableCell>{taskRunId.taskGuid}</TableCell>
                        </TableRow>
                      ))
                  )}
                </TableBody>
              </Table>

              {pages && (
                <Pagination
                  limit={limit}
                  onLimitChange={newLimit => setLimit(newLimit as (typeof SEARCH_LIMITS)[number])}
                  onLoadMore={() => setSize(size + 1)}
                  isLoading={isDataLoading}
                  limitOptions={SEARCH_LIMITS}
                  hasNextBookmark={!!pages[pages.length - 1]?.bookmark}
                />
              )}
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
            <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
              <div>
                <p className="text-muted-foreground text-sm font-medium">Name</p>
                <p className="font-mono text-lg">{taskDef.id?.name}</p>
              </div>
              <div>
                <p className="text-muted-foreground text-sm font-medium">Created At</p>
                <p className="text-lg">{taskDef.createdAt ? new Date(taskDef.createdAt).toLocaleString() : 'N/A'}</p>
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
                      <TableCell className="font-mono font-medium">{variable.name}</TableCell>
                      <TableCell>
                        <Badge variant="outline" className="font-mono">
                          {variable.type || 'UNKNOWN'}
                        </Badge>
                      </TableCell>
                      <TableCell className="font-mono text-sm">
                        {variable.defaultValue ? JSON.stringify(variable.defaultValue).slice(0, 50) + '...' : 'None'}
                      </TableCell>
                      <TableCell>{variable.maskedValue ? 'Yes' : 'No'}</TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            ) : (
              <div className="text-muted-foreground py-8 text-center">No input variables defined for this task</div>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  )
}
