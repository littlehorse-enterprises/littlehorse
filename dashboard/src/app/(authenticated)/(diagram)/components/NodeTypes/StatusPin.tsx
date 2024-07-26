import { LHStatus } from 'littlehorse-client/proto'
import {
  BugIcon,
  CheckIcon,
  CirclePauseIcon,
  CirclePlayIcon,
  CircleSlashIcon,
  CircleXIcon,
  EllipsisIcon,
  TriangleAlertIcon,
} from 'lucide-react'
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
    Icon: EllipsisIcon,
  },
  [LHStatus.RUNNING]: {
    color: 'green',
    Icon: CirclePlayIcon,
  },
  [LHStatus.COMPLETED]: {
    color: 'green',
    Icon: CheckIcon,
  },
  [LHStatus.HALTING]: {
    color: 'gray',
    Icon: CircleSlashIcon,
  },
  [LHStatus.HALTED]: {
    color: 'gray',
    Icon: CirclePauseIcon,
  },
  [LHStatus.ERROR]: {
    color: 'red',
    Icon: CircleXIcon,
  },
  [LHStatus.EXCEPTION]: {
    color: 'orange',
    Icon: TriangleAlertIcon,
  },
  [LHStatus.UNRECOGNIZED]: {
    color: 'gray',
    Icon: BugIcon,
  },
}

export const StatusPin: FC<{ status?: LHStatus }> = ({ status }) => {
  if (!status) return <></>
  const { color, Icon } = Status[status]
  return (
    <div className={`absolute -right-4 -top-4 rounded-full bg-${color}-200 z-10 p-1`}>
      <Icon className={`h-4 w-4 stroke-${color}-500 fill-transparent`} />
    </div>
  )
}
