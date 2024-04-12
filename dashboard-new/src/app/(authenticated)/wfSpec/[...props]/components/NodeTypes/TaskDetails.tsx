import { getVariable } from '@/app/utils'
import { ArrowTopRightOnSquareIcon } from '@heroicons/react/24/solid'
import { TaskNode } from 'littlehorse-client/dist/proto/common_wfspec'
import { NodeRun } from 'littlehorse-client/dist/proto/node_run'
import Link from 'next/link'
import { FC } from 'react'
import { NodeDetails } from './NodeDetails'

export const TaskDetails: FC<{ task?: TaskNode; nodeRun?: NodeRun }> = ({ task, nodeRun }) => {
  if (!task) return null
  return (
    <NodeDetails>
      <div className="mb-2">
        <div className="flex items-center items-center gap-1 text-nowrap">
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
        </div>
      </div>
      {task.variables && task.variables.length > 0 && (
        <div className="">
          <h3 className="font-bold">Inputs</h3>
          <ul className="list-inside list-disc">
            {task.variables.map((variable, i) => (
              <li key={`variable.${i}`}>{getVariable(variable)}</li>
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
