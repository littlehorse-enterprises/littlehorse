import { UserIcon } from 'lucide-react'
import { UserTaskNode } from 'littlehorse-client/proto'
import { FC, memo } from 'react'
import { DiagramNodeCard, DiagramNodeShell } from '../DiagramNodeChrome'
import { DiagramNodeHandles } from '../DiagramNodeHandles'
import { blueNodeTheme } from '../nodeThemes'
import { Fade } from '../Fade'
import { NodeProps } from '../index'
import { SelectedNode } from '../SelectedNode'

const Node: FC<NodeProps<'userTask', UserTaskNode>> = ({ id, data, selected }) => {
  const { fade, nodeRunsList, userTaskDefName, sourceHandleCount, targetHandleCount } = data
  const nodeRun = nodeRunsList?.[0]

  return (
    <>
      <SelectedNode />
      <Fade fade={fade} status={nodeRun?.status}>
        <DiagramNodeShell id={id} label="User Task" icon={UserIcon} theme={blueNodeTheme}>
          <div className="relative">
            <DiagramNodeCard selected={selected} theme={blueNodeTheme}>
              {userTaskDefName}
            </DiagramNodeCard>
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

export const UserTask = memo(Node)
