import { Section } from '../../section'
import { Label } from '../../label'
import { getVariable } from '@/utils/data/variables'
import { WaitForThreadsNode } from 'littlehorse-client/proto'

export function WaitForThreadsNodeComponent(waitForThreads: WaitForThreadsNode) {
  return (
    <Section title="WaitForThreadsNode">
      {waitForThreads.threads && <Label label="Threads">{JSON.stringify(waitForThreads.threads)}</Label>}
      {waitForThreads.threadList && <Label label="Thread List">{getVariable(waitForThreads.threadList)}</Label>}

      {
        // todo: How do we want to handle repeated sections? They could be quite bloated.
        waitForThreads.perThreadFailureHandlers && waitForThreads.perThreadFailureHandlers.length > 0 && (
          <Section title="Failure Handlers">
            <div className="space-y-1 text-xs">
              {waitForThreads.perThreadFailureHandlers.map((handler, index) => (
                <Label key={index} label={`Handler ${index + 1}`} variant="highlight">
                  {JSON.stringify(handler)}
                </Label>
              ))}
            </div>
          </Section>
        )
      }
    </Section>
  )
}
