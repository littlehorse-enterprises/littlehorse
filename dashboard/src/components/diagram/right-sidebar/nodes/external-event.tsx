import { getVariable } from "@/utils/data/variables";
import { Section } from "../section";
import { Label } from "../label";
import { NodeForType } from "@/utils/data/node";

export function ExternalEventNodeComponent({ externalEvent }: NodeForType<'EXTERNAL_EVENT'>) {
  return (
    <Section title="ExternalEventNode">
      {externalEvent.externalEventDefId && (
        <Label label="EventDef" valueClassName="font-mono text-blue-600">{externalEvent.externalEventDefId.name}</Label>
      )}
      {externalEvent.timeoutSeconds && (
        <Label label="Timeout">{getVariable(externalEvent.timeoutSeconds)} seconds</Label>
      )}
    </Section>
  )
}
