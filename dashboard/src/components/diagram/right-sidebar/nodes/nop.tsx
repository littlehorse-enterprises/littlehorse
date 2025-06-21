import { Node, NopNode } from "littlehorse-client/proto";
import { BaseNodeComponent } from "./base-node";

interface NopNodeComponentProps {
  nopNode: Node & { nop: NopNode }
}

export function NopNodeComponent({ }: NopNodeComponentProps) {
  return (
    <BaseNodeComponent
      title="NOP Properties"
      type="NOP"
    />
  )
} 
