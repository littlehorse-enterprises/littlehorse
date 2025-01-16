import LinkWithTenant from '@/app/[tenantId]/components/LinkWithTenant'
import { getTaskDef } from '@/app/[tenantId]/taskDef/[name]/getTaskDef'
import { getVariable, getVariableValue } from '@/app/utils'
import { useQuery } from '@tanstack/react-query'
import { NodeRun, TaskNode } from 'littlehorse-client/proto'
import { ExternalLinkIcon } from 'lucide-react'
import { useParams } from 'next/navigation'
import { FC } from 'react'
import { NodeRunsList } from '../../NodeRunsList'
import { NodeDetails } from '../NodeDetails'
import { getTaskRun } from './getTaskRun'
import { OverflowText } from '@/app/[tenantId]/components/OverflowText'
import { cn } from '@/components/utils'

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

  return (
    <NodeDetails>
      <div className="mb-2">
        <div className="flex items-center gap-1 whitespace-nowrap text-nowrap">
          <h3 className="font-bold">TaskDef</h3>
          {nodeRun ? (
            <TaskLink taskName={taskNode.taskDefId?.name} />
          ) : (
            <>
              {taskNode.taskDefId && <TaskLink taskName={taskNode.taskDefId.name} />}
              {taskNode.dynamicTask && <>{getVariable(taskNode.dynamicTask)}</>}
            </>
          )}
        </div>
        <div className="flex gap-2 text-nowrap">
          <div className="flex items-center justify-center">Timeout: {taskNode.timeoutSeconds}s</div>
          <div className="flex items-center justify-center">Retries: {taskNode.retries}</div>
        </div>
      </div>
      {taskNode.variables && taskNode.variables.length > 0 && !(nodeRunsList?.length > 1) && (
        <div className="whitespace-nowrap">
          <h3 className="font-bold">Inputs</h3>
          <ol className="list-inside list-decimal">
            {taskNode.variables.map((variable, i) => (
              <li className="mb-1 flex gap-1" key={`variable.${i}`}>
                <div className="bg-gray-200 px-2 font-mono text-fuchsia-500">
                  {data?.inputVariables?.[i]?.varName ??
                    taskDef?.inputVars[i].name ??
                    variable.variableName ??
                    `arg${i}`}
                </div>
                <div> = </div>
                <div className="truncate">
                  {data?.inputVariables?.[i]?.value
                    ? getVariableValue(data?.inputVariables[i].value)
                    : getVariable(variable)}
                </div>
              </li>
            ))}
          </ol>
        </div>
      )}
      <NodeRunsList nodeRuns={nodeRunsList} />
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
