import { Node, SleepNode } from "littlehorse-client/proto";
import { BaseNodeComponent } from "./base-node";
import { getVariable } from "@/utils/data/variables";

interface SleepNodeComponentProps {
  sleepNode: Node & { sleep: SleepNode }
}

export function SleepNodeComponent({ sleepNode }: SleepNodeComponentProps) {
  const nodeCase = sleepNode.sleep.sleepLength?.$case;

  return (
    <BaseNodeComponent
      title="Sleep Properties"
      type="SLEEP"
    >
      {nodeCase === 'rawSeconds' && sleepNode.sleep.sleepLength && (
        <div className="flex justify-between">
          <span className="text-[#656565]">Duration:</span>
          <span className="font-mono">{getVariable(sleepNode.sleep.sleepLength.rawSeconds)} seconds</span>
        </div>
      )}
      {nodeCase === 'timestamp' && sleepNode.sleep.sleepLength && (
        <div className="flex justify-between">
          <span className="text-[#656565]">Until Timestamp:</span>
          <span className="font-mono">{getVariable(sleepNode.sleep.sleepLength.timestamp)}</span>
        </div>
      )}
      {nodeCase === 'isoDate' && sleepNode.sleep.sleepLength && (
        <div className="flex justify-between">
          <span className="text-[#656565]">Until Date:</span>
          <span className="font-mono">{getVariable(sleepNode.sleep.sleepLength.isoDate)}</span>
        </div>
      )}
    </BaseNodeComponent>
  )
}
