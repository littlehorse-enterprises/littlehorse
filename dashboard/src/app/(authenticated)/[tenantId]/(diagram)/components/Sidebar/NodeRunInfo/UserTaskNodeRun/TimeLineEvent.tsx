import React, { ReactNode } from 'react'

type TimelineItemProps = {
  dotColor?: string
  children: ReactNode
  isLast: boolean
}

export const TimelineItem = ({ dotColor = 'bg-blue-500', children, isLast }: TimelineItemProps) => {
  return (
    <div className="relative  pb-2 pl-2 ">
      {!isLast && (
        <div className="absolute left-0 mt-2 h-[calc(100%-8px)] w-0.5 -translate-x-1/2 transform bg-gray-300"></div>
      )}
      <div className="absolute left-0 top-0 h-4 w-4 -translate-x-1/2 transform rounded-full border-2 border-white shadow-sm">
        <div className={`h-full w-full rounded-full ${dotColor}`}></div>
      </div>
      <div className=" mb-2 flex flex-col  items-start">{children}</div>
    </div>
  )
}
