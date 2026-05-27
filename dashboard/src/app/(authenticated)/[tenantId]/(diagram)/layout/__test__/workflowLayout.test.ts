import ELK from 'elkjs/lib/elk.bundled.js'
import { Edge, Node } from 'reactflow'
import { layoutWorkflow } from '../workflowLayout'

const mockNodes: Node[] = [
  { id: 'entry', type: 'entrypoint', position: { x: 0, y: 0 }, data: {}, width: 80, height: 40 },
  { id: 'nop-1', type: 'nop', position: { x: 0, y: 0 }, data: { outgoingEdges: [{ sinkNodeName: 'task-a' }, { sinkNodeName: 'task-b' }] }, width: 80, height: 40 },
  { id: 'task-a', type: 'task', position: { x: 0, y: 0 }, data: {}, width: 120, height: 50 },
  { id: 'task-b', type: 'task', position: { x: 0, y: 0 }, data: {}, width: 120, height: 50 },
]

const mockEdges: Edge[] = [
  { id: 'e1', source: 'entry', target: 'nop-1', sourceHandle: 'source-0', targetHandle: 'target-0' },
  { id: 'e2', source: 'nop-1', target: 'task-a', sourceHandle: 'source-0', targetHandle: 'target-0' },
  { id: 'e3', source: 'nop-1', target: 'task-b', sourceHandle: 'source-1', targetHandle: 'target-0' },
]

describe('layoutWorkflow integration', () => {
  it('assigns non-zero positions from ELK', async () => {
    const result = await layoutWorkflow(mockNodes, mockEdges)
    expect(result.nodes.some(node => node.position.x > 0 || node.position.y > 0)).toBe(true)
    expect(result.edges.every(edge => edge.data?.layoutPath)).toBe(true)
  })

  it('runs ELK directly with ports', async () => {
    const elk = new ELK()
    try {
      const graph = await elk.layout({
        id: 'root',
        layoutOptions: { 'elk.algorithm': 'layered', 'elk.direction': 'RIGHT' },
        children: [
          { id: 'a', width: 100, height: 50, ports: [{ id: 'a:out', width: 1, height: 1, layoutOptions: { 'org.eclipse.elk.port.side': 'EAST' } }] },
          { id: 'b', width: 100, height: 50, ports: [{ id: 'b:in', width: 1, height: 1, layoutOptions: { 'org.eclipse.elk.port.side': 'WEST' } }] },
        ],
        edges: [{ id: 'e', sources: ['a:out'], targets: ['b:in'] }],
      })
      expect(graph.children?.[1]?.x).toBeGreaterThan(0)
    } catch (error) {
      throw error
    }
  })
})
