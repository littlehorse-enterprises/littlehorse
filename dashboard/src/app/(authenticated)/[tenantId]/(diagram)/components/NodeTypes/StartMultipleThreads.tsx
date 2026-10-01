import { StartMultipleThreadsNode as StartMultipleThreadsNodeProto } from 'littlehorse-client/proto'
import { Ellipsis, Spool } from 'lucide-react'
import { FC, memo } from 'react'
import { Handle, Position } from 'reactflow'
import { NodeProps } from '.'
import { Fade } from './Fade'
import { SelectedNode } from './SelectedNode'

const STACKED_CARD = 'absolute inset-0 rounded-md border-[1px] border-orange-500 bg-orange-100'

const Node: FC<NodeProps<'startMultipleThreads', StartMultipleThreadsNodeProto>> = ({ data }) => {
  const { fade, nodeRunsList, threadSpecName } = data
  const nodeRun = nodeRunsList?.[0]

  return (
    <>
      <SelectedNode />
      <Fade fade={fade} status={nodeRun?.status}>
        <div className="relative w-40 cursor-pointer">
          <div className={`${STACKED_CARD} translate-x-2 translate-y-2`} />
          <div className={`${STACKED_CARD} translate-x-1 translate-y-1`} />
          <div className="relative flex flex-col items-center rounded-md border-[1px] border-orange-500 bg-orange-200 px-2 pt-1 text-center text-xs">
            <Handle type="target" position={Position.Left} id="target-0" className="bg-transparent" />
            <div className="flex items-center">
              <Spool className="h-4 w-4 stroke-orange-500" strokeWidth={1.5} />
              <Ellipsis className="h-4 w-4 stroke-orange-500" strokeWidth={1.5} />
            </div>
            <span className="max-w-full truncate">{threadSpecName}</span>
            <Handle type="source" position={Position.Right} id="source-0" className="bg-transparent" />
          </div>
        </div>
      </Fade>
    </>
  )
}

export const StartMultipleThreads = memo(Node)
