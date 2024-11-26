import { UserTaskDefDetails } from '@/app/[tenantId]/(diagram)/components/NodeTypes/UserTask/UserTaskDefDetails'
import LinkWithTenant from '@/app/[tenantId]/components/LinkWithTenant'
import { ExternalLinkIcon, UserIcon } from 'lucide-react'
import { FC, memo } from 'react'
import { Handle, Position } from 'reactflow'
import { NodeRunsList } from '../../NodeRunsList'
import { Fade } from '../Fade'
import { NodeProps } from '../index'
import { NodeDetails } from '../NodeDetails'

const Node: FC<NodeProps> = ({ data, selected }) => {
  if (!data.userTask) return null
  const { fade, userTask, nodeRun, nodeNeedsToBeHighlighted, nodeRunsList } = data

  return (
    <>
      <NodeDetails>
        <div className="">
          <div className="flex items-center gap-1 text-nowrap">
            <h3 className="font-bold">UserTask</h3>
            <LinkWithTenant
              className="flex items-center justify-center gap-1 text-blue-500 hover:underline"
              target="_blank"
              href={`/userTaskDef/${userTask.userTaskDefName}`}
            >
              {userTask.userTaskDefName} <ExternalLinkIcon className="h-4 w-4" />
            </LinkWithTenant>
          </div>
          {nodeRun ? (
            <NodeRunsList nodeRuns={nodeRunsList} userTaskNode={userTask} nodeRun={nodeRun} />
          ) : (
            <UserTaskDefDetails userTask={userTask} />
          )}
        </div>
      </NodeDetails>
      <Fade fade={fade} status={nodeRunsList?.[nodeRunsList.length - 1]?.status}>
        <div
          className={
            'flex cursor-pointer flex-col items-center rounded-md border-[1px] border-blue-500 bg-blue-200 px-2 pt-1 text-xs ' +
            (selected ? 'bg-blue-300' : '') +
            (nodeNeedsToBeHighlighted ? ' shadow-lg shadow-blue-500' : '')
          }
        >
          <UserIcon className="h-4 w-4 text-blue-500" />
          {data.userTask?.userTaskDefName}
          <Handle type="source" position={Position.Right} className="bg-transparent" />
          <Handle type="target" position={Position.Left} className="bg-transparent" />
        </div>
      </Fade>
    </>
  )
}

export const UserTask = memo(Node)
