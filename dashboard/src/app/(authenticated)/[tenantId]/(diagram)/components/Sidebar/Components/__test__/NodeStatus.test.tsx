import '@testing-library/jest-dom'
import { render, screen } from '@testing-library/react'
import { LHStatus, TaskStatus } from 'littlehorse-client/proto'
import React from 'react'
import { NodeStatus } from '../NodeStatus'

jest.mock('../StatusColor', () => {
  const MockIcon = (props: any) => React.createElement('svg', { 'data-testid': 'icon', className: props.className })
  const MockIcon2 = (props: any) => React.createElement('svg', { 'data-testid': 'icon-2', className: props.className })

  const { LHStatus, TaskStatus } = jest.requireActual('littlehorse-client/proto')

  return {
    WF_RUN_STATUS: {
      [LHStatus.RUNNING]: { color: 'blue', Icon: MockIcon, textColor: 'text-blue-700', backgroundColor: 'bg-blue-200' },
      [LHStatus.COMPLETED]: {
        color: 'green',
        Icon: MockIcon,
        textColor: 'text-green-700',
        backgroundColor: 'bg-green-200',
      },
    },
    TASK_STATUS: {
      [TaskStatus.TASK_FAILED]: {
        color: 'red',
        Icon: MockIcon2,
        textColor: 'text-red-700',
        backgroundColor: 'bg-red-200',
      },
      [TaskStatus.TASK_SUCCESS]: {
        color: 'teal',
        Icon: MockIcon2,
        textColor: 'text-teal-700',
        backgroundColor: 'bg-teal-200',
      },
    },
  }
})

test('renders workflow status (default type) with correct classes and icon', () => {
  const { container } = render(<NodeStatus status={LHStatus.RUNNING} />)

  const statusText = screen.getByText('Running')
  expect(statusText).toBeInTheDocument()
  expect(statusText).toHaveClass('text-blue-700')

  const icon = screen.getByTestId('icon')
  expect(icon).toBeInTheDocument()
  expect(icon).toHaveClass('h-4', 'w-4', 'text-blue-700')

  expect(container.querySelector('.bg-blue-200')).toBeInTheDocument()
})

test('renders task status when type="task" with correct classes and icon', () => {
  const { container } = render(<NodeStatus status={TaskStatus.TASK_FAILED} type="task" />)

  const statusText = screen.getByText('Task Failed')
  expect(statusText).toBeInTheDocument()
  expect(statusText).toHaveClass('text-red-700')

  const icon = screen.getByTestId('icon-2')
  expect(icon).toBeInTheDocument()
  expect(icon).toHaveClass('h-4', 'w-4', 'text-red-700')

  expect(container.querySelector('.bg-red-200')).toBeInTheDocument()
})
