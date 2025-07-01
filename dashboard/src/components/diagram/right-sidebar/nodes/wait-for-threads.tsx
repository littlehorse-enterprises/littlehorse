import { Node, WaitForThreadsNode } from "littlehorse-client/proto";
import { BaseNodeComponent } from "./base-node";
import { getVariable } from "@/utils/data/variables";

interface WaitForThreadsNodeComponentProps {
  waitForThreadsNode: Node & { waitForThreads: WaitForThreadsNode }
}

export function WaitForThreadsNodeComponent({ waitForThreadsNode }: WaitForThreadsNodeComponentProps) {
  const mainContent = (
    <>
      {waitForThreadsNode.waitForThreads.threadsToWaitFor?.$case === 'threads' && (
        <div className="flex justify-between">
          <span className="text-[#656565]">Threads:</span>
          <span className="font-mono">{JSON.stringify(waitForThreadsNode.waitForThreads.threadsToWaitFor.threads.threads)}</span>
        </div>
      )}
      {waitForThreadsNode.waitForThreads.threadsToWaitFor?.$case === 'threadList' && (
        <div className="flex justify-between">
          <span className="text-[#656565]">Thread List:</span>
          <span className="font-mono">{getVariable(waitForThreadsNode.waitForThreads.threadsToWaitFor.threadList)}</span>
        </div>
      )}
    </>
  );

  const additionalSections = waitForThreadsNode.waitForThreads.perThreadFailureHandlers && waitForThreadsNode.waitForThreads.perThreadFailureHandlers.length > 0 ? [
    {
      title: "Failure Handlers",
      content: (
        <div className="space-y-1 text-xs">
          {waitForThreadsNode.waitForThreads.perThreadFailureHandlers.map((handler, index) => (
            <div key={index} className="font-mono">
              <span className="text-purple-600">Handler {index + 1}:</span>{' '}
              <span className="text-blue-600">{JSON.stringify(handler)}</span>
            </div>
          ))}
        </div>
      )
    }
  ] : undefined;

  return (
    <BaseNodeComponent
      title="Wait For Threads Properties"
      type="WAIT_FOR_THREADS"
      additionalSections={additionalSections}
    >
      {mainContent}
    </BaseNodeComponent>
  )
}
