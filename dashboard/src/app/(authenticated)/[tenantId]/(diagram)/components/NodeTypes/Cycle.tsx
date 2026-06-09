import { FC, memo } from 'react'
import { Handle, Position } from 'reactflow'
import { RefreshCwIcon } from 'lucide-react'
import { NodeProps } from '.'
import { DiagramNodeCircle, DiagramNodeShell } from './DiagramNodeChrome'
import { blueNodeTheme } from './nodeThemes'
import { Fade } from './Fade'
import { SelectedNode } from './SelectedNode'

const Node: FC<NodeProps<any, any>> = ({ id, data, selected }) => {
  const { fade, nodeRunsList } = data
  const nodeRun = nodeRunsList?.[0]

  return (
    <>
      <SelectedNode />
      <Fade fade={fade} status={nodeRun?.status}>
        <DiagramNodeShell id={id} label="Cycle" icon={RefreshCwIcon} theme={blueNodeTheme}>
          <div className="relative">
            <DiagramNodeCircle
              selected={selected}
              theme={blueNodeTheme}
              icon={RefreshCwIcon}
              iconClass="fill-none stroke-blue-600"
            />
            <Handle type="target" position={Position.Right} id="target-0" className="bg-transparent" />
            <Handle type="source" position={Position.Left} id="source-0" className="bg-transparent" />
          </div>
        </DiagramNodeShell>
      </Fade>
    </>
  )
}

export const Cycle = memo(Node)
