import { UserIcon } from 'lucide-react'

import { UserTaskNode } from 'littlehorse-client/proto'
import { FC, memo } from 'react'
import { Handle, Position } from 'reactflow'
import { Fade } from '../Fade'
import { NodeProps } from '../index'
import { SelectedNode } from '../SelectedNode'

const Node: FC<NodeProps<'userTask', UserTaskNode>> = ({ data }) => {
  const { fade, nodeRunsList, userTaskDefName } = data
  const nodeRun = nodeRunsList?.[0]

  return (
    <>
      <SelectedNode />
      <Fade fade={fade} status={nodeRun?.status}>
        <div className="flex cursor-pointer flex-col items-center rounded-md border-[1px] border-blue-500 bg-blue-200 px-2 pt-1 text-xs ">
          <UserIcon className="h-4 w-4 text-blue-500" />
          {userTaskDefName}
          <Handle type="source" position={Position.Right} className="bg-transparent" />
          <Handle type="target" position={Position.Left} className="bg-transparent" />
        </div>
      </Fade>
    </>
  )
}

export const UserTask = memo(Node)
