import { UserTaskNode } from 'littlehorse-client/proto'
import { UserIcon } from 'lucide-react'
import { FC, memo } from 'react'
import { Handle, Position } from 'reactflow'
import { DiagramNodeCard, DiagramNodeShell } from '../DiagramNodeChrome'
import { Fade } from '../Fade'
import { NodeProps } from '../index'
import { blueNodeTheme } from '../nodeThemes'
import { SelectedNode } from '../SelectedNode'

const Node: FC<NodeProps<'userTask', UserTaskNode>> = ({ id, data, selected }) => {
  const { fade, nodeRunsList, userTaskDefName } = data
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
            <Handle type="source" id="source-0" position={Position.Right} className="bg-transparent" />
            <Handle type="target" id="target-0" position={Position.Left} className="bg-transparent" />
          </div>
        </DiagramNodeShell>
      </Fade>
    </>
  )
}

export const UserTask = memo(Node)
