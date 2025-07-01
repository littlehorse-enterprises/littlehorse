import { Node, SleepNode } from "littlehorse-client/proto";
import { BaseNodeComponent } from "./base-node";
import { getVariable } from "@/utils/data/variables";

interface SleepNodeComponentProps {
  sleepNode: Node & { sleep: SleepNode }
}

export function SleepNodeComponent({ sleepNode }: SleepNodeComponentProps) {
  const mainContent = (
    <>
      {sleepNode.sleep.sleepLength?.$case === 'rawSeconds' && (
        <div className="flex justify-between">
          <span className="text-[#656565]">Duration:</span>
          <span className="font-mono">{getVariable(sleepNode.sleep.sleepLength.rawSeconds)} seconds</span>
        </div>
      )}
      {sleepNode.sleep.sleepLength?.$case === 'timestamp' && (
        <div className="flex justify-between">
          <span className="text-[#656565]">Until Timestamp:</span>
          <span className="font-mono">{getVariable(sleepNode.sleep.sleepLength.timestamp)}</span>
        </div>
      )}
      {sleepNode.sleep.sleepLength?.$case === 'isoDate' && (
        <div className="flex justify-between">
          <span className="text-[#656565]">Until Date:</span>
          <span className="font-mono">{getVariable(sleepNode.sleep.sleepLength.isoDate)}</span>
        </div>
      )}
    </>
  );

  return (
    <BaseNodeComponent
      title="Sleep Properties"
      type="SLEEP"
    >
      {mainContent}
    </BaseNodeComponent>
  )
}
