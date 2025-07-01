import { Section } from '../../section'
import { Label } from '../../label'
import { getVariable } from '@/utils/data/variables'
import { StartMultipleThreadsNode } from 'littlehorse-client/proto'

export function StartMultipleThreadsNodeComponent(startMultipleThreads: StartMultipleThreadsNode) {
  return (
    <>
      <Section title="StartMultipleThreadsNode">
        {startMultipleThreads.threadSpecName && (
          <Label label="ThreadSpec Name" variant="highlight">
            {startMultipleThreads.threadSpecName}
          </Label>
        )}
        {startMultipleThreads.iterable && <Label label="Iterable">{getVariable(startMultipleThreads.iterable)}</Label>}
      </Section>

      {startMultipleThreads.variables && Object.keys(startMultipleThreads.variables).length > 0 && (
        <Section title="Variables">
          <div className="space-y-1">
            {Object.entries(startMultipleThreads.variables).map(([key, variable]) => (
              <Label key={key} label={key}>
                {getVariable(variable)}
              </Label>
            ))}
          </div>
        </Section>
      )}
    </>
  )
}
