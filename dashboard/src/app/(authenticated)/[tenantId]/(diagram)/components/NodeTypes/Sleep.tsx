import { SleepNode } from 'littlehorse-client/proto'
import { ClockIcon } from 'lucide-react'
import { FC, memo } from 'react'
import { NodeProps } from '.'
import { DiagramNodeCircle, DiagramNodeShell } from './DiagramNodeChrome'
import { DiagramNodeHandles } from './DiagramNodeHandles'
import { grayNodeTheme } from './nodeThemes'
import { Fade } from './Fade'
import { SelectedNode } from './SelectedNode'

const Node: FC<NodeProps<'sleep', SleepNode>> = ({ id, data, selected }) => {
  const { fade, nodeRunsList, sourceHandleCount, targetHandleCount } = data
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

export const Sleep = memo(Node)
