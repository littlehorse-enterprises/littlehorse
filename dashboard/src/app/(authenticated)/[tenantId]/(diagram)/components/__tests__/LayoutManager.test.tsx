import { act, render } from '@testing-library/react'
import { Node, useStore, useReactFlow } from 'reactflow'
import { layoutDiagram } from '../graphLayout'
import { LayoutManager } from '../LayoutManager'

jest.mock('reactflow', () => ({ useStore: jest.fn(), useReactFlow: jest.fn(), useOnViewportChange: jest.fn() }))
jest.mock('../graphLayout', () => ({ layoutDiagram: jest.fn() }))

const node = (layoutKey: string): Node => ({
  id: 'shared-node-name',
  type: 'nop',
  data: { layoutKey },
  position: { x: 0, y: 0 },
  width: 30,
  height: 30,
})
const layout = () => ({
  graph: { id: 'root', children: [{ id: 'shared-node-name', x: 100, y: 100 }] },
  routes: new Map(),
})
let nodes: Node[]
const edges: [] = []
const props = { viewportKey: 'test-viewport', setNodes: jest.fn(), setEdges: jest.fn(), onLayoutComplete: jest.fn() }

beforeEach(() => {
  jest.clearAllMocks()
  jest.useFakeTimers()
  nodes = [node('parent')]
  jest.mocked(useStore).mockImplementation(selector => selector({ getNodes: () => nodes, edges } as never))
  jest.mocked(useReactFlow).mockReturnValue({ fitView: jest.fn(), setViewport: jest.fn() } as never)
  jest.mocked(layoutDiagram).mockResolvedValue(layout() as never)
})
afterEach(() => {
  jest.clearAllTimers()
  jest.useRealTimers()
})

it('waits for the selected thread nodes instead of laying out the previous thread', async () => {
  const { rerender } = render(<LayoutManager {...props} layoutKey="child" />)
  expect(layoutDiagram).not.toHaveBeenCalled()
  nodes = [node('child')]
  await act(async () => {
    rerender(<LayoutManager {...props} layoutKey="child" />)
  })
  expect(layoutDiagram).toHaveBeenCalledTimes(1)
  expect(props.setNodes).toHaveBeenCalledWith([
    expect.objectContaining({ data: expect.objectContaining({ layoutKey: 'child' }) }),
  ])
})

it('discards an old layout that finishes after switching threads', async () => {
  let finishParent!: (value: Awaited<ReturnType<typeof layoutDiagram>>) => void
  jest.mocked(layoutDiagram).mockImplementationOnce(
    () =>
      new Promise(resolve => {
        finishParent = resolve
      })
  )
  const { rerender } = render(<LayoutManager {...props} layoutKey="parent" />)
  nodes = [node('child')]
  await act(async () => {
    rerender(<LayoutManager {...props} layoutKey="child" />)
  })
  expect(props.setNodes).toHaveBeenCalledTimes(1)
  await act(async () => {
    finishParent(layout() as never)
  })
  expect(props.setNodes).toHaveBeenCalledTimes(1)
  expect(props.setEdges).toHaveBeenCalledTimes(1)
  expect(props.onLayoutComplete).toHaveBeenCalledTimes(1)
})
