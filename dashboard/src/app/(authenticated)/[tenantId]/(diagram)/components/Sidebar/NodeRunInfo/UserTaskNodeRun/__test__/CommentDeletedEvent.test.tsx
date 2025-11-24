import React from 'react'
import { render, screen } from '@testing-library/react'
import { CommentDeletedEvent } from '@/app/(authenticated)/[tenantId]/(diagram)/components/Sidebar/NodeRunInfo/UserTaskNodeRun/CommentDeletedEvent'
import { formatDateReadable } from '@/app/utils'

jest.mock('@/app/utils', () => ({
  formatDateReadable: jest.fn(),
}))

describe('CommentDeletedEvent', () => {
  beforeEach(() => {
    ;(formatDateReadable as jest.Mock).mockReset()
  })

  it('renders deletion message and time when it has been provided and calls formatDateReadable', () => {
    ;(formatDateReadable as jest.Mock).mockReturnValue('Formatted Date')
    const time = '2021-01-01T00:00:00Z'

    render(<CommentDeletedEvent event={{ userCommentId: 'comment-1', userId: 'user-1' } as any} time={time} />)
    expect(formatDateReadable).toHaveBeenCalledWith(time)
    expect(screen.getByText(/Comment added/i)).toBeInTheDocument()
    expect(screen.getByText('Formatted Date')).toBeInTheDocument()
    expect(screen.getByText(/comment-1 has been deleted by user-1/i)).toBeInTheDocument()
  })
})
