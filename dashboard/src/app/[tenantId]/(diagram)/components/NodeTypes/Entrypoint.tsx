import { FC, memo } from 'react'
import { Handle, Position } from 'reactflow'

const EntrypointNode: FC = ({}) => {
  return (
    <div className="flex h-6 w-6 cursor-pointer rounded-xl border-[1px] border-gray-500 bg-green-200">
      <Handle type="source" position={Position.Right} className="bg-transparent" />
    </div>
  )
}

export const Entrypoint = memo(EntrypointNode)
