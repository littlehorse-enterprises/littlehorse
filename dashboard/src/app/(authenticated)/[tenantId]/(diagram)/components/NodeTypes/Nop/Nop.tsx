import { FC, memo } from 'react'
import { Handle, Position } from 'reactflow'
import { NodeProps } from '..'
import { Fade } from '../Fade'

const NopNode: FC<NodeProps<'entrypoint', {}>> = props => {
  const {
    data: { fade },
  } = props

  return (
    <Fade fade={fade}>
      <div className="">
        <div className="cursor-pointer1 ml-1 flex h-6 w-6 rotate-45 border-[1px] border-gray-500 bg-gray-200"></div>
        <Handle type="target" position={Position.Left} id="target-0" className="bg-transparent" />
        <Handle type="source" position={Position.Right} id="source-0" className="bg-transparent" />
        {/* {handlers?.map((pos: Position, index: number) => (
          <Handle
            key={`custom-${index}`}
            type="source"
            position={pos}
            className="bg-transparent"
            id={`source-${index}`}
          />
        ))} */}
      </div>
    </Fade>
  )
}

export const Nop = memo(NopNode)
