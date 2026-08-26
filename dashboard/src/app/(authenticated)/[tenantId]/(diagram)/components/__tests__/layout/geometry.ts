/** Pure 2D geometry for the layout invariants. No rendering, no DOM. */

export interface Rect {
  x: number
  y: number
  w: number
  h: number
}

export interface Pt {
  x: number
  y: number
}

export type Polyline = Pt[]

export const rectCenter = (r: Rect): Pt => ({ x: r.x + r.w / 2, y: r.y + r.h / 2 })

export const inflate = (r: Rect, pad: number): Rect => ({
  x: r.x - pad,
  y: r.y - pad,
  w: r.w + 2 * pad,
  h: r.h + 2 * pad,
})

export const rectsIntersect = (a: Rect, b: Rect): boolean =>
  a.x < b.x + b.w && b.x < a.x + a.w && a.y < b.y + b.h && b.y < a.y + a.h

/**
 * Parses an SVG path into a polyline. `getSmoothStepPath` with borderRadius 0
 * emits only M/L commands; Q commands (rounded corners) are flattened to
 * their endpoints, which is exact enough for overlap detection.
 */
export const pathToPolyline = (path: string): Polyline => {
  const points: Pt[] = []
  const re = /([MLQ])\s*([-\d.]+)[ ,]([-\d.]+)(?:[ ,]([-\d.]+)[ ,]([-\d.]+))?/g
  let match: RegExpExecArray | null
  while ((match = re.exec(path)) !== null) {
    const [, cmd, a, b, c, d] = match
    if (cmd === 'Q' && c !== undefined && d !== undefined) {
      points.push({ x: Number(c), y: Number(d) })
    } else {
      points.push({ x: Number(a), y: Number(b) })
    }
  }
  return points
}

const between = (v: number, lo: number, hi: number): boolean => v >= Math.min(lo, hi) && v <= Math.max(lo, hi)

/** Segment/rect intersection (segments here are axis-aligned or near enough). */
export const segmentIntersectsRect = (p1: Pt, p2: Pt, r: Rect): boolean => {
  const minX = Math.min(p1.x, p2.x)
  const maxX = Math.max(p1.x, p2.x)
  const minY = Math.min(p1.y, p2.y)
  const maxY = Math.max(p1.y, p2.y)
  return minX < r.x + r.w && r.x < maxX && minY < r.y + r.h && r.y < maxY
}

export const polylineIntersectsRect = (poly: Polyline, r: Rect): boolean => {
  for (let i = 0; i < poly.length - 1; i++) {
    if (segmentIntersectsRect(poly[i], poly[i + 1], r)) return true
  }
  return false
}

interface AxisSegment {
  axis: 'h' | 'v'
  /** The fixed coordinate (y for horizontal, x for vertical). */
  at: number
  from: number
  to: number
}

const toAxisSegments = (poly: Polyline, epsilon = 0.5): AxisSegment[] => {
  const out: AxisSegment[] = []
  for (let i = 0; i < poly.length - 1; i++) {
    const a = poly[i]
    const b = poly[i + 1]
    if (Math.abs(a.y - b.y) <= epsilon && Math.abs(a.x - b.x) > epsilon) {
      out.push({ axis: 'h', at: a.y, from: Math.min(a.x, b.x), to: Math.max(a.x, b.x) })
    } else if (Math.abs(a.x - b.x) <= epsilon && Math.abs(a.y - b.y) > epsilon) {
      out.push({ axis: 'v', at: a.x, from: Math.min(a.y, b.y), to: Math.max(a.y, b.y) })
    }
    // Diagonal segments (shouldn't exist with borderRadius 0) are ignored here;
    // they still participate in rect checks above.
  }
  return out
}

/**
 * Length of collinear overlap between two orthogonal polylines: stretches
 * where both run along the same line (within `laneTolerance` px) for more
 * than `minOverlap` px. This is the "two edges drawn on top of each other"
 * defect — crossing at a point is not counted, sharing a lane is.
 */
export const collinearOverlapLength = (
  a: Polyline,
  b: Polyline,
  laneTolerance = 2,
  minOverlap = 8
): number => {
  const segsA = toAxisSegments(a)
  const segsB = toAxisSegments(b)
  let total = 0
  for (const sa of segsA) {
    for (const sb of segsB) {
      if (sa.axis !== sb.axis) continue
      if (Math.abs(sa.at - sb.at) > laneTolerance) continue
      const overlap = Math.min(sa.to, sb.to) - Math.max(sa.from, sb.from)
      if (overlap >= minOverlap) total += overlap
    }
  }
  return total
}

/** Number of transversal crossings (h-segment of one crossing v-segment of the other). */
export const crossingCount = (a: Polyline, b: Polyline): number => {
  const segsA = toAxisSegments(a)
  const segsB = toAxisSegments(b)
  let count = 0
  for (const sa of segsA) {
    for (const sb of segsB) {
      if (sa.axis === sb.axis) continue
      const h = sa.axis === 'h' ? sa : sb
      const v = sa.axis === 'h' ? sb : sa
      if (between(v.at, h.from, h.to) && between(h.at, v.from, v.to)) count++
    }
  }
  return count
}
