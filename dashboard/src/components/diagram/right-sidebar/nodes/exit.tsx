import { ExitNode, Node } from "littlehorse-client/proto";
import { BaseNodeComponent } from "./base-node";

interface ExitNodeComponentProps {
  exitNode: Node & { exit: ExitNode }
}

export function ExitNodeComponent({ exitNode }: ExitNodeComponentProps) {
  const additionalSections = exitNode.exit.failureDef ? [
    {
      title: "Failure Definition",
      content: (
        <>
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
        </>
      )
    }
  ] : undefined;

  return (
    <BaseNodeComponent
      title="Exit Properties"
      type="EXIT"
      description="This node completes the thread execution."
      additionalSections={additionalSections}
    />
  )
}
