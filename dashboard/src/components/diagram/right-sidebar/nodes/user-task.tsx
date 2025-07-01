import { Node, UserTaskNode } from "littlehorse-client/proto";
import { BaseNodeComponent } from "./base-node";
import { getVariable } from "@/utils/data/variables";

interface UserTaskNodeComponentProps {
  userTaskNode: Node & { userTask: UserTaskNode }
}

export function UserTaskNodeComponent({ userTaskNode }: UserTaskNodeComponentProps) {
  const mainContent = (
    <>
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
          <span className="font-mono">{getVariable(userTaskNode.userTask.userGroup)}</span>
        </div>
      )}
      {userTaskNode.userTask.userId && (
        <div className="flex justify-between">
          <span className="text-[#656565]">User ID:</span>
          <span className="font-mono">{getVariable(userTaskNode.userTask.userId)}</span>
        </div>
      )}
      {userTaskNode.userTask.notes && (
        <div className="flex justify-between">
          <span className="text-[#656565]">Notes:</span>
          <span className="font-mono">{getVariable(userTaskNode.userTask.notes)}</span>
        </div>
      )}
    </>
  );

  const additionalSections = userTaskNode.userTask.actions && userTaskNode.userTask.actions.length > 0 ? [
    {
      title: "Actions",
      content: (
        <div className="space-y-1 text-xs">
          {userTaskNode.userTask.actions.map((action, index) => (
            <div key={index} className="font-mono">
              <span className="text-purple-600">Action {index + 1}:</span>{' '}
              <span className="text-blue-600">{JSON.stringify(action)}</span>
            </div>
          ))}
        </div>
      )
    }
  ] : undefined;

  return (
    <BaseNodeComponent
      title="User Task Properties"
      type="USER_TASK"
      additionalSections={additionalSections}
    >
      {mainContent}
    </BaseNodeComponent>
  )
}
