import { cn } from '@/components/utils'
import { useQuery } from '@tanstack/react-query'
import { TaskAttempt } from 'littlehorse-client/proto'
import { ClipboardIcon, RefreshCwIcon } from 'lucide-react'
import { FC, Fragment } from 'react'

import { formatJsonOrReturnOriginalValue, getVariableValue, utcToLocalDateTime } from '@/app/utils'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { getTaskRun } from '../../NodeTypes/Task/getTaskRun'
import { AccordionNode } from './AccordionContent'
import { OverflowText } from '@/app/(authenticated)/[tenantId]/components/OverflowText'
import { Accordion, AccordionItem, AccordionTrigger, AccordionContent } from '@/components/ui/accordion'

export const TaskDefDetail: FC<AccordionNode> = ({ nodeRun }) => {
  const taskId = nodeRun.task?.taskRunId?.taskGuid
  const wfRunId = nodeRun.task?.taskRunId?.wfRunId?.id

  const { data, isLoading } = useQuery({
    queryKey: ['taskRun', wfRunId, taskId],
    queryFn: async () => {
      if (!wfRunId) return
      if (!taskId) return
      const taskRun = await getTaskRun({
        wfRunId: {
          id: wfRunId,
          parentWfRunId: undefined,
        },
        taskGuid: taskId,
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

  const lastLogOutput = data?.attempts[data?.attempts.length - 1]?.logOutput?.str

  return (
    <>
      <div className="flex justify-between align-top">
        {!!data.inputVariables?.length && (
          <div>
            <h2 className="mb-2 text-sm font-bold">Input Variables</h2>
            {data.inputVariables?.map(({ varName, value }) => {
              const type = Object.keys(value || {})?.[0]
              return (
                <div key={varName} className="mb-1 flex items-center gap-1">
                  <div className="rounded bg-gray-100 px-2 py-1 font-mono text-fuchsia-500">{varName}</div>
                  <div className="">= {getVariableValue(value)}</div>
                </div>
              )
            })}
          </div>
        )}

        <div className="mb-2 mt-1 flex ">
          <span className="font-bold">Task GUID :</span>
          <span> {taskId}</span>
          <span className="ml-2 mt-1">
            <ClipboardIcon
              className="h-4 w-4 cursor-pointer fill-transparent stroke-blue-500"
              onClick={() => {
                navigator.clipboard.writeText(taskId ?? '')
              }}
            />
          </span>
        </div>
      </div>

      <hr className="mt-6" />

      <div className="flex min-h-[160px] flex-col gap-4">
        <Accordion type="single" defaultValue="item-1" collapsible>
          {data?.attempts.map((attempt, index) => (
            <Fragment key={attempt.taskWorkerId}>
              <AccordionItem value={`item-${index + 1}`}>
                <AccordionTrigger>
                  TaskAttempt {index + 1}
                </AccordionTrigger>
                <AccordionContent>
                  <Table>
                    <TableHeader>
                      <TableRow className="bg-neutral-300 ">
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
                      <TableRow>
                        <TableCell className="p-1">{attempt.startTime && utcToLocalDateTime(attempt.startTime)}</TableCell>
                        <TableCell className="p-1">{attempt.endTime && utcToLocalDateTime(attempt.endTime)}</TableCell>
                        <TableCell className="p-1">
                          <div
                            className={cn(
                              'flex items-center justify-between p-2',
                              attempt.exception || attempt.error ? 'bg-red-200' : 'bg-green-200'
                            )}
                          >
                            {attempt?.status}
                          </div>
                        </TableCell>
                      </TableRow>
                    </TableBody>
                  </Table>

                  <div className="flex gap-2 mb-3">
                    {nodeRun && (
                      <div
                        className={cn('mt-2 h-fit flex flex-col rounded bg-green-200 p-1', { 'bg-red-200': nodeRun.errorMessage })}
                      >
                        <p className="text-md font-bold">{nodeRun.errorMessage ? 'TaskRun Error' : 'No TaskRun Error'}</p>
                        <OverflowText variant="error" className="w-36 text-nowrap" text={nodeRun.errorMessage ?? ''} />
                      </div>
                    )}
                    {nodeRun && (
                      <div className={'mt-2 flex h-fit flex-col rounded bg-gray-200 p-1'}>
                        <p className="text-md font-bold">{lastLogOutput ? 'Worker Log Output' : 'No Worker Log Output'}</p>
                        <OverflowText className="w-36 text-nowrap" text={lastLogOutput ?? ''} />
                      </div>
                    )}
                  </div>

                  <AttemptErrorExceptionOutput attempt={attempt} />
                </AccordionContent>
              </AccordionItem>
            </Fragment>
          ))}
        </Accordion>
      </div>
    </>
  )
}

export function AttemptErrorExceptionOutput({ attempt }: { attempt: TaskAttempt }) {
  if (!attempt.output && !attempt.error && !attempt.exception) return

  return (
    <div
      className={cn(
        'flex w-full flex-col overflow-auto rounded p-1',
        attempt.output ? 'bg-zinc-500 text-white' : 'bg-red-200'
      )}
    >
      <h3 className="font-bold">
        {attempt.error && 'Error'}
        {attempt.exception && 'Exception'}
        {attempt.output && 'Output'}
      </h3>
      <pre className="overflow-auto">
        {attempt.error && attempt.error.message}
        {attempt.exception && attempt.exception.message}
        {attempt.output && typeof getVariableValue(attempt.output) === 'string'
          ? formatJsonOrReturnOriginalValue(getVariableValue(attempt.output) as string)
          : String(getVariableValue(attempt.output))}
      </pre>
    </div>
  )
}
