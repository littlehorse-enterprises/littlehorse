import { SleepNode } from 'littlehorse-client/proto'
import { ClockIcon } from 'lucide-react'
import { FC, memo } from 'react'
import { Handle, Position } from 'reactflow'
import { NodeProps } from '.'
import { DiagramNodeCircle, DiagramNodeShell } from './DiagramNodeChrome'
import { Fade } from './Fade'
import { grayNodeTheme } from './nodeThemes'
import { SelectedNode } from './SelectedNode'

const Node: FC<NodeProps<'sleep', SleepNode>> = ({ id, data, selected }) => {
  const { fade, nodeRunsList } = data
  const nodeRun = nodeRunsList?.[0]

  return (
    <>
      <SelectedNode />
      <Fade fade={fade} status={nodeRun?.status}>
        <DiagramNodeShell id={id} label="Sleep" icon={ClockIcon} theme={grayNodeTheme}>
          <div className="relative">
            <DiagramNodeCircle
              selected={selected}
              theme={grayNodeTheme}
              icon={ClockIcon}
              iconClass="fill-none stroke-gray-600"
            />
            <Handle type="target" id="target-0" position={Position.Left} className="bg-transparent" />
            <Handle type="source" id="source-0" position={Position.Right} className="bg-transparent" />
          </div>
        </DiagramNodeShell>
      </Fade>
    </>
  )
}

export const Sleep = memo(Node)
