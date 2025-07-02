'use client'

import { searchWorkflowEvent } from '@/actions/searchWorkflowEvent'
import LinkWithTenant from '@/components/link-with-tenant'
import { Pagination } from '@/components/ui/load-more-pagination'
import { SEARCH_LIMIT_DEFAULT, SEARCH_LIMITS } from '@/constants'
import { formatDateTimeWithMs } from '@/utils/ui/status-utils'
import { Card, CardContent, CardHeader, CardTitle } from '@littlehorse-enterprises/ui-library/card'
import {
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableHeader,
    TableRow,
} from '@littlehorse-enterprises/ui-library/table'
import { WorkflowEventDef } from 'littlehorse-client/proto'
import { Activity, ArrowLeft, Clock, Hash, Loader2 } from 'lucide-react'
import { useParams } from 'next/navigation'
import { useState } from 'react'
import useSWRInfinite from 'swr/infinite'

interface WorkflowEventDefClientProps {
  workflowEventDef: WorkflowEventDef
}

export default function WorkflowEventDefClient({ workflowEventDef }: WorkflowEventDefClientProps) {
  const { tenantId, workflowEventDefId } = useParams<{ tenantId: string; workflowEventDefId: string }>()
  const [limit, setLimit] = useState(SEARCH_LIMIT_DEFAULT)

  const getKey = (pageIndex: number, previousPageData: Awaited<ReturnType<typeof searchWorkflowEvent>> | null) => {
    if (previousPageData && !previousPageData.bookmark) return null // reached the end
    return ['searchWorkflowEvent', tenantId, limit, workflowEventDefId, previousPageData?.bookmark] as const
  }

  const {
    data: pages,
    size,
    setSize,
    isLoading: isDataLoading,
  } = useSWRInfinite(getKey, async key => {
    const [, tenantId, limit, workflowEventDefId, bookmark] = key
    return searchWorkflowEvent({
      workflowEventDefId: { name: workflowEventDefId },
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
          {workflowEventDef?.id?.name || workflowEventDefId}
        </h1>
        <p className="text-muted-foreground mt-2">Workflow Event Definition</p>
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
            <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
              <div>
                <p className="text-muted-foreground text-sm font-medium">Name</p>
                <p className="font-mono text-lg">{workflowEventDef.id?.name || workflowEventDefId}</p>
              </div>
              <div>
                <p className="text-muted-foreground text-sm font-medium">Created At</p>
                <p className="text-lg">
                  {workflowEventDef.createdAt ? formatDateTimeWithMs(workflowEventDef.createdAt) : 'N/A'}
                </p>
              </div>
            </div>
          </CardContent>
        </Card>

        {/* Related Workflow Events */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Activity className="h-5 w-5" />
              Related Workflow Events
            </CardTitle>
          </CardHeader>
          <CardContent>
            <>
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>WfRun Id</TableHead>
                    <TableHead>Number</TableHead>
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
                        No WorkflowEvents found for this WorkflowEventDef
                      </TableCell>
                    </TableRow>
                  ) : (
                    pages
                      .flatMap(page => page.results)
                      .map((workflowEventId, index) => (
                        <TableRow key={index}>
                          <TableCell>{workflowEventId.wfRunId && workflowEventId.wfRunId.id}</TableCell>
                          <TableCell>{workflowEventId.number}</TableCell>
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
      </div>
    </div>
  )
}
