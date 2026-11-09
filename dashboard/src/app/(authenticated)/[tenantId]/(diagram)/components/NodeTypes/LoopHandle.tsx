import { Node } from 'littlehorse-client/proto'
import { ComponentType, FC } from 'react'
import { Handle, NodeProps, Position } from 'reactflow'

type Props = NodeProps<{ outgoingEdges?: Node['outgoingEdges'] }>

export const withLoopHandle = (Component: ComponentType<Props>): FC<Props> => {
  const WithLoopHandle: FC<Props> = props => (
    <>
      <Component {...props} />
      {props.data.outgoingEdges?.some(edge => edge.sinkNodeName.startsWith('cycle-')) && (
        <Handle type="source" id="source-loop" position={Position.Bottom} className="bg-transparent" />
      )}
    </>
  )
  return WithLoopHandle
}
