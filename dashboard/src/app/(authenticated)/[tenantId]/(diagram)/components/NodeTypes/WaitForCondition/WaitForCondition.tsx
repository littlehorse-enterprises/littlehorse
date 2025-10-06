import { LHStatus, WaitForConditionNode } from 'littlehorse-client/proto'
import { CircleEqualIcon } from 'lucide-react'
import { FC, memo } from 'react'
import { Handle, Position } from 'reactflow'
import { NodeProps } from '..'

import { Fade } from '../Fade'
import { SelectedNode } from '../SelectedNode'

import { Condition } from './Condition'
const Node: FC<NodeProps<'waitForCondition', WaitForConditionNode>> = ({ data }) => {
  const { fade, condition, nodeRunsList } = data
  if (!condition) return null
  const nodeRun = nodeRunsList?.[0]
  // nodeRun.status =   'RUNNING' as LHStatus
  return (
    <>
      <SelectedNode />
      <Fade fade={fade} status={nodeRun?.status}>
        <div className="relative cursor-pointer items-center justify-center text-xs">
          <div className="items-center-justify-center flex rounded-full border-[1px] border-blue-500 bg-blue-200 p-[1px] text-xs">
            <div className="items-center-justify-center flex rounded-full border-[1px] border-blue-500 bg-blue-200 p-2 text-xs">
              <CircleEqualIcon className="h-4 w-4 fill-transparent stroke-blue-500" />
            </div>
          </div>
          <Handle type="source" position={Position.Right} className="h-2 w-2 bg-transparent" />
          <Handle type="target" position={Position.Left} className="bg-transparent" />
          <div className="absolute flex w-full items-center justify-center whitespace-nowrap text-center"></div>
        </div>
      </Fade>
      <Condition {...condition} />
    </>
  )
}

export const WaitForCondition = memo(Node)
