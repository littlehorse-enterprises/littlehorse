'use client'
import LinkWithTenant from '@/app/(authenticated)/[tenantId]/components/LinkWithTenant'
import { getTaskDef } from '@/app/(authenticated)/[tenantId]/taskDef/[name]/getTaskDef'
import { getVariable, getVariableValue } from '@/app/utils'
import { useQuery } from '@tanstack/react-query'
import { NodeRun, TaskNode } from 'littlehorse-client/proto'
import { ExternalLinkIcon } from 'lucide-react'
import { useParams } from 'next/navigation'
import { FC, useState } from 'react'
import { NodeDetails } from '../NodeDetails'
import { DiagramDataGroup } from '../DataGroupComponents/DiagramDataGroup'
import { Duration } from '../DataGroupComponents/Duration'
import { Entry } from '../DataGroupComponents/Entry'
import { Status } from '../DataGroupComponents/Status'
import { getTaskRun } from './getTaskRun'
import { DiagramDataGroupIndexer } from '../DataGroupComponents/DiagramDataGroupIndexer'
import { Result } from '../DataGroupComponents/Result'
import { OverflowText } from '@/app/(authenticated)/[tenantId]/components/OverflowText'
import { ViewVariables } from '../DataGroupComponents/Variables'

export const TaskDetails: FC<{
  taskNode?: TaskNode
  nodeRun?: NodeRun
  selected: boolean
  nodeRunsList: NodeRun[]
}> = ({ taskNode, nodeRun, selected, nodeRunsList }) => {
  const [taskAttemptIndex, setTaskAttemptIndex] = useState(0)
  const tenantId = useParams().tenantId as string
  const { data: taskRunData } = useQuery({
    queryKey: ['taskRun', nodeRun, tenantId],
    queryFn: async () => {
      if (!nodeRun?.task?.taskRunId) return null
      return await getTaskRun({ tenantId, ...nodeRun.task.taskRunId })
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

  const message = taskRunData?.attempts[taskAttemptIndex].error?.message ?? taskRunData?.attempts[taskAttemptIndex].exception?.message ?? String(getVariableValue(taskRunData?.attempts[taskAttemptIndex].output)) ?? undefined
  const resultString = taskRunData?.attempts[taskAttemptIndex].error ? "ERROR" : taskRunData?.attempts[taskAttemptIndex].exception ? "EXCEPTION" : taskRunData?.attempts[taskAttemptIndex].output ? "OUTPUT" : undefined

  // ! ensure taskRunData is mapping to the correct nodeRun from nodeRunsList. idk smthn like that
  console.log(nodeRunsList)

  return (
    <NodeDetails nodeRunList={nodeRunsList}>
      {nodeRun ? (
        taskRunData ? (
          <>
            <DiagramDataGroup tab="Task" label="TaskRun">
              <Entry label="Status:">
                <Status status={taskRunData.status} />
              </Entry>
              <Entry label="Timeout:">
                {taskRunData.timeoutSeconds}
              </Entry>
              <Entry label="Max Attempts:">
                {taskRunData.totalAttempts}
              </Entry>
              <Entry label="Input Variables:">
                <ViewVariables variables={taskRunData.inputVariables} />
              </Entry>
            </DiagramDataGroup>

            <DiagramDataGroup tab="Task" label="TaskAttempt" from="TaskRun">
              <DiagramDataGroupIndexer index={taskAttemptIndex} setIndex={setTaskAttemptIndex} indexes={taskRunData.attempts.length} />
              <Entry label="Status:">
                <Status status={taskRunData.attempts[taskAttemptIndex].status} />
              </Entry>
              {message && resultString &&
                <Entry label="Result:">
                  <Result resultString={resultString} resultMessage={message} variant={resultString === "ERROR" ? "error" : undefined} />
                </Entry>
              }
              <Entry label="Worker Log Output:">
                <div className={"bg-gray-300 rounded-lg text-center border border-black max-w-52 text-nowrap min-h-5"} >
                  <OverflowText text={taskRunData.attempts[taskAttemptIndex].logOutput?.str ?? "-"} className="text-xs" variant={resultString === "ERROR" ? "error" : undefined} />
                </div>
              </Entry>
              <Entry separator>
                <Duration arrival={taskRunData.attempts[taskAttemptIndex].startTime} ended={taskRunData.attempts[taskAttemptIndex].endTime} />
              </Entry>
            </DiagramDataGroup>
          </>
        ) : null
      ) : (
        <div className="mb-2">
          <div className="flex items-center gap-1 whitespace-nowrap text-nowrap">
            <h3 className="font-bold">TaskDef</h3>
            {taskNode.dynamicTask && <>{getVariable(taskNode.dynamicTask)}</>}
          </div>
        </div>
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
