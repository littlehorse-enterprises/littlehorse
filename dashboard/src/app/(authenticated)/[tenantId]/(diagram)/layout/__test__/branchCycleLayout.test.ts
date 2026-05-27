import { Edge, Node } from 'reactflow'
import { extractEdges } from '../../components/EdgeTypes/extractEdges'
import { extractNodes, getCycleNodes } from '../../components/NodeTypes/extractNodes'
import { layoutWorkflow } from '../workflowLayout'

const threadSpec = {
  nodes: {
    '0-entrypoint-ENTRYPOINT': {
      outgoingEdges: [{ sinkNodeName: '4-nop-NOP', variableMutations: [] }],
      failureHandlers: [],
      node: { $case: 'entrypoint', value: {} },
    },
    '4-nop-NOP': {
      outgoingEdges: [
        { sinkNodeName: '3-greet-TASK', edgeCondition: { left: {}, right: {} }, variableMutations: [] },
        { sinkNodeName: '7-greet-TASK', variableMutations: [] },
      ],
      failureHandlers: [],
      node: { $case: 'nop', value: {} },
    },
    '3-greet-TASK': {
      outgoingEdges: [{ sinkNodeName: '8-nop-NOP', variableMutations: [] }],
      failureHandlers: [],
      node: { $case: 'task', value: { taskToExecute: { $case: 'taskDefId', value: { name: 'greet' } } } },
    },
    '7-greet-TASK': {
      outgoingEdges: [{ sinkNodeName: '8-nop-NOP', variableMutations: [] }],
      failureHandlers: [],
      node: { $case: 'task', value: { taskToExecute: { $case: 'taskDefId', value: { name: 'greet' } } } },
    },
    '8-nop-NOP': {
      outgoingEdges: [{ sinkNodeName: '10-nop-NOP', variableMutations: [] }],
      failureHandlers: [],
      node: { $case: 'nop', value: {} },
    },
    '10-nop-NOP': {
      outgoingEdges: [{ sinkNodeName: '3-greet-TASK', variableMutations: [] }],
      failureHandlers: [],
      node: { $case: 'nop', value: {} },
    },
    '20-exit-EXIT': {
      outgoingEdges: [],
      failureHandlers: [],
      node: { $case: 'exit', value: {} },
    },
  },
} as any

getCycleNodes(threadSpec)

describe('branching and cycle fixture layout', () => {
  it('lays out branching workflow without ELK errors', async () => {
    const nodes = extractNodes(threadSpec).map(node => ({ ...node, width: 150, height: 50 })) as Node[]
    const edges = extractEdges(threadSpec) as Edge[]
    const result = await layoutWorkflow(nodes, edges)

    expect(result.nodes.some(node => node.position.x > 0)).toBe(true)
    expect(result.edges.every(edge => edge.data?.layoutPath)).toBe(true)
    expect(result.nodes.some(node => node.type === 'cycle')).toBe(true)
  })
})
