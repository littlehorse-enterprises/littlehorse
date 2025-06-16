import { ExitNode, Node } from "littlehorse-client/proto";

interface ExitNodeComponentProps {
  exitNode: Node & { exit: ExitNode }
}

export function ExitNodeComponent({ exitNode }: ExitNodeComponentProps) {
  return (
    <div className="space-y-4">
      <div className="rounded-md border border-gray-200 bg-gray-50 p-3">
        <h4 className="mb-2 text-xs font-medium">Exit Properties</h4>
        <div className="space-y-1 text-xs">
          <div className="flex justify-between">
            <span className="text-[#656565]">Type:</span>
            <span className="font-mono">EXIT</span>
          </div>
          <div className="text-[#656565] text-xs">
            This node completes the thread execution.
          </div>
        </div>
      </div>

      {exitNode.exit.failureDef && (
        <div className="rounded-md border border-gray-200 bg-gray-50 p-3">
          <h4 className="mb-2 text-xs font-medium">Failure Definition</h4>
          <div className="space-y-1 text-xs">
            <div className="flex justify-between">
              <span className="text-[#656565]">Failure Name:</span>
              <span className="font-mono text-red-600">{exitNode.exit.failureDef.failureName}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-[#656565]">Message:</span>
              <span className="font-mono">{exitNode.exit.failureDef.message}</span>
            </div>
            {exitNode.exit.failureDef.content && (
              <div className="flex justify-between">
                <span className="text-[#656565]">Content:</span>
                <span className="font-mono">{JSON.stringify(exitNode.exit.failureDef.content)}</span>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  )
}
