import { EntrypointNode, EntrypointRun, Node, NodeRun } from "littlehorse-client/proto";

interface EntrypointNodeComponentProps {
  entrypointNode: Node & { entrypoint: EntrypointNode }
}

export function EntrypointNodeComponent({ }: EntrypointNodeComponentProps) {
  return (
    <div className="space-y-4">
      <div className="rounded-md border border-gray-200 bg-gray-50 p-3">
        <h4 className="mb-2 text-xs font-medium">Entrypoint Properties</h4>
        <div className="space-y-1 text-xs">
          <div className="flex justify-between">
            <span className="text-[#656565]">Type:</span>
            <span className="font-mono">ENTRYPOINT</span>
          </div>
          <div className="text-[#656565] text-xs">
            This is the entry point node where the workflow execution begins.
          </div>
        </div>
      </div>
    </div>
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
