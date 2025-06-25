import React from 'react'
import { Clock, CheckCircle, XCircle, Loader2 } from 'lucide-react'
import { Badge } from '@littlehorse-enterprises/ui-library/badge'
import { LHStatus, TaskStatus } from 'littlehorse-client/proto'

// Timestamp can be either a protobuf timestamp with seconds/nanos or a Date-like object
interface ProtobufTimestamp {
  seconds?: number
  nanos?: number
}

type TimestampInput = ProtobufTimestamp | Date | string | null | undefined

/**
 * Format dates for display - more compact format
 */
export function formatDate(timestamp: TimestampInput): string {
  if (!timestamp) return 'In progress'
  try {
    const date = (timestamp as ProtobufTimestamp)?.seconds
      ? new Date(
          (timestamp as ProtobufTimestamp).seconds! * 1000 + ((timestamp as ProtobufTimestamp).nanos || 0) / 1000000
        )
      : new Date(timestamp as Date | string)
    return (
      date.toLocaleTimeString() +
      ' ' +
      date.toLocaleDateString(undefined, { month: 'numeric', day: 'numeric', year: '2-digit' })
    )
  } catch {
    return 'Invalid date'
  }
}

/**
 * Calculate duration between two timestamps
 */
export function calculateDuration(startTime?: string, endTime?: string): string {
  if (!startTime) return 'N/A'
  if (!endTime) return 'In progress'

  const start = new Date(startTime).getTime()
  const end = new Date(endTime).getTime()
  const duration = end - start

  if (duration < 1000) return `${duration}ms`
  if (duration < 60000) return `${(duration / 1000).toFixed(1)}s`
  return `${(duration / 60000).toFixed(1)}m`
}

/**
 * Get status icon based on LHStatus
 */
export function getStatusIcon(status: LHStatus): React.ReactElement {
  if (status === LHStatus.COMPLETED) {
    return <CheckCircle className="mr-1 h-3 w-3 text-green-500" />
  } else if (status === LHStatus.ERROR || status === LHStatus.EXCEPTION) {
    return <XCircle className="mr-1 h-3 w-3 text-red-500" />
  } else if (status === LHStatus.RUNNING) {
    return <Loader2 className="mr-1 h-3 w-3 animate-spin text-blue-500" />
  } else {
    return <Clock className="mr-1 h-3 w-3 text-[#656565]" />
  }
}

/**
 * Get status badge based on LHStatus
 */
export function getStatusBadge(status: LHStatus): React.ReactElement {
  if (status === LHStatus.COMPLETED) {
    return (
      <Badge variant="secondary" className="bg-green-100 text-green-800 hover:bg-green-100">
        COMPLETED
      </Badge>
    )
  } else if (status === LHStatus.ERROR || status === LHStatus.EXCEPTION) {
    return (
      <Badge variant="destructive" className="bg-red-100 text-red-800 hover:bg-red-100">
        FAILED
      </Badge>
    )
  } else if (status === LHStatus.RUNNING) {
    return (
      <Badge variant="secondary" className="bg-blue-100 text-blue-800 hover:bg-blue-100">
        RUNNING
      </Badge>
    )
  } else {
    return (
      <Badge variant="outline" className="bg-gray-100 text-gray-800 hover:bg-gray-100">
        HALTED
      </Badge>
    )
  }
}

/**
 * Get status icon based on TaskStatus
 */
export function getTaskStatusIcon(status: TaskStatus): React.ReactElement {
  if (status === TaskStatus.TASK_SUCCESS) {
    return <CheckCircle className="mr-1 h-3 w-3 text-green-500" />
  } else if (
    status === TaskStatus.TASK_FAILED ||
    status === TaskStatus.TASK_TIMEOUT ||
    status === TaskStatus.TASK_OUTPUT_SERDE_ERROR ||
    status === TaskStatus.TASK_INPUT_VAR_SUB_ERROR ||
    status === TaskStatus.TASK_EXCEPTION
  ) {
    return <XCircle className="mr-1 h-3 w-3 text-red-500" />
  } else if (status === TaskStatus.TASK_RUNNING) {
    return <Loader2 className="mr-1 h-3 w-3 animate-spin text-blue-500" />
  } else {
    return <Clock className="mr-1 h-3 w-3 text-[#656565]" />
  }
}

/**
 * Get status badge based on TaskStatus
 */
export function getTaskStatusBadge(status: TaskStatus): React.ReactElement {
  if (status === TaskStatus.TASK_SUCCESS) {
    return (
      <Badge variant="secondary" className="bg-green-100 text-green-800 hover:bg-green-100">
        SUCCESS
      </Badge>
    )
  } else if (
    status === TaskStatus.TASK_FAILED ||
    status === TaskStatus.TASK_TIMEOUT ||
    status === TaskStatus.TASK_OUTPUT_SERDE_ERROR ||
    status === TaskStatus.TASK_INPUT_VAR_SUB_ERROR ||
    status === TaskStatus.TASK_EXCEPTION
  ) {
    return (
      <Badge variant="destructive" className="bg-red-100 text-red-800 hover:bg-red-100">
        FAILED
      </Badge>
    )
  } else if (status === TaskStatus.TASK_RUNNING) {
    return (
      <Badge variant="secondary" className="bg-blue-100 text-blue-800 hover:bg-blue-100">
        RUNNING
      </Badge>
    )
  } else {
    return (
      <Badge variant="outline" className="bg-gray-100 text-gray-800 hover:bg-gray-100">
        SCHEDULED
      </Badge>
    )
  }
}
