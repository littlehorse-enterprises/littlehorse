import { EntrypointNode, EntrypointRun, Node, NodeRun } from "littlehorse-client/proto";
import { BaseNodeComponent } from "./base-node";

interface EntrypointNodeComponentProps {
  entrypointNode: Node & { entrypoint: EntrypointNode }
}

export function EntrypointNodeComponent({ }: EntrypointNodeComponentProps) {
  return (
    <BaseNodeComponent
      title="Entrypoint Properties"
      type="ENTRYPOINT"
      description="This is the entry point node where the workflow execution begins."
    />
  )
}

interface EntrypointNodeRunComponentProps {
  entrypointNodeRun: NodeRun & { entrypoint: EntrypointRun }
}

export function EntrypointNodeRunComponent({ }: EntrypointNodeRunComponentProps) {
  return (
    <div>
      <h4 className="mb-2 text-xs font-medium">Entrypoint Run</h4>
    </div>
  )
}
