import { FC, memo } from 'react'
import { Handle, Position } from 'reactflow'

import { ExitNode as ExitNodeProto, LHStatus } from 'littlehorse-client/proto'
import { NodeProps } from '.'
import { Fade } from './Fade'
import { SelectedNode } from './SelectedNode'

const ExitNode: FC<NodeProps<'exit', ExitNodeProto>> = ({ data }) => {
  const { fade } = data
  const failureDef = data.result?.$case === 'failureDef' ? data.result.value : undefined
  return (
    <>
      <SelectedNode />
      <Fade fade={fade} status={failureDef ? LHStatus.EXCEPTION : undefined}>
        <div
          className={`flex h-6 w-6 cursor-pointer rounded-xl border-[3px] border-gray-500 ${failureDef ? 'bg-red-200' : 'bg-green-200'}`}
        >
          <Handle type="target" position={Position.Left} className="bg-transparent" />
        </div>
      </Fade>
    </>
  )
}

export const Exit = memo(ExitNode)
