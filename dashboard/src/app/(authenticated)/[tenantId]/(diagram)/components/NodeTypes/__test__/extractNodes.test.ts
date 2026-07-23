import type { ThreadSpec } from 'littlehorse-client/proto'
import { extractNodes, getNodeAfterEntrypoint } from '../extractNodes'

const node = (kind: string, payload: Record<string, unknown> = {}, outgoing: string[] = []) => ({
  outgoingEdges: outgoing.map(sink => ({
    sinkNodeName: sink,
    variableMutations: [],
    edgeCondition: { oneofKind: undefined },
  })),
  failureHandlers: [],
  node: { oneofKind: kind, [kind]: payload },
})

const spec = (nodes: Record<string, ReturnType<typeof node>>): ThreadSpec =>
  ({ nodes }) as unknown as ThreadSpec

describe('extractNodes', () => {
  it('types each reactflow node by its oneof kind', () => {
    const nodes = extractNodes(
      spec({
        entrypoint: node('entrypoint', {}, ['greet']),
        greet: node('task', { taskDefId: { name: 'greet' } }),
        end: node('exit'),
      })
    )
    expect(nodes.map(n => n.type)).toEqual(['entrypoint', 'task', 'exit'])
  })

  it('spreads the node-type payload onto data so components can read it', () => {
    const nodes = extractNodes(spec({ greet: node('task', { taskDefId: { name: 'greet' } }) }))
    const greet = nodes.find(n => n.id === 'greet')!
    expect((greet.data as { taskDefId?: { name: string } }).taskDefId?.name).toBe('greet')
  })

  it('getNodeAfterEntrypoint returns the entrypoint node\'s first outgoing sink', () => {
    const threadSpec = spec({
      entrypoint: node('entrypoint', {}, ['greet']),
      greet: node('task'),
    })
    expect(getNodeAfterEntrypoint(threadSpec)).toBe('greet')
  })

  it('getNodeAfterEntrypoint returns undefined when there is no entrypoint', () => {
    expect(getNodeAfterEntrypoint(spec({ greet: node('task') }))).toBeUndefined()
  })
})
