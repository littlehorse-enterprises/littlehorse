import { Node } from 'littlehorse-client/proto'
import { FC, memo } from 'react'
import { Handle, Position } from 'reactflow'
import { NodeProps } from '..'
import { Fade } from '../Fade'
import { SelectedNode } from '../SelectedNode'

const NopNode: FC<NodeProps<'entrypoint', Node>> = props => {
  const { data } = props

  const { fade, nodeRunsList } = data
  const nodeRun = nodeRunsList?.[0]
  return (
    <>
      <SelectedNode />
      <Fade fade={fade} status={nodeRun?.status}>
        <div className="flex">
          <div className="cursor-pointer1 relative grid h-8 w-8 place-items-center">
            <div className="absolute inset-0 bg-gray-400 [clip-path:polygon(50%_0,100%_50%,50%_100%,0_50%)]"></div>

            <div className="absolute inset-[1px] bg-gray-200 [clip-path:polygon(50%_0,100%_50%,50%_100%,0_50%)]"></div>
          </div>
          <Handle type="target" position={Position.Left} id="target-0" className="bg-transparent" />
          <Handle type="source" position={Position.Right} id="source-0" className="bg-transparent" />
        </div>
      </Fade>
    </>
  )
}

export const Nop = memo(NopNode)
