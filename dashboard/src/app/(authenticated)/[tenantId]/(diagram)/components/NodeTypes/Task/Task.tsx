import { getVariable } from '@/app/utils'
import { cn } from '@/lib/utils'
import { TaskNode } from 'littlehorse-client/proto'
import { SettingsIcon } from 'lucide-react'
import { FC, memo } from 'react'
import { Handle, Position } from 'reactflow'
import { NodeProps } from '..'
import { Fade } from '../Fade'
import { SelectedNode } from '../SelectedNode'

const Node: FC<NodeProps<'task', TaskNode>> = ({ id, data, selected }) => {
  const { fade, nodeRunsList, taskToExecute } = data
  if (!taskToExecute) return null
  const nodeRun = nodeRunsList?.[0]

  return (
    <>
      <SelectedNode />
      <Fade fade={fade} status={nodeRun?.status}>
        <div className="flex flex-col items-center gap-0.5">
          <div className="flex items-center gap-0.5 text-[9px] font-semibold uppercase tracking-wider text-orange-700/75">
            <SettingsIcon className="h-2.5 w-2.5 shrink-0 fill-orange-500 stroke-orange-600" strokeWidth={2} />
            <span>Task</span>
          </div>
          <div
            className={cn(
              'relative min-w-[3.25rem] cursor-pointer overflow-hidden rounded-md border-[1px] border-orange-500 bg-orange-200 px-2 py-1.5 text-xs',
              selected && 'bg-orange-300'
            )}
          >
            <SettingsIcon
              aria-hidden
              strokeWidth={1.25}
              className="pointer-events-none absolute -left-2.5 top-1/2 z-0 h-12 w-12 -translate-y-1/2 fill-orange-500/15 stroke-orange-500/30"
            />
            <span className="relative z-10 block truncate px-1 text-center font-medium leading-tight text-orange-950">
              {getTaskName(taskToExecute)}
            </span>
            <Handle type="source" position={Position.Right} className="bg-transparent" />
            <Handle type="target" position={Position.Left} className="bg-transparent" />
          </div>
          <span
            className="max-w-[10rem] truncate text-center font-mono text-[10px] leading-tight text-slate-500"
            title={id}
          >
            {id}
          </span>
        </div>
      </Fade>
    </>
  )
}

export const Task = memo(Node)

export const getTaskName = (task: TaskNode['taskToExecute']): string => {
  if (!task) return ''

  if (task.$case === 'taskDefId') return task.value.name
  if (task.$case === 'dynamicTask') return getVariable(task.value)
  return ''
}
