'use client'

import { Section } from '../../section'
import { Label } from '../../label'
import { getVariable } from '@/utils/data/variables'
import { TaskNode } from 'littlehorse-client/proto'
import { VariableAssignmentComponent } from '../variable-assignment'

export function TaskNodeComponent(task: TaskNode) {
  return (
    <Section title="TaskNode">
      <Label label="TaskToExecute" variant="highlight">
        {task.taskToExecute?.$case === 'taskDefId'
          ? task.taskToExecute.taskDefId.name
          : getVariable(task.taskToExecute?.dynamicTask)}
      </Label>
      <Label label="Timeout">{`${task.timeoutSeconds} s`}</Label>
      <Label label="Retries">{task.retries}</Label>
      {
        <Section title="ExponentialBackoffRetryPolicy">
          <Label label="MaxDelayMs">{task.exponentialBackoff?.maxDelayMs}</Label>
          <Label label="BaseIntervalMs">{task.exponentialBackoff?.baseIntervalMs}</Label>
          <Label label="Multiplier">{task.exponentialBackoff?.multiplier}</Label>
        </Section>
      }
      {task.variables && Object.keys(task.variables).length > 0 && (
        <Section title="Variables">
          {Object.entries(task.variables).map(([key, variable]) => (
            <VariableAssignmentComponent key={key} {...variable} />
          ))}
        </Section>
      )}
    </Section>
  )
}
