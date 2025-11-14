import { LHStatus, TaskStatus } from 'littlehorse-client/proto'
import { CircleCheck } from 'lucide-react'
import '../Node/node.css'

export const NodeStatus = ({ status, type }: { status: LHStatus | TaskStatus; type?: string }) => {
  const statusType = type === 'task' ? `task-status--${status.toUpperCase()}` : `node-status--${status.toLowerCase()}`
  return (
    <div className="ml-1 mt-2 flex items-center gap-2">
      <CircleCheck className={statusType} />
      <div className={statusType}>{status}</div>
    </div>
  )
}
