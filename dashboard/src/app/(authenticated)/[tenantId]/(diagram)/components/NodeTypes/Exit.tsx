import { FC, memo } from 'react'
import { Handle, Position } from 'reactflow'

import { ExitNode as ExitNodeProto, LHStatus } from 'littlehorse-client/proto'
import { NodeProps } from '.'
import { Fade } from './Fade'
import { NodeDetails } from './NodeDetails'

const ExitNode: FC<NodeProps<'exit', ExitNodeProto>> = ({ data }) => {
  const { fade } = data
  const failureDef = data.result?.$case === 'failureDef' ? data.result.value : undefined
  return (
    <Fade fade={fade} status={failureDef ? LHStatus.EXCEPTION : undefined}>
      {failureDef && (
        <NodeDetails nodeRunList={data.nodeRunsList}>
          <div className="mb-2 flex gap-1 text-nowrap">
            <h3 className="font-bold">FailureDef</h3>
            {failureDef.failureName}
          </div>
          <div className="">
            <pre className="max-w-36 truncate bg-slate-600 p-1 font-mono text-xs text-white hover:max-w-full">
              {failureDef.message}
            </pre>
          </div>
        </NodeDetails>
      )}
      <div
        className={`flex h-6 w-6 cursor-pointer rounded-xl border-[3px] border-gray-500 ${failureDef ? 'bg-red-200' : 'bg-green-200'}`}
      >
        <Handle type="target" position={Position.Left} className="bg-transparent" />
      </div>
    </Fade>
  )
}

export const Exit = memo(ExitNode)
