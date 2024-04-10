import { getVariable } from '@/app/utils'
import { ArrowTopRightOnSquareIcon, Cog6ToothIcon } from '@heroicons/react/16/solid'
import { Node as NodeProto } from 'littlehorse-client/dist/proto/wf_spec'
import Link from 'next/link'
import { FC, memo } from 'react'
import { Handle, Position } from 'reactflow'
import { NodeProps } from '.'
import { Fade } from './Fade'
import { NodeDetails } from './NodeDetails'

const Node: FC<NodeProps<NodeProto>> = ({ selected, data }) => {
  const { fade } = data
  return (
    <Fade fade={fade} status={data.nodeRun?.status}>
      <NodeDetails>
        <div className="mb-2 flex gap-1 text-nowrap">
          <h3 className="font-bold">TaskDef</h3>
          <Link
            className="flex items-center justify-center gap-1 text-blue-500 hover:underline"
            target="_blank"
            href={`/taskDef/${data.task?.taskDefId?.name}`}
          >
            {data.task?.taskDefId?.name} <ArrowTopRightOnSquareIcon className="h-4 w-4" />
          </Link>
        </div>
        {data.task?.variables && data.task?.variables.length > 0 && (
          <div className="">
            <h3 className="font-bold">Inputs</h3>
            <ul className="list-inside list-disc">
              {data.task.variables.map((variable, i) => (
                <li key={`variable.${i}`}>{getVariable(variable)}</li>
              ))}
            </ul>
          </div>
        )}
        {data.nodeRun && <div></div>}
      </NodeDetails>
      <div
        className={
          'flex cursor-pointer flex-col items-center rounded-md border-[1px] border-orange-500 bg-orange-200 px-2 pt-1 text-xs' +
          (selected ? ' bg-orange-300' : '')
        }
      >
        <Cog6ToothIcon className="h-4 w-4 fill-orange-500" />
        {data.task?.taskDefId?.name}
        <Handle type="source" position={Position.Right} className="bg-transparent" />
        <Handle type="target" position={Position.Left} className="bg-transparent" />
      </div>
    </Fade>
  )
}

export const Task = memo(Node)
