import { ExitNode, Node } from "littlehorse-client/proto";

interface ExitNodeComponentProps {
  exitNode: Node & { exit: ExitNode }
}

export function ExitNodeComponent({}: ExitNodeComponentProps) {
  return null;
}
