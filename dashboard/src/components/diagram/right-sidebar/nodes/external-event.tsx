import { getVariable } from "@/utils/data/variables";
import { Section } from "../section";
import { Label } from "../label";
import { NodeTypedOneOf } from "@/utils/data/node";

interface ExternalEventNodeComponentProps {
  node: NodeTypedOneOf<'EXTERNAL_EVENT'>
}

export function ExternalEventNodeComponent({ node }: ExternalEventNodeComponentProps) {
  return (
    <Section title="ExternalEventNode">
      {node.externalEvent.externalEventDefId && (
        <Label label="EventDef" valueClassName="font-mono text-blue-600">{node.externalEvent.externalEventDefId.name}</Label>
      )}
      {node.externalEvent.timeoutSeconds && (
        <Label label="Timeout">{getVariable(node.externalEvent.timeoutSeconds)} seconds</Label>
      )}
    </Section>
  )
}
