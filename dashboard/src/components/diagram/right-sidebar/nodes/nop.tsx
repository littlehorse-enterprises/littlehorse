import { Node, NopNode } from "littlehorse-client/proto";

interface NopNodeComponentProps {
  nopNode: Node & { nop: NopNode }
}

export function NopNodeComponent({ }: NopNodeComponentProps) {
  return (
    <div className="space-y-4">
      <div className="rounded-md border border-gray-200 bg-gray-50 p-3">
        <h4 className="mb-2 text-xs font-medium">NOP Properties</h4>
        <div className="space-y-1 text-xs">
          <div className="flex justify-between">
            <span className="text-[#656565]">Type:</span>
            <span className="font-mono">NOP</span>
          </div>
          <div className="text-[#656565] text-xs">
            This is a No-Operation node that does nothing and immediately continues to the next node.
          </div>
        </div>
      </div>
    </div>
  )
} 
