import { formatDateReadable } from '@/app/utils'
import { UserTaskEvent_UTECancelled } from 'littlehorse-client/proto'
import React from 'react'

export const CancelledEvent = ({ event, time }: { event: UserTaskEvent_UTECancelled; time?: string }) => {
  return (
    <>
      {time && (
        <div className="ml-1 flex justify-between">
          <p className="mr-1 text-xs font-bold text-slate-500">Cancelled </p>
          <p className="text-xs text-slate-500">{formatDateReadable(time)}</p>
        </div>
      )}
      <div className="ml-1  truncate text-xs text-slate-400">{event.message}</div>
    </>
  )
}
