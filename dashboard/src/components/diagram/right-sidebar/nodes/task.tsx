'use client'

import { Section } from '../section'
import { Label } from '../label'
import { getVariable } from '@/utils/data/variables'
import { NodeTypedOneOf } from '@/utils/data/node'

export function TaskNodeComponent({ task }: NodeTypedOneOf<'TASK'>) {
  return (
    <>
      <Section title="TaskNode">
        <Label label="Timeout">{task.timeoutSeconds && `${task.timeoutSeconds} s`}</Label>
        <Label label="Retries">{task.retries}</Label>
        {task.taskDefId && (
          <Label label="TaskDef" valueClassName="font-mono text-blue-600">{task.taskDefId.name}</Label>
        )}
      </Section>

      {task.variables && Object.keys(task.variables).length > 0 && (
        <Section title="Variables">
          <div className="space-y-1">
            {Object.entries(task.variables).map(([key, variable]) => (
              <Label key={key} label={key} valueClassName="font-mono text-xs text-blue-600">{getVariable(variable)}</Label>
            ))}
          </div>
        </Section>
      )}
    </>
  )
}
