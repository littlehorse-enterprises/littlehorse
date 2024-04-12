import {
  ExclamationTriangleIcon,
  XCircleIcon,
  BugAntIcon,
  CheckIcon,
  EllipsisHorizontalIcon,
  PlayIcon,
  PlayPauseIcon,
  StopIcon,
} from '@heroicons/react/24/outline'
import { LHStatus } from 'littlehorse-client/dist/proto/common_enums'
import React, { FC } from 'react'

type Pin = {
  color: string
  Icon: React.ForwardRefExoticComponent<
    React.PropsWithoutRef<React.SVGProps<SVGSVGElement>> & {
      title?: string
      titleId?: string
    } & React.RefAttributes<SVGSVGElement>
  >
}
const Status: Record<LHStatus, Pin> = {
  [LHStatus.STARTING]: {
    color: 'blue',
    Icon: EllipsisHorizontalIcon,
  },
  [LHStatus.RUNNING]: {
    color: 'green',
    Icon: PlayIcon,
  },
  [LHStatus.COMPLETED]: {
    color: 'green',
    Icon: CheckIcon,
  },
  [LHStatus.HALTING]: {
    color: 'gray',
    Icon: PlayPauseIcon,
  },
  [LHStatus.HALTED]: {
    color: 'gray',
    Icon: StopIcon,
  },
  [LHStatus.ERROR]: {
    color: 'red',
    Icon: XCircleIcon,
  },
  [LHStatus.EXCEPTION]: {
    color: 'orange',
    Icon: ExclamationTriangleIcon,
  },
  [LHStatus.UNRECOGNIZED]: {
    color: 'gray',
    Icon: BugAntIcon,
  },
}

export const StatusPin: FC<{ status?: LHStatus }> = ({ status }) => {
  if (!status) return <></>
  const { color, Icon } = Status[status]
  return (
    <div className={`absolute -right-2 -top-2 rounded-full bg-${color}-200 z-10 p-1`}>
      <Icon className={`h-4 w-4 stroke-${color}-500 fill-transparent`} />
    </div>
  )
}
