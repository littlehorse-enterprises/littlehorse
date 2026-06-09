import { WaitForConditionNode } from 'littlehorse-client/proto'
import { CircleEqualIcon } from 'lucide-react'
import { FC, memo } from 'react'
import { NodeProps } from '.'
import { DiagramNodeCircle, DiagramNodeShell } from './DiagramNodeChrome'
import { DiagramNodeHandles } from './DiagramNodeHandles'
import { blueNodeTheme } from './nodeThemes'
import { Fade } from './Fade'
import { SelectedNode } from './SelectedNode'

const Node: FC<NodeProps<'waitForCondition', WaitForConditionNode>> = ({ id, data, selected }) => {
  const { fade, nodeCondition, nodeRunsList, sourceHandleCount, targetHandleCount } = data
  if (!nodeCondition) return null
  const nodeRun = nodeRunsList?.[0]

  return (
    <>
      <SelectedNode />
      <Fade fade={fade} status={nodeRun?.status}>
        <DiagramNodeShell id={id} label="Wait For Condition" icon={CircleEqualIcon} theme={blueNodeTheme}>
          <div className="relative">
            <DiagramNodeCircle
              selected={selected}
              theme={blueNodeTheme}
              icon={CircleEqualIcon}
              iconClass="fill-none stroke-blue-600"
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

export const WaitForCondition = memo(Node)
