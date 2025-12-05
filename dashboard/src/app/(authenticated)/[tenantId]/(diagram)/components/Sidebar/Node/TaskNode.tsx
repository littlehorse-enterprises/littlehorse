import LinkWithTenant from '@/app/(authenticated)/[tenantId]/components/LinkWithTenant'
import { TaskNode as TaskNodeProto } from 'littlehorse-client/proto'
import { LinkIcon } from 'lucide-react'
import { FC } from 'react'
import { VariableAssignment } from '../Components'
import { TaskNodeMetric } from '../Components/TaskNodeMetric'
import './node.css'
import { getTaskName } from '../../NodeTypes/Task'

export const TaskNode: FC<{ node: TaskNodeProto }> = ({ node }) => {
  const { taskToExecute, exponentialBackoff, retries, timeoutSeconds, variables } = node
  return (
    <div className="flex max-w-full flex-1 flex-col">
      <small className="node-title">Task</small>
      <div className="mb-2 flex items-center">
        <p className="flex-grow truncate text-lg font-medium">{getTaskName(node.taskToExecute)}</p>
        {taskToExecute?.$case === 'taskDefId' && (
          <LinkWithTenant href={`/taskDef/${taskToExecute.value.name}`}>
            <LinkIcon className="ml-1 h-4 w-4 cursor-pointer hover:text-slate-600" />
          </LinkWithTenant>
        )}
      </div>
      <div className="flex gap-4">
        <TaskNodeMetric title="Retries" value={retries} />
        <TaskNodeMetric title="Timeout" value={timeoutSeconds} measure="ms" />
      </div>

      {exponentialBackoff && (
        <div className="flex gap-4">
          <TaskNodeMetric title="Retry Interval" value={exponentialBackoff.baseIntervalMs} measure="ms" />
          <TaskNodeMetric title="Max Wait" value={exponentialBackoff.maxDelayMs} measure="ms" />
          <TaskNodeMetric title="Multiplier" value={exponentialBackoff.multiplier} measure="x" />
        </div>
      )}

      {variables && variables.length > 0 && (
        <div className="flex flex-col gap-2">
          <small className="node-title">Inputs</small>
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
