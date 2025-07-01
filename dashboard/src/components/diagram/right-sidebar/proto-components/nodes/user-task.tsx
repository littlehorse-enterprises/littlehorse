import { Section } from '../../section'
import { Label } from '../../label'
import { UserTaskNode } from 'littlehorse-client/proto'
import { getVariable } from '@/utils/data/variables'
import { TaskNodeComponent } from './task'
import VariableMutationComponent from '../variable-mutation'

export function UserTaskNodeComponent(userTask: UserTaskNode) {
  return (
    <Section title="UserTaskNode">
      <Label label="UserTaskDefName">{userTask.userTaskDefName}</Label>
      {userTask.userGroup && <Label label="UserGroup">{getVariable(userTask.userGroup)}</Label>}
      {userTask.userId && <Label label="UserId">{getVariable(userTask.userId)}</Label>}
      {userTask.userTaskDefVersion && <Label label="UserTaskDefVersion">{userTask.userTaskDefVersion}</Label>}
      {userTask.notes && <Label label="Notes">{getVariable(userTask.notes)}</Label>}
      {userTask.onCancellationExceptionName && (
        <Label label="OnCancellationExceptionName">{getVariable(userTask.onCancellationExceptionName)}</Label>
      )}
      <Section title="Actions">
        {userTask.actions.map((actionTrigger, index) => (
          <Section key={JSON.stringify(actionTrigger) + index} title={`UTActionTrigger ${index + 1}`}>
            {actionTrigger.action?.$case === 'task' && (
              <Section title="UTATask">
                {actionTrigger.action.task.task && <TaskNodeComponent {...actionTrigger.action.task.task} />}
                <Section title="Mutations">
                  {actionTrigger.action.task.mutations.map((mutation, index) => (
                    <VariableMutationComponent key={JSON.stringify(mutation) + index} {...mutation} />
                  ))}
                </Section>
              </Section>
            )}
          </Section>
        ))}
      </Section>
    </Section>
  )
}
