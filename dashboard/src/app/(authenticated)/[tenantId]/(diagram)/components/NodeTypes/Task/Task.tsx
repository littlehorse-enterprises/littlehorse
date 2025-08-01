import { getVariable } from '@/app/utils'
import { sortNodeRunsByLatest } from '@/app/utils/sortNodeRunsByLatest'
import { TaskNode } from 'littlehorse-client/proto'
import { SettingsIcon } from 'lucide-react'
import { FC, memo } from 'react'
import { Handle, Position } from 'reactflow'
import { NodeProps } from '..'
import { Fade } from '../Fade'
import { getTaskName, TaskDetails } from './TaskDetails'

const Node: FC<NodeProps<'task', TaskNode>> = node => {
  const { fade, nodeNeedsToBeHighlighted, nodeRunsList, taskToExecute } = node.data
  if (!taskToExecute) return null
  const nodeRun = sortNodeRunsByLatest(nodeRunsList)[0]

  return (
    <>
      <TaskDetails {...node} />
      <Fade fade={fade} status={nodeRun?.status}>
        <div
          className={
            'flex cursor-pointer flex-col items-center rounded-md border-[1px] border-orange-500 bg-orange-200 px-2 pt-1 text-xs' +
            (node.selected ? ' bg-orange-300' : '') +
            (nodeNeedsToBeHighlighted ? ' shadow-lg shadow-orange-500' : '')
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
