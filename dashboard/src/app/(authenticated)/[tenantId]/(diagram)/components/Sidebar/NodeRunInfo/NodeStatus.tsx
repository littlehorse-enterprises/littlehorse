import { LHStatus } from 'littlehorse-client/proto'
import { wfRunStatusColorText } from '../../../StatusColor'
import { CircleCheck } from 'lucide-react'

export const NodeStatus = ({ status }: { status: LHStatus }) => {
    const getStatusIcon = (status: LHStatus) => {
        switch (status) {
            case LHStatus.COMPLETED:
                return <CircleCheck color="green" />;
            case LHStatus.ERROR:
            case LHStatus.EXCEPTION:
                return <CircleCheck color="red" />;
            case LHStatus.RUNNING:
                return <CircleCheck color="blue" />;
            default:
                return null;
        }
    };
  return (
    <div className="flex items-center gap-2 ml-1">
      {getStatusIcon(status)}
      <span className={`text-${wfRunStatusColorText[status]}  font-semibold`}>{status}</span>
    </div>
  )

}
