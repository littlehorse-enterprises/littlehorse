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
        <Handle type="target" position={Position.Left} id="target-0" className="bg-transparent" />
        <Handle type="target" position={Position.Top} id="target-1" className="bg-transparent" />
        <Handle type="target" position={Position.Bottom} id="target-2" className="bg-transparent" />
        <Handle type="source" position={Position.Right} className="bg-transparent" id="source-0" />
        <Handle type="source" position={Position.Top} className="bg-transparent" id="source-1" />
        <Handle type="source" position={Position.Bottom} className="bg-transparent" id="source-2" />
      </div>
    </Fade>
  )
}

export const Nop = memo(NopNode)
