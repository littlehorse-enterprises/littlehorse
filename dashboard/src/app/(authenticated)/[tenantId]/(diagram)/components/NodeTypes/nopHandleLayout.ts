import { Position } from 'reactflow'
import { Node } from 'littlehorse-client/proto'

export type NopHandlePlacement = {
  /** Handle id, aligned with the edge's index in outgoingEdges: `source-<i>`. */
  id: string
  position: Position
  /** Offset along the side as a percentage (reactflow `left`/`top` style). */
  pct: number
}

/**
 * Source-handle placement for a NOP node.
 *
 * The contract this restores: handle ids are contiguous and index-aligned
 * with `outgoingEdges`, so the `source-<i>` that extractEdges assigns to the
 * i-th edge ALWAYS exists. (The previous generator skipped ids in some
 * branches — e.g. three conditional edges plus a loop produced source-0,
 * source-1, source-3 — and reactflow silently drops an edge whose handle id
 * does not resolve, which is one reason branchy/loopy graphs lost edges.)
 *
 * Visual intent kept from the original: conditional/else edges fan out over
 * the top, right, and bottom of the diamond; loop plumbing (edges into
 * synthesized `cycle-` nodes) leaves from the bottom.
 */
export const nopSourceHandlePlacements = (outgoingEdges: Node['outgoingEdges']): NopHandlePlacement[] => {
  const cycleIndexes: number[] = []
  const regularIndexes: number[] = []
  outgoingEdges.forEach((edge, index) =>
    (edge.sinkNodeName.startsWith('cycle-') ? cycleIndexes : regularIndexes).push(index)
  )

  const placements: NopHandlePlacement[] = new Array(outgoingEdges.length)
  const spread = (count: number, slot: number) => (slot + 1) / (count + 1)

  // Regular edges: 1 → right; 2 → top/bottom (top/top when a loop occupies
  // the bottom); 3+ → spread across top, then right, then bottom.
  const n = regularIndexes.length
  const hasCycle = cycleIndexes.length > 0
  const sides: { position: Position; pct: number }[] = []
  if (n === 1) {
    sides.push({ position: Position.Right, pct: 0.5 })
  } else if (n === 2) {
    if (hasCycle) {
      sides.push({ position: Position.Top, pct: 0.33 }, { position: Position.Top, pct: 0.66 })
    } else {
      sides.push({ position: Position.Top, pct: 0.5 }, { position: Position.Bottom, pct: 0.5 })
    }
  } else if (n >= 3) {
    const topCount = Math.ceil(n / 2)
    const rightCount = n % 2 === 1 ? 1 : 0
    const bottomCount = hasCycle ? n - topCount - rightCount : n - topCount - rightCount
    for (let i = 0; i < topCount; i++) sides.push({ position: Position.Top, pct: spread(topCount, i) })
    for (let i = 0; i < rightCount; i++) sides.push({ position: Position.Right, pct: 0.5 })
    for (let i = 0; i < bottomCount; i++)
      sides.push({ position: hasCycle ? Position.Right : Position.Bottom, pct: spread(bottomCount + 1, i + 1) })
  }
  regularIndexes.forEach((edgeIndex, slot) => {
    placements[edgeIndex] = { id: `source-${edgeIndex}`, ...sides[slot] }
  })

  // Loop edges: always leave from the bottom, spread if there are several.
  cycleIndexes.forEach((edgeIndex, slot) => {
    placements[edgeIndex] = {
      id: `source-${edgeIndex}`,
      position: Position.Bottom,
      pct: spread(cycleIndexes.length, slot),
    }
  })

  return placements
}
