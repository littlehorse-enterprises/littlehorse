import { Section } from "../section";
import { NodeForType } from "@/utils/data/node";

export function NopNodeComponent({ }: NodeForType<'NOP'>) {
  return <Section title="NopNode" />
}
