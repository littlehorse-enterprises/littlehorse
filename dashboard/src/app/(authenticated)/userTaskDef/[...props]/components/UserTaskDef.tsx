'use client'

import { UserTaskDef as UserTaskDefProto, UserTaskRunStatus } from 'littlehorse-client/dist/proto/user_tasks'
import React, { FC, Fragment, useState } from 'react'
import { Details } from './Details'
import { Navigation } from '@/app/(authenticated)/components/Navigation'
import { Fields } from './Fields'
import { Button } from '@headlessui/react'
import { SEARCH_DEFAULT_LIMIT } from '@/app/constants'
import Link from 'next/link'
import { useInfiniteQuery } from '@tanstack/react-query'
import { UserTaskRunIdList } from 'littlehorse-client/dist/proto/service'
import { useWhoAmI } from '@/contexts/WhoAmIContext'
import { searchUserTaskRun } from '@/app/(authenticated)/userTaskDef/[...props]/actions/searchUserTaskRun'
import { ArrowPathIcon } from '@heroicons/react/16/solid'
import { concatWfRunIds } from '@/app/utils'

type Props = {
  spec: UserTaskDefProto
}
export const UserTaskDef: FC<Props> = ({ spec }) => {
  const userTaskPossibleStatuses = [
    UserTaskRunStatus.ASSIGNED,
    UserTaskRunStatus.CANCELLED,
    UserTaskRunStatus.DONE,
    UserTaskRunStatus.UNASSIGNED,
  ]
  const [selectedStatus, setSelectedStatus] = useState(UserTaskRunStatus.UNASSIGNED)
  const { tenantId } = useWhoAmI()
  const [limit, setLimit] = useState<number>(SEARCH_DEFAULT_LIMIT)

  const { isPending, data, hasNextPage, fetchNextPage } = useInfiniteQuery({
    queryKey: ['userTaskRun', selectedStatus, tenantId, limit],
    initialPageParam: undefined,
    getNextPageParam: (lastPage: UserTaskRunIdList) => lastPage.bookmark?.toString('base64'),
    queryFn: async ({ pageParam }) => {
      return await searchUserTaskRun({
        tenantId,
        bookmark: pageParam ? Buffer.from(pageParam, 'base64') : undefined,
        limit,
        status: selectedStatus,
        userTaskDefName: spec.name,
      })
    },
  })

  return (
    <>
      <Navigation href="/?type=UserTaskDef" title="Go back to UserTaskDefs" />
      <Details id={spec} />
      <Fields fields={spec.fields} />
      <hr className="mt-6" />
      <div className="mb-4 mt-6 flex items-center justify-between">
        <h2 className="text-2xl font-bold">Related User Task Run&apos;s:</h2>
        <div className="flex">
          {userTaskPossibleStatuses.map(status => (
            <Button
              onClick={() => setSelectedStatus(status)}
              key={status}
              className={`flex items-center border-y-2 border-l-2 p-2 text-xs first-of-type:rounded-l-lg first-of-type:border-l-2 last-of-type:rounded-r-lg last-of-type:border-r-2 ${status === selectedStatus ? 'border-blue-500 bg-blue-500 text-white' : ' text-gray-500'}`}
            >
              {status}
            </Button>
          ))}
        </div>
      </div>

      {isPending ? (
        <div className="flex min-h-[360px] items-center justify-center text-center">
          <ArrowPathIcon className="h-8 w-8 animate-spin fill-blue-500 stroke-none" />
        </div>
      ) : (
        <div className="flex min-h-[360px] flex-col gap-4">
          <table className="text-surface min-w-full text-center text-sm font-light">
            <thead className="border-b border-neutral-200 bg-neutral-300 font-medium">
              <th scope="col" className="px-6 py-4">
                WfRun Id
              </th>
              <th scope="col" className="px-6 py-4">
                User Task GUID
              </th>
            </thead>
            <tbody>
              {data?.pages.map((page, i) => (
                <Fragment key={i}>
                  {page.results.length > 0 ? (
                    page.results.map(userTaskRunId => (
                      <tr key={userTaskRunId.userTaskGuid} className="border-b border-neutral-200">
                        <td className="px-6 py-4">
                          <Link
                            className="py-2 text-blue-500 hover:underline"
                            href={`/wfRun/${concatWfRunIds(userTaskRunId.wfRunId!)}`}
                          >
                            {concatWfRunIds(userTaskRunId.wfRunId!)}
                          </Link>
                        </td>
                        <td className="px-6 py-4">{userTaskRunId.userTaskGuid}</td>
                      </tr>
                    ))
                  ) : (
                    <tr>
                      <td colSpan={2}>No data</td>
                    </tr>
                  )}
                </Fragment>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </>
  )
}
