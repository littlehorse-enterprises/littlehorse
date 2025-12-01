import React, { FC } from 'react'
import { render, screen } from '@testing-library/react'
import '@testing-library/jest-dom'
import { NodeRunComponent } from '../NodeRunComponent'
import { useDiagram } from '../../../../hooks/useDiagram'
import {
  ExternalEventNodeRun,
  RunChildWfNodeRun,
  SleepNodeRun,
  StartMultipleThreadsRun,
  StartThreadRun,
  TaskNodeRun,
  ThrowEventNodeRun,
  UserTaskNodeRun,
  WaitForThreadsRun,
} from 'littlehorse-client/proto'

jest.mock('../TaskRunNode', () => ({
  TaskRunNode: ({}: { node: TaskNodeRun }) => <div data-testid="TaskRunNode">Task</div>,
}))
jest.mock('../ExternalEventNodeRun/ExternalEventNodeRun', () => ({
  ExternalEventNodeRun: ({}: { node: ExternalEventNodeRun }) => <div data-testid="ExternalEventNodeRun">External</div>,
}))
jest.mock('../UserTaskNodeRun/UserTaskRunNode', () => ({
  UserTaskRunNode: ({}: { node: UserTaskNodeRun }) => <div data-testid="UserTaskRunNode">User</div>,
}))
jest.mock('../SleepNodeRun', () => ({
  SleepNodeRun: ({}: { node: SleepNodeRun }) => <div data-testid="SleepNodeRun">Sleep</div>,
}))
jest.mock('../WaitForThreadNodeRun', () => ({
  WaitForThreadsNodeRun: ({}: { node: { node: WaitForThreadsRun } }) => (
    <div data-testid="WaitForThreadsNodeRun">Wait</div>
  ),
}))
jest.mock('../StartThreadRunNode', () => ({
  StartThreadRunNode: ({}: { node: { node: StartThreadRun } }) => (
    <div data-testid="StartThreadRunNode">StartThreadRunNode</div>
  ),
}))
jest.mock('../StartMultipleThreadRunNode', () => ({
  StartMultipleThreadRunNode: ({}: { node: StartMultipleThreadsRun }) => (
    <div data-testid="StartMultipleThreadRunNode">StartMultipleThreadRunNode</div>
  ),
}))
jest.mock('../ThrowEventRunNode', () => ({
  ThrowEventRunNode: ({}: { node: ThrowEventNodeRun }) => <div data-testid="ThrowEventRunNode">ThrowEventRunNode</div>,
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
    ['task', 'TaskRunNode'],
    ['externalEvent', 'ExternalEventNodeRun'],
    ['userTask', 'UserTaskRunNode'],
    ['sleep', 'SleepNodeRun'],
    ['waitForThreads', 'WaitForThreadsNodeRun'],
    ['startThread', 'StartThreadRunNode'],
    ['startMultipleThreads', 'StartMultipleThreadRunNode'],
    ['throwEvent', 'ThrowEventRunNode'],
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
