import { Node, ThrowEventNode } from "littlehorse-client/proto";
import { BaseNodeComponent } from "./base-node";

interface ThrowEventNodeComponentProps {
  throwEventNode: Node & { throwEvent: ThrowEventNode }
}

export function ThrowEventNodeComponent({ throwEventNode }: ThrowEventNodeComponentProps) {
  const mainContent = (
    <>
      {throwEventNode.throwEvent.eventDefId && (
        <div className="flex justify-between">
          <span className="text-[#656565]">Event Def ID:</span>
          <span className="font-mono text-blue-600">{JSON.stringify(throwEventNode.throwEvent.eventDefId)}</span>
        </div>
      )}
      {throwEventNode.throwEvent.content && (
        <div className="flex justify-between">
          <span className="text-[#656565]">Content:</span>
          <span className="font-mono">{JSON.stringify(throwEventNode.throwEvent.content)}</span>
        </div>
      )}
    </>
  );

  return (
    <BaseNodeComponent
      title="Throw Event Properties"
      type="THROW_EVENT"
    >
      {mainContent}
    </BaseNodeComponent>
  )
} 
