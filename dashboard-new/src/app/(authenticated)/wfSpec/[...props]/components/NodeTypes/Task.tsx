import { getVariable } from '@/app/utils'
import { Cog6ToothIcon } from '@heroicons/react/16/solid'
import { Node as NodeProto } from 'littlehorse-client/dist/proto/wf_spec'
import { FC, memo } from 'react'
import { Handle, NodeToolbar, Position } from 'reactflow'
import { NodeProps } from '.'
import { Fade } from './Fade'
import { NodeDetails } from './NodeDetails'

const Node: FC<NodeProps<NodeProto>> = ({ selected, data }) => {
  const { fade } = data
  return (
    <Fade fade={fade} status={data.nodeRun?.status}>
      <NodeDetails>
        <div className="max-w-96 overflow-hidden rounded-md bg-white p-2 text-xs">
          Task Def {data.task?.taskDefId?.name}
          <br />
          {JSON.stringify(data.task)}
          <br />
          {JSON.stringify(data.nodeRun)}
          {data.task?.variables.map((variable, i) => <div key={`variable.${i}`}>{getVariable(variable)}</div>)}
        </div>
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
        <Handle
          type="target"
          position={Position.Left}
          className="bg-transparent"
        />
      </div>
    </Fade>
  )
}

export const Task = memo(Node)
