import { TreeNode } from '@/types/buildNodeTree'

/**
 * Sort nodes in ascending order based on the first character (number)
 * Falls back to alphabetical sorting if not numbers
 */
export function sortNodeTree(nodeTree: TreeNode[]): TreeNode[] {
  return [...nodeTree].sort((a, b) => {
    const aFirstChar = a.id.charAt(0)
    const bFirstChar = b.id.charAt(0)

    // If the first character is a number, sort numerically in ascending order
    if (!isNaN(Number(aFirstChar)) && !isNaN(Number(bFirstChar))) {
      return Number(aFirstChar) - Number(bFirstChar)
    }

    // Fallback to alphabetical sorting if not numbers
    return bFirstChar.localeCompare(aFirstChar)
  })
}
