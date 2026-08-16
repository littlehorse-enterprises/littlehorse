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
        <div className="relative flex h-12 w-40 cursor-pointer items-center justify-center rounded-md border border-green-500 bg-green-200 px-6 text-center text-xs text-green-950">
          <Handle type="target" position={Position.Left} id="target-0" className="bg-transparent" />
          <span className="truncate">{threadSpecName}</span>
          <Spool className="absolute right-1 top-1 h-4 w-4 stroke-green-600" strokeWidth={1.5} />
          <Handle type="source" position={Position.Right} id="source-0" className="bg-transparent" />
        </div>
      </Fade>
    </>
  )
}

export const StartThread = memo(Node)
