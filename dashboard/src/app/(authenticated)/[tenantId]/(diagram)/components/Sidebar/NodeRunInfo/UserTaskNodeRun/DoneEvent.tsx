import { formatDateReadable } from '@/app/utils'
import React from 'react'

export const DoneEvent = ({ time }: { time?: string }) => {
  return (
    <>
      {time && (
        <div className="ml-1 flex justify-between">
          <p className="mr-1 text-xs font-bold text-slate-500">Done </p>
          <p className="text-xs text-slate-500">{formatDateReadable(time)}</p>
        </div>
      )}
    </>
  )
}
