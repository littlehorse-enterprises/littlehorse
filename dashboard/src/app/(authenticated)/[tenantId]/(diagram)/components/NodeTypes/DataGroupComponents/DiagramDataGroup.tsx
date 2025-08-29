'use client'
import { ReactNode } from 'react'

export function DiagramDataGroup({
  label,
  from,
  children,
  index,
  indexes,
  arrow,
}: {
  label: string
  index?: number
  indexes?: number
  from?: string
  children?: ReactNode
  arrow?: boolean
}) {
  return (
    <div className="relative flex h-fit w-fit min-w-36 flex-col justify-around rounded-lg bg-white">
      <div className="absolute -top-5 left-0 flex w-fit flex-nowrap gap-2 rounded-lg bg-white px-3 py-1 font-semibold">
        {label} {index !== undefined && indexes !== undefined && indexes > 1 ? `#${indexes - 1 - index}` : ''}
      </div>
      {from && (
        <div className="absolute -top-8 left-0 w-fit text-[8px] font-semibold text-gray-500">( From: {from} )</div>
      )}
      <div className="z-10 flex flex-col gap-1 p-2 ">{children}</div>
      {arrow && (
        <div className="absolute -bottom-2 left-1/2 -z-10 h-6 w-6 -translate-x-1/2 rotate-45 transform border border-gray-200 bg-white" />
      )}
    </div>
  )
}
