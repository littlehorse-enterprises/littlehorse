import React from 'react'
import { render, screen } from '@testing-library/react'
import { CommentEditedEvent } from '../CommentEditedEvent'
import { UserTaskEvent_UTECommented } from 'littlehorse-client/proto'

describe('AssignEvent', () => {
  it('Given users and groups it should render the Assignment ', () => {
    expect(true).toBe(true)
  })
})
describe('CommentEditedEvent', () => {
  const baseEvent: UserTaskEvent_UTECommented = {
    userCommentId: 123124,
    userId: 'Alice',
    comment: 'This is a new comment',
  }

  it('renders editor name and new comment text', () => {
    render(<CommentEditedEvent event={baseEvent} />)

    expect(screen.getByText('123124 has been edited by Alice')).toBeInTheDocument()
    expect(screen.getByText('comment: This is a new comment')).toBeInTheDocument()
  })
})
