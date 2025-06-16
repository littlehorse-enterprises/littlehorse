import { EntrypointNode, EntrypointRun, Node, NodeRun } from "littlehorse-client/proto";

interface EntrypointNodeComponentProps {
  entrypointNode: Node & { entrypoint: EntrypointNode }
}

export function EntrypointNodeComponent({ }: EntrypointNodeComponentProps) {
  return null;
}

interface EntrypointNodeRunComponentProps {
  entrypointNodeRun: NodeRun & { entrypoint: EntrypointRun }
}

export function EntrypointNodeRunComponent({ }: EntrypointNodeRunComponentProps) {
  return null;
}