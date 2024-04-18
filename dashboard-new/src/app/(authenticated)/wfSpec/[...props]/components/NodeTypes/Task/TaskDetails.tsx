import { getVariable, getVariableValue } from '@/app/utils'
import { ArrowTopRightOnSquareIcon } from '@heroicons/react/24/solid'
import { useQuery } from '@tanstack/react-query'
import { TaskNode } from 'littlehorse-client/dist/proto/common_wfspec'
import { NodeRun } from 'littlehorse-client/dist/proto/node_run'
import Link from 'next/link'
import { FC } from 'react'
import { getTaskRun } from './getTaskRun'
import { NodeDetails } from '../NodeDetails'

export const TaskDetails: FC<{ task?: TaskNode; nodeRun?: NodeRun }> = ({ task, nodeRun }) => {
  const { data } = useQuery({
    queryKey: ['taskRun', nodeRun],
    queryFn: async () => {
      if (nodeRun?.task?.taskRunId) return await getTaskRun(nodeRun.task.taskRunId)
    },
  })
  if (!task) return null
  return (
    <NodeDetails>
      <div className="mb-2">
        <div className="flex items-center items-center gap-1 whitespace-nowrap text-nowrap">
          <h3 className="font-bold">TaskDef</h3>
          {nodeRun ? (
            <TaskLink taskName={task.taskDefId?.name} />
          ) : (
            <>
              {task.taskDefId && <TaskLink taskName={task.taskDefId.name} />}
              {task.dynamicTask && <>{getVariable(task.dynamicTask)}</>}
            </>
          )}
        </div>
        <div className="flex gap-2 text-nowrap">
          <div className="flex items-center justify-center">Timeout: {task.timeoutSeconds}s</div>
          <div className="flex items-center justify-center">Retries: {task.retries}</div>
          {data?.totalAttempts !== undefined && (
            <div className="flex items-center justify-center">Attempts: {data.totalAttempts}</div>
          )}
        </div>
      </div>
      {data === undefined && task.variables && task.variables.length > 0 && (
        <div className="whitespace-nowrap">
          <h3 className="font-bold">Inputs</h3>
          <ul className="list-inside list-disc">
            {task.variables.map((variable, i) => (
              <li key={`variable.${i}`}>{getVariable(variable)}</li>
            ))}
          </ul>
        </div>
      )}
      {data?.inputVariables && data.inputVariables.length > 0 && (
        <div className="mt-2 whitespace-nowrap">
          <h3 className="font-bold">Inputs</h3>
          <ul className="list-inside list-disc">
            {data.inputVariables.map(({ varName, value }) => (
              <li key={`variable.${varName}`}>{`${varName} = ${getVariableValue(value)}`}</li>
            ))}
          </ul>
        </div>
      )}
      {nodeRun && nodeRun.errorMessage && (
        <div className="mt-2 flex flex-col rounded bg-red-200 p-1">
          <h3 className="font-bold">Error</h3>
          <pre className="overflow-x-auto">{nodeRun.errorMessage}</pre>
        </div>
      )}
    </NodeDetails>
  )
}

const TaskLink: FC<{ taskName?: string }> = ({ taskName }) => {
  return (
    <Link
      className="flex items-center justify-center gap-1 text-blue-500 hover:underline"
      target="_blank"
      href={`/taskDef/${taskName}`}
    >
      {taskName} <ArrowTopRightOnSquareIcon className="h-4 w-4" />
    </Link>
  )
}
