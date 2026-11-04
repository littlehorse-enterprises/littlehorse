/**
 * Helpers for edges routed by ELK.
 *
 * ELK's layered algorithm computes orthogonal edge routes (start point, bend
 * points, end point) that respect the configured edge-edge and edge-node
 * clearances. LayoutManager attaches that route to each edge's data; the
 * custom edge renders it verbatim instead of re-deriving a naive path from
 * handle positions. Pure functions, shared with the headless layout tests.
 */

import { Edge, Position } from 'reactflow'
import { edgeLabelSize } from './edgeLabel'
import type { DiagramEdgeData } from './extractEdges'

export type RoutePoint = { x: number; y: number }

export type EdgeRoute = {
  points: RoutePoint[]
  paths: RoutePoint[][]
  labelPoint?: RoutePoint
}

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

const samePoint = (a: RoutePoint, b: RoutePoint) => a.x === b.x && a.y === b.y

/** Subtract only shared trunks; unrelated collinear routes remain defects. */
export const unsharedPaths = (route: RoutePoint[], others: RoutePoint[][]): RoutePoint[][] => {
  const paths: RoutePoint[][] = []
  for (let i = 1; i < route.length; i++) {
    const a = route[i - 1]
    const b = route[i]
    if (samePoint(a, b)) continue
    const horizontal = a.y === b.y
    const axis = horizontal ? 'x' : 'y'
    const fixed = horizontal ? 'y' : 'x'
    let intervals = [[Math.min(a[axis], b[axis]), Math.max(a[axis], b[axis])]]
    for (const other of others) {
      for (let j = 1; j < other.length; j++) {
        const c = other[j - 1]
        const d = other[j]
        if (c[fixed] !== a[fixed] || d[fixed] !== a[fixed]) continue
        const lo = Math.min(c[axis], d[axis])
        const hi = Math.max(c[axis], d[axis])
        intervals = intervals.flatMap(([from, to]) => {
          if (hi <= from || lo >= to) return [[from, to]]
          return [...(lo > from ? [[from, lo]] : []), ...(hi < to ? [[hi, to]] : [])]
        })
      }
    }
    if (b[axis] < a[axis]) intervals.reverse()
    for (const [lo, hi] of intervals) {
      const start = { ...a, [axis]: b[axis] > a[axis] ? lo : hi }
      const end = { ...b, [axis]: b[axis] > a[axis] ? hi : lo }
      const previous = paths[paths.length - 1]
      if (previous && samePoint(previous[previous.length - 1], start)) previous.push(end)
      else paths.push([start, end])
    }
  }
  return paths
}

export const planRoutes = (
  edges: Edge<DiagramEdgeData>[],
  routes: { id: string; points: RoutePoint[] }[]
): Map<string, EdgeRoute> => {
  const byId = new Map(routes.map(route => [route.id, route.points]))
  const planned = new Map<string, EdgeRoute>()
  for (const edge of edges) {
    const points = byId.get(edge.id)
    if (!points) throw new Error(`Missing route for ${edge.id}`)
    const siblings = edges.filter(
      other =>
        other.id !== edge.id &&
        ((other.source === edge.source && other.sourceHandle === edge.sourceHandle) ||
          (other.target === edge.target && other.targetHandle === edge.targetHandle))
    )
    const paths = unsharedPaths(
      points,
      siblings.filter(other => planned.has(other.id)).map(other => byId.get(other.id)!)
    )
    const size = edgeLabelSize(edge.data)
    let labelPoint: RoutePoint | undefined
    if (size) {
      const unique = unsharedPaths(
        points,
        siblings.map(other => byId.get(other.id)!)
      )
      for (const path of unique) {
        for (let i = 1; i < path.length; i++) {
          const a = path[i - 1]
          const b = path[i]
          if (a.y === b.y && Math.abs(b.x - a.x) >= size.w + 16) {
            labelPoint = { x: (a.x + b.x) / 2, y: a.y }
            break
          }
        }
        if (labelPoint) break
      }
      if (!labelPoint) throw new Error(`No unique horizontal label run for ${edge.id}`)
    }
    planned.set(edge.id, { points, paths, labelPoint })
  }
  return planned
}

export const pathReachesTarget = (path: RoutePoint[], route: RoutePoint[]) =>
  samePoint(path[path.length - 1], route[route.length - 1])

export const snapRoute = (
  points: RoutePoint[],
  source: RoutePoint,
  target: RoutePoint,
  sourcePosition: Position,
  targetPosition: Position
): RoutePoint[] => {
  const snapped = points.map(point => ({ ...point }))
  if (snapped.length < 2) return snapped
  const [first, second] = snapped
  if (first.y === second.y && (second.x > first.x ? Position.Right : Position.Left) === sourcePosition) {
    first.x = source.x
  } else if (first.x === second.x && (second.y > first.y ? Position.Bottom : Position.Top) === sourcePosition) {
    first.y = source.y
  }
  const last = snapped[snapped.length - 1]
  const previous = snapped[snapped.length - 2]
  if (last.y === previous.y && (last.x > previous.x ? Position.Left : Position.Right) === targetPosition) {
    last.x = target.x
  } else if (last.x === previous.x && (last.y > previous.y ? Position.Top : Position.Bottom) === targetPosition) {
    last.y = target.y
  }
  return snapped
}

export const snapEdgeRoute = (
  route: EdgeRoute,
  source: RoutePoint,
  target: RoutePoint,
  sourcePosition: Position,
  targetPosition: Position
): EdgeRoute => {
  const points = snapRoute(route.points, source, target, sourcePosition, targetPosition)
  return {
    ...route,
    points,
    paths: route.paths.map(path =>
      path.map(point => {
        if (samePoint(point, route.points[0])) return points[0]
        if (samePoint(point, route.points[route.points.length - 1])) return points[points.length - 1]
        return point
      })
    ),
  }
}
