import { ThrowEventNode } from 'littlehorse-client/proto'
import { CircleArrowOutUpRightIcon } from 'lucide-react'
import { FC, memo } from 'react'
import { Handle, Position } from 'reactflow'
import { NodeProps } from '..'
import { DiagramNodeCircle, DiagramNodeShell } from '../DiagramNodeChrome'
import { Fade } from '../Fade'
import { purpleNodeTheme } from '../nodeThemes'
import { SelectedNode } from '../SelectedNode'

const Node: FC<NodeProps<'throwEvent', ThrowEventNode>> = ({ id, data, selected }) => {
  const { fade, eventDefId, nodeRunsList } = data
  if (!eventDefId) return null
  const nodeRun = nodeRunsList?.[0]

  return (
    <>
      <SelectedNode />
      <Fade fade={fade} status={nodeRun?.status}>
        <DiagramNodeShell
          id={id}
          label="Throw Event"
          icon={CircleArrowOutUpRightIcon}
          theme={purpleNodeTheme}
          subtitle={eventDefId.name}
        >
          <div className="relative">
            <DiagramNodeCircle
              selected={selected}
              theme={purpleNodeTheme}
              icon={CircleArrowOutUpRightIcon}
              iconClass="fill-none stroke-purple-600"
            />
            <Handle type="source" id="source-0" position={Position.Right} className="bg-transparent" />
            <Handle type="target" id="target-0" position={Position.Left} className="bg-transparent" />
          </div>
        </DiagramNodeShell>
      </Fade>
    </>
  )
}

export const ThrowEvent = memo(Node)
