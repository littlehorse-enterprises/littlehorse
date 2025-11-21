import { formatDateReadable } from '@/app/utils'
import { UserTaskEvent_UTETaskExecuted } from 'littlehorse-client/proto'
import React from 'react'

export const TaskExecutedEvent = ({ event, time }: { event: UserTaskEvent_UTETaskExecuted; time?: string }) => {
  return (
    <>
      {time && (
        <div className="ml-1 flex justify-between">
          <p className="font-bold mr-1 text-xs text-slate-500">Executed </p>
          <p className="text-xs text-slate-500">{formatDateReadable(time)}</p>
        </div>
      )}
      <div className="ml-1  truncate text-xs text-slate-400">
        {event.taskRun?.taskGuid} has been executed by {event.taskRun?.wfRunId?.id}
      </div>
    </>
  )
}
