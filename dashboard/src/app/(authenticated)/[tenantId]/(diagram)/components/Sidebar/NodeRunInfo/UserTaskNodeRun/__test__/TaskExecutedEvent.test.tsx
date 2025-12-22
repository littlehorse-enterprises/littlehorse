import React from 'react'
import { TaskExecutedEvent } from '../TaskExecutedEvent'
import { formatDateReadable } from '@/app/utils/dateTime'
import { render, screen } from '@testing-library/react'
import { UserTaskEvent_UTETaskExecuted } from 'littlehorse-client/proto'

jest.mock('@/app/utils/dateTime', () => ({
  formatDateReadable: jest.fn(),
}))

describe('TaskExecutedEvent', () => {
  it('renders <TaskExecutedEvent />  when date is provided and time', () => {
    ;(formatDateReadable as jest.Mock).mockReturnValue('Formatted Time')

    const event: UserTaskEvent_UTETaskExecuted = {
      taskRun: {
        taskGuid: 'task-123',
        wfRunId: { id: 'wf-456' },
      },
    }

    render(<TaskExecutedEvent event={event} time="2020-01-01T00:00:00Z" />)
    expect(screen.getByText('task-123 has been executed by wf-456')).toBeInTheDocument()
    expect(screen.getByText('Formatted Time')).toBeInTheDocument()
  })
})
