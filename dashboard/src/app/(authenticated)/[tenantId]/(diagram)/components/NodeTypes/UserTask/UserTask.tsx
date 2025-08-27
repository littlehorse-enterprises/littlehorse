import { UserTaskDefDetails } from '@/app/(authenticated)/[tenantId]/(diagram)/components/NodeTypes/UserTask/UserTaskDefDetails'
import { UserIcon } from 'lucide-react'

import { UserTaskNode } from 'littlehorse-client/proto'
import { FC, memo } from 'react'
import { Handle, Position } from 'reactflow'
import { ExternalLinkButton } from '../../ExternalLinkButton'
import { NodeRunsList } from '../../NodeRunsList'
import { DiagramDataGroup } from '../DataGroupComponents/DiagramDataGroup'
import { Fade } from '../Fade'
import { NodeProps } from '../index'
import { SelectedNode } from '../SelectedNode'

const Node: FC<NodeProps<'userTask', UserTaskNode>> = ({ data, selected }) => {
  const { fade, nodeRun, nodeNeedsToBeHighlighted, nodeRunsList, ...userTaskNode } = data

  return (
    <>
      <SelectedNode nodeRunList={data.nodeRunsList}>
        <DiagramDataGroup label={nodeRun ? 'UserTaskRun' : 'UserTaskDef'}>
          <div className="flex flex-col">
            <ExternalLinkButton
              href={`/userTaskDef/${userTaskNode.userTaskDefName}`}
              label={userTaskNode.userTaskDefName}
            />
            {nodeRun && (
              <ExternalLinkButton
                href={`/userTaskDef/audit/${nodeRun.id?.wfRunId?.id}/${nodeRun.nodeType.value.userTaskRunId?.userTaskGuid}`}
                label="Audit Log"
              />
            )}

            {nodeRun ? (
              <NodeRunsList nodeRuns={nodeRunsList} userTaskNode={userTaskNode} nodeRun={nodeRun} />
            ) : (
              <UserTaskDefDetails userTask={userTaskNode} />
            )}
          </div>
        </DiagramDataGroup>
      </SelectedNode>
      <Fade fade={fade} status={nodeRunsList?.[nodeRunsList.length - 1]?.status}>
        <div
          className={
            'flex cursor-pointer flex-col items-center rounded-md border-[1px] border-blue-500 bg-blue-200 px-2 pt-1 text-xs ' +
            (selected ? 'bg-blue-300' : '') +
            (nodeNeedsToBeHighlighted ? ' shadow-lg shadow-blue-500' : '')
          }
        >
          <UserIcon className="h-4 w-4 text-blue-500" />
          {data.userTaskDefName}
          <Handle type="source" position={Position.Right} className="bg-transparent" />
          <Handle type="target" position={Position.Left} className="bg-transparent" />
        </div>
      </Fade>
    </>
  )
}

export const UserTask = memo(Node)
