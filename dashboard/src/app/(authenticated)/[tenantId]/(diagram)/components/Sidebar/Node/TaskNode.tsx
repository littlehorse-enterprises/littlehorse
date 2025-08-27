import { TaskNode as TaskNodeProto } from 'littlehorse-client/proto'
import { FC } from 'react'
import { getTaskName } from '../../NodeTypes/Task/TaskDetails'

export const TaskNode: FC<{ node: TaskNodeProto }> = ({ node }) => {
  return (
    <div className="flex max-w-full flex-1 flex-col">
      <h2 className="text-lg font-semibold">TaskNode</h2>
      <p>
        <strong>Task:</strong> {getTaskName(node.taskToExecute)}
      </p>
    </div>
  )
}
