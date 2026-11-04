import { UserTaskEvent } from 'littlehorse-client/proto'
import { AssignEvent } from './AssignEvent'
import { TaskExecutedEvent } from './TaskExecutedEvent'
import { CancelledEvent } from './CancelledEvent'
import { SavedEvent } from './SavedEvent'
import { getEventTime } from '@/app/utils'
import { CommentAddedEvent } from './CommentAddedEvent'
import { CommentEditedEvent } from './CommentEditedEvent'
import { CommentDeletedEvent } from './CommentDeletedEvent'
import { TimeLineEvent } from './TimeLineEvent'
import { DoneEvent } from './DoneEvent'

export const Events = ({ events }: { events: UserTaskEvent[] }) => {
  const sortedEvents = [...events].sort((a, b) => getEventTime(b) - getEventTime(a))

  const renderEvent = (userTask: UserTaskEvent) => {
    const { event, time } = userTask
    switch (event?.oneofKind) {
      case 'taskExecuted':
        return <TaskExecutedEvent event={event.taskExecuted} time={time} />
      case 'assigned':
        return <AssignEvent event={event.assigned} time={time} />
      case 'cancelled':
        return <CancelledEvent event={event.cancelled} time={time} />
      case 'saved':
        return <SavedEvent event={event.saved} time={time} />
      case 'commentAdded':
        return <CommentAddedEvent event={event.commentAdded} time={time} />
      case 'commentEdited':
        return <CommentEditedEvent event={event.commentEdited} time={time} />
      case 'commentDeleted':
        return <CommentDeletedEvent event={event.commentDeleted} time={time} />
      case 'completed':
        return <DoneEvent time={time} />
      default:
        return <></>
    }
  }

  return (
    <div className="ml-1  mt-1 ">
      <div className=" mb-1 text-sm font-bold ">Events</div>

      <div className="container mx-auto  max-w-2xl  px-2 py-2">
        {sortedEvents.map((event, index) => (
          <TimeLineEvent
            key={index}
            dotColor={event.event?.oneofKind === 'completed' ? 'bg-green-500' : 'bg-blue-500'}
            isLast={index === sortedEvents.length - 1}
          >
            <div className="w-full" key={index}>
              {renderEvent(event)}
            </div>
          </TimeLineEvent>
        ))}
      </div>
    </div>
  )
}
