import { Node, WaitForConditionNode } from "littlehorse-client/proto";

interface WaitForConditionNodeComponentProps {
  waitForConditionNode: Node & { waitForCondition: WaitForConditionNode }
}

export function WaitForConditionNodeComponent({ waitForConditionNode }: WaitForConditionNodeComponentProps) {
  return (
    <div className="space-y-4">
      <div className="rounded-md border border-gray-200 bg-gray-50 p-3">
        <h4 className="mb-2 text-xs font-medium">Wait For Condition Properties</h4>
        <div className="space-y-1 text-xs">
          <div className="flex justify-between">
            <span className="text-[#656565]">Type:</span>
            <span className="font-mono">WAIT_FOR_CONDITION</span>
          </div>
          {waitForConditionNode.waitForCondition.condition && (
            <div className="flex justify-between">
              <span className="text-[#656565]">Condition:</span>
              <span className="font-mono">{JSON.stringify(waitForConditionNode.waitForCondition.condition)}</span>
            </div>
          )}
        </div>
      </div>
    </div>
  )
} 
