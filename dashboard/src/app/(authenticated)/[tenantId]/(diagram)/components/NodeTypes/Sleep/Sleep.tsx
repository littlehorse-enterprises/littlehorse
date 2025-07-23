import { ClockIcon } from 'lucide-react'
import { FC, memo } from 'react'
import { Handle, Position } from 'reactflow'
import { NodeProps } from '..'
import { Fade } from '../Fade'
import { SleepDetails } from './SleepDetails'

const Node: FC<NodeProps> = ({ data }) => {
  const { fade, sleep, nodeRunsList } = data

  return (
    <>
      <SleepDetails sleepNode={sleep} nodeRunsList={nodeRunsList} />
      <Fade fade={fade} status={nodeRunsList?.[nodeRunsList.length - 1]?.status}>
        <div className="relative cursor-pointer">
          <div className="ml-1 flex h-10 w-10 items-center justify-center rounded-full border-[1px] border-gray-500 bg-gray-200">
            <ClockIcon className="h-4 w-4 fill-none stroke-gray-500" />
          </div>

          <Handle type="target" position={Position.Left} className="bg-transparent" />
          <Handle type="source" position={Position.Right} className="bg-transparent" />
        </div>
      </Fade>
    </>
  )
}

export const Sleep = memo(Node)
