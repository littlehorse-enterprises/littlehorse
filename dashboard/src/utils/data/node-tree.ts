import { LHStatus, WfSpec } from 'littlehorse-client/proto'
import { NodeType } from '../ui/node-utils'

export interface TreeNode {
  id: string
  label: string
  type?: NodeType
  status?: LHStatus
  children: TreeNode[]
  level: number
}

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

/**
 * Sort nodes in ascending order based on the first character (number)
 * Falls back to alphabetical sorting if not numbers
 */
export function sortNodeTree(nodeTree: TreeNode[]): TreeNode[] {
  return [...nodeTree].sort((a, b) => {
    const aFirstPart = a.id.split('-')[0]
    const bFirstPart = b.id.split('-')[0]

    // If the first part is a number, sort numerically in ascending order
    if (!isNaN(Number(aFirstPart)) && !isNaN(Number(bFirstPart))) {
      return Number(aFirstPart) - Number(bFirstPart)
    }

    // Fallback to alphabetical sorting if not numbers
    return bFirstPart.localeCompare(aFirstPart)
  })
}
