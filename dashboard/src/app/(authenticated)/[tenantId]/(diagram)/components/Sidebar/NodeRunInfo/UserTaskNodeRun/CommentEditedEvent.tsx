import { formatDateReadable } from '@/app/utils'
import { UserTaskEvent_UTECommented } from 'littlehorse-client/proto'
import React from 'react'

export const CommentEditedEvent = ({ event, time }: { event: UserTaskEvent_UTECommented; time?: string }) => {
  const { userCommentId, userId, comment } = event

  return (
    <>
      {time && (
      <div className="ml-1 flex justify-between">
          <span className="font-bold mr-1 text-xs text-slate-500">Comment edited </span>
          <span className="text-xs text-slate-500">{formatDateReadable(time)}</span>
        </div>
      )}
      <div className="ml-1  truncate text-xs text-slate-400">
        <div>
          {userCommentId} has been added by {userId}
        </div>
        <div>comment: {comment}</div>
      </div>
    </>
  )
}
