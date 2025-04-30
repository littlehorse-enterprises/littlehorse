import { FC, memo } from 'react'
import { Handle, Position } from '@xyflow/react'

import { NodeProps } from '.'
import { Fade } from './Fade'

const EntrypointNode: FC<NodeProps> = ({ data }) => {
  const { fade } = data

  return (
    <Fade fade={fade} >
      <div className="flex h-6 w-6 cursor-pointer rounded-xl border-[1px] border-gray-500 bg-green-200">
        <Handle type="target" position={Position.Left} className="bg-transparent" id="target-0" />
        <Handle type="source" position={Position.Right} className="bg-transparent" id="source-0" />
      </div>
    </Fade>
  )
}

export const Entrypoint = memo(EntrypointNode)