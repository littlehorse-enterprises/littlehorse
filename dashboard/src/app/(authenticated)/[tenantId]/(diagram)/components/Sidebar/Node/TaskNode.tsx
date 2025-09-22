import LinkWithTenant from '@/app/(authenticated)/[tenantId]/components/LinkWithTenant'
import { TaskNode as TaskNodeProto } from 'littlehorse-client/proto'
import { LinkIcon } from 'lucide-react'
import { FC } from 'react'
import { getTaskName } from '../../NodeTypes/Task/TaskDetails'
import { VariableAssignment } from '../Components'

export const TaskNode: FC<{ node: TaskNodeProto }> = ({ node }) => {
  const { taskToExecute, exponentialBackoff, retries, timeoutSeconds, variables } = node
  return (
    <div className="flex max-w-full flex-1 flex-col">
      <small className="text-[0.75em] text-slate-400">Task</small>
      <div className="mb-2 flex items-center">
        <p className="flex-grow truncate text-lg font-medium">{getTaskName(node.taskToExecute)}</p>
        {taskToExecute?.$case === 'taskDefId' && (
          <LinkWithTenant href={`/taskDef/${taskToExecute.value.name}`}>
            <LinkIcon className="ml-1 h-4 w-4 cursor-pointer hover:text-slate-600" />
          </LinkWithTenant>
        )}
      </div>
      <div className="flex gap-4">
        <div className="flex flex-1 flex-col">
          <small className="text-[0.75em] text-slate-400">Retries</small>
          <p className="text-lg font-medium">{retries}</p>
        </div>
        <div className="flex flex-1 flex-col">
          <small className="text-[0.75em] text-slate-400">Timeout</small>
          <p className="text-lg font-medium">
            {timeoutSeconds}
            <span className="text-sm text-slate-400">ms</span>
          </p>
        </div>
      </div>

      {exponentialBackoff && (
        <div className="flex gap-4">
          <div className="flex flex-1 flex-col">
            <small className="text-[0.75em] text-slate-400">Retry Interval</small>
            <p className="text-lg font-medium">
              {exponentialBackoff.baseIntervalMs}
              <span className="text-sm text-slate-400">ms</span>
            </p>
          </div>
          <div className="flex flex-1 flex-col">
            <small className="text-[0.75em] text-slate-400">Max Wait</small>
            <p className="text-lg font-medium">
              {exponentialBackoff.maxDelayMs}
              <span className="text-sm text-slate-400">ms</span>
            </p>
          </div>
          <div className="flex flex-1 flex-col">
            <small className="text-[0.75em] text-slate-400">Multiplier</small>
            <p className="text-lg font-medium">
              {exponentialBackoff.multiplier}
              <span className="text-sm text-slate-400">x</span>
            </p>
          </div>
        </div>
      )}

      {variables && variables.length > 0 && (
        <div className="flex flex-col gap-2">
          <small className="text-[0.75em] text-slate-400">Inputs</small>
          {variables.map((v, i) => (
            <div key={JSON.stringify(v)} className="flex">
              <span className="bg-gray-200 px-2 font-mono">{i}</span>
              <VariableAssignment variableAssigment={v} />
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
