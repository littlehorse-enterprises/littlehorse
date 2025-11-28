import React from 'react'
import { render, screen } from '@testing-library/react'
import { CommentAddedEvent } from '../CommentAddedEvent'
import { UserTaskEvent_UTECommented } from 'littlehorse-client/proto'

jest.mock('@/app/utils', () => ({
  formatDateReadable: jest.fn((time: string) => `formatted-${time}`),
}))

describe('CommentAddedEvent', () => {
  const mockEvent: UserTaskEvent_UTECommented = {
    userCommentId: 83749,
    userId: 'user-xyz',
    comment: 'This is a test comment',
  }

  it('renders comment details and time when is provided', () => {
    const time = '2025-01-01T12:00:00Z'

    render(<CommentAddedEvent event={mockEvent} time={time} />)

    expect(screen.getByText(/83749 has been added by user-xyz/)).toBeInTheDocument()
    expect(screen.getByText(/comment: This is a test comment/)).toBeInTheDocument()
    expect(screen.getByText(`formatted-${time}`)).toBeInTheDocument()
  })
})
