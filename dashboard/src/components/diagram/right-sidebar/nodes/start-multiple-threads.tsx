import { Section } from "../section";
import { Label } from "../label";
import { getVariable } from "@/utils/data/variables";
import { NodeTypedOneOf } from "@/utils/data/node";

export function StartMultipleThreadsNodeComponent({ startMultipleThreads }: NodeTypedOneOf<'START_MULTIPLE_THREADS'>) {
  return (
    <>
      <Section title="StartMultipleThreadsNode">
        {startMultipleThreads.threadSpecName && (
          <Label label="Thread Spec" valueClassName="font-mono text-blue-600">{startMultipleThreads.threadSpecName}</Label>
        )}
        {startMultipleThreads.iterable && (
          <Label label="Iterable">{getVariable(startMultipleThreads.iterable)}</Label>
        )}
      </Section>

      {startMultipleThreads.variables && Object.keys(startMultipleThreads.variables).length > 0 && (
        <Section title="Variables">
          <div className="space-y-1">
            {Object.entries(startMultipleThreads.variables).map(([key, variable]) => (
              <Label key={key} label={key} valueClassName="font-mono text-xs text-blue-600">{getVariable(variable)}</Label>
            ))}
          </div>
        </Section>
      )}
    </>
  )
}
