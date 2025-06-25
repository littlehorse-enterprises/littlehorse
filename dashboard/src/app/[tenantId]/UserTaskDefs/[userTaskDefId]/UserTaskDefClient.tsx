'use client'

import { searchUserTaskRun } from '@/actions/searchUserTaskRun'
import { Pagination } from '@/components/ui/load-more-pagination'
import { SEARCH_LIMIT_DEFAULT, SEARCH_LIMITS } from '@/utils/ui/constants'
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
import { UserTaskDef } from 'littlehorse-client/proto'
import { ArrowLeft, Clock, Hash, Loader2 } from 'lucide-react'
import Link from 'next/link'
import { useParams } from 'next/navigation'
import { useState } from 'react'
import useSWRInfinite from 'swr/infinite'

interface UserTaskDefClientProps {
  userTaskDef: UserTaskDef
}

export default function UserTaskDefClient({ userTaskDef }: UserTaskDefClientProps) {
  const { tenantId } = useParams<{ tenantId: string }>()
  const [limit, setLimit] = useState(SEARCH_LIMIT_DEFAULT)

  const getKey = (pageIndex: number, previousPageData: Awaited<ReturnType<typeof searchUserTaskRun>> | null) => {
    if (previousPageData && !previousPageData.bookmark) return null // reached the end
    return ['searchUserTaskRun', tenantId, limit, userTaskDef.name, previousPageData?.bookmark] as const
  }

  const {
    data: pages,
    size,
    setSize,
    isLoading: isDataLoading,
  } = useSWRInfinite(getKey, async key => {
    const [, tenantId, limit, userTaskDefName, bookmark] = key
    return searchUserTaskRun({
      userTaskDefName,
      tenantId,
      limit,
      bookmark,
    })
  })

  return (
    <div className="container mx-auto py-6">
      {/* Header */}
      <div className="mb-6 flex items-center gap-4">
        <Link href={`/${tenantId}`} className="text-muted-foreground hover:text-foreground flex items-center gap-2">
          <ArrowLeft className="h-4 w-4" />
          Go back to UserTaskDefs
        </Link>
      </div>

      <div className="mb-8">
        <h1 className="flex items-center gap-3 text-4xl font-bold">
          <Hash className="h-8 w-8" />
          {userTaskDef.name}
        </h1>
        <div className="mt-2 flex items-center gap-2">
          <Badge variant="outline" className="font-mono">
            v{userTaskDef.version}
          </Badge>
        </div>
        <p className="text-muted-foreground mt-2">UserTaskDef</p>
      </div>

      <div className="grid gap-6">
        {/* Fields */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">Fields</CardTitle>
          </CardHeader>
          <CardContent>
            {userTaskDef.fields && userTaskDef.fields.length > 0 ? (
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Name</TableHead>
                    <TableHead>Display</TableHead>
                    <TableHead>Type</TableHead>
                    <TableHead>Required</TableHead>
                    <TableHead>Description</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {userTaskDef.fields.map((field, idx) => (
                    <TableRow key={idx}>
                      <TableCell className="font-mono text-purple-600">{field.name}</TableCell>
                      <TableCell className="text-muted-foreground font-mono">{field.displayName || '-'}</TableCell>
                      <TableCell>
                        <Badge variant="outline" className="font-mono">
                          {field.type}
                        </Badge>
                      </TableCell>
                      <TableCell>
                        {field.required ? (
                          <Badge variant="destructive">Required</Badge>
                        ) : (
                          <Badge variant="outline">Optional</Badge>
                        )}
                      </TableCell>
                      <TableCell className="text-muted-foreground text-xs">{field.description || ''}</TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            ) : (
              <div className="text-muted-foreground py-8 text-center">No fields defined for this user task</div>
            )}
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
                <p className="font-mono text-lg">{userTaskDef.name}</p>
              </div>
              <div>
                <p className="text-muted-foreground text-sm font-medium">Version</p>
                <p className="font-mono text-lg">{userTaskDef.version}</p>
              </div>
              <div>
                <p className="text-muted-foreground text-sm font-medium">Created At</p>
                <p className="text-lg">
                  {userTaskDef.createdAt ? new Date(userTaskDef.createdAt).toLocaleString() : 'N/A'}
                </p>
              </div>
            </div>
          </CardContent>
        </Card>

        {/* Related User Task Runs */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">Related User Task Runs</CardTitle>
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
                        No UserTaskRuns found for this UserTaskDef
                      </TableCell>
                    </TableRow>
                  ) : (
                    pages
                      .flatMap(page => page.results)
                      .map((userTaskRun, index) => (
                        <TableRow key={index}>
                          <TableCell>{userTaskRun.wfRunId?.id}</TableCell>
                          <TableCell>{userTaskRun.userTaskGuid}</TableCell>
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
