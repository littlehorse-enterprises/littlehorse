import { Section } from '../../section'
import { Label } from '../../label'
import { getVariable } from '@/utils/data/variables'
import { SleepNode } from 'littlehorse-client/proto'

export function SleepNodeComponent(sleep: SleepNode) {
  return (
    <Section title="SleepNode">
      {sleep.rawSeconds && <Label label="Duration">{getVariable(sleep.rawSeconds)} seconds</Label>}
      {sleep.timestamp && <Label label="Until Timestamp">{getVariable(sleep.timestamp)}</Label>}
      {sleep.isoDate && <Label label="Until Date">{getVariable(sleep.isoDate)}</Label>}
    </Section>
  )
}
