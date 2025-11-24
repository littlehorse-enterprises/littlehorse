import React from 'react'
import { render, screen } from '@testing-library/react'
import '@testing-library/jest-dom'
import { CancelledEvent } from '../CancelledEvent'
import { UserTaskEvent_UTECancelled } from 'littlehorse-client/proto'
import { formatDateReadable } from '@/app/utils'

jest.mock('@/app/utils', () => ({
  formatDateReadable: jest.fn(),
}))

describe('CancelledEvent', () => {
  beforeEach(() => {
    ;(formatDateReadable as jest.Mock).mockClear()
  })

  it('renders message and  time when date is provided and calls formatDateReadable', () => {
    ;(formatDateReadable as jest.Mock).mockReturnValue('Formatted Time')

    const event = { message: 'User cancelled the task' } as UserTaskEvent_UTECancelled

    render(<CancelledEvent event={event} time="2020-01-01T00:00:00Z" />)

    expect(screen.getByText('Cancelled')).toBeInTheDocument()
    expect(screen.getByText('Formatted Time')).toBeInTheDocument()
    expect(screen.getByText('User cancelled the task')).toBeInTheDocument()
    expect(formatDateReadable).toHaveBeenCalledWith('2020-01-01T00:00:00Z')
  })
})
