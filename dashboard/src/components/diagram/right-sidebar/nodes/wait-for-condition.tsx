import { Node, WaitForConditionNode } from "littlehorse-client/proto";
import { BaseNodeComponent } from "./base-node";

interface WaitForConditionNodeComponentProps {
  waitForConditionNode: Node & { waitForCondition: WaitForConditionNode }
}

export function WaitForConditionNodeComponent({ waitForConditionNode }: WaitForConditionNodeComponentProps) {
  const mainContent = (
    <>
      {waitForConditionNode.waitForCondition.condition && (
        <div className="flex justify-between">
          <span className="text-[#656565]">Condition:</span>
          <span className="font-mono">{JSON.stringify(waitForConditionNode.waitForCondition.condition)}</span>
        </div>
      )}
    </>
  );

  return (
    <BaseNodeComponent
      title="Wait For Condition Properties"
      type="WAIT_FOR_CONDITION"
    >
      {mainContent}
    </BaseNodeComponent>
  )
} 
