import React from 'react'
import { render, screen, fireEvent } from '@testing-library/react'
import { Attempts } from '../Attempts'
import { TaskAttempt, TaskStatus, Timestamp, VariableValue } from 'littlehorse-client/proto'
import { utcToLocalDateTime } from '@/app/utils'

jest.mock('../NodeStatus', () => ({
  NodeStatus: ({ status, type }: any) => {
    const { TaskStatus, LHStatus } = require('littlehorse-client/proto')
    const name = type === 'task' ? TaskStatus[status] : LHStatus[status]
    return (
      <div>
        NodeStatus:{name}:{type}
      </div>
    )
  },
}))

jest.mock('../NodeVariable', () => ({
  NodeVariable: ({ label, text, type }: any) => {
    const { utcToLocalDateTime } = require('@/app/utils')
    const display = type === 'date' ? utcToLocalDateTime(text) : text
    return <div>{`${label} ${display}`}</div>
  },
}))

jest.mock('../OutputModal', () => ({
  OutputModal: (_props: { label: string; message: string }) => <div>OutputModal</div>,
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
  getAttemptOutput: (_v: VariableValue | undefined) => `OUT`,
  getAttemptResult: (_v: VariableValue | undefined) => `RES`,
}))

describe('Attempts component', () => {
  beforeEach(() => {
    jest.clearAllMocks()
  })

  it('renders attempt details, status and output', () => {
    const scheduleTime = Timestamp.fromDate(new Date('2021-01-01T00:00:00Z'))
    const startTime = Timestamp.fromDate(new Date('2021-01-01T00:01:00Z'))
    const endTime = Timestamp.fromDate(new Date('2021-01-01T00:02:00Z'))
    const attempts: TaskAttempt[] = [
      TaskAttempt.create({
        status: TaskStatus.TASK_RUNNING,
        scheduleTime,
        startTime,
        endTime,
        taskWorkerId: 'worker-1',
        taskWorkerVersion: 'v1',
        logOutput: {
          value: {
            oneofKind: 'str',
            str: '',
          },
        },
        result: {
          oneofKind: 'output',
          output: {
            value: {
              oneofKind: 'str',
              str: "Hi what's your name?",
            },
          },
        },
        maskedValue: false,
      }),
    ]
    const setAttemptIndex = jest.fn()
    render(<Attempts attempts={attempts} attemptIndex={0} setAttemptIndex={setAttemptIndex} />)

    expect(screen.getByText('Attempts')).toBeInTheDocument()
    expect(screen.getByText(`scheduleTime: ${utcToLocalDateTime(scheduleTime)}`)).toBeInTheDocument()
    expect(screen.getByText(`startTime: ${utcToLocalDateTime(startTime)}`)).toBeInTheDocument()
    expect(screen.getByText(`endTime: ${utcToLocalDateTime(endTime)}`)).toBeInTheDocument()
    expect(screen.getByText('taskWorkerId: worker-1')).toBeInTheDocument()
    expect(screen.getByText('taskWorkerVersion: v1')).toBeInTheDocument()
    expect(screen.getByText('logOutput: OUT')).toBeInTheDocument()
    expect(screen.getByText('NodeStatus:TASK_RUNNING:task')).toBeInTheDocument()
    expect(screen.getByText('OutputModal')).toBeInTheDocument()
  })

  it('calls setAttemptIndex when a dropdown item is clicked', () => {
    const attempts: TaskAttempt[] = [
      TaskAttempt.create({
        status: TaskStatus.TASK_RUNNING,
        scheduleTime: Timestamp.create(),
        taskWorkerId: '',
        maskedValue: false,
      }),
      TaskAttempt.create({
        status: TaskStatus.TASK_RUNNING,
        scheduleTime: Timestamp.create(),
        taskWorkerId: '',
        maskedValue: false,
      }),
    ]
    const setAttemptIndex = jest.fn()
    render(<Attempts attempts={attempts} attemptIndex={0} setAttemptIndex={setAttemptIndex} />)

    const itemTwo = screen.getByRole('button', { name: 'Attempt 2' })
    fireEvent.click(itemTwo)
    expect(setAttemptIndex).toHaveBeenCalledWith(1)
  })
})
