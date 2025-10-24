import { SleepNode } from 'littlehorse-client/proto'
import { ClockIcon } from 'lucide-react'
import { FC, memo } from 'react'
import { Handle, Position } from 'reactflow'
import { NodeProps } from '..'
import { Fade } from '../Fade'
import { SelectedNode } from '../SelectedNode'

const Node: FC<NodeProps<'sleep', SleepNode>> = ({ data }) => {
  const { fade, nodeRunsList } = data
  const nodeRun = nodeRunsList?.[0]

  return (
    <>
      <SelectedNode />
      <Fade fade={fade} status={nodeRun?.status}>
        <div className="relative cursor-pointer">
          <div className="flex h-10 w-10 items-center justify-center rounded-full border-[1px] border-gray-500 bg-gray-200">
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
