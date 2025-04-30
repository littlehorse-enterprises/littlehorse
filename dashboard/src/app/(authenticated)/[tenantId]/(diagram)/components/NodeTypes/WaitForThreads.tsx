import { getVariable } from '@/app/utils'
import { PlusIcon } from 'lucide-react'
import { FC, memo } from 'react'
import { Handle, Position } from '@xyflow/react'
import { NodeProps } from '.'
import { DiagramDataGroup } from './DataGroupComponents/DiagramDataGroup'
import { Fade } from './Fade'
import { NodeDetails } from './NodeDetails'
const Node: FC<NodeProps> = ({ data }) => {
  const { isFaded, nodeRunsList } = data

  return (
    <>
      <NodeDetails nodeRunList={nodeRunsList}>
        <DiagramDataGroup label="WaitForThreads">
          {data.waitForThreads?.threadList && (
            <div className="whitespace-nowrap">{getVariable(data.waitForThreads?.threadList)}</div>
          )}
          {data.waitForThreads?.threads?.threads.map(thread => (
            <div
              key={`${getVariable(thread.threadRunNumber)}`}
              className="flex items-center justify-center whitespace-nowrap"
            >
              {getVariable(thread.threadRunNumber)}
            </div>
          ))}
          {data.nodeRun && data.nodeRun.errorMessage && (
            <div className="mt-2 flex flex-col rounded bg-red-200 p-1">
              <h3 className="font-bold">Error</h3>
              <pre className="overflow-x-auto">{data.nodeRun.errorMessage}</pre>
            </div>
          )}
        </DiagramDataGroup>
      </NodeDetails>
      <Fade isFaded={isFaded} status={data.nodeRun?.status}>
        <div className="relative cursor-pointer">
          <div className="ml-1 flex h-6 w-6 rotate-45 items-center justify-center border-[1px] border-gray-500 bg-gray-200">
            <PlusIcon className="h-4 w-4 rotate-45 fill-gray-500" />
          </div>
          <Handle type="target" position={Position.Left} className="bg-transparent" id="target-0" />
          <Handle type="target" position={Position.Bottom} className="bg-transparent" id="bottom-0" />
          <Handle type="source" position={Position.Right} className="bg-transparent" id="source-0" />
        </div>
      </Fade>
    </>
  )
}

export const WaitForThreads = memo(Node)
