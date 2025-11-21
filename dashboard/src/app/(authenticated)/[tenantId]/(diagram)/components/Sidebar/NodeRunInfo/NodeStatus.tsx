import { LHStatus, TaskStatus } from 'littlehorse-client/proto'
import '../Node/node.css'
import { TASK_STATUS, WF_RUN_STATUS } from '../Components/StatusColor'

export const NodeStatus = ({
  status,
  type = 'workflow',
}: {
  status: LHStatus | TaskStatus
  type?: 'workflow' | 'task'
}) => {
  const { color, Icon, textColor } =
    type === 'workflow' ? WF_RUN_STATUS[status as LHStatus] : TASK_STATUS[status as TaskStatus]
  return (
    <div className="ml-1 mt-2 flex items-center gap-2">
      <div className="fixed "></div>
      <div className={`z-10   bg-${color}-200 rounded-full p-1`}>
        <Icon className={`h-4 w-4 stroke-${color}-500 fill-transparent`} />
      </div>
      <div className={textColor}>{status}</div>
    </div>
  )
}
