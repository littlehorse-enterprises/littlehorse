import { pathMidpoint, pointsToPath, routeForwardEdge } from '../edgeRouter'
import { getNopSourceHandleConfigs } from '../nopHandles'

describe('edgeRouter', () => {
  it('builds an orthogonal path from points', () => {
    expect(pointsToPath([{ x: 0, y: 0 }, { x: 10, y: 0 }, { x: 10, y: 20 }])).toBe(
      'M 0 0 L 10 0 L 10 20'
    )
  })

  it('finds the midpoint of a path', () => {
    expect(pathMidpoint([{ x: 0, y: 0 }, { x: 100, y: 0 }])).toEqual({ x: 50, y: 0 })
  })

  it('offsets parallel forward edges', () => {
    const first = routeForwardEdge({ x: 0, y: 0 }, { x: 200, y: 80 }, 0, 2)
    const second = routeForwardEdge({ x: 0, y: 0 }, { x: 200, y: 80 }, 1, 2)
    expect(first.path).not.toEqual(second.path)
  })
})

describe('nopHandles', () => {
  it('places two branch handles on top and bottom', () => {
    const handles = getNopSourceHandleConfigs([{ sinkNodeName: 'a' }, { sinkNodeName: 'b' }])
    expect(handles).toHaveLength(2)
    expect(handles[0].id).toBe('source-0')
    expect(handles[1].id).toBe('source-1')
  })

  it('reserves a bottom handle for cycle edges', () => {
    const handles = getNopSourceHandleConfigs([
      { sinkNodeName: 'body' },
      { sinkNodeName: 'cycle-0-1' },
    ])
    expect(handles).toHaveLength(2)
    expect(handles[1].side).toBe('SOUTH')
  })
})
