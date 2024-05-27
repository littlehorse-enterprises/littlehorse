'use client'

import { UserTaskDef as UserTaskDefProto, UserTaskRunStatus } from 'littlehorse-client/dist/proto/user_tasks'
import React, { FC, Fragment, useState } from 'react'
import { Details } from './Details'
import { Navigation } from '@/app/(authenticated)/components/Navigation'
import { Fields } from './Fields'
import { Button, Field, Input, Label } from '@headlessui/react'
import { SEARCH_DEFAULT_LIMIT } from '@/app/constants'
import Link from 'next/link'
import { useInfiniteQuery } from '@tanstack/react-query'
import { useWhoAmI } from '@/contexts/WhoAmIContext'
import {
  PaginatedUserTaskRunList,
  searchUserTaskRun,
} from '@/app/(authenticated)/userTaskDef/[...props]/actions/searchUserTaskRun'
import { ArrowPathIcon } from '@heroicons/react/16/solid'
import { concatWfRunIds, localDateTimeToUTCIsoString, utcToLocalDateTime } from '@/app/utils'
import { useDebounce } from 'use-debounce'
import { SearchFooter } from '@/app/(authenticated)/components/SearchFooter'

type Props = {
  spec: UserTaskDefProto
}
export const UserTaskDef: FC<Props> = ({ spec }) => {
  const DEBOUNCE_DELAY = 1000
  const userTaskPossibleStatuses = [
    UserTaskRunStatus.ASSIGNED,
    UserTaskRunStatus.CANCELLED,
    UserTaskRunStatus.DONE,
    UserTaskRunStatus.UNASSIGNED,
  ]
  const [selectedStatus, setSelectedStatus] = useState(UserTaskRunStatus.UNASSIGNED)
  const [userId, setUserId] = useState('')
  const [userGroup, setUserGroup] = useState('')
  const [createdAfter, setCreatedAfter] = useState('')
  const [createdBefore, setCreatedBefore] = useState('')
  const { tenantId } = useWhoAmI()
  const [limit, setLimit] = useState<number>(SEARCH_DEFAULT_LIMIT)
  const [userIdToSearchFor] = useDebounce(userId, DEBOUNCE_DELAY)
  const [userGroupToSearchFor] = useDebounce(userGroup, DEBOUNCE_DELAY)

  const { isPending, data, hasNextPage, fetchNextPage } = useInfiniteQuery({
    queryKey: [
      'userTaskRun',
      selectedStatus,
      userIdToSearchFor,
      userGroupToSearchFor,
      tenantId,
      limit,
      createdAfter,
      createdBefore,
    ],
    initialPageParam: undefined,
    getNextPageParam: (lastPage: PaginatedUserTaskRunList) => lastPage.bookmarkAsString,
    queryFn: async ({ pageParam }) => {
      return await searchUserTaskRun({
        tenantId,
        bookmarkAsString: pageParam,
        limit,
        status: selectedStatus,
        userTaskDefName: spec.name,
        earliestStart: createdAfter ? localDateTimeToUTCIsoString(createdAfter) : undefined,
        latestStart: createdBefore ? localDateTimeToUTCIsoString(createdBefore) : undefined,
        userId: userIdToSearchFor.trim() != '' ? userIdToSearchFor : undefined,
        userGroup: userGroupToSearchFor.trim() != '' ? userGroupToSearchFor : undefined,
      })
    },
  })

  const NOT_APPLICABLE_LABEL = 'N/A'

  return (
    <>
      <Navigation href="/?type=UserTaskDef" title="Go back to UserTaskDefs" />
      <Details id={spec} />
      <Fields fields={spec.fields} />
      <hr className="mt-6" />
      <div className="mb-4 mt-6 flex items-center justify-between">
        <h2 className="text-2xl font-bold">Related User Task Runs:</h2>
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
      <div className="mb-5 flex max-w-fit items-start justify-between">
        <Field className="flex items-center justify-between">
          <Label className="block w-1/2 font-bold">User Id:</Label>
          <Input
            type="text"
            value={userId}
            onChange={(e: React.ChangeEvent<HTMLInputElement>) => setUserId(e.target.value)}
            className="focus:shadow-outline ml-6 w-full appearance-none rounded border px-3 py-2 leading-tight shadow focus:outline-none"
          />
        </Field>

        <Field className="ml-16 flex items-center justify-between">
          <Label className="block w-1/2 font-bold">User Group:</Label>
          <Input
            type="text"
            value={userGroup}
            onChange={(e: React.ChangeEvent<HTMLInputElement>) => setUserGroup(e.target.value)}
            className="focus:shadow-outline ml-4 w-full appearance-none rounded border px-3 py-2 leading-tight shadow focus:outline-none"
          />
        </Field>
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
      </div>
    </>
  )
}
