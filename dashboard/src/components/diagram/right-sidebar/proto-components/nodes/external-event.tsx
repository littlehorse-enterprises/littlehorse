import { getVariable } from "@/utils/data/variables";
import { Section } from "../../section";
import { Label } from "../../label";
import { ExternalEventNode } from "littlehorse-client/proto";

export function ExternalEventNodeComponent(externalEvent: ExternalEventNode) {
  return (
    <Section title="ExternalEventNode">
      <Label label="ExternalEventDefIdName">{externalEvent.externalEventDefId?.name}</Label>
      {externalEvent.correlationKey && (
        <Label label="Correlation Key" variant="highlight">{getVariable(externalEvent.correlationKey)}</Label>
      )}
      {externalEvent.timeoutSeconds && (
        <Label label="Timeout">{getVariable(externalEvent.timeoutSeconds)} seconds</Label>
      )}
    </Section>
  )
}
