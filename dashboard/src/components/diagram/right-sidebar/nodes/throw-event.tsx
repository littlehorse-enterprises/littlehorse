import { Node, ThrowEventNode } from "littlehorse-client/proto";

interface ThrowEventNodeComponentProps {
  throwEventNode: Node & { throwEvent: ThrowEventNode }
}

export function ThrowEventNodeComponent({ throwEventNode }: ThrowEventNodeComponentProps) {
  return (
    <div className="space-y-4">
      <div className="rounded-md border border-gray-200 bg-gray-50 p-3">
        <h4 className="mb-2 text-xs font-medium">Throw Event Properties</h4>
        <div className="space-y-1 text-xs">
          <div className="flex justify-between">
            <span className="text-[#656565]">Type:</span>
            <span className="font-mono">THROW_EVENT</span>
          </div>
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
        </div>
      </div>
    </div>
  )
} 
