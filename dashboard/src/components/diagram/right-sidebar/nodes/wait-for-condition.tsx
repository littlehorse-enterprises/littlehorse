import { Section } from "../section";
import { Label } from "../label";
import { WaitForConditionNode } from "littlehorse-client/proto";

export function WaitForConditionNodeComponent(waitForCondition: WaitForConditionNode) {
  return (
    <Section title="WaitForConditionNode">
      {waitForCondition.condition && (
        <Label label="Condition">{JSON.stringify(waitForCondition.condition)}</Label>
      )}
    </Section>
  )
} 
