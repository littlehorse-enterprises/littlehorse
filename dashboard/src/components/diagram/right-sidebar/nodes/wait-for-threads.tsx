import { Node, WaitForThreadsNode } from "littlehorse-client/proto";

interface WaitForThreadsNodeComponentProps {
  waitForThreadsNode: Node & { waitForThreads: WaitForThreadsNode }
}

export function WaitForThreadsNodeComponent({ waitForThreadsNode }: WaitForThreadsNodeComponentProps) {
  return (
    <div className="space-y-4">
      <div className="rounded-md border border-gray-200 bg-gray-50 p-3">
        <h4 className="mb-2 text-xs font-medium">Wait For Threads Properties</h4>
        <div className="space-y-1 text-xs">
          <div className="flex justify-between">
            <span className="text-[#656565]">Type:</span>
            <span className="font-mono">WAIT_FOR_THREADS</span>
          </div>
          {waitForThreadsNode.waitForThreads.threads && (
            <div className="flex justify-between">
              <span className="text-[#656565]">Threads:</span>
              <span className="font-mono">{JSON.stringify(waitForThreadsNode.waitForThreads.threads)}</span>
            </div>
          )}
          {waitForThreadsNode.waitForThreads.threadList && (
            <div className="flex justify-between">
              <span className="text-[#656565]">Thread List:</span>
              <span className="font-mono">{JSON.stringify(waitForThreadsNode.waitForThreads.threadList)}</span>
            </div>
          )}
        </div>
      </div>

      {waitForThreadsNode.waitForThreads.perThreadFailureHandlers && waitForThreadsNode.waitForThreads.perThreadFailureHandlers.length > 0 && (
        <div className="rounded-md border border-gray-200 bg-gray-50 p-3">
          <h4 className="mb-2 text-xs font-medium">Failure Handlers</h4>
          <div className="space-y-1 text-xs">
            {waitForThreadsNode.waitForThreads.perThreadFailureHandlers.map((handler, index) => (
              <div key={index} className="font-mono">
                <span className="text-purple-600">Handler {index + 1}:</span>{' '}
                <span className="text-blue-600">{JSON.stringify(handler)}</span>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  )
}
