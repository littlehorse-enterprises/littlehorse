'use client'
import { Navigation } from '@/app/(authenticated)/components/Navigation'
import { Field, Input, Label } from '@headlessui/react'
import { TaskStatus } from 'littlehorse-client/dist/proto/common_enums'
import { TaskDef as TaskDefProto } from 'littlehorse-client/dist/proto/task_def'
import { FC, useState } from 'react'
import { Details } from './Details'
import { InputVars } from './InputVars'

type Props = {
  spec: TaskDefProto
}
export const TaskDef: FC<Props> = ({ spec }) => {
  const [selectedStatus, setSelectedStatus] = useState(TaskStatus.TASK_SUCCESS)
  const [createdAfter, setCreatedAfter] = useState('')
  const [createdBefore, setCreatedBefore] = useState('')
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
          {Object.keys(TaskStatus).map(status => (
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
      {/*

      {isPending ? (
        <div className="flex min-h-[360px] items-center justify-center text-center">
          <ArrowPathIcon className="h-8 w-8 animate-spin fill-blue-500 stroke-none" />
        </div>
      ) : (
        <div className="flex min-h-[360px] flex-col gap-4">
          <table className="text-surface min-w-full text-center text-sm font-light">
            <thead className="border-b border-neutral-200 bg-neutral-300 font-medium">
              <tr>
                <td scope="col" className="px-6 py-4">
                  WfRun Id
                </td>
                <th scope="col" className="px-6 py-4">
                  User Task GUID
                </th>
                <th scope="col" className="px-6 py-4">
                  User Id
                </th>
                <th scope="col" className="px-6 py-4">
                  User Group
                </th>
                <th scope="col" className="px-6 py-4">
                  Creation Date
                </th>
              </tr>
            </thead>
            <tbody>
              {data?.pages.map((page, i) => (
                <Fragment key={i}>
                  {page.resultsWithDetails.length > 0 ? (
                    page.resultsWithDetails.map(({ userTaskRun, nodeRun }) => {
                      return (
                        <tr key={userTaskRun.id?.userTaskGuid} className="border-b border-neutral-200">
                          <td className="px-6 py-4">
                            <Link
                              className="py-2 text-blue-500 hover:underline"
                              target="_blank"
                              href={`/wfRun/${concatWfRunIds(userTaskRun.id?.wfRunId!)}?threadRunNumber=${userTaskRun.nodeRunId?.threadRunNumber}&nodeRunName=${nodeRun.nodeName}`}
                            >
                              {concatWfRunIds(userTaskRun.id?.wfRunId!)}
                            </Link>
                          </td>
                          <td className="px-6 py-4">{userTaskRun.id?.userTaskGuid}</td>
                          <td className="px-6 py-4">
                            {userTaskRun.userId ? userTaskRun.userId : NOT_APPLICABLE_LABEL}
                          </td>
                          <td className="px-6 py-4">
                            {userTaskRun.userGroup ? userTaskRun.userGroup : NOT_APPLICABLE_LABEL}
                          </td>
                          <td className="px-6 py-4">
                            {userTaskRun.scheduledTime
                              ? utcToLocalDateTime(userTaskRun.scheduledTime)
                              : NOT_APPLICABLE_LABEL}
                          </td>
                        </tr>
                      )
                    })
                  ) : (
                    <tr>
                      <td colSpan={5}>No data</td>
                    </tr>
                  )}
                </Fragment>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <div className="mt-6">
        <SearchFooter
          currentLimit={limit}
          setLimit={setLimit}
          hasNextPage={hasNextPage}
          fetchNextPage={fetchNextPage}
        />
      </div> */}
    </>
  )
}
