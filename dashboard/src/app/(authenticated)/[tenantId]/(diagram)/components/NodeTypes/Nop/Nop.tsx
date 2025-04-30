import { FC, memo } from 'react'
import { Handle, Position } from '@xyflow/react'
import { NodeProps } from '..'
import { Fade } from '../Fade'
import { nopHandles } from './nopHandles'

const NopNode: FC<NodeProps> = props => {
  const {
    data: { fade },
  } = props

  const handlers = nopHandles(props)

  return (
    <Fade fade={fade}>
      <div className="">
        <div className="cursor-pointer1 ml-1 flex h-6 w-6 rotate-45 border-[1px] border-gray-500 bg-gray-200"></div>
        <Handle type="target" position={Position.Left} id="target-0" className="bg-transparent" />
        {handlers?.map((pos: Position, index: number) => (
          <Handle
            key={`custom-${index}`}
            type="source"
            position={pos}
            className="bg-transparent"
            id={`source-${index}`}
          />
        ))}
      </div>
    </Fade>
  )
}

export const Nop = memo(NopNode)
