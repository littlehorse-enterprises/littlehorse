import { TreeNode } from '@/types/buildNodeTree'
import { WfSpec } from 'littlehorse-client/proto'

export function buildNodeTree(wfSpec: WfSpec, threadSpecName: string): TreeNode[] {
  const result: TreeNode[] = []

  Object.entries(wfSpec.threadSpecs[threadSpecName].nodes).forEach(([nodeId]) => {
    result.push({
      id: `${nodeId}:${threadSpecName}`,
      label: nodeId,
      type: undefined,
      status: undefined,
      children: [],
      level: 0,
    })
  })

  return result
}
