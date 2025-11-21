import { formatDateReadable } from '@/app/utils'
import { UserTaskEvent_UTECommented } from 'littlehorse-client/proto'
import React from 'react'

export const CommentAddedEvent = ({ event, time }: { event: UserTaskEvent_UTECommented; time?: string }) => {
  const { userCommentId, userId, comment } = event
  return (
    <>
      {time && (
        <div className="ml-1 flex justify-between">
          <span className="text-xs  font-bold text-slate-500">Comment added </span>
          <span className="text-xs text-slate-500">{formatDateReadable(time)}</span>
        </div>
      )}
      <div className="ml-1  truncate text-xs text-slate-400">
        <div>
          {userCommentId} has been edited by {userId}
        </div>
        <div>comment: {comment}</div>
      </div>
    </>
  )
}
