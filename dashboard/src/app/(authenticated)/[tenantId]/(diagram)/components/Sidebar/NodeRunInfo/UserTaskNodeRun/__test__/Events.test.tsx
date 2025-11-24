import React from 'react'
import { Events } from '../Events'
import { TimelineItemProps } from '../TimeLineEvent'
import { render, screen } from '@testing-library/react'
import {
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
  getEventTime: (evt: UserTaskEvent) => evt.time,
}))
jest.mock('../AssignEvent', () => ({
  AssignEvent: ({ time }: { time: string }) => <div data-testid="AssignEvent">{time}</div>,
}))
jest.mock('../TaskExecutedEvent', () => ({
  TaskExecutedEvent: ({ time }: { time: string }) => <div data-testid="TaskExecutedEvent">{time}</div>,
}))
jest.mock('../CancelledEvent', () => ({
  CancelledEvent: ({ time }: { time: string }) => <div data-testid="CancelledEvent">{time}</div>,
}))
jest.mock('../SavedEvent', () => ({
  SavedEvent: ({ time }: { time: string }) => <div data-testid="SavedEvent">{time}</div>,
}))
jest.mock('../CommentAddedEvent', () => ({
  CommentAddedEvent: ({ time }: { time: string }) => <div data-testid="CommentAddedEvent">{time}</div>,
}))
jest.mock('../CommentEditedEvent', () => ({
  CommentEditedEvent: ({ time }: { time: string }) => <div data-testid="CommentEditedEvent">{time}</div>,
}))
jest.mock('../CommentDeletedEvent', () => ({
  CommentDeletedEvent: ({ time }: { time: string }) => <div data-testid="CommentDeletedEvent">{time}</div>,
}))
jest.mock('../DoneEvent', () => ({
  DoneEvent: ({ time }: { time: string }) => <div data-testid="DoneEvent">{time}</div>,
}))

jest.mock('../TimeLineEvent', () => ({
  TimelineItem: ({ children, dotColor, isLast }: TimelineItemProps) => (
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
            $case: 'str',
            value: 'testing',
          },
        },
        justification: {
          value: {
            $case: 'str',
            value: 'as',
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
      { event: { $case: 'taskExecuted', value: executeEvent }, time: new Date().toDateString() },
      { event: { $case: 'assigned', value: assignEvent }, time: new Date().toDateString() },
      { event: { $case: 'cancelled', value: cancelledEvent }, time: new Date().toDateString() },
      { event: { $case: 'saved', value: savedEvent }, time: new Date().toDateString() },
      { event: { $case: 'commentAdded', value: addedCommentEvent }, time: new Date().toDateString() },
      { event: { $case: 'commentEdited', value: editCommentEvent }, time: new Date().toDateString() },
      { event: { $case: 'commentDeleted', value: deleteCommentEvent }, time: new Date().toDateString() },
      { event: { $case: 'completed', value: completeEvent }, time: new Date().toDateString() },
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
