import type { ThreadSpec } from 'littlehorse-client/proto'
import { extractEdges } from '../extractEdges'

/**
 * Minimal proto-shaped builders. With @protobuf-ts, `edgeCondition` is a oneof group
 * that is ALWAYS present as an object: `{ oneofKind: undefined }` when there is no
 * condition. These tests pin that behavior so the "empty label on every edge" /
 * "else edge never detected" regressions cannot come back silently.
 */
const edge = (sink: string, opts: { condition?: boolean } = {}) => ({
  sinkNodeName: sink,
  variableMutations: [],
  edgeCondition: opts.condition
    ? { oneofKind: 'condition' as const, condition: {} }
    : { oneofKind: undefined },
})

const spec = (nodes: Record<string, ReturnType<typeof edge>[]>): ThreadSpec =>
  ({
    nodes: Object.fromEntries(
      Object.entries(nodes).map(([name, outgoingEdges]) => [
        name,
        { outgoingEdges, failureHandlers: [], node: { oneofKind: undefined } },
      ])
    ),
  }) as unknown as ThreadSpec

const isElse = (edges: ReturnType<typeof extractEdges>, target: string) =>
  edges.find(e => e.target === target)!.data!.isElseEdge

describe('extractEdges', () => {
  it('builds one reactflow edge per outgoing edge, wired source -> sink', () => {
    const edges = extractEdges(spec({ a: [edge('b')], b: [] }))
    expect(edges).toHaveLength(1)
    expect(edges[0]).toMatchObject({ source: 'a', target: 'b', type: 'custom' })
  })

  it('marks the conditionless sibling as the else edge and leaves the conditional one alone', () => {
    const edges = extractEdges(
      spec({ gateway: [edge('task-a', { condition: true }), edge('task-b')], 'task-a': [], 'task-b': [] })
    )
    expect(isElse(edges, 'task-a')).toBe(false) // has a condition
    expect(isElse(edges, 'task-b')).toBe(true) // conditionless sibling -> else
  })

  it('never marks a lone outgoing edge as an else edge', () => {
    // Single outgoing edge, no condition: this is the common case that was rendering
    // an empty "else"/condition pill on every edge after the protobuf-ts switch.
    const edges = extractEdges(spec({ a: [edge('b')], b: [] }))
    expect(isElse(edges, 'b')).toBe(false)
  })

  it('treats a present-but-unset oneof ({ oneofKind: undefined }) as "no condition"', () => {
    const edges = extractEdges(spec({ a: [edge('b'), edge('c')], b: [], c: [] }))
    // both siblings are conditionless -> both are else candidates
    expect(edges.filter(e => e.data!.isElseEdge)).toHaveLength(2)
  })
})
