import { formatDateReadable } from '@/app/utils'
import { UserTaskEvent_UTESaved } from 'littlehorse-client/proto'
import { Results } from '../../Components/Results'

export const SavedEvent = ({ event, time }: { event: UserTaskEvent_UTESaved; time?: string }) => {
  const resultsArray = Object.entries(event?.results || {})

  return (
    <>
      {time && (
        <div className="ml-1 flex justify-between">
          <p className="mr-1 text-xs font-bold text-slate-500">Saved </p>
          <p className="text-xs text-slate-500">{formatDateReadable(time)}</p>
        </div>
      )}
      <div className="ml-1  truncate text-xs text-slate-400">Saved by {event.userId}</div>
      <div>{resultsArray.length > 0 && <Results variables={resultsArray} classTitle="font-bold text-slate-500" />}</div>
    </>
  )
}
