/**
 * Deterministic readability invariants over a laid-out scene.
 *
 * Each check encodes one way a diagram stops being readable, as geometry —
 * so "the graph looks wrong" becomes a countable defect instead of a
 * judgment. Crossing edges at a point is NOT a defect (non-planar graphs
 * exist); running along the same lane, passing through a node, or flowing
 * backwards is.
 */
import { Scene } from './layoutModel'
import {
  collinearOverlapLength,
  crossingCount,
  inflate,
  polylineIntersectsRect,
  rectCenter,
  rectsIntersect,
} from './geometry'

export type DefectType =
  | 'node-overlaps-node' // two node boxes intersect
  | 'edge-through-node' // an edge path crosses a node it neither starts nor ends at
  | 'edges-share-lane' // two edges drawn collinearly on top of each other
  | 'forward-edge-goes-backwards' // a non-loop edge flows right-to-left
  | 'label-covers-node' // an edge label chip sits on a node
  | 'missing-handle' // extractEdges addressed a handle the component never renders
  | 'node-unreachable' // a non-entrypoint node has no incoming edge (graph transform lost edges)
  | 'node-dead-end' // a non-exit node has no outgoing edge (graph transform lost edges)

export interface Defect {
  type: DefectType
  detail: string
}

/** Clearance around nodes an edge must respect (px). */
const NODE_CLEARANCE = 2
/** Two edges sharing >= this many px of the same lane is an overlap defect. */
const LANE_OVERLAP_MIN = 8

export interface Verdict {
  defects: Defect[]
  /** Point-crossings between edges — reported as a metric, not asserted. */
  crossings: number
}

export const checkScene = (scene: Scene): Verdict => {
  const defects: Defect[] = []
  const { nodes, edges } = scene
  const nodeById = new Map(nodes.map(n => [n.id, n]))

  // 1. Nodes must not overlap each other.
  for (let i = 0; i < nodes.length; i++) {
    for (let j = i + 1; j < nodes.length; j++) {
      if (rectsIntersect(inflate(nodes[i].rect, 2), nodes[j].rect)) {
        defects.push({ type: 'node-overlaps-node', detail: `${nodes[i].id} ~ ${nodes[j].id}` })
      }
    }
  }

  // 2. An edge must not pass through any node it is not attached to.
  for (const edge of edges) {
    for (const node of nodes) {
      if (node.id === edge.source || node.id === edge.target) continue
      if (polylineIntersectsRect(edge.polyline, inflate(node.rect, NODE_CLEARANCE))) {
        defects.push({ type: 'edge-through-node', detail: `${edge.id} through ${node.id}` })
      }
    }
  }

  // 3. Two edges must not share a lane (collinear overlap). A point crossing is fine.
  for (let i = 0; i < edges.length; i++) {
    for (let j = i + 1; j < edges.length; j++) {
      const a = edges[i]
      const b = edges[j]
      // Stubs leaving/entering the same handle necessarily share a few px;
      // only flag pairs that do not share an endpoint node.
      const sharesNode =
        a.source === b.source || a.source === b.target || a.target === b.source || a.target === b.target
      if (sharesNode) continue
      const overlap = collinearOverlapLength(a.polyline, b.polyline, 2, LANE_OVERLAP_MIN)
      if (overlap > 0) {
        defects.push({ type: 'edges-share-lane', detail: `${a.id} ~ ${b.id} (${Math.round(overlap)}px)` })
      }
    }
  }

  // 4. In a left-to-right layered layout, every non-loop edge flows forward.
  //    Loop plumbing (cycle-node edges) is exempt: flowing back is its job.
  for (const edge of edges) {
    if (edge.isCycleEdge) continue
    const source = nodeById.get(edge.source)!
    const target = nodeById.get(edge.target)!
    if (rectCenter(target.rect).x < rectCenter(source.rect).x - 1) {
      defects.push({ type: 'forward-edge-goes-backwards', detail: edge.id })
    }
  }

  // 5. Edge labels must not cover nodes.
  for (const edge of edges) {
    if (!edge.labelRect) continue
    for (const node of nodes) {
      if (rectsIntersect(edge.labelRect, node.rect)) {
        defects.push({ type: 'label-covers-node', detail: `label of ${edge.id} on ${node.id}` })
      }
    }
  }

  // 6. Every handle extractEdges addresses must exist on the component.
  for (const edge of edges) {
    if (edge.missingSourceHandle) defects.push({ type: 'missing-handle', detail: `${edge.id} (source)` })
    if (edge.missingTargetHandle) defects.push({ type: 'missing-handle', detail: `${edge.id} (target)` })
  }

  // 7. Connectivity survives the graph transforms: getCycleNodes rewrites
  //    edges, and a rewrite that drops one leaves a node floating (observed
  //    live: empty-body while loops lost both forward edges, orphaning the
  //    loop-end nop). Every node keeps a way in and a way out.
  const hasIncoming = new Set(edges.map(e => e.target))
  const hasOutgoing = new Set(edges.map(e => e.source))
  for (const node of nodes) {
    if (node.type !== 'entrypoint' && !hasIncoming.has(node.id)) {
      defects.push({ type: 'node-unreachable', detail: node.id })
    }
    if (node.type !== 'exit' && !hasOutgoing.has(node.id)) {
      defects.push({ type: 'node-dead-end', detail: node.id })
    }
  }

  // Metric only: transversal crossings between edge pairs.
  let crossings = 0
  for (let i = 0; i < edges.length; i++) {
    for (let j = i + 1; j < edges.length; j++) {
      crossings += crossingCount(edges[i].polyline, edges[j].polyline)
    }
  }

  return { defects, crossings }
}

export const countByType = (defects: Defect[]): Record<string, number> => {
  const out: Record<string, number> = {}
  for (const d of defects) out[d.type] = (out[d.type] ?? 0) + 1
  return out
}
