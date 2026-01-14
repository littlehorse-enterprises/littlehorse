import React from 'react'
import { render, screen, fireEvent } from '@testing-library/react'
import { Attempts } from '../Attempts'
import { TaskAttempt, TaskStatus, VariableValue } from 'littlehorse-client/proto'

jest.mock('../NodeStatus', () => ({
  NodeStatus: ({ status, type }: any) => (
    <div>
      NodeStatus:{status}:{type}
    </div>
  ),
}))

jest.mock('../NodeVariable', () => ({
  NodeVariable: ({ label, text }: any) => <div>{`${label} ${text}`}</div>,
}))

jest.mock('../OutputModal', () => ({
  OutputModal: ({ label, message }: { label: string; message: string }) => <div>OutputModal</div>,
}))

jest.mock('@/components/ui/dropdown-menu', () => ({
  DropdownMenu: ({ children }: { children: React.ReactNode }) => <div>{children}</div>,
  DropdownMenuTrigger: ({ children }: { children: React.ReactNode }) => <div>{children}</div>,
  DropdownMenuContent: ({ children }: { children: React.ReactNode }) => <div>{children}</div>,
  DropdownMenuItem: ({ children, onClick }: { children: React.ReactNode; onClick: () => void }) => (
    <button onClick={onClick}>{children}</button>
  ),
}))

jest.mock('@/components/ui/button', () => ({
  Button: ({ children, ...rest }: { children: React.ReactNode }) => <button {...rest}>{children}</button>,
}))

jest.mock('lucide-react', () => ({
  ChevronDown: () => <span>v</span>,
}))

jest.mock('@/app/utils/struct', () => ({
  getAttemptOutput: (v: VariableValue | undefined) => `OUT`,
  getAttemptResult: (v: VariableValue | undefined) => `RES`,
}))

describe('Attempts component', () => {
  beforeEach(() => {
    jest.clearAllMocks()
  })

  it('renders attempt details, status and output', () => {
    const attempts: TaskAttempt[] = [
      {
        status: 'TASK_RUNNING' as TaskStatus,
        scheduleTime: '2021-01-01T00:00:00Z',
        startTime: '2021-01-01T00:01:00Z',
        endTime: '2021-01-01T00:02:00Z',
        taskWorkerId: 'worker-1',
        taskWorkerVersion: 'v1',
        logOutput: {
          value: {
            $case: 'str',
            value: '',
          },
        },
        result: {
          $case: 'output',
          value: {
            value: {
              $case: 'str',
              value: "Hi what's your name?",
            },
          },
        },
        maskedValue: false,
      },
    ]
    const setAttemptIndex = jest.fn()
    render(<Attempts attempts={attempts} attemptIndex={0} setAttemptIndex={setAttemptIndex} />)

    expect(screen.getByText('Attempts')).toBeInTheDocument()
    expect(screen.getByText('scheduleTime: 2021-01-01T00:00:00Z')).toBeInTheDocument()
    expect(screen.getByText('startTime: 2021-01-01T00:01:00Z')).toBeInTheDocument()
    expect(screen.getByText('endTime: 2021-01-01T00:02:00Z')).toBeInTheDocument()
    expect(screen.getByText('taskWorkerId: worker-1')).toBeInTheDocument()
    expect(screen.getByText('taskWorkerVersion: v1')).toBeInTheDocument()
    expect(screen.getByText('logOutput: OUT')).toBeInTheDocument()
    expect(screen.getByText('NodeStatus:TASK_RUNNING:task')).toBeInTheDocument()
    expect(screen.getByText('OutputModal')).toBeInTheDocument()
  })

  it('calls setAttemptIndex when a dropdown item is clicked', () => {
    const attempts: TaskAttempt[] = [
      {
        status: 'TASK_RUNNING' as TaskStatus,
        scheduleTime: 't1',
        taskWorkerId: '',
        maskedValue: false,
      },
      {
        status: 'TASK_RUNNING' as TaskStatus,
        scheduleTime: 't2',
        taskWorkerId: '',
        maskedValue: false,
      },
    ]
    const setAttemptIndex = jest.fn()
    render(<Attempts attempts={attempts} attemptIndex={0} setAttemptIndex={setAttemptIndex} />)

    const itemTwo = screen.getByRole('button', { name: 'Attempt 2' })
    fireEvent.click(itemTwo)
    expect(setAttemptIndex).toHaveBeenCalledWith(1)
  })
})
