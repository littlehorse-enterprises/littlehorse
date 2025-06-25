import { Node, StartThreadNode } from "littlehorse-client/proto";
import { BaseNodeComponent } from "./base-node";
import { getVariable } from "@/utils/data/variables";

interface StartThreadNodeComponentProps {
  startThreadNode: Node & { startThread: StartThreadNode }
}

export function StartThreadNodeComponent({ startThreadNode }: StartThreadNodeComponentProps) {
  const mainContent = (
    <>
      {startThreadNode.startThread.threadSpecName && (
        <div className="flex justify-between">
          <span className="text-[#656565]">Thread Spec:</span>
          <span className="font-mono text-blue-600">{startThreadNode.startThread.threadSpecName}</span>
        </div>
      )}
    </>
  );

  const additionalSections = startThreadNode.startThread.variables && Object.keys(startThreadNode.startThread.variables).length > 0 ? [
    {
      title: "Variables",
      content: (
        <div className="space-y-1">
          {Object.entries(startThreadNode.startThread.variables).map(([key, variable]) => (
            <div key={key} className="font-mono text-xs">
              <span className="text-purple-600">{key}:</span>{' '}
              <span className="text-blue-600">{getVariable(variable)}</span>
            </div>
          ))}
        </div>
      )
    }
  ] : undefined;

  return (
    <BaseNodeComponent
      title="Start Thread Properties"
      type="START_THREAD"
      additionalSections={additionalSections}
    >
      {mainContent}
    </BaseNodeComponent>
  )
}
