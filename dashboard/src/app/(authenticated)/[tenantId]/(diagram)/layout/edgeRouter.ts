export type Point = { x: number; y: number }

export type ElkEdgeSection = {
  startPoint: Point
  endPoint: Point
  bendPoints?: Point[]
}

export const pointsToPath = (points: Point[]): string => {
  if (points.length === 0) return ''
  const [first, ...rest] = points
  return `M ${first.x} ${first.y} ${rest.map(point => `L ${point.x} ${point.y}`).join(' ')}`
}

export const sectionToPath = (section: ElkEdgeSection): string => {
  const points = [section.startPoint, ...(section.bendPoints ?? []), section.endPoint]
  return pointsToPath(points)
}

export const pathMidpoint = (points: Point[]): Point => {
  if (points.length === 0) return { x: 0, y: 0 }
  if (points.length === 1) return points[0]

  let totalLength = 0
  const segments: Array<{ start: Point; end: Point; length: number }> = []

  for (let i = 0; i < points.length - 1; i++) {
    const start = points[i]
    const end = points[i + 1]
    const length = Math.hypot(end.x - start.x, end.y - start.y)
    segments.push({ start, end, length })
    totalLength += length
  }

  const half = totalLength / 2
  let walked = 0

  for (const segment of segments) {
    if (walked + segment.length >= half) {
      const ratio = (half - walked) / segment.length
      return {
        x: segment.start.x + (segment.end.x - segment.start.x) * ratio,
        y: segment.start.y + (segment.end.y - segment.start.y) * ratio,
      }
    }
    walked += segment.length
  }

  return points[points.length - 1]
}

export const routeForwardEdge = (
  source: Point,
  target: Point,
  channelIndex: number,
  channelCount: number
): { path: string; labelPoint: Point } => {
  const gap = 24
  const channelOffset = (channelIndex - (channelCount - 1) / 2) * gap
  const midX = source.x + (target.x - source.x) / 2
  const bendY = source.y + channelOffset

  const points =
    Math.abs(source.y - target.y) < 8
      ? [source, { x: target.x, y: source.y }]
      : [source, { x: midX, y: bendY }, { x: midX, y: target.y }, target]

  return { path: pointsToPath(points), labelPoint: pathMidpoint(points) }
}
