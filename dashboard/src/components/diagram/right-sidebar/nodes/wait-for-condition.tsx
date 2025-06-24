import { Section } from "../section";
import { Label } from "../label";
import { NodeForType } from "@/utils/data/node";

export function WaitForConditionNodeComponent({ waitForCondition }: NodeForType<'WAIT_FOR_CONDITION'>) {
  return (
    <Section title="WaitForConditionNode">
      {waitForCondition.condition && (
        <Label label="Condition">{JSON.stringify(waitForCondition.condition)}</Label>
      )}
    </Section>
  )
} 
