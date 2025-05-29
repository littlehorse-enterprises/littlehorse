import { TreeNode } from "@/types/buildNodeTree";
import { ThreadSpec } from "littlehorse-client/proto";

export function buildNodeTree(nodes: ThreadSpec["nodes"]): TreeNode[] {
  if (!nodes) return [];
  const result: TreeNode[] = [];

  Object.entries(nodes).forEach(([nodeId, node]) => {
    result.push({
      id: nodeId,
      label: nodeId,
      type: undefined,
      status: undefined,
      children: [],
      level: 0,
    });
  });

  return result;
}
