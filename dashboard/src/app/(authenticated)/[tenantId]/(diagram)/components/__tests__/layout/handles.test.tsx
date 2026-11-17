import { render } from '@testing-library/react'
import { ComponentProps } from 'react'
import { ExitNode, Node as NodeProto, NodeRun, ThreadSpec, VariableAssignment } from 'littlehorse-client/proto'
import { ReactFlowProvider } from 'reactflow'
import { Exit } from '../../NodeTypes/Exit'
import { Nop } from '../../NodeTypes/Nop'
import { extractNodes } from '../../NodeTypes/extractNodes'
import { rendersHandle } from '../../layoutHarness/layoutModel'
import { extractEdges } from '../../EdgeTypes/extractEdges'

jest.mock('../../NodeTypes/SelectedNode', () => ({ SelectedNode: () => null }))

const outgoingEdges = Array.from({ length: 10 }, (_, i) => ({
  sinkNodeName: `branch-${i}`,
  variableMutations: [],
  edgeCondition: { oneofKind: undefined },
}))
const data: ComponentProps<typeof Nop>['data'] = {
  ...NodeProto.create(),
  outgoingEdges,
  nodeRunsList: [{ ...NodeRun.create(), nodeType: { oneofKind: 'entrypoint', entrypoint: {} } }],
}
const props = {
  id: 'nop',
  type: 'nop',
  data,
  xPos: 0,
  yPos: 0,
  zIndex: 1,
  selected: false,
  dragging: false,
  isConnectable: false,
}

test('NOP has one east source and one west target', () => {
  const { container } = render(
    <ReactFlowProvider>
      <Nop {...props} />
    </ReactFlowProvider>
  )
  expect([...container.querySelectorAll('.source')].map(handle => handle.getAttribute('data-handleid'))).toEqual([
    'source-0',
  ])
  expect(container.querySelectorAll('.react-flow__handle-right')).toHaveLength(1)
  expect(container.querySelectorAll('.react-flow__handle-left')).toHaveLength(1)
  expect(container.querySelectorAll('.react-flow__handle-top')).toHaveLength(0)
  expect(container.querySelectorAll('.react-flow__handle-bottom')).toHaveLength(0)
})

test('extraction uses rendered handles and invalid ids do not resolve', () => {
  const spec = ThreadSpec.create({ nodes: { nop: data } })
  const edges = extractEdges(spec)
  expect(edges.every(edge => edge.sourceHandle === 'source-0')).toBe(true)
  for (const edge of edges) expect(rendersHandle('nop', 'source', edge.sourceHandle!)).toBe(true)
  expect(rendersHandle('nop', 'source', 'source-1')).toBe(false)
  expect(rendersHandle('task', 'target', 'target-1')).toBe(false)
  expect(rendersHandle('entrypoint', 'target', 'target-0')).toBe(false)
  expect(rendersHandle('exit', 'source', 'source-0')).toBe(true)
})

test('a while loop is drawn exactly as the spec gives it, back edge and condition included', () => {
  // The shape a doWhile compiles to: loop-start -> body -> loop-end, with a
  // conditional edge from loop-end back to loop-start.
  const condition = {
    oneofKind: 'condition' as const,
    condition: VariableAssignment.create({ source: { oneofKind: 'variableName', variableName: 'keep-looping' } }),
  }
  const edge = (sinkNodeName: string, edgeCondition: NodeProto['outgoingEdges'][number]['edgeCondition']) => ({
    sinkNodeName,
    variableMutations: [],
    edgeCondition,
  })
  const nop = (outgoing: NodeProto['outgoingEdges']) =>
    NodeProto.create({ node: { oneofKind: 'nop', nop: {} }, outgoingEdges: outgoing })
  const spec = ThreadSpec.create({
    nodes: {
      '0-entrypoint-ENTRYPOINT': NodeProto.create({
        node: { oneofKind: 'entrypoint', entrypoint: {} },
        outgoingEdges: [edge('1-nop-NOP', { oneofKind: undefined })],
      }),
      '1-nop-NOP': nop([edge('2-body-TASK', condition), edge('3-nop-NOP', { oneofKind: undefined })]),
      '2-body-TASK': nop([edge('3-nop-NOP', { oneofKind: undefined })]),
      '3-nop-NOP': nop([edge('1-nop-NOP', condition), edge('4-exit-EXIT', { oneofKind: undefined })]),
      '4-exit-EXIT': NodeProto.create({ node: { oneofKind: 'exit', exit: ExitNode.create() } }),
    },
  })
  const before = ThreadSpec.clone(spec)

  expect(extractNodes(spec).map(node => node.id)).toEqual(Object.keys(before.nodes))
  const edges = extractEdges(spec)
  expect(edges.map(e => `${e.source}->${e.target}`)).toEqual(
    Object.entries(before.nodes).flatMap(([id, node]) => node.outgoingEdges.map(e => `${id}->${e.sinkNodeName}`))
  )
  const backEdge = edges.find(e => e.source === '3-nop-NOP' && e.target === '1-nop-NOP')!
  expect(backEdge.data?.edgeCondition).toEqual(condition)
  expect(spec).toEqual(before)
})

test('an EXIT node can draw the outgoing edges the spec gives it', () => {
  // fail() inside a doElse compiles to an EXIT node that still has an edge to
  // the NOP closing the if/else. The edge is in the spec, so it is drawn.
  const { container } = render(
    <ReactFlowProvider>
      <Exit {...(props as unknown as ComponentProps<typeof Exit>)} id="exit" type="exit" />
    </ReactFlowProvider>
  )
  expect(container.querySelector('.source')).toHaveClass('react-flow__handle-right')
  expect(container.querySelector('.target')).toHaveClass('react-flow__handle-left')
})
