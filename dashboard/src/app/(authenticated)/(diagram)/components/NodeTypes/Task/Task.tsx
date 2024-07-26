import { getVariable } from '@/app/utils'
import { SettingsIcon } from 'lucide-react'
import { FC, memo } from 'react'
import { Handle, Position } from 'reactflow'
import { NodeProps } from '..'
import { Fade } from '../Fade'
import { TaskDetails } from './TaskDetails'

const Node: FC<NodeProps> = ({ selected, data }) => {
  const { fade, nodeNeedsToBeHighlighted } = data
  if (!data.task) return null
  const { task } = data
  return (
    <>
      <TaskDetails task={task} nodeRun={data.nodeRun} />
      <Fade fade={fade} status={data.nodeRun?.status}>
        <div
          className={
            'flex cursor-pointer flex-col items-center rounded-md border-[1px] border-orange-500 bg-orange-200 px-2 pt-1 text-xs' +
            (selected ? ' bg-orange-300' : '') +
            (nodeNeedsToBeHighlighted ? ' shadow-lg shadow-orange-500' : '')
          }
        >
          <SettingsIcon className="h-4 w-4 fill-orange-500" />
          {task.taskDefId?.name}
          {getVariable(task.dynamicTask)}
          <Handle type="source" position={Position.Right} className="bg-transparent" />
          <Handle type="target" position={Position.Left} className="bg-transparent" />
        </div>
      </Fade>
    </>
  )
}

export const Task = memo(Node)
