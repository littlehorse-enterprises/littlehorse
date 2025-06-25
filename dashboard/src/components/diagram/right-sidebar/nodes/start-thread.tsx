import { Section } from "../section";
import { Label } from "../label";
import { getVariable } from "@/utils/data/variables";
import { NodeTypedOneOf } from "@/utils/data/node";

export function StartThreadNodeComponent({ startThread }: NodeTypedOneOf<'START_THREAD'>) {
  return (
    <>
      <Section title="StartThreadNode">
        {startThread.threadSpecName && (
          <Label label="Thread Spec" valueClassName="font-mono text-blue-600">{startThread.threadSpecName}</Label>
        )}
      </Section>

      {startThread.variables && Object.keys(startThread.variables).length > 0 && (
        <Section title="Variables">
          <div className="space-y-1">
            {Object.entries(startThread.variables).map(([key, variable]) => (
              <Label key={key} label={key} valueClassName="font-mono text-xs text-blue-600">{getVariable(variable)}</Label>
            ))}
          </div>
        </Section>
      )}
    </>
  )
}
