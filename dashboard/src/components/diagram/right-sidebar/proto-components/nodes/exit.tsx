import { getVariable } from "@/utils/data/variables";
import { Section } from "../../section";
import { Label } from "../../label";
import { ExitNode } from "littlehorse-client/proto";
import TestSection from "../../test-section";

export function ExitNodeComponent(exit: ExitNode) {
  return (
    // <Section title="ExitNode">
    //   <Label label="Failure Name" variant="highlight">{exit.failureDef?.failureName}</Label>
    //   <Label label="Message">{exit.failureDef?.message}</Label>
    //   {exit.failureDef?.content && (
    //     <Label label="Content">{getVariable(exit.failureDef.content)}</Label>
    //   )}
    // </Section>
    <TestSection />
  )
}
