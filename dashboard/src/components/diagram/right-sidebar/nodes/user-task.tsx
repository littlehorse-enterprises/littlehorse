import { Section } from "../section";
import { Label } from "../label";
import { UserTaskNode } from "littlehorse-client/proto";
import { getVariable } from "@/utils/data/variables";
import { TaskNodeComponent } from "./task";
import { TaskNode } from "littlehorse-client/proto";

export function UserTaskNodeComponent(userTask: UserTaskNode) {
  return (
    <Section title="UserTaskNode">
      <Label label="UserTaskDefName">{userTask.userTaskDefName}</Label>
      {userTask.userGroup && <Label label="UserGroup">{getVariable(userTask.userGroup)}</Label>}
      {userTask.userId && <Label label="UserId">{getVariable(userTask.userId)}</Label>}
      {userTask.userTaskDefVersion && <Label label="UserTaskDefVersion">{userTask.userTaskDefVersion}</Label>}
      {userTask.notes && <Label label="Notes">{getVariable(userTask.notes)}</Label>}
      {userTask.onCancellationExceptionName && <Label label="OnCancellationExceptionName">{getVariable(userTask.onCancellationExceptionName)}</Label>}
      <Section title="Actions">
        {userTask.actions.map((action, index) => (
          <Section key={JSON.stringify(action) + index} title={`UTActionTrigger ${index + 1}`}>
            {action.task && <Section title="UTATask">
              <TaskNodeComponent {...(action.task.task as TaskNode)} />
              {/* // todo : util component for VariableMutations to render into a section */}
              <Section title="Mutations">
              </Section>
            </Section>}
          </Section>
        ))}
      </Section>
    </Section>


  )
}
