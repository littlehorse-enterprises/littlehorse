import { LHStatus } from 'littlehorse-client/proto'
import { FC, PropsWithChildren } from 'react'
import { useNodeId } from 'reactflow'
import { useDiagram } from '../../hooks/useDiagram'
import { StatusPin } from './StatusPin'

type Props = PropsWithChildren<{ fade?: boolean; status?: LHStatus }>
export const Fade: FC<Props> = ({ fade, status, children }) => {
  const nodeId = useNodeId()
  const { failedNodeId } = useDiagram()
  const isFailedNode = failedNodeId === nodeId
  const displayStatus = status ?? (isFailedNode ? LHStatus.ERROR : undefined)

  return (
    <div className="relative">
      <StatusPin status={displayStatus} />
      <div className={'relative ' + (fade ? 'opacity-25' : 'opacity-100')}>{children}</div>
    </div>
  )
}
