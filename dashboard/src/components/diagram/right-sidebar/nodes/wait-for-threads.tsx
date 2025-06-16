import { Node, WaitForThreadsNode } from "littlehorse-client/proto";

interface WaitForThreadsNodeComponentProps {
  waitForThreadsNode: Node & { waitForThreads: WaitForThreadsNode }
}

export function WaitForThreadsNodeComponent({  }: WaitForThreadsNodeComponentProps) {
  return null;
}