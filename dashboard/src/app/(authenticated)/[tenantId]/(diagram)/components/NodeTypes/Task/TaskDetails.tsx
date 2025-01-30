import LinkWithTenant from '@/app/(authenticated)/[tenantId]/components/LinkWithTenant'
import { getTaskDef } from '@/app/(authenticated)/[tenantId]/taskDef/[name]/getTaskDef'
import { getVariable, getVariableValue } from '@/app/utils'
import { useQuery } from '@tanstack/react-query'
import { NodeRun, TaskNode } from 'littlehorse-client/proto'
import { ExternalLinkIcon } from 'lucide-react'
import { useParams } from 'next/navigation'
import { FC } from 'react'
import { NodeRunsList } from '../../NodeRunsList'
import { NodeDetails } from '../NodeDetails'
import { getTaskRun } from './getTaskRun'
import { OverflowText } from '@/app/(authenticated)/[tenantId]/components/OverflowText'
import { cn } from '@/components/utils'
import { DiagramDataGroup } from '../DiagramDataGroup/DiagramDataGroup'
import { Duration } from '../DiagramDataGroup/Duration'
import { Entry } from '../DiagramDataGroup/Entry'

export const TaskDetails: FC<{
  taskNode?: TaskNode
  nodeRun?: NodeRun
  selected: boolean
  nodeRunsList: [NodeRun]
}> = ({ taskNode, nodeRun, selected, nodeRunsList }) => {
  const tenantId = useParams().tenantId as string
  const { data } = useQuery({
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

  const lastLogOutput = data?.attempts[data?.attempts.length - 1]?.logOutput?.str

  return nodeRun ? (
    <NodeDetails>
      <DiagramDataGroup tab="Task" label="TaskRun">
        <div>TaskRun</div>
      </DiagramDataGroup>
      <DiagramDataGroup tab="Task" label="TaskAttempts">
        <Entry separator>
          {/* // ! use taskRun arrival here */}
          <Duration arrival={nodeRun.arrivalTime} ended={nodeRun.endTime} />
        </Entry>
      </DiagramDataGroup>
    </NodeDetails>
  ) : (
    <NodeDetails>
      <div className="mb-2">
        <div className="flex items-center gap-1 whitespace-nowrap text-nowrap">
          <h3 className="font-bold">TaskDef</h3>
          {taskNode.dynamicTask && <>{getVariable(taskNode.dynamicTask)}</>}
        </div>
      </div>
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
