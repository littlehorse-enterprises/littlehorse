import { FC, memo } from 'react'
import { Handle, Position } from 'reactflow'
import { NodeProps } from '.'
import { Fade } from './Fade'

const NopNode: FC<NodeProps> = ({ data }) => {
  const { fade } = data
  return (
    <Fade fade={fade}>
      <div className="">
        <div className="cursor-pointer1 ml-1 flex h-6 w-6 rotate-45 border-[1px] border-gray-500 bg-gray-200"></div>
        <Handle
          type="target"
          position={Position.Left}
          id="0"
          className="border-b-4 border-l-4 border-t-4 border-b-transparent border-l-gray-500 border-t-transparent bg-transparent"
        />
        <Handle type="source" position={Position.Right} className="bg-transparent" id="0" />
        <Handle type="source" position={Position.Top} className="bg-transparent" id="1" />
        <Handle type="source" position={Position.Bottom} className="bg-transparent" id="2" />
      </div>
    </Fade>
  )
}

export const Nop = memo(NopNode)
