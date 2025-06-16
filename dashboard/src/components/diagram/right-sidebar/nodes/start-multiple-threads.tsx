import { Node, StartMultipleThreadsNode } from "littlehorse-client/proto";

interface StartMultipleThreadsNodeComponentProps {
  startMultipleThreadsNode: Node & { startMultipleThreads: StartMultipleThreadsNode }
}

export function StartMultipleThreadsNodeComponent({ startMultipleThreadsNode }: StartMultipleThreadsNodeComponentProps) {
  return (
    <div className="space-y-4">
      <div className="rounded-md border border-gray-200 bg-gray-50 p-3">
        <h4 className="mb-2 text-xs font-medium">Start Multiple Threads Properties</h4>
        <div className="space-y-1 text-xs">
          <div className="flex justify-between">
            <span className="text-[#656565]">Type:</span>
            <span className="font-mono">START_MULTIPLE_THREADS</span>
          </div>
          {startMultipleThreadsNode.startMultipleThreads.threadSpecName && (
            <div className="flex justify-between">
              <span className="text-[#656565]">Thread Spec:</span>
              <span className="font-mono text-blue-600">{startMultipleThreadsNode.startMultipleThreads.threadSpecName}</span>
            </div>
          )}
          {startMultipleThreadsNode.startMultipleThreads.iterable && (
            <div className="flex justify-between">
              <span className="text-[#656565]">Iterable:</span>
              <span className="font-mono">{JSON.stringify(startMultipleThreadsNode.startMultipleThreads.iterable)}</span>
            </div>
          )}
        </div>
      </div>

      {startMultipleThreadsNode.startMultipleThreads.variables && Object.keys(startMultipleThreadsNode.startMultipleThreads.variables).length > 0 && (
        <div className="rounded-md border border-gray-200 bg-gray-50 p-3">
          <h4 className="mb-2 text-xs font-medium">Variables</h4>
          <div className="space-y-1">
            {Object.entries(startMultipleThreadsNode.startMultipleThreads.variables).map(([key, variable]) => (
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
