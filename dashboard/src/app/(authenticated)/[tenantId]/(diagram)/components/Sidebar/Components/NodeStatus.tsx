import { LHStatus, TaskStatus } from 'littlehorse-client/proto'
import '../Node/node.css'
import { TASK_STATUS, WF_RUN_STATUS } from './StatusColor'

const formatStatusText = (status: string): string => {
  return status
    .replace(/_/g, ' ')
    .toLowerCase()
    .split(' ')
    .map(word => word.charAt(0).toUpperCase() + word.slice(1))
    .join(' ')
}

export const NodeStatus = ({
  status,
  type = 'workflow',
}: {
  status: LHStatus | TaskStatus
  type?: 'workflow' | 'task'
}) => {
  const statusConfig = type === 'workflow' ? WF_RUN_STATUS[status as LHStatus] : TASK_STATUS[status as TaskStatus]
  const { backgroundColor, textColor, Icon } = statusConfig

  return (
    <div className="mb-3 flex items-center gap-2.5 rounded-lg border border-gray-200 bg-gray-50 px-3 py-2.5">
      <div className={`flex h-7 w-7 shrink-0 items-center justify-center rounded-full ${backgroundColor}`}>
        <Icon className={`h-4 w-4 ${textColor}`} />
      </div>
      <span className={`text-sm font-semibold ${textColor}`}>{formatStatusText(status)}</span>
    </div>
  )
}
