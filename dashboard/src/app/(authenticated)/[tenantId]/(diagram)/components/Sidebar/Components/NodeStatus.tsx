import { LHStatus, TaskStatus } from 'littlehorse-client/proto'
import { Expand } from 'lucide-react'
import { useCallback, useState } from 'react'
import { useModal } from '../../../hooks/useModal'
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
  errorMessage,
}: {
  status: LHStatus | TaskStatus
  type?: 'workflow' | 'task'
  errorMessage?: string
}) => {
  const { setModal, setShowModal } = useModal()
  const [isHovered, setIsHovered] = useState(false)

  const onExpand = useCallback(() => {
    if (!errorMessage) return
    setModal({ type: 'output', data: { message: errorMessage, label: 'Error' } })
    setShowModal(true)
  }, [errorMessage, setModal, setShowModal])

  const statusConfig = type === 'workflow' ? WF_RUN_STATUS[status as LHStatus] : TASK_STATUS[status as TaskStatus]
  const { backgroundColor, textColor, Icon } = statusConfig

  return (
    <div
      className="mb-3 flex flex-col gap-2.5 rounded-lg border border-gray-200 bg-gray-50 px-3 py-2.5"
      onMouseEnter={() => setIsHovered(true)}
      onMouseLeave={() => setIsHovered(false)}
    >
      <div className="flex items-center gap-2.5">
        <div className={`flex h-7 w-7 shrink-0 items-center justify-center rounded-full ${backgroundColor}`}>
          <Icon className={`h-4 w-4 ${textColor}`} />
        </div>
        <span className={`text-sm font-semibold ${textColor}`}>{formatStatusText(status)}</span>
      </div>
      {errorMessage && (
        <div className="flex min-w-0 items-center gap-1 border-t border-gray-200 pt-2 text-sm text-red-800">
          <span className="min-w-0 flex-1 truncate font-mono text-xs">{errorMessage}</span>
          {isHovered && (
            <Expand
              className="shrink-0 cursor-pointer text-gray-500 hover:text-gray-700"
              size={14}
              onClick={onExpand}
            />
          )}
        </div>
      )}
    </div>
  )
}
