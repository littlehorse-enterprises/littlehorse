import { Section } from "../section";
import { Label } from "../label";
import { getVariable } from "@/utils/data/variables";
import { NodeForType } from "@/utils/data/node";

export function SleepNodeComponent({ sleep }: NodeForType<'SLEEP'>) {
  return (
    <Section title="SleepNode">
      {sleep.rawSeconds && (
        <Label label="Duration">{getVariable(sleep.rawSeconds)} seconds</Label>
      )}
      {sleep.timestamp && (
        <Label label="Until Timestamp">{getVariable(sleep.timestamp)}</Label>
      )}
      {sleep.isoDate && (
        <Label label="Until Date">{getVariable(sleep.isoDate)}</Label>
      )}
    </Section>
  )
}
