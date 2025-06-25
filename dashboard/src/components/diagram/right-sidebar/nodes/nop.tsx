import { Section } from "../section";
import { NodeTypedOneOf } from "@/utils/data/node";

export function NopNodeComponent({ }: NodeTypedOneOf<'NOP'>) {
  return <Section title="NopNode" />
}
