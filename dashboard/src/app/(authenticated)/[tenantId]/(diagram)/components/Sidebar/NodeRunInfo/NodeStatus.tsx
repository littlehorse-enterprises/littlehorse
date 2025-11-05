import { LHStatus } from 'littlehorse-client/proto'
import { CircleCheck } from 'lucide-react'
import '../Node/node.css'
export const NodeStatus = ({ status }: { status: LHStatus }) => {

  return (
    <div className="ml-1 flex items-center gap-2">
      <CircleCheck   className={`node-status--${status.toLowerCase()}`} />
      <div className={` node-status--${status.toLowerCase()} font-semibold `}>{status}</div>
    </div>
  )
}
