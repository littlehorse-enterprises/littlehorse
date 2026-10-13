'use client'

import { useNow } from '@/app/hooks/useNow'
import { DateLike, toDate } from '@/app/utils'
import { LHStatus } from 'littlehorse-client/proto'
import { FC } from 'react'

export const formatRangeDuration = (start: DateLike, endMs: number) => {
  const s = toDate(start)?.getTime()
  if (s === undefined) return '—'
  const sec = Math.max(0, Math.floor((endMs - s) / 1000))
  if (sec < 60) return `${sec}s`
  const m = Math.floor(sec / 60)
  const rs = sec % 60
  if (m < 60) return `${m}m ${rs}s`
  const h = Math.floor(m / 60)
  const rm = m % 60
  return `${h}h ${rm}m`
}

type Props = { startTime?: DateLike; endTime?: DateLike; status: LHStatus }

export const WfRunDuration: FC<Props> = ({ startTime, endTime, status }) => {
  const end = toDate(endTime)?.getTime()
  const now = useNow(status === LHStatus.RUNNING && end === undefined && !!startTime)
  return <>{formatRangeDuration(startTime, end ?? now)}</>
}
