'use client'
import LinkWithTenant from '@/app/(authenticated)/[tenantId]/components/LinkWithTenant'
import { Navigation } from '@/app/(authenticated)/[tenantId]/components/Navigation'
import { SearchFooter } from '@/app/(authenticated)/[tenantId]/components/SearchFooter'
import { SEARCH_DEFAULT_LIMIT } from '@/app/constants'
import { localDateTimeToUTCIsoString, utcToLocalDateTime, wfRunIdToPath } from '@/app/utils'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { useInfiniteQuery } from '@tanstack/react-query'
import { WorkflowEventDef as WorkflowEventDefProto } from 'littlehorse-client/proto'
import { RefreshCwIcon } from 'lucide-react'
import { useParams } from 'next/navigation'
import { FC, Fragment, useState } from 'react'
import { PaginatedWorkflowEventList, searchWorkflowEvent } from '../actions/searchWorkflowEvent'
import { Details } from './Details'

type Props = {
  spec: WorkflowEventDefProto
}

export const WorkflowEventDef: FC<Props> = ({ spec }) => {
  const [createdAfter, setCreatedAfter] = useState('')
  const [createdBefore, setCreatedBefore] = useState('')
  const [limit, setLimit] = useState<number>(SEARCH_DEFAULT_LIMIT)
  const tenantId = useParams().tenantId as string

  const { isPending, data, hasNextPage, fetchNextPage } = useInfiniteQuery({
    queryKey: ['workflowEvent', tenantId, createdAfter, limit, createdBefore],
    initialPageParam: undefined,
    getNextPageParam: (lastPage: PaginatedWorkflowEventList) => lastPage.bookmarkAsString,
    queryFn: async ({ pageParam }) => {
      return await searchWorkflowEvent({
        tenantId,
        bookmarkAsString: pageParam,
        limit,
        workflowEventDefId: { name: spec.id?.name ?? '' },
        earliestStart: createdAfter ? localDateTimeToUTCIsoString(createdAfter) : undefined,
        latestStart: createdBefore ? localDateTimeToUTCIsoString(createdBefore) : undefined,
      })
    },
  })

  return (
    <>
      <Navigation href="/?type=WorkflowEventDef" title="Go back to WorkflowEventDef" />
      <Details spec={spec} />
      <hr className="mt-6" />
      <div className="mb-4 mt-6 flex items-center justify-between">
        <h2 className="text-2xl font-bold">Related Workflow Event&apos;s:</h2>
      </div>
      <div className="mb-5 flex max-w-fit items-start justify-between">
        <div className="flex items-center justify-between">
          <Label>Created after:</Label>
          <Input
            type="datetime-local"
            value={createdAfter}
            onChange={(e: React.ChangeEvent<HTMLInputElement>) => setCreatedAfter(e.target.value)}
            className="focus:shadow-outline ml-3 w-full appearance-none rounded border px-3 py-2 leading-tight shadow focus:outline-none"
          />
        </div>

        <div className="ml-10 flex items-center justify-between">
          <Label className="block w-1/2 font-bold">Created before:</Label>
          <Input
            type="datetime-local"
            value={createdBefore}
            onChange={(e: React.ChangeEvent<HTMLInputElement>) => setCreatedBefore(e.target.value)}
            className="focus:shadow-outline ml-4 w-full appearance-none rounded border px-3 py-2 leading-tight shadow focus:outline-none"
          />
        </div>
      </div>

      {isPending ? (
        <div className="flex min-h-[360px] items-center justify-center text-center">
          <RefreshCwIcon className="h-8 w-8 animate-spin text-blue-500" />
        </div>
      ) : (
        <div className="flex min-h-[360px] flex-col gap-4">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead scope="col">WfRun Id</TableHead>
                <TableHead scope="col">Sequence Number</TableHead>
                <TableHead scope="col">Triggered Date</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {data?.pages.map((page, i) => (
                <Fragment key={i}>
                  {page.resultsWithDetails.length > 0 ? (
                    page.resultsWithDetails.map(({ workflowEvent }) => {
                      if (!workflowEvent.id?.wfRunId) return
                      return (
                        <TableRow key={workflowEvent.id.number}>
                          <TableCell>
                            <LinkWithTenant
                              className="py-2 text-blue-500 hover:underline"
                              target="_blank"
                              href={`/wfRun/${wfRunIdToPath(workflowEvent.id.wfRunId)}?threadRunNumber=${workflowEvent.nodeRunId?.threadRunNumber}&nodeRunName=${workflowEvent.nodeRunId?.position}-throw-${spec.id?.name}-THROW_EVENT`}
                            >
                              {wfRunIdToPath(workflowEvent.id.wfRunId)}
                            </LinkWithTenant>
                          </TableCell>
                          <TableCell>{workflowEvent.id?.number}</TableCell>

                          <TableCell>
                            {workflowEvent.createdAt ? utcToLocalDateTime(workflowEvent.createdAt) : 'N/A'}
                          </TableCell>
                        </TableRow>
                      )
                    })
                  ) : (
                    <TableRow>
                      <TableCell colSpan={3} className="text-center">
                        No data
                      </TableCell>
                    </TableRow>
                  )}
                </Fragment>
              ))}
            </TableBody>
          </Table>
        </div>
      )}
      <div className="mt-6">
        <SearchFooter
          currentLimit={limit}
          setLimit={setLimit}
          hasNextPage={hasNextPage}
          fetchNextPage={fetchNextPage}
        />
      </div>
    </>
  )
}
