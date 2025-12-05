import { TaskNode } from 'littlehorse-client/proto'
import { SettingsIcon } from 'lucide-react'
import { FC, memo } from 'react'
import { Handle, Position } from 'reactflow'
import { NodeProps } from '..'
import { Fade } from '../Fade'
import { SelectedNode } from '../SelectedNode'
import { getVariable } from '@/app/utils'

const Node: FC<NodeProps<'task', TaskNode>> = node => {
  const { fade, nodeRunsList, taskToExecute } = node.data
  if (!taskToExecute) return null
  const nodeRun = nodeRunsList?.[0]

  return (
    <>
      <SelectedNode />
      <Fade fade={fade} status={nodeRun?.status}>
        <div
          className={
            'flex cursor-pointer flex-col items-center rounded-md border-[1px] border-orange-500 bg-orange-200 px-2 pt-1 text-xs' +
            (node.selected ? ' bg-orange-300' : '')
          }
        >
          <SettingsIcon className="h-4 w-4 fill-orange-500" />
          {getTaskName(taskToExecute)}
          <Handle type="source" position={Position.Right} className="bg-transparent" />
          <Handle type="target" position={Position.Left} className="bg-transparent" />
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
