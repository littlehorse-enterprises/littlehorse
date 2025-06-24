import { Section } from "../section";
import { Label } from "../label";
import { getVariable } from "@/utils/data/variables";
import { NodeForType } from "@/utils/data/node";

export function ThrowEventNodeComponent({ throwEvent }: NodeForType<'THROW_EVENT'>) {
  return (
    <Section title="ThrowEventNode">
      {throwEvent.eventDefId && (
        <Label label="EventDef" valueClassName="font-mono text-blue-600">{throwEvent.eventDefId.name}</Label>
      )}
      {throwEvent.content && (
        <Label label="Content">{getVariable(throwEvent.content)}</Label>
      )}
    </Section>
  )
} 
