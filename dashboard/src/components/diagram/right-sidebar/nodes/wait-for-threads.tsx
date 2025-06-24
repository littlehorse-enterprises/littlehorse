import { Section } from "../section";
import { Label } from "../label";
import { getVariable } from "@/utils/data/variables";
import { NodeForType } from "@/utils/data/node";

export function WaitForThreadsNodeComponent({ waitForThreads }: NodeForType<'WAIT_FOR_THREADS'>) {
  return (
    <>
      <Section title="WaitForThreadsNode">
        {waitForThreads.threads && (
          <Label label="Threads">{JSON.stringify(waitForThreads.threads)}</Label>
        )}
        {waitForThreads.threadList && (
          <Label label="Thread List">{getVariable(waitForThreads.threadList)}</Label>
        )}
      </Section>

      {waitForThreads.perThreadFailureHandlers && waitForThreads.perThreadFailureHandlers.length > 0 && (
        <Section title="Failure Handlers">
          <div className="space-y-1 text-xs">
            {waitForThreads.perThreadFailureHandlers.map((handler, index) => (
              <Label key={index} label={`Handler ${index + 1}`} valueClassName="font-mono text-blue-600">{JSON.stringify(handler)}</Label>
            ))}
          </div>
        </Section>
      )}
    </>
  )
}
