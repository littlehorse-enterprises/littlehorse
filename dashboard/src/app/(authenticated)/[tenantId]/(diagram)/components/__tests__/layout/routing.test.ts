import { Edge as EdgeProto } from 'littlehorse-client/proto'
import { Edge, Position } from 'reactflow'
import { planRoutes, pathReachesTarget, snapRoute, unsharedPaths } from '../../EdgeTypes/elkRoute'
import { DiagramEdgeData } from '../../EdgeTypes/extractEdges'
import { checkScene } from '../../layoutHarness/invariants'
import { SceneEdge } from '../../layoutHarness/layoutModel'
import { layoutDiagram } from '../../graphLayout'
import { crossingCount } from '../../layoutHarness/geometry'

const edge = (id: string, source = id, target = 'sink'): Edge<DiagramEdgeData> => ({
  id,
  source,
  target,
  sourceHandle: 'source-0',
  targetHandle: 'target-0',
  data: EdgeProto.create(),
})

const sceneEdge = (id: string): SceneEdge => ({
  id,
  source: id,
  target: 'sink',
  targetHandle: 'target-0',
  isCycleEdge: false,
  hasLabel: true,
  missingSourceHandle: false,
  missingTargetHandle: false,
  labelRect: { x: 60, y: 40, w: 40, h: 20 },
  polyline: [
    { x: 20, y: 50 },
    { x: 100, y: 50 },
  ],
  paths: [
    [
      { x: 20, y: 50 },
      { x: 100, y: 50 },
    ],
  ],
})

test('shared target tails and even small label intersections are defects', () => {
  const a = sceneEdge('a')
  const b = { ...sceneEdge('b'), labelRect: { x: 97, y: 40, w: 40, h: 20 } }
  const { defects } = checkScene({
    nodes: ['a', 'b', 'sink'].map(id => ({
      id,
      type: id === 'sink' ? 'exit' : 'entrypoint',
      rect: { x: id === 'sink' ? 100 : 0, y: 0, w: 20, h: 20 },
    })),
    edges: [a, b],
  })
  expect(defects.map(defect => defect.type)).toEqual(
    expect.arrayContaining(['edges-share-lane', 'label-covers-label', 'handle-fan-in'])
  )
})

test('subtracts partial collinear tails without deleting a branch', () => {
  const a = [
    { x: 0, y: 0 },
    { x: 100, y: 0 },
  ]
  const b = [
    { x: 0, y: 40 },
    { x: 60, y: 40 },
    { x: 60, y: 0 },
    { x: 100, y: 0 },
  ]
  expect(unsharedPaths(b, [a])).toEqual([
    [
      { x: 0, y: 40 },
      { x: 60, y: 40 },
      { x: 60, y: 0 },
    ],
  ])
  const routes = planRoutes(
    [edge('a'), edge('b')],
    [
      { id: 'a', points: a },
      { id: 'b', points: b },
    ]
  )
  expect(
    [...routes.values()].flatMap(route => route.paths.filter(path => pathReachesTarget(path, route.points)))
  ).toHaveLength(1)
  expect(b[b.length - 1]).toEqual({ x: 100, y: 0 })
})

test('unrelated shared lanes are not hidden by stroke deduplication', () => {
  const points = [
    { x: 0, y: 0 },
    { x: 100, y: 0 },
  ]
  const routes = planRoutes(
    [edge('a', 'a', 'c'), edge('b', 'b', 'd')],
    [
      { id: 'a', points },
      { id: 'b', points },
    ]
  )
  expect(routes.get('b')?.paths).toEqual([points])
})

test('shared trunk junctions are not transversal crossings', () => {
  const horizontal = [
    { x: 0, y: 0 },
    { x: 100, y: 0 },
  ]
  expect(
    crossingCount(horizontal, [
      { x: 50, y: 0 },
      { x: 50, y: 40 },
    ])
  ).toBe(0)
  expect(
    crossingCount(horizontal, [
      { x: 50, y: -40 },
      { x: 50, y: 40 },
    ])
  ).toBe(1)
})

test('snapping only extends the endpoint on the side its handle faces', () => {
  const points = [
    { x: 0, y: 0 },
    { x: 0, y: 40 },
    { x: 100, y: 40 },
  ]
  expect(snapRoute(points, { x: 20, y: 10 }, { x: 120, y: 40 }, Position.Right, Position.Left)).toEqual([
    { x: 0, y: 0 },
    { x: 0, y: 40 },
    { x: 120, y: 40 },
  ])
})

test('parallel conditional branches get separate horizontal label runs and one arrow', async () => {
  const edges = [edge('a', 'source'), edge('b', 'source'), edge('c', 'source')].map(e => ({
    ...e,
    data: { ...EdgeProto.create(), isElseEdge: true },
  }))
  const { routes } = await layoutDiagram(
    ['source', 'sink'].map(id => ({ id, type: 'nop', data: {}, position: { x: 0, y: 0 } })),
    edges
  )
  expect(new Set([...routes.values()].map(route => JSON.stringify(route.labelPoint))).size).toBe(3)
  expect(
    [...routes.values()].flatMap(route => route.paths.filter(path => pathReachesTarget(path, route.points)))
  ).toHaveLength(1)
})
