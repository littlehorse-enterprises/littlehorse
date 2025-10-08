import { ThrowEventNode } from 'littlehorse-client/proto'
import { CircleArrowOutUpRightIcon } from 'lucide-react'
import { FC, memo } from 'react'
import { Handle, Position } from 'reactflow'
import { NodeProps } from '..'

import { Fade } from '../Fade'
import { SelectedNode } from '../SelectedNode'

const Node: FC<NodeProps<'throwEvent', ThrowEventNode>> = ({ data }) => {
  const { fade, eventDefId, nodeRunsList } = data
  if (!eventDefId) return null
  const nodeRun = nodeRunsList?.[0]

  return (
    <>
      <SelectedNode />
      <Fade fade={fade} status={nodeRun?.status}>
        <div className="relative cursor-pointer items-center justify-center text-xs">
          <div className="items-center-justify-center flex rounded-full border-[1px] border-purple-500 bg-purple-200 p-[1px] text-xs">
            <div className="items-center-justify-center flex rounded-full border-[1px] border-purple-500 bg-purple-200 p-2 text-xs">
              <CircleArrowOutUpRightIcon className="h-4 w-4 fill-transparent stroke-purple-500" />
            </div>
          </div>
          <Handle type="source" position={Position.Right} className="h-2 w-2 bg-transparent" />
          <Handle type="target" position={Position.Left} className="bg-transparent" />
          <div className="absolute flex w-full items-center justify-center whitespace-nowrap text-center">
            <div className="block">{eventDefId.name}</div>
          </div>
        </div>
      </Fade>
    </>
  )
}

export const ThrowEvent = memo(Node)
