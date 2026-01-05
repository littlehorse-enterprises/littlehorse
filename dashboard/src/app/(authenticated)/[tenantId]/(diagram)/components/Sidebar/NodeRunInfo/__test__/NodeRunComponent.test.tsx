import React from 'react'
import { render, screen } from '@testing-library/react'
import '@testing-library/jest-dom'
import { NodeRunComponent } from '../NodeRunComponent'
import { useDiagram } from '../../../../hooks/useDiagram'
import {
  ExternalEventNodeRun,
  RunChildWfNodeRun,
  SleepNodeRun,
  StartMultipleThreadsRun,
  StartThreadRun as StartThreadRunProto,
  TaskNodeRun,
  ThrowEventNodeRun,
  UserTaskNodeRun,
  WaitForThreadsRun,
} from 'littlehorse-client/proto'

jest.mock('../TaskNodeRun', () => ({
  TaskNodeRun: ({}: { node: TaskNodeRun }) => <div data-testid="TaskNodeRun">Task</div>,
}))
jest.mock('../ExternalEventNodeRun/ExternalEventNodeRun', () => ({
  ExternalEventNodeRun: ({}: { node: ExternalEventNodeRun }) => <div data-testid="ExternalEventNodeRun">External</div>,
}))
jest.mock('../UserTaskNodeRun/UserTaskNodeRun', () => ({
  UserTaskNodeRun: ({}: { node: UserTaskNodeRun }) => <div data-testid="UserTaskNodeRun">User</div>,
}))
jest.mock('../SleepNodeRun', () => ({
  SleepNodeRun: ({}: { node: SleepNodeRun }) => <div data-testid="SleepNodeRun">Sleep</div>,
}))
jest.mock('../WaitForThreadNodeRun', () => ({
  WaitForThreadsNodeRun: ({}: { node: { node: WaitForThreadsRun } }) => (
    <div data-testid="WaitForThreadsNodeRun">Wait</div>
  ),
}))
jest.mock('../StartThreadNodeRun', () => ({
  StartThreadNodeRun: ({}: { node: { node: StartThreadRunProto } }) => (
    <div data-testid="StartThreadNodeRun">StartThreadNodeRun</div>
  ),
}))
jest.mock('../StartMultipleThreadNodeRun', () => ({
  StartMultipleThreadNodeRun: ({}: { node: StartMultipleThreadsRun }) => (
    <div data-testid="StartMultipleThreadNodeRun">StartMultipleThreadNodeRun</div>
  ),
}))
jest.mock('../ThrowEventNodeRun', () => ({
  ThrowEventNodeRun: ({}: { node: ThrowEventNodeRun }) => <div data-testid="ThrowEventNodeRun">ThrowEventNodeRun</div>,
}))
jest.mock('../ChildWFNodeRun', () => ({
  ChildWFNodeRun: ({}: { node: RunChildWfNodeRun }) => <div data-testid="ChildWFNodeRun">ChildWFNodeRun</div>,
}))
jest.mock('lucide-react', () => ({
  OctagonAlert: () => <svg data-testid="octagon" />,
}))

jest.mock('../../../../hooks/useDiagram', () => ({
  useDiagram: jest.fn(),
}))
const mockedUseDiagram = useDiagram as jest.Mock

describe('NodeRunComponent', () => {
  beforeEach(() => {
    mockedUseDiagram.mockReset()
  })

  test('renders nothing when there is no selectedNode', () => {
    mockedUseDiagram.mockReturnValue({ selectedNode: null })
    const { container } = render(<NodeRunComponent nodeRunIndex={0} />)
    expect(container.firstChild).toBeNull()
  })

  test('renders nothing when selectedNode has no nodeRunsList', () => {
    mockedUseDiagram.mockReturnValue({ selectedNode: { data: {} } })
    const { container } = render(<NodeRunComponent nodeRunIndex={0} />)
    expect(container.firstChild).toBeNull()
  })

  const cases: Array<[string, string]> = [
    ['task', 'TaskNodeRun'],
    ['externalEvent', 'ExternalEventNodeRun'],
    ['userTask', 'UserTaskNodeRun'],
    ['sleep', 'SleepNodeRun'],
    ['waitForThreads', 'WaitForThreadsNodeRun'],
    ['startThread', 'StartThreadNodeRun'],
    ['startMultipleThreads', 'StartMultipleThreadNodeRun'],
    ['throwEvent', 'ThrowEventNodeRun'],
    ['runChildWf', 'ChildWFNodeRun'],
  ]

  cases.forEach(([caseName, testId]) => {
    test(`renders ${testId} when nodeType.$case is "${caseName}"`, () => {
      const nodeRun = { nodeType: { $case: caseName, value: { id: `${caseName}-id` } } }
      mockedUseDiagram.mockReturnValue({ selectedNode: { data: { nodeRunsList: [nodeRun] } } })
      render(<NodeRunComponent nodeRunIndex={0} />)
      expect(screen.getByTestId(testId)).toBeInTheDocument()
    })
  })

  test('renders fallback when nodeType.$case is unknown', () => {
    const nodeRun = { nodeType: { $case: 'somethingElse', value: { id: 'x' } } }
    mockedUseDiagram.mockReturnValue({ selectedNode: { data: { nodeRunsList: [nodeRun] } } })
    render(<NodeRunComponent nodeRunIndex={0} />)
    expect(screen.getByText('No information required here.')).toBeInTheDocument()
    expect(screen.getByTestId('octagon')).toBeInTheDocument()
  })
})
