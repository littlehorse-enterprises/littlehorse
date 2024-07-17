import { getVariable, getVariableValue } from '@/app/utils'
import { useWhoAmI } from '@/contexts/WhoAmIContext'
import { useQuery } from '@tanstack/react-query'
import { NodeRun, TaskNode } from 'littlehorse-client/proto'
import { ExternalLinkIcon, EyeIcon } from 'lucide-react'
import Link from 'next/link'
import { FC, useCallback } from 'react'
import { useModal } from '../../../hooks/useModal'
import { NodeDetails } from '../NodeDetails'
import { getTaskRun } from './getTaskRun'

export const TaskDetails: FC<{ task?: TaskNode; nodeRun?: NodeRun }> = ({ task, nodeRun }) => {
  const { tenantId } = useWhoAmI()
  const { data } = useQuery({
    queryKey: ['taskRun', nodeRun, tenantId],
    queryFn: async () => {
      if (nodeRun?.task?.taskRunId) return await getTaskRun({ tenantId, ...nodeRun.task.taskRunId })
      return null
    },
  })

  const { setModal, setShowModal } = useModal()

  const onClick = useCallback(() => {
    if (data) {
      setModal({ type: 'taskRun', data: data })
      setShowModal(true)
    }
  }, [data, setModal, setShowModal])

  if (!task) return null

  return (
    <NodeDetails>
      <div className="mb-2">
        <div className="flex items-center gap-1 whitespace-nowrap text-nowrap">
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
        <div className="whitespace-nowrap">
          <h3 className="font-bold">Inputs</h3>
          <ol className="list-inside list-decimal">
            {task.variables.map((variable, i) => (
              <li className="mb-1 flex gap-1" key={`variable.${i}`}>
                <div className="bg-gray-200 px-2 font-mono text-fuchsia-500">
                  {data?.inputVariables?.[i]?.varName ?? `arg${i}`}
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
      {nodeRun && nodeRun.errorMessage && (
        <div className="mt-2 flex flex-col rounded bg-red-200 p-1">
          <h3 className="font-bold">Error</h3>
          <pre className="overflow-x-auto">{nodeRun.errorMessage}</pre>
        </div>
      )}
      {nodeRun && (
        <div className="mt-2 flex justify-center">
          <button className="flex items-center gap-1 p-1 text-blue-500 hover:bg-gray-200" onClick={onClick}>
            <EyeIcon className="h-4 w-4" />
            Inspect TaskRun
          </button>
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
      {taskName} <ExternalLinkIcon className="h-4 w-4" />
    </Link>
  )
}
