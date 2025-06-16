import { Node, StartThreadNode } from "littlehorse-client/proto";

interface StartThreadNodeComponentProps {
  startThreadNode: Node & { startThread: StartThreadNode }
}

export function StartThreadNodeComponent({  }: StartThreadNodeComponentProps) {
  return null;
}