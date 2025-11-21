
import { LHStatus, TaskStatus, WfSpec } from 'littlehorse-client/proto'
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
export type Pin = {
  color: string
  Icon: React.ForwardRefExoticComponent<
    React.PropsWithoutRef<React.SVGProps<SVGSVGElement>> & {
      title?: string
      titleId?: string
    } & React.RefAttributes<SVGSVGElement>
  >
}
export const WF_SPEC_STATUS_COLOR: { [key in WfSpec['status']]: string } = {
  ARCHIVED: 'bg-gray-200',
  ACTIVE: 'bg-blue-200',
  TERMINATING: 'bg-yellow-200',
  UNRECOGNIZED: 'bg-red-200',
}
type WFRunStatusProp = Record<LHStatus, { backgroundColor: string; textColor: string } & Pin>
type TaskStatusProp = Record<TaskStatus, { backgroundColor: string; textColor: string } & Pin>
export const WF_RUN_STATUS: WFRunStatusProp = {
  [LHStatus.STARTING]: {
    color: 'teal',
    textColor: 'text-teal-600',
    backgroundColor: 'bg-teal-200',
    Icon: EllipsisIcon,
  },
  [LHStatus.RUNNING]: {
    color: 'blue',
    textColor: 'text-blue-600',
    backgroundColor: 'bg-blue-200',
    Icon: CirclePlayIcon,
  },
  [LHStatus.COMPLETED]: {
    color: 'green',
    textColor: 'text-green-600',
    backgroundColor: 'bg-green-200',
    Icon: CheckIcon,
  },
  [LHStatus.HALTING]: {
    color: 'orange',
    backgroundColor: 'bg-orange-200',
    textColor: 'text-orange-600',
    Icon: CircleSlashIcon,
  },
  [LHStatus.HALTED]: {
    color: 'gray',
    textColor: 'text-gray-600',
    backgroundColor: 'bg-gray-200',
    Icon: CirclePauseIcon,
  },
  [LHStatus.ERROR]: {
    color: 'yellow',
    textColor: 'text-yellow-600',
    backgroundColor: 'bg-yellow-200',
    Icon: CircleXIcon,
  },
  [LHStatus.EXCEPTION]: {
    color: 'red',
    textColor: 'text-red-600',
    backgroundColor: 'bg-red-200',
    Icon: TriangleAlertIcon,
  },
  [LHStatus.UNRECOGNIZED]: {
    color: 'gray',
    textColor: 'text-gray-600',
    backgroundColor: 'bg-gray-200',
    Icon: BugIcon,
  },
}
export const TASK_STATUS: TaskStatusProp = {
  [TaskStatus.TASK_SCHEDULED]: {
    color: 'teal',
    textColor: 'text-teal-600',
    backgroundColor: 'bg-teal-200',
    Icon: EllipsisIcon,
  },
  [TaskStatus.TASK_RUNNING]: {
    color: 'blue',
    textColor: 'text-blue-600',
    backgroundColor: 'bg-blue-200',
    Icon: CirclePlayIcon,
  },
  [TaskStatus.TASK_SUCCESS]: {
    color: 'green',
    textColor: 'text-green-600',
    backgroundColor: 'bg-green-200',
    Icon: CheckIcon,
  },
  [TaskStatus.TASK_FAILED]: {
    color: 'orange',
    backgroundColor: 'bg-orange-200',
    textColor: 'text-orange-600',
    Icon: CircleSlashIcon,
  },
  [TaskStatus.TASK_TIMEOUT]: {
    color: 'yellow',
    textColor: 'text-yellow-600',
    backgroundColor: 'bg-yellow-200',
    Icon: CircleXIcon,
  },
  [TaskStatus.TASK_OUTPUT_SERDE_ERROR]: {
    color: 'orange',
    backgroundColor: 'bg-orange-200',
    textColor: 'text-orange-600',
    Icon: CircleSlashIcon,
  },
  [TaskStatus.TASK_INPUT_VAR_SUB_ERROR]: {
    color: 'orange',
    backgroundColor: 'bg-orange-200',
    textColor: 'text-orange-600',
    Icon: CircleSlashIcon,
  },
  [TaskStatus.TASK_EXCEPTION]: {
    color: 'red',
    textColor: 'text-red-600',
    backgroundColor: 'bg-red-200',
    Icon: TriangleAlertIcon,
  },
  [TaskStatus.TASK_PENDING]: {
    color: 'blue',
    textColor: 'text-blue-600',
    backgroundColor: 'bg-blue-200',
    Icon: CirclePlayIcon,
  },
  [TaskStatus.UNRECOGNIZED]: {
    color: 'teal',
    textColor: 'text-teal-600',
    backgroundColor: 'bg-teal-200',
    Icon: EllipsisIcon,
  },
}
