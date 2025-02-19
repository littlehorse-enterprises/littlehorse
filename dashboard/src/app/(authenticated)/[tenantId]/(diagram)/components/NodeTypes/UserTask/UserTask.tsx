import { UserTaskDefDetails } from '@/app/(authenticated)/[tenantId]/(diagram)/components/NodeTypes/UserTask/UserTaskDefDetails'
import { UserIcon } from 'lucide-react'
import { useParams, useRouter } from 'next/navigation'
import { FC, memo } from 'react'
import { Handle, Position } from 'reactflow'
import { ExternalLinkButton } from '../../ExternalLinkButton'
import { NodeRunsList } from '../../NodeRunsList'
import { DiagramDataGroup } from '../DataGroupComponents/DiagramDataGroup'
import { Fade } from '../Fade'
import { NodeProps } from '../index'
import { NodeDetails } from '../NodeDetails'

const Node: FC<NodeProps> = ({ data, selected }) => {
  const router = useRouter()
  const tenantId = useParams().tenantId as string

  if (!data.userTask) return null
  const { fade, userTask, nodeRun, nodeNeedsToBeHighlighted, nodeRunsList } = data

  return (
    <>
      <NodeDetails nodeRunList={data.nodeRunsList}>
        <DiagramDataGroup label={nodeRun ? "UserTaskRun" : "UserTaskDef"}>
          <div className="flex flex-col">
            <ExternalLinkButton href={`/userTaskDef/${userTask.userTaskDefName}`} label={userTask.userTaskDefName} />
            {nodeRun && (
              <ExternalLinkButton
                href={`/userTaskDef/audit/${nodeRun.id?.wfRunId?.id}/${nodeRun.userTask?.userTaskRunId?.userTaskGuid}`}
                label="Audit Log"
              />
            )}

            {nodeRun ? (
              <NodeRunsList nodeRuns={nodeRunsList} userTaskNode={userTask} nodeRun={nodeRun} />
            ) : (
              <UserTaskDefDetails userTask={userTask} />
            )}
          </div>
        </DiagramDataGroup>
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
