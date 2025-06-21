import { ExternalEventNode, Node } from "littlehorse-client/proto";
import { BaseNodeComponent } from "./base-node";

interface ExternalEventNodeComponentProps {
  externalEventNode: Node & { externalEvent: ExternalEventNode }
}

export function ExternalEventNodeComponent({ externalEventNode }: ExternalEventNodeComponentProps) {
  const mainContent = (
    <>
      {externalEventNode.externalEvent.externalEventDefId && (
        <div className="flex justify-between">
          <span className="text-[#656565]">Event Def:</span>
          <span className="font-mono text-blue-600">{JSON.stringify(externalEventNode.externalEvent.externalEventDefId)}</span>
        </div>
      )}
      {externalEventNode.externalEvent.timeoutSeconds && (
        <div className="flex justify-between">
          <span className="text-[#656565]">Timeout:</span>
          <span className="font-mono">{JSON.stringify(externalEventNode.externalEvent.timeoutSeconds)} seconds</span>
        </div>
      )}
    </>
  );

  return (
    <BaseNodeComponent
      title="External Event Properties"
      type="EXTERNAL_EVENT"
    >
      {mainContent}
    </BaseNodeComponent>
  )
}
