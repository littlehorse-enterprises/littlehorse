import { LHStatus, TaskStatus } from 'littlehorse-client/proto'
import '../Node/node.css'
import { wfRunStatus } from '../../../StatusColor'

export const NodeStatus = ({ status }: { status: LHStatus | TaskStatus }) => {
  const { color, Icon, textColor } = wfRunStatus[status]

  return (
    <div className="ml-1 mt-2 flex items-center gap-2">
      <div className="fixed "></div>
      <div className={`rounded-full bg-${color}-200 z-10 p-1`}>
        <Icon className={`h-4 w-4 stroke-${color}-500 fill-transparent`} />
      </div>
      <div className={textColor}>{status}</div>
    </div>
  )
}
