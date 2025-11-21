import { formatDateReadable } from '@/app/utils'
import { UserTaskEvent_UTECommentDeleted } from 'littlehorse-client/proto'
import React from 'react'

export const CommentDeletedEvent = ({ event, time }: { event: UserTaskEvent_UTECommentDeleted; time?: string }) => {
  const { userCommentId, userId } = event
  return (
    <>
      {time && (
        <div className="ml-1 flex justify-between">
          <p className="mr-1 text-xs font-bold text-slate-500">Comment added </p>
          <p className="text-xs text-slate-500">{formatDateReadable(time)}</p>
        </div>
      )}
      <div className="ml-1  truncate text-xs text-slate-400">
        <div>
          {userCommentId} has been deleted by {userId}
        </div>
      </div>
    </>
  )
}
