import { Node, UserTaskNode } from "littlehorse-client/proto";

interface UserTaskNodeComponentProps {
  userTaskNode: Node & { userTask: UserTaskNode }
}

export function UserTaskNodeComponent({ userTaskNode }: UserTaskNodeComponentProps) {
  return (
    <div className="space-y-4">
      <div className="rounded-md border border-gray-200 bg-gray-50 p-3">
        <h4 className="mb-2 text-xs font-medium">User Task Properties</h4>
        <div className="space-y-1 text-xs">
          <div className="flex justify-between">
            <span className="text-[#656565]">Type:</span>
            <span className="font-mono">USER_TASK</span>
          </div>
          {userTaskNode.userTask.userTaskDefName && (
            <div className="flex justify-between">
              <span className="text-[#656565]">Task Def:</span>
              <span className="font-mono text-blue-600">{userTaskNode.userTask.userTaskDefName}</span>
            </div>
          )}
          {userTaskNode.userTask.userTaskDefVersion && (
            <div className="flex justify-between">
              <span className="text-[#656565]">Version:</span>
              <span className="font-mono">{userTaskNode.userTask.userTaskDefVersion}</span>
            </div>
          )}
          {userTaskNode.userTask.userGroup && (
            <div className="flex justify-between">
              <span className="text-[#656565]">User Group:</span>
              <span className="font-mono">{JSON.stringify(userTaskNode.userTask.userGroup)}</span>
            </div>
          )}
          {userTaskNode.userTask.userId && (
            <div className="flex justify-between">
              <span className="text-[#656565]">User ID:</span>
              <span className="font-mono">{JSON.stringify(userTaskNode.userTask.userId)}</span>
            </div>
          )}
          {userTaskNode.userTask.notes && (
            <div className="flex justify-between">
              <span className="text-[#656565]">Notes:</span>
              <span className="font-mono">{JSON.stringify(userTaskNode.userTask.notes)}</span>
            </div>
          )}
        </div>
      </div>

      {userTaskNode.userTask.actions && userTaskNode.userTask.actions.length > 0 && (
        <div className="rounded-md border border-gray-200 bg-gray-50 p-3">
          <h4 className="mb-2 text-xs font-medium">Actions</h4>
          <div className="space-y-1 text-xs">
            {userTaskNode.userTask.actions.map((action, index) => (
              <div key={index} className="font-mono">
                <span className="text-purple-600">Action {index + 1}:</span>{' '}
                <span className="text-blue-600">{JSON.stringify(action)}</span>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  )
}
