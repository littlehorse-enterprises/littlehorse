import { Section } from '../../section'
import { Label } from '../../label'
import { getVariable } from '@/utils/data/variables'
import { ThrowEventNode } from 'littlehorse-client/proto'

export function ThrowEventNodeComponent(throwEvent: ThrowEventNode) {
  return (
    <Section title="ThrowEventNode">
      <Label label="WorkflowEventDefIdName">{throwEvent.eventDefId?.name}</Label>
      {throwEvent.content && <Label label="Content">{getVariable(throwEvent.content)}</Label>}
    </Section>
  )
}
