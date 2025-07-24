'use client'
import LinkWithTenant from '@/app/(authenticated)/[tenantId]/components/LinkWithTenant'
import { OverflowText } from '@/app/(authenticated)/[tenantId]/components/OverflowText'
import { getTaskDef } from '@/app/(authenticated)/[tenantId]/taskDef/[name]/getTaskDef'
import { getVariableValue } from '@/app/utils'
import { useQuery } from '@tanstack/react-query'
import { NodeRun, TaskNode } from 'littlehorse-client/proto'
import { ExternalLinkIcon } from 'lucide-react'
import { useParams } from 'next/navigation'
import { FC, useState } from 'react'
import { DiagramDataGroup } from '../DataGroupComponents/DiagramDataGroup'
import { DiagramDataGroupIndexer } from '../DataGroupComponents/DiagramDataGroupIndexer'
import { Duration } from '../DataGroupComponents/Duration'
import { Entry } from '../DataGroupComponents/Entry'
import { Result } from '../DataGroupComponents/Result'
import { Status } from '../DataGroupComponents/Status'
import { ViewVariableAssignments, ViewVariables } from '../DataGroupComponents/Variables'
import { NodeDetails } from '../NodeDetails'
import { getTaskRun } from './getTaskRun'

export const TaskDetails: FC<{
  taskNode?: TaskNode
  nodeRun?: NodeRun
  selected: boolean
  nodeRunsList: NodeRun[]
}> = ({ taskNode, nodeRun, selected, nodeRunsList }) => {
  const [nodeRunsIndex, setNodeRunsIndex] = useState(0)
  const [taskAttemptIndex, setTaskAttemptIndex] = useState(0)
  const tenantId = useParams().tenantId as string
  const { data: taskRunData } = useQuery({
    queryKey: ['taskRun', nodeRun, tenantId, nodeRunsIndex],
    queryFn: async () => {
      if (!nodeRunsList[nodeRunsIndex].task?.taskRunId) return null
      return await getTaskRun({ tenantId, ...nodeRunsList[nodeRunsIndex].task.taskRunId })
    },
  })

  const { data: taskDef } = useQuery({
    queryKey: ['taskDef', taskNode, tenantId, nodeRun, selected],
    queryFn: async () => {
      if (!taskNode?.taskDefId?.name) return null
      if (nodeRun?.task?.taskRunId) return null
      if (!selected) return null
      const taskDef = await getTaskDef(tenantId, {
        name: taskNode?.taskDefId?.name,
      })
      return taskDef
    },
  })

  if (!taskNode || (!taskDef && !nodeRun?.task?.taskRunId)) return null
  taskRunData?.attempts.sort((a, b) => new Date(b.startTime ?? 0).getTime() - new Date(a.startTime ?? 0).getTime())

  const message =
    taskRunData?.attempts[taskAttemptIndex].error?.message ??
    taskRunData?.attempts[taskAttemptIndex].exception?.message ??
    String(getVariableValue(taskRunData?.attempts[taskAttemptIndex].output))
  const resultString = taskRunData?.attempts[taskAttemptIndex].error
    ? 'ERROR'
    : taskRunData?.attempts[taskAttemptIndex].exception
      ? 'EXCEPTION'
      : taskRunData?.attempts[taskAttemptIndex].output
        ? 'OUTPUT'
        : undefined

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
              {message && message !== '' && resultString && (
                <Entry label="Result:">
                  <Result
                    resultString={resultString}
                    resultMessage={message}
                    variant={resultString === 'ERROR' ? 'error' : undefined}
                  />
                </Entry>
              )}
              <Entry label="Worker Log Output:">
                <div className="flex w-full text-nowrap items-center justify-center rounded-lg border border-black bg-gray-300 p-1">
                  <div className="max-w-52">
                    <OverflowText
                      text={taskRunData.attempts[taskAttemptIndex].logOutput?.str ?? '-'}
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
          <Entry label="Retries">{taskNode?.retries}</Entry>
          <Entry label="Timeout">{taskNode?.timeoutSeconds}</Entry>
          <Entry label="Variables">
            <ViewVariableAssignments variables={taskNode?.variables} />
          </Entry>
        </DiagramDataGroup>
      )}
    </NodeDetails>
  )
}

export const TaskLink: FC<{ taskName?: string }> = ({ taskName }) => {
  return (
    <LinkWithTenant
      className="flex items-center justify-center gap-1 text-blue-500 hover:underline"
      target="_blank"
      href={`/taskDef/${taskName}`}
    >
      {taskName} <ExternalLinkIcon className="h-4 w-4" />
    </LinkWithTenant>
  )
}
