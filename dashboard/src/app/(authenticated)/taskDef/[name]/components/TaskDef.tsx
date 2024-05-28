'use client'
import { Navigation } from '@/app/(authenticated)/components/Navigation'
import { concatWfRunIds, localDateTimeToUTCIsoString, utcToLocalDateTime } from '@/app/utils'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { useWhoAmI } from '@/contexts/WhoAmIContext'
import { Field, Input, Label } from '@headlessui/react'
import { RefreshCwIcon } from 'lucide-react'
import { useInfiniteQuery } from '@tanstack/react-query'
import { TaskStatus } from 'littlehorse-client/dist/proto/common_enums'
import { TaskDef as TaskDefProto } from 'littlehorse-client/dist/proto/task_def'
import Link from 'next/link'
import { FC, Fragment, useState } from 'react'
import { PaginatedTaskRunList, searchTaskRun } from '../actions/searchTaskRun'
import { Details } from './Details'
import { InputVars } from './InputVars'

type Props = {
  spec: TaskDefProto
}
export const TaskDef: FC<Props> = ({ spec }) => {
  const [selectedStatus, setSelectedStatus] = useState<TaskStatus | 'ALL'>('ALL')
  const [createdAfter, setCreatedAfter] = useState('')
  const [createdBefore, setCreatedBefore] = useState('')
  const { tenantId } = useWhoAmI()

  const { isPending, data, hasNextPage, fetchNextPage } = useInfiniteQuery({
    queryKey: ['taskRun', selectedStatus, tenantId, 10, createdAfter, createdBefore],
    initialPageParam: undefined,
    getNextPageParam: (lastPage: PaginatedTaskRunList) => lastPage.bookmarkAsString,
    queryFn: async ({ pageParam }) => {
      return await searchTaskRun({
        tenantId,
        bookmarkAsString: pageParam,
        limit: 10,
        status: selectedStatus == 'ALL' ? undefined : selectedStatus,
        taskDefName: spec.id?.name || '',
        earliestStart: createdAfter ? localDateTimeToUTCIsoString(createdAfter) : undefined,
        latestStart: createdBefore ? localDateTimeToUTCIsoString(createdBefore) : undefined,
      })
    },
  })
  console.log(data)
  return (
    <>
      <Navigation href="/?type=TaskDef" title="Go back to TaskDefs" />
      <Details id={spec.id} />
      <InputVars inputVars={spec.inputVars} />
      <hr className="mt-6" />
      <div className="mb-4 mt-6 flex items-center justify-between">
        <h2 className="text-2xl font-bold">Related Task Run&apos;s:</h2>
        <select
          className="rounded border px-2 py-2"
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
      </div>
      <div className="mb-5 flex max-w-fit items-start justify-between">
        <Field className="flex items-center justify-between">
          <Label className="block w-1/2 font-bold">Created after:</Label>
          <Input
            type="datetime-local"
            value={createdAfter}
            onChange={(e: React.ChangeEvent<HTMLInputElement>) => setCreatedAfter(e.target.value)}
            className="focus:shadow-outline ml-3 w-full appearance-none rounded border px-3 py-2 leading-tight shadow focus:outline-none"
          />
        </Field>

        <Field className="ml-10 flex items-center justify-between">
          <Label className="block w-1/2 font-bold">Created before:</Label>
          <Input
            type="datetime-local"
            value={createdBefore}
            onChange={(e: React.ChangeEvent<HTMLInputElement>) => setCreatedBefore(e.target.value)}
            className="focus:shadow-outline ml-4 w-full appearance-none rounded border px-3 py-2 leading-tight shadow focus:outline-none"
          />
        </Field>
      </div>

      {isPending ? (
        <div className="flex min-h-[360px] items-center justify-center text-center">
          <RefreshCwIcon className="h-8 w-8 animate-spin fill-blue-500 stroke-none" />
        </div>
      ) : (
        <div className="flex min-h-[360px] flex-col gap-4">
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
                            <Link
                              className="py-2 text-blue-500 hover:underline"
                              target="_blank"
                              href={`/wfRun/${concatWfRunIds(taskRun.id?.wfRunId!)}?threadRunNumber=${taskRun.source?.taskNode?.nodeRunId?.threadRunNumber || taskRun.source?.userTaskTrigger?.nodeRunId?.threadRunNumber}&nodeName=${taskRun.source?.taskNode?.nodeRunId?.position}-${taskRun.source?.taskNode?.nodeRunId}-TASK`}
                            >
                              {concatWfRunIds(taskRun.id?.wfRunId!)}
                            </Link>
                          </TableCell>
                          <TableCell>{taskRun.id?.taskGuid}</TableCell>

                          <TableCell>{taskRun.scheduledAt ? utcToLocalDateTime(taskRun.scheduledAt) : 'N/A'}</TableCell>
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
    </>
  )
}
