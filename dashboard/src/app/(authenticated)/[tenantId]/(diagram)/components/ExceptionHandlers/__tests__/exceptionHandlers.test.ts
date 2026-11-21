import { WorkflowDefinition } from '@/types'
import { LHStatus, WfSpec } from 'littlehorse-client/proto'
import { readFileSync } from 'fs'
import { join } from 'path'
import { extractEdges } from '../../EdgeTypes/extractEdges'
import { layoutDiagram } from '../../graphLayout'
import { extractNodes } from '../../NodeTypes/extractNodes'
import {
  chipFootprint,
  extractExceptionHandlers,
  handlerHeaderWidth,
  listHandlerGroups,
  rootSourceNodeId,
  type ExceptionHandlerData,
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

describe('handler layout', () => {
  const layout = async (spec: WorkflowDefinition, runOf?: () => typeof handled) => {
    const thread = spec.threadSpecs.entrypoint
    const handlers = extractExceptionHandlers(spec, 'entrypoint', { runOf })
    const nodes = [...extractNodes(thread), ...handlers.nodes]
    const edges = [...extractEdges(thread), ...handlers.edges]
    return { nodes, ...(await layoutDiagram(nodes, edges)) }
  }

  test('the box contains its entry and exit, and is wide enough for its header and badge', async () => {
    const { nodes, boxes } = await layout(loadSpec(), () => handled)
    const group = boxes.get(GROUP)!
    const data = nodes.find(node => node.id === GROUP)!.data as ExceptionHandlerData
    expect(group.width).toBeGreaterThanOrEqual(handlerHeaderWidth(data))

    for (const child of ['0-entrypoint-ENTRYPOINT', '2-exit-EXIT']) {
      const box = boxes.get(`${GROUP}/${child}`)!
      expect(box.x!).toBeGreaterThanOrEqual(group.x!)
      expect(box.y!).toBeGreaterThanOrEqual(group.y!)
      expect(box.x! + box.width!).toBeLessThanOrEqual(group.x! + group.width!)
      expect(box.y! + box.height!).toBeLessThanOrEqual(group.y! + group.height!)
    }
  })

  test('a long handler name still leaves room for the badge', async () => {
    const spec = loadSpec()
    const longName = 'exn-handler-a-really-long-handler-thread-name-that-would-clip-the-badge'
    const thread = WfSpec.clone(spec as WfSpec).threadSpecs.entrypoint
    thread.nodes['1-fail-TASK'].failureHandlers[0].handlerSpecName = longName
    const renamed = {
      ...spec,
      threadSpecs: { ...spec.threadSpecs, entrypoint: thread, [longName]: spec.threadSpecs[HANDLER] },
    }
    const { nodes, boxes } = await layout(renamed, () => handled)
    const data = nodes.find(node => node.id === GROUP)!.data as ExceptionHandlerData
    expect(boxes.get(GROUP)!.width).toBeGreaterThanOrEqual(handlerHeaderWidth(data))
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
