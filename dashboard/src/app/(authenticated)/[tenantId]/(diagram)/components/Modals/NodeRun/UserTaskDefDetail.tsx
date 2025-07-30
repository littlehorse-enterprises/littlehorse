import { getVariable, getVariableValue, utcToLocalDateTime } from '@/app/utils'
import { useWhoAmI } from '@/contexts/WhoAmIContext'
import { useQuery } from '@tanstack/react-query'
import { UserTaskRun as LHUserTaskRun, UserTaskEvent, UserTaskRunStatus } from 'littlehorse-client/proto'
import { ClipboardIcon, RefreshCwIcon } from 'lucide-react'
import { FC } from 'react'
import { getUserTaskRun } from '../../NodeTypes/UserTask/getUserTaskRun'
import { AccordionNode } from './AccordionContent'

export const UserTaskDefDetail: FC<AccordionNode<'userTask'>> = ({ nodeRun, userTaskNode }) => {
  const { userTaskRunId } = nodeRun.nodeType.value
  const { tenantId } = useWhoAmI()

  const { data, isLoading } = useQuery({
    queryKey: ['userTaskRun', userTaskRunId],
    queryFn: async () => {
      if (!userTaskRunId) return
      const taskRun = await getUserTaskRun({
        tenantId,
        ...userTaskRunId,
      })

      return taskRun
    },
  })

  if (isLoading) {
    return (
      <div className="flex min-h-[60px] items-center justify-center text-center">
        <RefreshCwIcon className="h-8 w-8 animate-spin text-blue-500" />
      </div>
    )
  }

  if (!data) return

  const lhUserTaskRun = data as LHUserTaskRun
  const assigmentHistory = lhUserTaskRun.events.filter(hasEventCase('assigned'))
  const cancellationHistory = lhUserTaskRun.events.filter(hasEventCase('cancelled'))
  const resultsToRender = Object.keys(lhUserTaskRun.results).map(k => ({
    field: k,
    value: getVariableValue(lhUserTaskRun.results[k]),
  }))

  return (
    <>
      <div className="mb-6 mt-3">
        <div className="ml-3 mr-2 flex h-2 justify-between ">
          <div>
            <span className="font-bold ">User Task: </span> <span>{lhUserTaskRun.userTaskDefId?.name}</span>
          </div>
          <div className="flex ">
            <span className="font-bold">User Task GUID: </span> <span>{lhUserTaskRun.id?.userTaskGuid}</span>
            <span className="ml-2 mt-1">
              <ClipboardIcon
                className="h-4 w-4 cursor-pointer fill-transparent stroke-blue-500"
                onClick={() => {
                  navigator.clipboard.writeText(lhUserTaskRun.id?.userTaskGuid ?? '')
                }}
              />
            </span>
          </div>
        </div>
      </div>
      <hr />
      <div className="mb-4 mt-4">
        <span className="ml-3 font-bold">Created On: </span>
        <span>{lhUserTaskRun.scheduledTime && utcToLocalDateTime(lhUserTaskRun.scheduledTime)}</span>
        {lhUserTaskRun.status === UserTaskRunStatus.DONE && (
          <div className="ml-3  mt-1">
            <span className="font-bold">Completed On: </span>
            <span> {nodeRun?.endTime && utcToLocalDateTime(nodeRun?.endTime)}</span>
          </div>
        )}
        {lhUserTaskRun.status === UserTaskRunStatus.CANCELLED && (
          <div className="ml-3">
            <span className="font-bold">Cancelled On: </span>
            <span> {cancellationHistory[0].time && utcToLocalDateTime(cancellationHistory[0].time)}</span>
          </div>
        )}
        {userTaskNode?.onCancellationExceptionName !== undefined && (
          <div className="ml-3">
            <span className="font-bold">Exception upon cancellation: </span>
            <span className="rounded bg-red-300 p-1 text-xs">
              {getVariable(userTaskNode?.onCancellationExceptionName)}
            </span>
          </div>
        )}
      </div>
      <hr />
      {resultsToRender.length > 0 && (
        <div className="mt-2">
          <div className="mb-2">
            <div className="ml-3 h-2 font-bold">Results</div>
          </div>
          <div className="mt-6 flex items-center justify-between p-2">
            <table className="text-surface min-w-full text-center text-sm font-light">
              <thead className="border-b border-neutral-200 bg-neutral-300 font-medium">
                <tr>
                  <th scope="col" className="px-6 py-4">
                    Field
                  </th>
                  <th scope="col" className="px-6 py-4">
                    Value
                  </th>
                </tr>
              </thead>
              <tbody>
                {resultsToRender.map((result, index) => (
                  <tr key={index} className="border-b border-neutral-200">
                    <td className="px-6 py-4">{result.field}</td>
                    <td className="px-6 py-4">{result.value?.toString()}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
      <div className="mt-2">
        <div className="mb-2">
          <div className="ml-3 h-2 font-bold">Assignment History</div>
        </div>
        <div className="mt-6 flex items-center justify-between p-2">
          <table className="text-surface min-w-full text-center text-sm font-light">
            <thead className="border-b border-neutral-200 bg-neutral-300 font-medium">
              <tr>
                <th scope="col" className="px-6 py-4">
                  Timestamp
                </th>
                <th scope="col" className="px-6 py-4">
                  Old User Group
                </th>
                <th scope="col" className="px-6 py-4">
                  New User Group
                </th>
                <th scope="col" className="px-6 py-4">
                  Old User Id
                </th>
                <th scope="col" className="px-6 py-4">
                  New User Id
                </th>
              </tr>
            </thead>
            <tbody>
              {assigmentHistory.map((e, index) => (
                <tr key={index} className="border-b border-neutral-200">
                  <td className="px-6 py-4">{e.time && utcToLocalDateTime(e.time)}</td>
                  <td className="px-6 py-4">{e.event?.value?.oldUserGroup}</td>
                  <td className="px-6 py-4">{e.event.value.newUserGroup}</td>
                  <td className="px-6 py-4">{e.event.value.oldUserId}</td>
                  <td className="px-6 py-4">{e.event.value.newUserId}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        {cancellationHistory.length > 0 && (
          <div>
            <div className="ml-3 mt-6 h-2 font-bold">Cancellation History</div>
            <div className="mt-6 flex items-center justify-between p-2">
              <table className="text-surface min-w-full text-center text-sm font-light">
                <thead className="border-b border-neutral-200 bg-neutral-300 font-medium">
                  <tr>
                    <th scope="col" className="px-6 py-4">
                      Timestamp
                    </th>
                    <th scope="col" className="px-6 py-4">
                      Message
                    </th>
                  </tr>
                </thead>
                <tbody>
                  {cancellationHistory.map((e, index) => (
                    <tr key={index} className="border-b border-neutral-200">
                      <td className="px-6 py-4">{e.time && utcToLocalDateTime(e.time)}</td>
                      <td className="px-6 py-4">{e.event.value.message}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}
      </div>
    </>
  )
}

function hasEventCase<
  C extends UserTaskEvent['event'] extends infer U ? (U extends { $case: string } ? U['$case'] : never) : never,
>(caseName: C) {
  return (
    e: UserTaskEvent
  ): e is Omit<UserTaskEvent, 'event'> & { event: Extract<NonNullable<UserTaskEvent['event']>, { $case: C }> } =>
    !!e.event && e.event.$case === caseName
}
