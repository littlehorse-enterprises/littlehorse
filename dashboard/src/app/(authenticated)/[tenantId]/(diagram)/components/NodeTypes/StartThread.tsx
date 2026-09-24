import { StartThreadNode } from 'littlehorse-client/proto'
import { Spool } from 'lucide-react'
import { FC, memo } from 'react'
import { Handle, Position } from 'reactflow'
import { NodeProps } from '.'
import { Fade } from './Fade'
import { SelectedNode } from './SelectedNode'

const Node: FC<NodeProps<'startThread', StartThreadNode>> = ({ data }) => {
  const { fade, nodeRunsList, threadSpecName } = data
  const nodeRun = nodeRunsList?.[0]

  return (
    <>
      <SelectedNode />
      <Fade fade={fade} status={nodeRun?.status}>
        <div className="flex w-40 cursor-pointer flex-col items-center rounded-md border-[1px] border-orange-500 bg-orange-200 px-2 pt-1 text-center text-xs">
          <Handle type="target" position={Position.Left} id="target-0" className="bg-transparent" />
          <Spool className="h-4 w-4 stroke-orange-500" strokeWidth={1.5} />
          <span className="truncate">{threadSpecName}</span>
          <Handle type="source" position={Position.Right} id="source-0" className="bg-transparent" />
        </div>
      </Fade>
    </>
  )
}

export const StartThread = memo(Node)
