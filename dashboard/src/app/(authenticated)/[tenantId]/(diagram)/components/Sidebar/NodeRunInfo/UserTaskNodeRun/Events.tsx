import { UserTaskEvent } from 'littlehorse-client/proto'

export const Events = (events: UserTaskEvent[]) => {
  console.log(events)
  const renderEvent = (event: UserTaskEvent, index: number) => {
    switch (event.event?.$case) {
      case 'taskExecuted':
        return <div> type : </div>
      case 'assigned':
        return <div> type : </div>
      case 'cancelled':
        return <div> type : </div>
      case 'saved':
        return <div> type : </div>
      case 'commentAdded':
      case 'commentEdited':
        return <div> type : </div>
      case 'commentDeleted':
        return <div> type : </div>
      case 'completed':
        return <div> type : </div>
    }
  }
  return (
    <div className="ml-1">
      <div className=" text-sm font-bold">Events</div>
    </div>
  )
}
