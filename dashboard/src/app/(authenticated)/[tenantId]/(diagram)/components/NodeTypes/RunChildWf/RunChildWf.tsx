import { getVariable } from '@/app/utils'
import { RunChildWfNode } from 'littlehorse-client/proto'
import { TrendingUpDown, Workflow } from 'lucide-react'
import { FC, memo } from 'react'
import { Handle, Position } from 'reactflow'
import { NodeProps } from '..'
import { DiagramNodeDiamond, DiagramNodeShell } from '../DiagramNodeChrome'
import { Fade } from '../Fade'
import { emeraldNodeTheme } from '../nodeThemes'
import { SelectedNode } from '../SelectedNode'

const Node: FC<NodeProps<'runChildWf', RunChildWfNode>> = ({ id, data, selected }) => {
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
        <DiagramNodeShell id={id} label="Run Child Wf" icon={Workflow} theme={emeraldNodeTheme} subtitle={wfSpecName}>
          <div className="relative flex cursor-pointer items-center">
            <Handle type="target" id="target-0" position={Position.Left} className="bg-transparent" />
            <DiagramNodeDiamond selected={selected} theme={emeraldNodeTheme}>
              <TrendingUpDown className="h-5 w-5 shrink-0 stroke-emerald-950" strokeWidth={1.5} />
              <div className="absolute -bottom-1 -right-1 grid h-4 w-4 place-items-center rounded border border-emerald-500 bg-emerald-200">
                <Workflow className="h-2.5 w-2.5 stroke-emerald-950" strokeWidth={1.5} />
              </div>
            </DiagramNodeDiamond>
            <Handle type="source" id="source-0" position={Position.Right} className="bg-transparent" />
          </div>
        </DiagramNodeShell>
      </Fade>
    </>
  )
}

export const RunChildWf = memo(Node)
