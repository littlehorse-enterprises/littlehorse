import { FC, Fragment } from 'react'
import { RefreshCwIcon } from 'lucide-react'
import { cn } from '@/components/utils'
import { utcToLocalDateTime, getVariableValue } from '@/app/utils'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { useQuery } from '@tanstack/react-query'
import { getTaskRun } from '../../NodeTypes/Task/getTaskRun'
import { AttemptErrorExceptionOutput } from '../TaskRun'

type Props = {
  wfRunId?: string
  taskId?: string
}

export const TaskDefDetail: FC<Props> = ({ wfRunId, taskId }) => {
  if (!wfRunId) return
  if (!taskId) return
  const { data, isLoading } = useQuery({
    queryKey: ['taskRun', wfRunId, taskId],
    queryFn: async () => {
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

  return (
    <>
      <div className="flex justify-between align-top">
        {data.inputVariables?.length && (
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
        <div className="mb-2 mt-1 text-sm font-bold">
          NodeRun Position:{' '}
          <span className="border-2 border-blue-500 p-1">{data?.source?.taskNode?.nodeRunId?.position}</span>
        </div>
      </div>

      <hr className="mt-6" />

      <div className="flex min-h-[160px] flex-col gap-4">
        <Table>
          <TableHeader>
            <TableRow className='bg-neutral-300 '>
              <TableHead scope="col">
                <strong>Attempt </strong>
              </TableHead>
              <TableHead scope="col">
                <strong>Start Time</strong>
              </TableHead>
              <TableHead scope="col">
                <strong>End Time</strong>{' '}
              </TableHead>
              <TableHead scope="col">
                <strong>Status</strong>{' '}
              </TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {data?.attempts.map((attempt, index) => {
              return (
                <Fragment key={attempt.taskWorkerId}>
                  <TableRow >
                    <TableCell className="p-1">{index + 1}</TableCell>
                    <TableCell className="p-1">{attempt.startTime && utcToLocalDateTime(attempt.startTime)}</TableCell>
                    <TableCell className="p-1">{attempt.endTime && utcToLocalDateTime(attempt.endTime)}</TableCell>
                    <TableCell className="p-1" >
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
                  <TableRow>
                    <TableCell colSpan={3} className="p-1">
                      <strong>Output</strong>
                    </TableCell>
                  </TableRow>
                  <TableRow>
                    <TableCell colSpan={3} className="px-0">
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
