import { Section } from "../section";
import { Label } from "../label";
import { getVariable } from "@/utils/data/variables";
import { NodeForType } from "@/utils/data/node";

export function UserTaskNodeComponent({ userTask }: NodeForType<'USER_TASK'>) {
  return (
    <>
      <Section title="UserTaskNode">
        {userTask.userTaskDefName && (
          <Label label="TaskDef" valueClassName="font-mono text-blue-600">{userTask.userTaskDefName}</Label>
        )}
        {userTask.userTaskDefVersion && (
          <Label label="Version">{userTask.userTaskDefVersion}</Label>
        )}
        {userTask.userGroup && (
          <Label label="User Group">{getVariable(userTask.userGroup)}</Label>
        )}
        {userTask.userId && (
          <Label label="User ID">{getVariable(userTask.userId)}</Label>
        )}
        {userTask.notes && (
          <Label label="Notes">{getVariable(userTask.notes)}</Label>
        )}
      </Section>

      {userTask.actions && userTask.actions.length > 0 && (
        <Section title="Actions">
          <div className="space-y-1 text-xs">
            {userTask.actions.map((action, index) => (
              <div key={index} className="font-mono">
                <span className="text-purple-600">Action {index + 1}:</span>{' '}
                <span className="text-blue-600">{JSON.stringify(action)}</span>
              </div>
            ))}
          </div>
        </Section>
      )}
    </>
  )
}
