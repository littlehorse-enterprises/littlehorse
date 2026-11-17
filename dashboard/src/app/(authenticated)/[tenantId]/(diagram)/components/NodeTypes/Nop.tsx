import { Node } from 'littlehorse-client/proto'
import { DiamondIcon } from 'lucide-react'
import { FC, memo } from 'react'
import { Handle, Position } from 'reactflow'
import { NodeProps } from '.'
import { DiagramNodeDiamond, DiagramNodeShell } from './DiagramNodeChrome'
import { Fade } from './Fade'
import { grayNodeTheme } from './nodeThemes'
import { SelectedNode } from './SelectedNode'

const NopNode: FC<NodeProps<'entrypoint', Node>> = ({ id, data, selected }) => {
  const { fade, nodeRunsList } = data
  const nodeRun = nodeRunsList?.[0]

  return (
    <>
      <SelectedNode />
      <Fade fade={fade} status={nodeRun?.status}>
        <DiagramNodeShell id={id} label="Nop" icon={DiamondIcon} theme={grayNodeTheme}>
          <div className="relative flex">
            <DiagramNodeDiamond
              selected={selected}
              theme={grayNodeTheme}
              sizeClass="h-8 w-8"
              innerInsetClass="inset-[1px]"
            />
            <Handle type="target" id="target-0" position={Position.Left} className="bg-transparent" />
            <Handle type="source" id="source-0" position={Position.Right} className="bg-transparent" />
          </div>
        </DiagramNodeShell>
      </Fade>
    </>
  )
}

export const Nop = memo(NopNode)
