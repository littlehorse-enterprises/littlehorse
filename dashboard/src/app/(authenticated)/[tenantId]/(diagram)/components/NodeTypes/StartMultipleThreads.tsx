import { StartMultipleThreadsNode as StartMultipleThreadsNodeProto } from 'littlehorse-client/proto'
import { Spool, TrendingUpDown } from 'lucide-react'
import { FC, memo } from 'react'
import { Handle, Position } from 'reactflow'
import { NodeProps } from '.'
import { Fade } from './Fade'
import { SelectedNode } from './SelectedNode'

const DIAMOND = '[clip-path:polygon(50%_0,100%_50%,50%_100%,0_50%)]'

const Node: FC<NodeProps<'startMultipleThreads', StartMultipleThreadsNodeProto>> = ({ data }) => {
  const { fade, nodeRunsList, threadSpecName } = data
  const nodeRun = nodeRunsList?.[0]

  return (
    <>
      <SelectedNode />
      <Fade fade={fade} status={nodeRun?.status}>
        <div className="flex cursor-pointer items-center">
          <Handle type="target" position={Position.Left} id="target-0" className="bg-transparent" />
          <div className="relative grid h-10 w-10 place-items-center">
            <div className={`absolute inset-0 bg-emerald-500 ${DIAMOND}`} />
            <div className={`absolute inset-[2px] bg-emerald-200 ${DIAMOND}`} />
            <TrendingUpDown className="relative z-10 h-5 w-5 shrink-0 stroke-emerald-950" strokeWidth={1.5} />
            <div className="absolute -bottom-1 -right-1 z-10 grid h-4 w-4 place-items-center rounded border border-emerald-500 bg-emerald-200">
              <Spool className="h-2.5 w-2.5 stroke-emerald-950" strokeWidth={1.5} />
            </div>
            <span className="pointer-events-none absolute left-1/2 top-full mt-2 -translate-x-1/2 whitespace-nowrap text-xs text-slate-700">
              {threadSpecName}
            </span>
          </div>
          <Handle type="source" position={Position.Right} id="source-0" className="bg-transparent" />
        </div>
      </Fade>
    </>
  )
}

export const StartMultipleThreads = memo(Node)
