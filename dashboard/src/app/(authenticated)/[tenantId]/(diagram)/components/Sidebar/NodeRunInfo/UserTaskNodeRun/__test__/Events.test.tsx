import React from 'react'
import { Events } from '../Events'
import { TimelineItemProps } from '../TimeLineEvent'
import { render, screen } from '@testing-library/react'
import {
  Timestamp,
  UserTaskEvent,
  UserTaskEvent_UTEAssigned,
  UserTaskEvent_UTECancelled,
  UserTaskEvent_UTECommentDeleted,
  UserTaskEvent_UTECommented,
  UserTaskEvent_UTECompleted,
  UserTaskEvent_UTESaved,
  UserTaskEvent_UTETaskExecuted,
} from 'littlehorse-client/proto'

jest.mock('@/app/utils', () => ({
  getEventTime: (evt: UserTaskEvent) => Number(evt.time?.seconds ?? 0),
}))
jest.mock('../AssignEvent', () => ({
  AssignEvent: () => <div data-testid="AssignEvent" />,
}))
jest.mock('../TaskExecutedEvent', () => ({
  TaskExecutedEvent: () => <div data-testid="TaskExecutedEvent" />,
}))
jest.mock('../CancelledEvent', () => ({
  CancelledEvent: () => <div data-testid="CancelledEvent" />,
}))
jest.mock('../SavedEvent', () => ({
  SavedEvent: () => <div data-testid="SavedEvent" />,
}))
jest.mock('../CommentAddedEvent', () => ({
  CommentAddedEvent: () => <div data-testid="CommentAddedEvent" />,
}))
jest.mock('../CommentEditedEvent', () => ({
  CommentEditedEvent: () => <div data-testid="CommentEditedEvent" />,
}))
jest.mock('../CommentDeletedEvent', () => ({
  CommentDeletedEvent: () => <div data-testid="CommentDeletedEvent" />,
}))
jest.mock('../DoneEvent', () => ({
  DoneEvent: () => <div data-testid="DoneEvent" />,
}))

jest.mock('../TimeLineEvent', () => ({
  TimeLineEvent: ({ children, dotColor, isLast }: TimelineItemProps) => (
    <div data-testid="TimelineItem" data-dotcolor={dotColor} data-islast={String(isLast)}>
      {children}
    </div>
  ),
}))

describe('Events component', () => {
  it('renders each event case correctly', () => {
    const executeEvent: UserTaskEvent_UTETaskExecuted = {
      taskRun: {
        wfRunId: {
          id: '23123',
        },
        taskGuid: 'id del task',
      },
    }
    const assignEvent: UserTaskEvent_UTEAssigned = {
      oldUserId: 'use-1',
      newUserId: 'user-2',
      oldUserGroup: 'group-1',
      newUserGroup: 'group-2',
    }
    const cancelledEvent: UserTaskEvent_UTECancelled = {
      message: 'canceled due a conflict',
    }
    const savedEvent: UserTaskEvent_UTESaved = {
      userId: '123123123',
      results: {
        requestedItem: {
          value: {
            oneofKind: 'str',
            str: 'testing',
          },
        },
        justification: {
          value: {
            oneofKind: 'str',
            str: 'as',
          },
        },
      },
    }
    const editCommentEvent: UserTaskEvent_UTECommented = {
      userCommentId: 3123123,
      userId: '123123123',
      comment: 'edited',
    }
    const addedCommentEvent: UserTaskEvent_UTECommented = {
      userCommentId: 123123123,
      userId: '234234234',
      comment: 'addded',
    }
    const deleteCommentEvent: UserTaskEvent_UTECommentDeleted = {
      userCommentId: 64323,
      userId: '123123',
    }
    const completeEvent: UserTaskEvent_UTECompleted = {}
    const sampleEvents: UserTaskEvent[] = [
      { event: { oneofKind: 'taskExecuted', taskExecuted: executeEvent }, time: Timestamp.fromDate(new Date()) },
      { event: { oneofKind: 'assigned', assigned: assignEvent }, time: Timestamp.fromDate(new Date()) },
      { event: { oneofKind: 'cancelled', cancelled: cancelledEvent }, time: Timestamp.fromDate(new Date()) },
      { event: { oneofKind: 'saved', saved: savedEvent }, time: Timestamp.fromDate(new Date()) },
      { event: { oneofKind: 'commentAdded', commentAdded: addedCommentEvent }, time: Timestamp.fromDate(new Date()) },
      { event: { oneofKind: 'commentEdited', commentEdited: editCommentEvent }, time: Timestamp.fromDate(new Date()) },
      {
        event: { oneofKind: 'commentDeleted', commentDeleted: deleteCommentEvent },
        time: Timestamp.fromDate(new Date()),
      },
      { event: { oneofKind: 'completed', completed: completeEvent }, time: Timestamp.fromDate(new Date()) },
    ]

    render(<Events events={sampleEvents} />)

    // All mocked event components should be present exactly once
    expect(screen.getByTestId('TaskExecutedEvent')).toBeInTheDocument()
    expect(screen.getByTestId('AssignEvent')).toBeInTheDocument()
    expect(screen.getByTestId('CancelledEvent')).toBeInTheDocument()
    expect(screen.getByTestId('SavedEvent')).toBeInTheDocument()
    expect(screen.getByTestId('CommentAddedEvent')).toBeInTheDocument()
    expect(screen.getByTestId('CommentEditedEvent')).toBeInTheDocument()
    expect(screen.getByTestId('CommentDeletedEvent')).toBeInTheDocument()
    expect(screen.getByTestId('DoneEvent')).toBeInTheDocument()
  })
})
