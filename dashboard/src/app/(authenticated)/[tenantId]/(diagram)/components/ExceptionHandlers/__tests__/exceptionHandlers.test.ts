import { WorkflowDefinition } from '@/types'
import { LHStatus, NodeRun, ThreadRun, WfSpec } from 'littlehorse-client/proto'
import { readFileSync } from 'fs'
import { join } from 'path'
import { extractEdges } from '../../EdgeTypes/extractEdges'
import { handlerFrames, layoutDiagram } from '../../graphLayout'
import { extractNodes } from '../../NodeTypes/extractNodes'
import {
  chipFootprint,
  extractExceptionHandlers,
  findHandlerRuns,
  HANDLER_LABEL_BAND,
  HANDLER_PADDING,
  NODE_CAPTION_HEIGHT,
  handlerLabelWidth,
  listHandlerGroups,
  rootSourceNodeId,
  type ExceptionHandlerData,
  type HandlerView,
} from '../exceptionHandlers'

// The WfSpec registered by examples/java/exception-handler.
const loadSpec = (): WorkflowDefinition =>
  WfSpec.fromJsonString(readFileSync(join(__dirname, 'example-exception-handler.json'), 'utf-8'), {
    ignoreUnknownFields: true,
  })

const GROUP = 'handler:1-fail-TASK:0'
const HANDLER = 'exn-handler-1-fail-TASK-FAILURE_TYPE_ERROR'
const handled = { number: 1, status: LHStatus.COMPLETED, failureName: 'TASK_FAILURE', failureMessage: 'Yikes' }

/** Gives the handler a second, failing exit, as a handler that rethrows has. */
const withRethrowingHandler = (spec: WorkflowDefinition): WorkflowDefinition => {
  const handler = WfSpec.clone(spec as WfSpec).threadSpecs[HANDLER]
  const exit = handler.nodes['2-exit-EXIT']
  exit.node = {
    oneofKind: 'exit',
    exit: { result: { oneofKind: 'failureDef', failureDef: { failureName: 'oops', message: 'rethrown' } } },
  }
  return { ...spec, threadSpecs: { ...spec.threadSpecs, [HANDLER]: handler } }
}

describe('extractExceptionHandlers', () => {
  test('an expanded handler holds its own entry, nodes and exit', () => {
    const { nodes, edges } = extractExceptionHandlers(loadSpec(), 'entrypoint')

    expect(nodes.map(node => [node.id, node.type])).toEqual([
      [GROUP, 'exceptionHandler'],
      [`${GROUP}/0-entrypoint-ENTRYPOINT`, 'entrypoint'],
      [`${GROUP}/1-my-task-TASK`, 'task'],
      [`${GROUP}/2-exit-EXIT`, 'exit'],
    ])
    expect(nodes.slice(1).every(node => node.data.groupId === GROUP)).toBe(true)

    const throwEdge = edges.find(edge => edge.data?.isExceptionEdge)
    expect(throwEdge).toMatchObject({
      source: '1-fail-TASK',
      sourceHandle: 'source-failure',
      target: `${GROUP}/0-entrypoint-ENTRYPOINT`,
      targetHandle: 'target-0',
    })
    // The handler's exit resumes where the failing task would have gone next.
    expect(edges.filter(edge => edge.data?.isResumeEdge).map(edge => [edge.source, edge.target])).toEqual([
      [`${GROUP}/2-exit-EXIT`, '2-my-task-TASK'],
    ])
    expect(edges.filter(edge => !edge.data?.isExceptionEdge && !edge.data?.isResumeEdge).map(e => e.target)).toEqual([
      `${GROUP}/1-my-task-TASK`,
      `${GROUP}/2-exit-EXIT`,
    ])
  })

  test('a rethrowing handler shows its failing exit and never resumes', () => {
    const { nodes, edges } = extractExceptionHandlers(withRethrowingHandler(loadSpec()), 'entrypoint')
    expect(nodes.map(node => node.id)).toContain(`${GROUP}/2-exit-EXIT`)
    expect(edges.some(edge => edge.data?.isResumeEdge)).toBe(false)
    expect((nodes[0].data as ExceptionHandlerData).rethrows).toBe(true)
  })

  test('a collapsed handler is one chip on the exception edge', () => {
    const { nodes, edges } = extractExceptionHandlers(loadSpec(), 'entrypoint', {
      isCollapsed: id => id === GROUP,
      runOf: () => handled,
    })
    expect(nodes.map(node => [node.id, node.type])).toEqual([[GROUP, 'exceptionHandlerChip']])
    expect(nodes[0].data.run).toEqual(handled)
    expect(nodes[0].data.footprint).toEqual(chipFootprint(nodes[0].data))
    expect(edges.map(edge => [edge.source, edge.sourceHandle, edge.target])).toEqual([
      ['1-fail-TASK', 'source-failure', GROUP],
    ])
  })

  test('hidden handlers draw nothing', () => {
    expect(extractExceptionHandlers(loadSpec(), 'entrypoint', { isVisible: () => false })).toEqual({
      nodes: [],
      edges: [],
    })
  })

  test('every group is listed whatever is drawn, with the node it belongs to', () => {
    const groups = listHandlerGroups(loadSpec(), 'entrypoint')
    expect(groups.map(group => group.groupId)).toEqual([GROUP])
    expect(rootSourceNodeId(GROUP)).toBe('1-fail-TASK')
    expect(rootSourceNodeId(`${GROUP}/handler:1-my-task-TASK:0`)).toBe('1-fail-TASK')
  })
})

test('a repeated throwing node shows its latest handler run, even when node runs arrive out of order', () => {
  const groups = listHandlerGroups(loadSpec(), 'entrypoint')
  const sourceRun = (position: number, handlerNumber: number) =>
    NodeRun.create({
      id: { position },
      nodeName: '1-fail-TASK',
      failures: [{ failureName: 'TASK_FAILURE', failureHandlerThreadrunId: handlerNumber }],
    })
  const child = NodeRun.create({ nodeName: '1-my-task-TASK' })
  const threads = [1, 2].map(number => ({
    ...ThreadRun.create({ number, threadSpecName: HANDLER, status: LHStatus.COMPLETED }),
    nodeRuns: number === 2 ? [child] : [],
  }))

  const runs = findHandlerRuns(groups, [sourceRun(8, 2), sourceRun(3, 1)], threads)

  expect(runs.get(GROUP)).toMatchObject({ run: { number: 2, failureName: 'TASK_FAILURE' }, nodeRuns: [child] })
})

type Box = { x: number; y: number; w: number; h: number }
const overlaps = (a: Box, b: Box) => a.x < b.x + b.w && b.x < a.x + a.w && a.y < b.y + b.h && b.y < a.y + a.h
const LONG_NAME = 'exn-handler-a-really-long-handler-thread-name-that-would-clip-the-badge'

/** The same workflow, with the handler thread under a much longer name. */
const withLongHandlerName = (spec: WorkflowDefinition): WorkflowDefinition => {
  const thread = WfSpec.clone(spec as WfSpec).threadSpecs.entrypoint
  thread.nodes['1-fail-TASK'].failureHandlers[0].handlerSpecName = LONG_NAME
  return { ...spec, threadSpecs: { ...spec.threadSpecs, entrypoint: thread, [LONG_NAME]: spec.threadSpecs[HANDLER] } }
}

describe('handler layout', () => {
  const layout = async (spec: WorkflowDefinition, view?: HandlerView) => {
    const thread = spec.threadSpecs.entrypoint
    const handlers = extractExceptionHandlers(spec, 'entrypoint', view)
    const nodes = [...extractNodes(thread), ...handlers.nodes]
    const edges = [...extractEdges(thread), ...handlers.edges]
    return { nodes, ...(await layoutDiagram(nodes, edges)) }
  }
  const rect = (box: { x?: number; y?: number; width?: number; height?: number }): Box => ({
    x: box.x!,
    y: box.y!,
    w: box.width!,
    h: box.height!,
  })

  test('the box fits its entry, nodes and exit with padding, and nothing more', async () => {
    const { boxes, frames } = await layout(loadSpec(), { runOf: () => handled })
    const frame = frames.get(GROUP)!
    const inside = ['0-entrypoint-ENTRYPOINT', '1-my-task-TASK', '2-exit-EXIT'].map(id =>
      rect(boxes.get(`${GROUP}/${id}`)!)
    )
    expect(frame.x).toBe(Math.min(...inside.map(b => b.x)) - HANDLER_PADDING)
    expect(frame.y).toBe(Math.min(...inside.map(b => b.y)) - HANDLER_PADDING - NODE_CAPTION_HEIGHT)
    expect(frame.x + frame.w).toBe(Math.max(...inside.map(b => b.x + b.w)) + HANDLER_PADDING)
    expect(frame.y + frame.h).toBe(Math.max(...inside.map(b => b.y + b.h)) + HANDLER_PADDING)
  })

  test('the header text never sizes the box', async () => {
    const short = (await layout(loadSpec(), { runOf: () => handled })).frames.get(GROUP)!
    const long = (await layout(withLongHandlerName(loadSpec()), { runOf: () => handled })).frames.get(GROUP)!
    const plain = (await layout(loadSpec())).frames.get(GROUP)!
    expect([long.w, long.h]).toEqual([short.w, short.h])
    expect([plain.w, plain.h]).toEqual([short.w, short.h])
  })

  test('the label row above the box is reserved: nothing else is placed or routed through it', async () => {
    const { nodes, boxes, frames, routes } = await layout(loadSpec(), { runOf: () => handled })
    const frame = frames.get(GROUP)!
    const data = nodes.find(node => node.id === GROUP)!.data as ExceptionHandlerData
    const labels: Box = {
      x: frame.x,
      y: frame.y - HANDLER_LABEL_BAND,
      w: Math.max(frame.w, handlerLabelWidth(data)),
      h: HANDLER_LABEL_BAND,
    }

    // Inside the space ELK laid out for the group...
    const group = rect(boxes.get(GROUP)!)
    expect(labels.x).toBeGreaterThanOrEqual(group.x)
    expect(labels.y).toBeGreaterThanOrEqual(group.y)
    expect(labels.x + labels.w).toBeLessThanOrEqual(group.x + group.w)

    // ...so it misses every node outside the handler and every edge.
    for (const node of nodes.filter(node => !node.data?.groupId && node.type !== 'exceptionHandler')) {
      expect(overlaps(labels, rect(boxes.get(node.id)!))).toBe(false)
    }
    for (const [id, route] of routes) {
      const points = route.points
      for (let i = 1; i < points.length; i++) {
        const [a, b] = [points[i - 1], points[i]]
        const segment = {
          x: Math.min(a.x, b.x),
          y: Math.min(a.y, b.y),
          w: Math.abs(a.x - b.x) || 1,
          h: Math.abs(a.y - b.y) || 1,
        }
        expect({ id, hit: overlaps(labels, segment) }).toEqual({ id, hit: false })
      }
    }
  })

  test('with real rendered sizes the box hugs the nodes and stays inside the reserved space', async () => {
    const { nodes, boxes } = await layout(loadSpec(), { runOf: () => handled })
    // As in the browser: a task draws 31px tall, centered in its 50px footprint.
    const measured = (node: { type?: string }) => (node.type === 'task' ? { w: 110, h: 31 } : undefined)
    const frame = handlerFrames(nodes, boxes, measured).get(GROUP)!
    const footprintFrame = handlerFrames(nodes, boxes).get(GROUP)!
    expect(frame.y + frame.h).toBeLessThan(footprintFrame.y + footprintFrame.h)

    const group = rect(boxes.get(GROUP)!)
    expect(frame.y - HANDLER_LABEL_BAND).toBeGreaterThanOrEqual(group.y)
    expect(frame.x).toBeGreaterThanOrEqual(group.x)
    expect(frame.x + frame.w).toBeLessThanOrEqual(group.x + group.w)
    expect(frame.y + frame.h).toBeLessThanOrEqual(group.y + group.h)
  })

  test('a label row wider than the box still gets its room', async () => {
    // An empty handler (entry straight to exit) is narrower than its labels.
    const spec = loadSpec()
    const handler = WfSpec.clone(spec as WfSpec).threadSpecs[HANDLER]
    delete handler.nodes['1-my-task-TASK']
    handler.nodes['0-entrypoint-ENTRYPOINT'].outgoingEdges[0].sinkNodeName = '2-exit-EXIT'
    const empty = { ...spec, threadSpecs: { ...spec.threadSpecs, [HANDLER]: handler } }
    const { nodes, boxes, frames } = await layout(empty, { runOf: () => handled })
    const frame = frames.get(GROUP)!
    const width = handlerLabelWidth(nodes.find(node => node.id === GROUP)!.data as ExceptionHandlerData)
    expect(width).toBeGreaterThan(frame.w)
    const group = rect(boxes.get(GROUP)!)
    expect(frame.x).toBe(group.x)
    expect(frame.x + width).toBeLessThanOrEqual(group.x + group.w)
  })
})

test('a collapsed handler lays out', async () => {
  const spec = loadSpec()
  const thread = spec.threadSpecs.entrypoint
  const handlers = extractExceptionHandlers(spec, 'entrypoint', { isCollapsed: () => true, runOf: () => handled })
  const nodes = [...extractNodes(thread), ...handlers.nodes]
  const edges = [...extractEdges(thread), ...handlers.edges]
  const { boxes, routes } = await layoutDiagram(nodes, edges)
  expect(boxes.get(GROUP)).toBeDefined()
  expect(routes.get(`${GROUP}/throw`)).toBeDefined()
})
