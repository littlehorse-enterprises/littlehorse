import { Node, SleepNode } from "littlehorse-client/proto";
import { BaseNodeComponent } from "./base-node";
import { getVariable } from "@/utils/data/variables";

interface SleepNodeComponentProps {
  sleepNode: Node & { sleep: SleepNode }
}

export function SleepNodeComponent({ sleepNode }: SleepNodeComponentProps) {
  const mainContent = (
    <>
      {sleepNode.sleep.rawSeconds && (
        <div className="flex justify-between">
          <span className="text-[#656565]">Duration:</span>
          <span className="font-mono">{getVariable(sleepNode.sleep.rawSeconds)} seconds</span>
        </div>
      )}
      {sleepNode.sleep.timestamp && (
        <div className="flex justify-between">
          <span className="text-[#656565]">Until Timestamp:</span>
          <span className="font-mono">{getVariable(sleepNode.sleep.timestamp)}</span>
        </div>
      )}
      {sleepNode.sleep.isoDate && (
        <div className="flex justify-between">
          <span className="text-[#656565]">Until Date:</span>
          <span className="font-mono">{getVariable(sleepNode.sleep.isoDate)}</span>
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
