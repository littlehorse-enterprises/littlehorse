import { Handle, Position } from '@xyflow/react'
import { FC, memo } from 'react'
import { NodeProps } from '..'
import { Fade } from '../Fade'

const NopNode: FC<NodeProps> = props => {
  const {
    data: { isFaded },
  } = props


  return (
    <Fade isFaded={isFaded}>
      <div className="">
        <div className="cursor-pointer1 ml-1 flex h-6 w-6 rotate-45 border-[1px] border-gray-500 bg-gray-200"></div>
        <Handle type="target" position={Position.Left} id="target-0" className="bg-transparent" />
        
          <Handle
            type="source"
            position={Position.Right}
            className="bg-transparent"
            id={`source-0`}
          />

      </div>
    </Fade>
  )
}

export const Nop = memo(NopNode)
