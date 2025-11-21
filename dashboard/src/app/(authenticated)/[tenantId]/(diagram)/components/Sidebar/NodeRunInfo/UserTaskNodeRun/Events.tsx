import {
  UserTaskEvent,
  UserTaskEvent_UTECancelled,
  UserTaskEvent_UTECommentDeleted,
  UserTaskEvent_UTECommented,
  UserTaskEvent_UTECompleted,
  UserTaskEvent_UTESaved,
  UserTaskEvent_UTETaskExecuted,
} from 'littlehorse-client/proto'
import { AssignEvent } from './AssignEvent'
import { TaskExecutedEvent } from './TaskExecutedEvent'
import { CancelledEvent } from './CancelledEvent'
import { SavedEvent } from './SavedEvent'
import { getEventTime } from '@/app/utils'
import { CommentAddedEvent } from './CommentAddedEvent'
import { CommentEditedEvent } from './CommentEditedEvent'
import { CommentDeletedEvent } from './CommentDeletedEvent'
import { TimelineItem } from './TimeLineEvent'
import { DoneEvent } from './DoneEvent'

export const Events = (events: UserTaskEvent[]) => {

  const sortedEvents = [...events].sort((a, b) => getEventTime(b) - getEventTime(a))

  const renderEvent = (userTask: UserTaskEvent, index: number) => {
    const { event, time } = userTask
    switch (event?.$case) {
      case 'taskExecuted':
        return <TaskExecutedEvent event={event.value} time={time} />
      case 'assigned':
        return <AssignEvent event={event.value} time={time}/>
      case 'cancelled':
        return <CancelledEvent event={event.value} time={time} />
      case 'saved':
        return <SavedEvent event={event.value} time={time} />
      case 'commentAdded':
        return <CommentAddedEvent event={event.value} time={time} />
      case 'commentEdited':
        return <CommentEditedEvent event={event.value} time={time} />
      case 'commentDeleted':
        return <CommentDeletedEvent event={event.value} time={time} />
      case 'completed':
        return <DoneEvent time={time} />
      default:
        return <></>
    }
  }

  return (
    // todo  calcualte stpace
    <div className=' overflow-y-auto max-h-24'>
      <div className=" mb-1 text-sm font-bold ">Events</div>

      <div className="container mx-auto max-h-[80vh] max-w-2xl overflow-y-scroll px-2 py-2">
        {sortedEvents.map((event, index) => (
          <TimelineItem
            key={index}
            dotColor={event.event?.$case === 'completed' ? 'bg-green-500' : 'bg-blue-500'}
            isLast={index === sortedEvents.length - 1}
          >
            <div className="w-full" key={index}>
              {renderEvent(event, index)}
            </div>
          </TimelineItem>
        ))}
      </div>
    </div>
  )
}
