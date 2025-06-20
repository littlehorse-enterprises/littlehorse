import { TreeNode } from '@/types/buildNodeTree'

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
