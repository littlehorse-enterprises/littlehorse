import { FC, memo } from 'react'
import { Handle, Position } from '@xyflow/react'

import { LHStatus } from 'littlehorse-client/proto'
import { NodeProps } from '.'
import { Fade } from './Fade'
import { NodeDetails } from './NodeDetails'

const ExitNode: FC<NodeProps> = ({ data }) => {
  const { fade } = data
  const failureDef = data.exit?.failureDef
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
        <Handle type="target" position={Position.Left} className="bg-transparent" id="target-0" />
        <Handle type="source" position={Position.Right} className="bg-transparent" id="source-0" />
      </div>
    </Fade>
  )
}

export const Exit = memo(ExitNode)
