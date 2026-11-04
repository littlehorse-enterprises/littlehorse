import { render } from '@testing-library/react'
import { ComponentProps } from 'react'
import { Node as NodeProto, NodeRun } from 'littlehorse-client/proto'
import { ReactFlowProvider } from 'reactflow'
import { Nop } from '../../NodeTypes/Nop'
import { Cycle } from '../../NodeTypes/Cycle'
import { rendersHandle } from '../../layoutHarness/layoutModel'
import { extractEdges } from '../../EdgeTypes/extractEdges'
import { ThreadSpec } from 'littlehorse-client/proto'

jest.mock('../../NodeTypes/SelectedNode', () => ({ SelectedNode: () => null }))

const outgoingEdges = Array.from({ length: 10 }, (_, i) => ({
  sinkNodeName: `branch-${i}`,
  variableMutations: [],
  edgeCondition: { oneofKind: undefined },
}))
const data: ComponentProps<typeof Nop>['data'] = {
  ...NodeProto.create(),
  outgoingEdges: [...outgoingEdges, { ...outgoingEdges[0], sinkNodeName: 'cycle-test' }],
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

test('NOP has one east source, one west target and one optional south loop source', () => {
  const { container, rerender } = render(
    <ReactFlowProvider>
      <Nop {...props} />
    </ReactFlowProvider>
  )
  expect([...container.querySelectorAll('.source')].map(handle => handle.getAttribute('data-handleid'))).toEqual([
    'source-0',
    'source-loop',
  ])
  expect(container.querySelectorAll('.react-flow__handle-right')).toHaveLength(1)
  expect(container.querySelectorAll('.react-flow__handle-bottom')).toHaveLength(1)
  expect(container.querySelectorAll('.react-flow__handle-left')).toHaveLength(1)
  expect(container.querySelectorAll('.react-flow__handle-top')).toHaveLength(0)
  rerender(
    <ReactFlowProvider>
      <Nop {...props} data={{ ...data, outgoingEdges }} />
    </ReactFlowProvider>
  )
  expect(container.querySelectorAll('.source')).toHaveLength(1)
})

test('cycle glyphs follow the same east/west handle contract', () => {
  const { container } = render(
    <ReactFlowProvider>
      <Cycle {...props} type="cycle" />
    </ReactFlowProvider>
  )
  expect(container.querySelector('.source')).toHaveClass('react-flow__handle-right')
  expect(container.querySelector('.target')).toHaveClass('react-flow__handle-left')
})

test('extraction uses rendered handles and invalid ids do not resolve', () => {
  const spec = ThreadSpec.create({ nodes: { nop: data } })
  const edges = extractEdges(spec)
  expect(edges.slice(0, 10).every(edge => edge.sourceHandle === 'source-0')).toBe(true)
  expect(edges[10].sourceHandle).toBe('source-loop')
  for (const edge of edges) expect(rendersHandle('nop', 'source', edge.sourceHandle!, data.outgoingEdges)).toBe(true)
  expect(rendersHandle('nop', 'source', 'source-1', data.outgoingEdges)).toBe(false)
  expect(rendersHandle('nop', 'source', 'source-loop', outgoingEdges)).toBe(false)
  expect(rendersHandle('task', 'target', 'target-1', outgoingEdges)).toBe(false)
  expect(rendersHandle('entrypoint', 'target', 'target-0', outgoingEdges)).toBe(false)
  expect(rendersHandle('exit', 'source', 'source-0', outgoingEdges)).toBe(false)
})
