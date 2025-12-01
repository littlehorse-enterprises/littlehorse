import { formatDateReadable } from '@/app/utils'
import { UserTaskEvent_UTEAssigned } from 'littlehorse-client/proto'
import { MoveRight } from 'lucide-react'

export const AssignEvent = ({ event, time }: { event: UserTaskEvent_UTEAssigned; time?: string }) => {
  const textValidation = (text: string | undefined) => {
    if (text) return text
    return <p>No assigned</p>
  }
  return (
    <div className="ml-1">
      {time && (
        <div className="flex justify-between">
          <p className="text-bold mr-1 text-xs text-slate-500">Assigned </p>
          <p className="text-xs text-slate-500">{formatDateReadable(time)}</p>
        </div>
      )}
      {(event.oldUserId || event.newUserId) && (
        <div className="mt-1 flex justify-between">
          <div className="truncate  text-xs text-slate-400">{textValidation(event.oldUserId)}</div>
          <MoveRight size={12} className="mt-1" />
          <div className="truncate  text-xs text-slate-400">{textValidation(event.newUserId)}</div>
        </div>
      )}
      {(event.oldUserGroup || event.newUserGroup) && (
        <div className="mt-1 flex justify-between">
          <div className="truncate  text-xs text-slate-400">{textValidation(event.oldUserGroup)} </div>
          <MoveRight size={12} className="mt-1" />
          <div className="truncate  text-xs text-slate-400">{textValidation(event.newUserGroup)}</div>
        </div>
      )}
    </div>
  )
}
