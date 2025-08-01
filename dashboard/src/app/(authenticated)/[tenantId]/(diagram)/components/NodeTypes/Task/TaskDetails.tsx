'use client'
import LinkWithTenant from '@/app/(authenticated)/[tenantId]/components/LinkWithTenant'
import { OverflowText } from '@/app/(authenticated)/[tenantId]/components/OverflowText'
import { getVariable, getVariableValue } from '@/app/utils'
import { useQuery } from '@tanstack/react-query'
import { TaskNode } from 'littlehorse-client/proto'
import { ExternalLinkIcon } from 'lucide-react'
import { useParams } from 'next/navigation'
import { FC, useState } from 'react'
import { NodeProps } from '..'
import { DiagramDataGroup } from '../DataGroupComponents/DiagramDataGroup'
import { DiagramDataGroupIndexer } from '../DataGroupComponents/DiagramDataGroupIndexer'
import { Duration } from '../DataGroupComponents/Duration'
import { Entry } from '../DataGroupComponents/Entry'
import { Result } from '../DataGroupComponents/Result'
import { Status } from '../DataGroupComponents/Status'
import { ViewVariableAssignments, ViewVariables } from '../DataGroupComponents/Variables'
import { NodeDetails } from '../NodeDetails'
import { getTaskRun } from './getTaskRun'

export const TaskDetails: FC<NodeProps<'task', TaskNode>> = ({ data }) => {
  const { nodeRunsList, nodeRun } = data
  const [nodeRunsIndex, setNodeRunsIndex] = useState(0)
  const [taskAttemptIndex, setTaskAttemptIndex] = useState(0)
  const tenantId = useParams().tenantId as string
  const { data: taskRunData } = useQuery({
    queryKey: ['taskRun', nodeRun, tenantId, nodeRunsIndex],
    queryFn: async () => {
      if (!nodeRunsList[nodeRunsIndex].nodeType.value.taskRunId) return null
      return await getTaskRun({ tenantId, ...nodeRunsList[nodeRunsIndex].nodeType.value.taskRunId })
    },
  })

  taskRunData?.attempts.sort((a, b) => new Date(b.startTime ?? 0).getTime() - new Date(a.startTime ?? 0).getTime())

  return (
    <NodeDetails nodeRunList={nodeRunsList} nodeRunsIndex={nodeRunsIndex} setNodeRunsIndex={setNodeRunsIndex}>
      {nodeRun ? (
        taskRunData ? (
          <>
            <DiagramDataGroup label="TaskRun">
              <Entry label="Status:">
                <Status status={taskRunData.status} />
              </Entry>
              <Entry label="Timeout:">{taskRunData.timeoutSeconds}</Entry>
              <Entry label="Max Attempts:">{taskRunData.totalAttempts}</Entry>
              <Entry label="Input Variables:">
                <ViewVariables variables={taskRunData.inputVariables} />
              </Entry>
            </DiagramDataGroup>

            <DiagramDataGroup
              label={'TaskAttempt'}
              index={taskAttemptIndex}
              indexes={taskRunData.attempts.length}
              from="TaskRun"
            >
              <DiagramDataGroupIndexer
                index={taskAttemptIndex}
                setIndex={setTaskAttemptIndex}
                indexes={taskRunData.attempts.length}
              />
              <Entry label="Status:">
                <Status status={taskRunData.attempts[taskAttemptIndex].status} />
              </Entry>
              <Result result={taskRunData.attempts[taskAttemptIndex].result} />
              <Entry label="Worker Log Output:">
                <div className="flex w-full items-center justify-center text-nowrap rounded-lg border border-black bg-gray-300 p-1">
                  <div className="max-w-52">
                    <OverflowText
                      text={
                        taskRunData.attempts[taskAttemptIndex].logOutput !== undefined
                          ? getVariableValue(taskRunData.attempts[taskAttemptIndex].logOutput)
                          : '-'
                      }
                      className="text-xs"
                    />
                  </div>
                </div>
              </Entry>
              <Entry separator>
                <Duration
                  arrival={taskRunData.attempts[taskAttemptIndex].startTime}
                  ended={taskRunData.attempts[taskAttemptIndex].endTime}
                />
              </Entry>
            </DiagramDataGroup>
          </>
        ) : null
      ) : (
        <DiagramDataGroup label="TaskDef">
          <Entry label="Retries">{data.retries}</Entry>
          <Entry label="Timeout">{data.timeoutSeconds}</Entry>
          <Entry label="Variables">
            <ViewVariableAssignments variables={data.variables} />
          </Entry>
        </DiagramDataGroup>
      )}
    </NodeDetails>
  )
}

export const TaskLink: FC<{ taskToExecute: NonNullable<TaskNode['taskToExecute']> }> = ({ taskToExecute }) => {
  const taskName = getTaskName(taskToExecute)
  return (
    <LinkWithTenant
      className="flex items-center justify-center gap-1 text-blue-500 hover:underline"
      target="_blank"
      href={`/taskDef/${taskName}`}
    >
      {`${taskName}`} <ExternalLinkIcon className="h-4 w-4" />
    </LinkWithTenant>
  )
}

export const getTaskName = (task: TaskNode['taskToExecute']): string => {
  if (!task) return ''

  if (task.$case === 'taskDefId') return task.value.name
  if (task.$case === 'dynamicTask') return getVariable(task.value)
  return ''
}
