import { Node } from 'littlehorse-client/proto'
import { TriangleAlertIcon } from 'lucide-react'
import { ComponentType, FC } from 'react'
import { Handle, NodeProps, Position } from 'reactflow'

type Props = NodeProps<{
  outgoingEdges?: Node['outgoingEdges']
  failureHandlers?: Node['failureHandlers']
  /** The node has exception handlers the diagram isn't showing right now. */
  handlersHidden?: boolean
}>

export const withLoopHandle = (Component: ComponentType<Props>): FC<Props> => {
  const WithLoopHandle: FC<Props> = props => (
    <>
      <Component {...props} />
      {props.data.outgoingEdges?.some(edge => edge.sinkNodeName.startsWith('cycle-')) && (
        <Handle type="source" id="source-loop" position={Position.Bottom} className="bg-transparent" />
      )}
      {(props.data.failureHandlers?.length ?? 0) > 0 && (
        <Handle type="source" id="source-failure" position={Position.Bottom} className="bg-transparent" />
      )}
      {props.data.handlersHidden && (
        <span
          title="Has exception handlers: select the node to show them"
          className="pointer-events-none absolute -right-1.5 -top-1.5 flex h-3.5 w-3.5 items-center justify-center rounded-full border border-rose-300 bg-rose-50"
        >
          <TriangleAlertIcon className="h-2 w-2 stroke-rose-600" strokeWidth={2.5} />
        </span>
      )}
    </>
  )
  return WithLoopHandle
}
