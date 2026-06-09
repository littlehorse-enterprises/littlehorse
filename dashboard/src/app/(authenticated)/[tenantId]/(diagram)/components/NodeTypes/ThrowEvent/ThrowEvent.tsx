import { ThrowEventNode } from 'littlehorse-client/proto'
import { CircleArrowOutUpRightIcon } from 'lucide-react'
import { FC, memo } from 'react'
import { NodeProps } from '..'
import { DiagramNodeCircle, DiagramNodeShell } from '../DiagramNodeChrome'
import { DiagramNodeHandles } from '../DiagramNodeHandles'
import { purpleNodeTheme } from '../nodeThemes'
import { Fade } from '../Fade'
import { SelectedNode } from '../SelectedNode'

const Node: FC<NodeProps<'throwEvent', ThrowEventNode>> = ({ id, data, selected }) => {
  const { fade, eventDefId, nodeRunsList, sourceHandleCount, targetHandleCount } = data
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
            <DiagramNodeHandles
              sourceCount={sourceHandleCount ?? 1}
              targetCount={targetHandleCount ?? 1}
            />
          </div>
        </DiagramNodeShell>
      </Fade>
    </>
  )
}

export const ThrowEvent = memo(Node)
