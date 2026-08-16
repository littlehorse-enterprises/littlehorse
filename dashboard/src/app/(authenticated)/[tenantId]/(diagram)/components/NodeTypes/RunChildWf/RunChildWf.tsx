import { RunChildWfNode } from 'littlehorse-client/proto'
import { Workflow } from 'lucide-react'
import { FC, memo } from 'react'
import { Handle, Position } from 'reactflow'
import { NodeProps } from '..'
import { Fade } from '../Fade'
import { SelectedNode } from '../SelectedNode'
import { getVariable } from '@/app/utils'

const Node: FC<NodeProps<'runChildWf', RunChildWfNode>> = ({ data }) => {
  const { fade, nodeRunsList, wfSpec } = data
  const nodeRun = nodeRunsList?.[0]
  const wfSpecName =
    wfSpec?.oneofKind === 'wfSpecName'
      ? wfSpec.wfSpecName
      : wfSpec?.oneofKind === 'wfSpecVar'
        ? getVariable(wfSpec.wfSpecVar)
        : ''

  return (
    <>
      <SelectedNode />
      <Fade fade={fade} status={nodeRun?.status}>
        <div className="flex w-40 cursor-pointer flex-col items-center rounded-md border-[1px] border-orange-500 bg-orange-200 px-2 pt-1 text-center text-xs">
          <Handle type="target" position={Position.Left} id="target-0" className="bg-transparent" />
          <Workflow className="h-4 w-4 stroke-orange-500" strokeWidth={1.5} />
          <span className="truncate">{wfSpecName}</span>
          <Handle type="source" position={Position.Right} id="source-0" className="bg-transparent" />
        </div>
      </Fade>
    </>
  )
}

export const RunChildWf = memo(Node)
