import { fireEvent, render, screen } from '@testing-library/react'
import { StartMultipleThreadsNode as StartMultipleThreadsNodeProto } from 'littlehorse-client/proto'
import { useDiagram } from '../../../../hooks/useDiagram'
import { StartMultipleThreadsNode } from '../StartMultipleThreadsNode'

const replace = jest.fn()
jest.mock('next/navigation', () => ({
  useRouter: () => ({ replace }),
  usePathname: () => '/default/wfRun/wf-run-1',
  useSearchParams: () => new URLSearchParams(),
}))
jest.mock('../../../../hooks/useDiagram', () => ({
  useDiagram: jest.fn(),
}))
jest.mock('../../Components', () => {
  const { getVariable } = require('@/app/utils')
  return {
    VariableAssignment: ({ variableAssigment }: any) => <p>{getVariable(variableAssigment)}</p>,
  }
})
const mockedUseDiagram = useDiagram as jest.Mock

const node: StartMultipleThreadsNodeProto = {
  threadSpecName: 'spawn-threads',
  iterable: {
    path: { oneofKind: 'jsonPath', jsonPath: '$.approvals' },
    source: { oneofKind: 'variableName', variableName: 'approval-chain' },
  },
  variables: {
    region: {
      path: { oneofKind: undefined },
      source: { oneofKind: 'literalValue', literalValue: { value: { oneofKind: 'str', str: 'us-west' } } },
    },
  },
}

describe('StartMultipleThreadsNode', () => {
  const setThread = jest.fn()

  beforeEach(() => {
    setThread.mockReset()
    replace.mockReset()
    mockedUseDiagram.mockReturnValue({ setThread, wfRun: undefined })
  })

  it('renders the threadSpecName, iterable and inputs', () => {
    render(<StartMultipleThreadsNode node={node} />)

    expect(screen.getByText('spawn-threads')).toBeInTheDocument()
    expect(screen.getByText('Iterable')).toBeInTheDocument()
    expect(screen.getByText('{approval-chain.approvals}')).toBeInTheDocument()
    expect(screen.getByText('Inputs')).toBeInTheDocument()
    expect(screen.getByText('region')).toBeInTheDocument()
    expect(screen.getByText('us-west')).toBeInTheDocument()
  })

  it('omits the iterable and inputs sections when they are not set', () => {
    render(<StartMultipleThreadsNode node={{ threadSpecName: 'spawn-threads', variables: {} }} />)

    expect(screen.queryByText('Iterable')).not.toBeInTheDocument()
    expect(screen.queryByText('Inputs')).not.toBeInTheDocument()
  })

  it('navigates to the spawned ThreadSpec in a WfSpec', () => {
    render(<StartMultipleThreadsNode node={node} />)

    fireEvent.click(screen.getByLabelText('Go to thread spawn-threads'))

    expect(setThread).toHaveBeenCalledWith({ name: 'spawn-threads', number: 0 })
    expect(replace).toHaveBeenCalledWith('/default/wfRun/wf-run-1?thread=spawn-threads')
  })

  it('navigates to the first spawned ThreadRun in a WfRun', () => {
    mockedUseDiagram.mockReturnValue({
      setThread,
      wfRun: {
        threadRuns: [
          { number: 0, threadSpecName: 'entrypoint' },
          { number: 2, threadSpecName: 'spawn-threads' },
          { number: 3, threadSpecName: 'spawn-threads' },
        ],
      },
    })
    render(<StartMultipleThreadsNode node={node} />)

    fireEvent.click(screen.getByLabelText('Go to thread spawn-threads'))

    expect(setThread).toHaveBeenCalledWith({ name: 'spawn-threads', number: 2 })
    expect(replace).toHaveBeenCalledWith('/default/wfRun/wf-run-1?threadRunNumber=2')
  })

  it('hides the thread link in a WfRun that has not spawned any threads', () => {
    mockedUseDiagram.mockReturnValue({
      setThread,
      wfRun: { threadRuns: [{ number: 0, threadSpecName: 'entrypoint' }] },
    })
    render(<StartMultipleThreadsNode node={node} />)

    expect(screen.queryByLabelText('Go to thread spawn-threads')).not.toBeInTheDocument()
  })
})
