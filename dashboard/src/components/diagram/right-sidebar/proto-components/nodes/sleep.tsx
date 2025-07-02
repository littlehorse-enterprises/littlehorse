import { Section } from '../../section'
import { Label } from '../../label'
import { getVariable } from '@/utils/data/variables'
import { SleepNode } from 'littlehorse-client/proto'

export function SleepNodeComponent(sleep: SleepNode) {
  const sleepLengthCase = sleep.sleepLength?.$case
  return (
    <Section title="SleepNode">
      {sleepLengthCase === 'rawSeconds' && (
        <Label label="Duration">{getVariable(sleep.sleepLength?.rawSeconds)} seconds</Label>
      )}
      {sleepLengthCase === 'timestamp' && (
        <Label label="Until Timestamp">{getVariable(sleep.sleepLength?.timestamp)}</Label>
      )}
      {sleepLengthCase === 'isoDate' && <Label label="Until Date">{getVariable(sleep.sleepLength?.isoDate)}</Label>}
    </Section>
  )
}
