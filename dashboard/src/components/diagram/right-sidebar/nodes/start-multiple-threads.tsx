import { Node, StartMultipleThreadsNode } from "littlehorse-client/proto";
import { BaseNodeComponent } from "./base-node";

interface StartMultipleThreadsNodeComponentProps {
  startMultipleThreadsNode: Node & { startMultipleThreads: StartMultipleThreadsNode }
}

export function StartMultipleThreadsNodeComponent({ startMultipleThreadsNode }: StartMultipleThreadsNodeComponentProps) {
  const mainContent = (
    <>
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
    </>
  );

  const additionalSections = startMultipleThreadsNode.startMultipleThreads.variables && Object.keys(startMultipleThreadsNode.startMultipleThreads.variables).length > 0 ? [
    {
      title: "Variables",
      content: (
        <div className="space-y-1">
          {Object.entries(startMultipleThreadsNode.startMultipleThreads.variables).map(([key, variable]) => (
            <div key={key} className="font-mono text-xs">
              <span className="text-purple-600">{key}:</span>{' '}
              <span className="text-blue-600">{JSON.stringify(variable)}</span>
            </div>
          ))}
        </div>
      )
    }
  ] : undefined;

  return (
    <BaseNodeComponent
      title="Start Multiple Threads Properties"
      type="START_MULTIPLE_THREADS"
      additionalSections={additionalSections}
    >
      {mainContent}
    </BaseNodeComponent>
  )
}
