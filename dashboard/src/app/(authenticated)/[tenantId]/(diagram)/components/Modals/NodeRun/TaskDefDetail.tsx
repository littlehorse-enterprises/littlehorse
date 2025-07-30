import { cn } from '@/components/utils'
import { useQuery } from '@tanstack/react-query'
import { NodeRun, TaskAttempt } from 'littlehorse-client/proto'
import { ClipboardIcon, RefreshCwIcon } from 'lucide-react'
import { FC, Fragment } from 'react'

import { getVariableValue, utcToLocalDateTime } from '@/app/utils'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { useWhoAmI } from '@/contexts/WhoAmIContext'
import { getTaskRun } from '../../NodeTypes/Task/getTaskRun'
import { AccordionNode } from './AccordionContent'

export const TaskDefDetail: FC<AccordionNode<"task">> = ({ nodeRun }) => {
  const { taskRunId } = nodeRun.nodeType.value
  const { tenantId } = useWhoAmI()

  const { data, isLoading } = useQuery({
    queryKey: ['taskRun', taskRunId],
    queryFn: async () => {
      if (!taskRunId) return
      const taskRun = await getTaskRun({ tenantId, ...taskRunId })

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

  return (
    <>
      <div className="flex justify-between align-top">
        {!!data.inputVariables?.length && (
          <div>
            <h2 className="mb-2 text-sm font-bold">Input Variables</h2>
            {data.inputVariables?.map(({ varName, value }) => {
              return (
                <div key={varName} className="mb-1 flex items-center gap-1">
                  <div className="rounded bg-gray-100 px-2 py-1 font-mono text-fuchsia-500">{varName}</div>
                  {value && <div className="">= {getVariableValue(value)}</div>}
                </div>
              )
            })}
          </div>
        )}
        <div className="mb-2 mt-1 flex ">
          <span className="font-bold">Task GUID :</span>
          <span> {taskRunId?.taskGuid}</span>
          <span className="ml-2 mt-1">
            <ClipboardIcon
              className="h-4 w-4 cursor-pointer fill-transparent stroke-blue-500"
              onClick={() => {
                navigator.clipboard.writeText(taskRunId?.taskGuid ?? '')
              }}
            />
          </span>
        </div>
      </div>

      <hr className="mt-6" />

      <div className="flex min-h-[160px] flex-col gap-4">
        <Table>
          <TableHeader>
            <TableRow className="bg-neutral-300 ">
              <TableHead scope="col">
                <strong>Attempt </strong>
              </TableHead>
              <TableHead scope="col">
                <strong>Start Time</strong>
              </TableHead>
              <TableHead scope="col">
                <strong>End Time</strong>
              </TableHead>
              <TableHead scope="col">
                <strong>Status</strong>
              </TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {data?.attempts.map((attempt, index) => {
              return (
                <Fragment key={attempt.taskWorkerId}>
                  <TableRow>
                    <TableCell className="p-1">{index + 1}</TableCell>
                    <TableCell className="p-1">{attempt.startTime && utcToLocalDateTime(attempt.startTime)}</TableCell>
                    <TableCell className="p-1">{attempt.endTime && utcToLocalDateTime(attempt.endTime)}</TableCell>
                    <TableCell className="p-1">
                      <div
                        className={cn(
                          'flex items-center justify-between p-2',
                          attempt.result?.$case === 'error' || attempt.result?.$case === 'exception'
                            ? 'bg-red-200'
                            : 'bg-green-200'
                        )}
                      >
                        {attempt?.status}
                      </div>
                    </TableCell>
                  </TableRow>

                  <TableRow>
                    <TableCell colSpan={4} className="px-0">
                      <AttemptErrorExceptionOutput attempt={attempt} />
                    </TableCell>
                  </TableRow>
                </Fragment>
              )
            })}
          </TableBody>
        </Table>
      </div>
    </>
  )
}

export function AttemptErrorExceptionOutput({ attempt }: { attempt: TaskAttempt }) {
  if (!attempt.result) return

  const message =
    attempt.result.$case === 'output' ? getVariableValue(attempt.result.value) : attempt.result.value.message

  return (
    <div
      className={cn(
        'flex w-full flex-col overflow-auto rounded p-1',
        attempt.result.$case === 'output' ? 'bg-zinc-500 text-white' : 'bg-red-200'
      )}
    >
      <h3 className="font-bold">{attempt.result.$case}</h3>
      <pre className="overflow-auto">{message}</pre>
    </div>
  )
}
