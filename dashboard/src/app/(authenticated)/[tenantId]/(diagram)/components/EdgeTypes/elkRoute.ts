/**
 * Helpers for edges routed by ELK.
 *
 * ELK's layered algorithm computes orthogonal edge routes (start point, bend
 * points, end point) that respect the configured edge-edge and edge-node
 * clearances. LayoutManager attaches that route to each edge's data; the
 * custom edge renders it verbatim instead of re-deriving a naive path from
 * handle positions. Pure functions, shared with the headless layout tests.
 */

export type RoutePoint = { x: number; y: number }

/** Orthogonal polyline -> SVG path. */
export const routeToPath = (points: RoutePoint[]): string =>
  points.map((p, i) => `${i === 0 ? 'M' : 'L'} ${p.x} ${p.y}`).join(' ')

/**
 * Where an edge's label chip goes: the midpoint of the route's longest
 * segment — deterministic, and on long runs it lands in open space instead
 * of on top of the endpoints.
 */
export const routeLabelPoint = (points: RoutePoint[]): RoutePoint => {
  if (points.length < 2) return points[0] ?? { x: 0, y: 0 }
  let best = 0
  let bestLength = -1
  for (let i = 0; i < points.length - 1; i++) {
    const length = Math.abs(points[i + 1].x - points[i].x) + Math.abs(points[i + 1].y - points[i].y)
    if (length > bestLength) {
      bestLength = length
      best = i
    }
  }
  return {
    x: (points[best].x + points[best + 1].x) / 2,
    y: (points[best].y + points[best + 1].y) / 2,
  }
}
