import { WfRun, WfSpec } from "littlehorse-client/proto";

export const wfSpecStatusColor: { [key in WfSpec['status']]: string } = {
  ARCHIVED: 'bg-gray-200',
  ACTIVE: 'bg-blue-200',
  TERMINATING: 'bg-yellow-200',
  UNRECOGNIZED: 'bg-red-200',
}
export const wfRunStatusColor: { [key in WfRun['status']]: string } = {
  STARTING: 'bg-teal-200',
  RUNNING: 'bg-blue-200',
  COMPLETED: 'bg-green-200',
  HALTING: 'bg-orange-200',
  HALTED: 'bg-gray-200',
  ERROR: 'bg-yellow-200',
  EXCEPTION: 'bg-red-200',
  UNRECOGNIZED: 'bg-gray-200',
}

export const wfRunStatusColorText : { [key in WfRun['status']]: string } = {
  STARTING: 'teal-600',
  RUNNING: 'blue-600',
  COMPLETED: 'green-600',
  HALTING: 'orange-600',
  HALTED: 'gray-600',
  ERROR: 'yellow-600',
  EXCEPTION: 'red-600',
  UNRECOGNIZED: 'gray-600',
}