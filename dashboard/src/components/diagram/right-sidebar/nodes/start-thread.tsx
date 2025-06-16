import { Node, StartThreadNode } from "littlehorse-client/proto";

interface StartThreadNodeComponentProps {
  startThreadNode: Node & { startThread: StartThreadNode }
}

export function StartThreadNodeComponent({ startThreadNode }: StartThreadNodeComponentProps) {
  return (
    <div className="space-y-4">
      <div className="rounded-md border border-gray-200 bg-gray-50 p-3">
        <h4 className="mb-2 text-xs font-medium">Start Thread Properties</h4>
        <div className="space-y-1 text-xs">
          <div className="flex justify-between">
            <span className="text-[#656565]">Type:</span>
            <span className="font-mono">START_THREAD</span>
          </div>
          {startThreadNode.startThread.threadSpecName && (
            <div className="flex justify-between">
              <span className="text-[#656565]">Thread Spec:</span>
              <span className="font-mono text-blue-600">{startThreadNode.startThread.threadSpecName}</span>
            </div>
          )}
        </div>
      </div>

      {startThreadNode.startThread.variables && Object.keys(startThreadNode.startThread.variables).length > 0 && (
        <div className="rounded-md border border-gray-200 bg-gray-50 p-3">
          <h4 className="mb-2 text-xs font-medium">Variables</h4>
          <div className="space-y-1">
            {Object.entries(startThreadNode.startThread.variables).map(([key, variable]) => (
              <div key={key} className="font-mono text-xs">
                <span className="text-purple-600">{key}:</span>{' '}
                <span className="text-blue-600">{JSON.stringify(variable)}</span>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  )
}
