import { FC, memo } from 'react'
import { Handle, Position } from 'reactflow'
import { RefreshCwIcon } from 'lucide-react'
import { NodeProps } from '.'
import { Fade } from './Fade'
import { SelectedNode } from './SelectedNode'

const Node: FC<NodeProps<any, any>> = ({ data }) => {
  const { fade, nodeRunsList } = data
  const nodeRun = nodeRunsList?.[0]

  return (
    <>
      <SelectedNode />
      <Fade fade={fade} status={nodeRun?.status}>
        <div className="relative cursor-pointer">
          <div className="flex h-10 w-10 items-center justify-center rounded-full border-[1px] border-blue-500 bg-blue-200">
            <RefreshCwIcon className="h-4 w-4 fill-none stroke-blue-600" />
          </div>

          <Handle type="target" position={Position.Right} className="bg-transparent" />
          <Handle type="source" position={Position.Left} className="bg-transparent" />
        </div>
      </Fade>
    </>
  )
}

export const Cycle = memo(Node)
