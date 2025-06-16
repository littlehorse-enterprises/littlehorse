import { ExternalEventNode, Node } from "littlehorse-client/proto";

interface ExternalEventNodeComponentProps {
  externalEventNode: Node & { externalEvent: ExternalEventNode }
}

export function ExternalEventNodeComponent({ externalEventNode }: ExternalEventNodeComponentProps) {
  return (
    <div className="space-y-4">
      <div className="rounded-md border border-gray-200 bg-gray-50 p-3">
        <h4 className="mb-2 text-xs font-medium">External Event Properties</h4>
        <div className="space-y-1 text-xs">
          <div className="flex justify-between">
            <span className="text-[#656565]">Type:</span>
            <span className="font-mono">EXTERNAL_EVENT</span>
          </div>
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
        </div>
      </div>
    </div>
  )
}
