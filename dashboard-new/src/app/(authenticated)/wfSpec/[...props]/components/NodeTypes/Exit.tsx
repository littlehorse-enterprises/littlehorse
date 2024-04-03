import { FC, memo } from 'react'
import { Handle, NodeProps, Position } from 'reactflow'

import { Node as NodeProto } from 'littlehorse-client/dist/proto/wf_spec'

const ExitNode: FC<NodeProps<NodeProto>> = ({ data }) => {
  return (
    <div className="flex h-6 w-6 cursor-pointer rounded-xl border-[3px] border-gray-500 bg-green-200">
      <Handle
        type="target"
        position={Position.Left}
        className="border-b-4 border-l-4 border-t-4 border-b-transparent border-l-gray-500 border-t-transparent bg-transparent"
      />
    </div>
  )
}

export const Exit = memo(ExitNode)
