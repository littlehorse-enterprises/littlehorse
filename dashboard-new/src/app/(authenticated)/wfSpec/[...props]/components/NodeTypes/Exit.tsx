import { FC, memo } from 'react'
import { Handle, NodeProps, Position } from 'reactflow'

import { Fade } from './Fade'

const ExitNode: FC<NodeProps> = ({ data }) => {
  const { fade } = data
  return (
    <Fade fade={fade}>
      <div className="flex h-6 w-6 cursor-pointer rounded-xl border-[3px] border-gray-500 bg-green-200">
        <Handle
          type="target"
          position={Position.Left}
          className="border-b-4 border-l-4 border-t-4 border-b-transparent border-l-gray-500 border-t-transparent bg-transparent"
        />
      </div>
    </Fade>
  )
}

export const Exit = memo(ExitNode)
