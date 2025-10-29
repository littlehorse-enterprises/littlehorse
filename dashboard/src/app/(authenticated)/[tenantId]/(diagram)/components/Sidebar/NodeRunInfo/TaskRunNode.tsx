import { TaskRunId } from 'littlehorse-client/proto';
import { ClipboardList } from 'lucide-react'

export const TaskRunNode = (node: any) => {
  return (
    <div>
      <div className="flex items-center ml-1">
        <ClipboardList  size={18} />
        <div className='ml-2'>task run node</div>
      </div>
      <div className="ml-1 mt-1 grid grid-cols-2 items-center">
        <label className=" text-bold   text-sm font-bold"> Task Run identifier:</label>
        <span className="text-xs text-slate-400">{node.node.taskRunId.taskGuid}</span>
      </div>
    </div>
  )
}
