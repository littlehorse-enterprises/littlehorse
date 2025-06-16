import { ExternalEventNode, Node } from "littlehorse-client/proto";

interface ExternalEventNodeComponentProps {
  externalEventNode: Node & { externalEvent: ExternalEventNode }
}

export function ExternalEventNodeComponent({ }: ExternalEventNodeComponentProps) {
  return null;
}
