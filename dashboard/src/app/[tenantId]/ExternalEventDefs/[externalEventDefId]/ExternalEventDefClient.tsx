'use client'

import { searchExternalEvent } from '@/actions/searchExternalEvent'
import LinkWithTenant from '@/components/link-with-tenant'
import { Pagination } from '@/components/ui/load-more-pagination'
import { SEARCH_LIMIT_DEFAULT, SEARCH_LIMITS } from '@/constants'
import { Card, CardContent, CardHeader, CardTitle } from '@littlehorse-enterprises/ui-library/card'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@littlehorse-enterprises/ui-library/table'
import { ExternalEventDef } from 'littlehorse-client/proto'
import { Activity, ArrowLeft, Clock, Hash, Loader2 } from 'lucide-react'
import { useParams } from 'next/navigation'
import { useState } from 'react'
import useSWRInfinite from 'swr/infinite'

interface ExternalEventDefClientProps {
  externalEventDef: ExternalEventDef
}

export default function ExternalEventDefClient({ externalEventDef }: ExternalEventDefClientProps) {
  const { tenantId, externalEventDefId } = useParams<{ tenantId: string; externalEventDefId: string }>()
  const [limit, setLimit] = useState(SEARCH_LIMIT_DEFAULT)

  const getKey = (pageIndex: number, previousPageData: Awaited<ReturnType<typeof searchExternalEvent>> | null) => {
    if (previousPageData && !previousPageData.bookmark) return null // reached the end
    return ['searchExternalEvent', tenantId, limit, externalEventDefId, previousPageData?.bookmark] as const
  }

  const {
    data: pages,
    size,
    setSize,
    isLoading: isDataLoading,
  } = useSWRInfinite(getKey, async key => {
    const [, tenantId, limit, externalEventDefId, bookmark] = key
    return searchExternalEvent({
      externalEventDefId: { name: externalEventDefId },
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
          {externalEventDef?.id?.name || externalEventDefId}
        </h1>
        <p className="text-muted-foreground mt-2">External Event Definition</p>
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
                <p className="font-mono text-lg">{externalEventDef.id?.name || externalEventDefId}</p>
              </div>
              <div>
                <p className="text-muted-foreground text-sm font-medium">Created At</p>
                <p className="text-lg">
                  {externalEventDef.createdAt ? new Date(externalEventDef.createdAt).toLocaleString() : 'N/A'}
                </p>
              </div>
              <div>
                <p className="text-muted-foreground text-sm font-medium">Retention Policy</p>
                <p className="font-mono text-lg">
                  {externalEventDef.retentionPolicy ? JSON.stringify(externalEventDef.retentionPolicy, null, 2) : 'N/A'}
                </p>
              </div>
            </div>
          </CardContent>
        </Card>

        {/* Related External Events */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Activity className="h-5 w-5" />
              Related External Events
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
                        No ExternalEvents found for this ExternalEventDef
                      </TableCell>
                    </TableRow>
                  ) : (
                    pages
                      .flatMap(page => page.results)
                      .map((externalEventId, index) => (
                        <TableRow key={index}>
                          <TableCell>{externalEventId.wfRunId && externalEventId.wfRunId.id}</TableCell>
                          <TableCell>{externalEventId.guid}</TableCell>
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
