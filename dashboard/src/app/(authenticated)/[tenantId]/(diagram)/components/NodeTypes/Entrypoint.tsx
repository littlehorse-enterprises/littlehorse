import { EntrypointNode as EntrypointNodeProto } from 'littlehorse-client/proto'
import { FC, memo } from 'react'
import { Handle, Position } from 'reactflow'
import { NodeProps } from '.'
import { SelectedNode } from './SelectedNode'

const EntrypointNode: FC<NodeProps<'entrypoint', EntrypointNodeProto>> = ({}) => {
  return (
    <>
    <SelectedNode />
    <div className="flex h-6 w-6 cursor-pointer rounded-xl border-[1px] border-gray-500 bg-green-200">
      <Handle type="source" position={Position.Right} className="bg-transparent" />
    </div>
    </>
  )
}

export const Entrypoint = memo(EntrypointNode)
