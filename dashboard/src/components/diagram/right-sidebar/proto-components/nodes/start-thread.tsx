import { Section } from "../../section";
import { Label } from "../../label";
import { getVariable } from "@/utils/data/variables";
import { StartThreadNode } from "littlehorse-client/proto";

export function StartThreadNodeComponent(startThread: StartThreadNode) {
  return (
    <>
      <Section title="StartThreadNode">
        {startThread.threadSpecName && (
          <Label label="ThreadSpec Name" variant="highlight">{startThread.threadSpecName}</Label>
        )}
      </Section>

      {startThread.variables && Object.keys(startThread.variables).length > 0 && (
        <Section title="Variables">
          {Object.entries(startThread.variables).map(([key, variable]) => (
            <Label key={key} label={key}>{getVariable(variable)}</Label>
          ))}
        </Section>
      )}
    </>
  )
}
