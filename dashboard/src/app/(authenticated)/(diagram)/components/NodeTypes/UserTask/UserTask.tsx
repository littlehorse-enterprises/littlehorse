import { getVariable } from '@/app/utils'
import { UserIcon } from '@heroicons/react/16/solid'
import { ArrowTopRightOnSquareIcon } from '@heroicons/react/24/solid'
import Link from 'next/link'
import { FC, memo } from 'react'
import { Handle, Position } from 'reactflow'
import { NodeProps } from '../index'
import { Fade } from '../Fade'
import { NodeDetails } from '../NodeDetails'
import { UserTaskRunDetails } from '@/app/(authenticated)/(diagram)/components/NodeTypes/UserTask/UserTaskRunDetails'
import { UserTaskDefDetails } from '@/app/(authenticated)/(diagram)/components/NodeTypes/UserTask/UserTaskDefDetails'

const Node: FC<NodeProps> = ({ data, selected }) => {
  if (!data.userTask) return null
  const { fade, userTask, nodeRun } = data

  return (
    <>
      <NodeDetails>
        <div className="">
          <div className="flex items-center items-center gap-1 text-nowrap">
            <h3 className="font-bold">UserTask</h3>
            <Link
              className="flex items-center justify-center gap-1 text-blue-500 hover:underline"
              target="_blank"
              href={`/userTaskDef/${userTask.userTaskDefName}`}
            >
              {userTask.userTaskDefName} <ArrowTopRightOnSquareIcon className="h-4 w-4" />
            </Link>
          </div>
          <div className="mb-2 flex gap-2 text-nowrap">
            {nodeRun ? (
              <UserTaskRunDetails userTask={userTask} nodeRun={nodeRun} />
            ) : (
              <UserTaskDefDetails userTask={userTask} />
            )}
          </div>
          {userTask.notes && (
            <div className="rounded bg-gray-200 p-1">
              <h3 className="mb-1 font-bold">Notes</h3>
              <pre className="overflow-x-auto">{getVariable(userTask.notes)}</pre>
            </div>
          )}
        </div>
      </NodeDetails>
      <Fade fade={fade} status={data.nodeRun?.status}>
        <div
          className={
            'flex cursor-pointer flex-col items-center rounded-md border-[1px] border-blue-500 bg-blue-200 px-2 pt-1 text-xs ' +
            (selected ? 'bg-blue-300' : '')
          }
        >
          <UserIcon className="h-4 w-4 fill-blue-500" />
          {data.userTask?.userTaskDefName}
          <Handle type="source" position={Position.Right} className="bg-transparent" />
          <Handle type="target" position={Position.Left} className="bg-transparent" />
        </div>
      </Fade>
    </>
  )
}

export const UserTask = memo(Node)
