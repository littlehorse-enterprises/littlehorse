'use client'
import LinkWithTenant from '@/app/(authenticated)/[tenantId]/components/LinkWithTenant'
import { Navigation } from '@/app/(authenticated)/[tenantId]/components/Navigation'
import { SearchFooter } from '@/app/(authenticated)/[tenantId]/components/SearchFooter'
import { SEARCH_DEFAULT_LIMIT } from '@/app/constants'
import { concatWfRunIds, localDateTimeToUTCIsoString, utcToLocalDateTime } from '@/app/utils'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { useInfiniteQuery, useQuery } from '@tanstack/react-query'
import { TaskDef as TaskDefProto, TaskStatus, WfSpecIdList } from 'littlehorse-client/proto'
import { RefreshCwIcon } from 'lucide-react'
import { useParams } from 'next/navigation'
import { FC, Fragment, useState } from 'react'
import { PaginatedTaskRunList, searchTaskRun } from '../actions/searchTaskRun'
import { Details } from './Details'
import { InputVars } from './InputVars'
import { searchWfSpecs, PaginatedWfSpecList } from '@/app/actions/getWfSpecsByTaskDef'
import { WfSpecData } from '@/types'
import { TagIcon } from 'lucide-react'
import { Separator } from '@/components/ui/separator'
import { SelectionLink } from '@/app/(authenticated)/[tenantId]/components/SelectionLink'

type Props = {
  spec: TaskDefProto
}

export const TaskDef: FC<Props> = ({ spec }) => {
  const [selectedStatus, setSelectedStatus] = useState<TaskStatus | 'ALL'>('ALL')
  const [createdAfter, setCreatedAfter] = useState('')
  const [createdBefore, setCreatedBefore] = useState('')
  const tenantId = useParams().tenantId as string
  const [limit, setLimit] = useState<number>(SEARCH_DEFAULT_LIMIT)
  const [wfSpecLimit, setWfSpecLimit] = useState<number>(SEARCH_DEFAULT_LIMIT)
  const taskDefName = spec.id?.name || ''

  const {
    isPending: wfSpecsIsPending,
    data: wfSpecsData,
    hasNextPage: wfSpecsHasNextPage,
    fetchNextPage: wfSpecsFetchNextPage,
  } = useInfiniteQuery({
    queryKey: ['wfSpecs', tenantId, limit, taskDefName],
    initialPageParam: undefined,
    getNextPageParam: (lastPage: PaginatedWfSpecList) => lastPage.bookmarkAsString,
    queryFn: async ({ pageParam }) => {
      return await searchWfSpecs({ tenantId, bookmarkAsString: pageParam, limit: wfSpecLimit, taskDefName })
    },
  })

  const { isPending, data, hasNextPage, fetchNextPage } = useInfiniteQuery({
    queryKey: ['taskRun', selectedStatus, tenantId, limit, createdAfter, createdBefore],
    initialPageParam: undefined,
    getNextPageParam: (lastPage: PaginatedTaskRunList) => lastPage.bookmarkAsString,
    queryFn: async ({ pageParam }) => {
      return await searchTaskRun({
        tenantId,
        bookmarkAsString: pageParam,
        limit,
        status: selectedStatus == 'ALL' ? undefined : selectedStatus,
        taskDefName: spec.id?.name || '',
        earliestStart: createdAfter ? localDateTimeToUTCIsoString(createdAfter) : undefined,
        latestStart: createdBefore ? localDateTimeToUTCIsoString(createdBefore) : undefined,
      })
    },
  })

  return (
    <>
      <Navigation href="/?type=TaskDef" title="Go back to TaskDefs" />
      <Details id={spec.id} />
      <InputVars inputVars={spec.inputVars} />

      <Separator className="my-4" />

      <div className="flex gap-1">
        <div className="flex-1">
          <h2 className="text-2xl font-bold">WfSpec Usage:</h2>
          {wfSpecsData && (
            <div className="flex max-h-[200px] flex-col overflow-auto">
              {wfSpecsData.pages
                .flatMap(page => page.results)
                .map(wfSpec => (
                  <Fragment key={wfSpec.name}>
                    <SelectionLink href={`/wfSpec/${wfSpec.name}/${wfSpec.majorVersion}.${wfSpec.revision}`}>
                      <p className="group">{wfSpec.name}</p>
                      <div className="flex items-center gap-2 rounded bg-blue-200 px-2 font-mono text-sm text-gray-500">
                        <TagIcon className="h-4 w-4 fill-none stroke-gray-500 stroke-1" />v{wfSpec.majorVersion}.
                        {wfSpec.revision}
                      </div>
                    </SelectionLink>
                    <Separator />
                  </Fragment>
                ))}
              <div className="mt-6">
                <SearchFooter
                  currentLimit={wfSpecLimit}
                  setLimit={setWfSpecLimit}
                  hasNextPage={wfSpecsHasNextPage}
                  fetchNextPage={wfSpecsFetchNextPage}
                />
              </div>
            </div>
          )}
        </div>

        <Separator orientation="vertical" className="h-92 mx-4" />

        <div className="flex-[2.5]">
          <h2 className="text-2xl font-bold">Related Task Run&apos;s:</h2>
          <div className="mb-5 flex w-full flex-col items-start justify-between">
            <div className="flex w-full justify-around gap-5">
              <select
                className="max-w-72 rounded border px-2 py-2"
                onChange={(e: React.ChangeEvent<HTMLSelectElement>) => {
                  setSelectedStatus(e.target.value as TaskStatus)
                }}
              >
                <option>ALL</option>
                {Object.keys(TaskStatus)
                  .filter(status => status != TaskStatus.UNRECOGNIZED)
                  .map(status => (
                    <option key={status}>{status}</option>
                  ))}
              </select>
              <div className="flex items-center justify-between gap-1">
                <Label className="text-right font-bold">Created after:</Label>
                <Input
                  type="datetime-local"
                  value={createdAfter}
                  onChange={(e: React.ChangeEvent<HTMLInputElement>) => setCreatedAfter(e.target.value)}
                  className="w-full"
                />
              </div>

              <div className="flex items-center justify-between gap-1">
                <Label className="text-right font-bold">Created before:</Label>
                <Input
                  type="datetime-local"
                  value={createdBefore}
                  onChange={(e: React.ChangeEvent<HTMLInputElement>) => setCreatedBefore(e.target.value)}
                  className="w-full"
                />
              </div>
            </div>
            {isPending ? (
              <div className="flex min-h-[360px] items-center justify-center text-center">
                <RefreshCwIcon className="h-8 w-8 animate-spin text-blue-500" />
              </div>
            ) : (
              <div className="flex min-h-[360px] w-full flex-col gap-4">
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead scope="col">WfRun Id</TableHead>
                      <TableHead scope="col">Task GUID</TableHead>
                      <TableHead scope="col">Creation Date</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {data?.pages.map((page, i) => (
                      <Fragment key={i}>
                        {page.resultsWithDetails.length > 0 ? (
                          page.resultsWithDetails.map(({ taskRun }) => {
                            return (
                              <TableRow key={taskRun.id?.taskGuid}>
                                <TableCell>
                                  <LinkWithTenant
                                    className="py-2 text-blue-500 hover:underline"
                                    target="_blank"
                                    href={`/wfRun/${concatWfRunIds(taskRun.id?.wfRunId!)}?threadRunNumber=${taskRun.source?.taskNode?.nodeRunId?.threadRunNumber ?? taskRun.source?.userTaskTrigger?.nodeRunId?.threadRunNumber}&nodeRunName=${taskRun.source?.taskNode?.nodeRunId?.position}-${spec.id?.name}-TASK`}
                                  >
                                    {concatWfRunIds(taskRun.id?.wfRunId!)}
                                  </LinkWithTenant>
                                </TableCell>
                                <TableCell>{taskRun.id?.taskGuid}</TableCell>

                                <TableCell>
                                  {taskRun.scheduledAt ? utcToLocalDateTime(taskRun.scheduledAt) : 'N/A'}
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
          </div>
          <div className="mt-6">
            <SearchFooter
              currentLimit={limit}
              setLimit={setLimit}
              hasNextPage={hasNextPage}
              fetchNextPage={fetchNextPage}
            />
          </div>

          <hr className="mt-6" />
        </div>
      </div>
    </>
  )
}
