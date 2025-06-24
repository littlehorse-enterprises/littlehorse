import { getVariable } from "@/utils/data/variables";
import { Section } from "../section";
import { Label } from "../label";
import { NodeForType } from "@/utils/data/node";

export function ExitNodeComponent({ exit }: NodeForType<'EXIT'>) {
  return (
    <>
      <Section title="ExitNode">
        <Label label="Failure Name" valueClassName="font-mono text-red-600">{exit.failureDef?.failureName}</Label>
        <Label label="Message">{exit.failureDef?.message}</Label>
        {exit.failureDef?.content && (
          <Label label="Content">{getVariable(exit.failureDef.content)}</Label>
        )}
      </Section>
    </>
  )
}
