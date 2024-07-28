// Code generated by protoc-gen-ts_proto. DO NOT EDIT.
// versions:
//   protoc-gen-ts_proto  v1.178.0
//   protoc               v4.23.4
// source: common_enums.proto

/* eslint-disable */

/** Status used for WfRun, ThreadRun, and NodeRun */
export enum LHStatus {
  STARTING = "STARTING",
  RUNNING = "RUNNING",
  COMPLETED = "COMPLETED",
  HALTING = "HALTING",
  HALTED = "HALTED",
  ERROR = "ERROR",
  EXCEPTION = "EXCEPTION",
  UNRECOGNIZED = "UNRECOGNIZED",
}

export function lHStatusFromJSON(object: any): LHStatus {
  switch (object) {
    case 0:
    case "STARTING":
      return LHStatus.STARTING;
    case 1:
    case "RUNNING":
      return LHStatus.RUNNING;
    case 2:
    case "COMPLETED":
      return LHStatus.COMPLETED;
    case 3:
    case "HALTING":
      return LHStatus.HALTING;
    case 4:
    case "HALTED":
      return LHStatus.HALTED;
    case 5:
    case "ERROR":
      return LHStatus.ERROR;
    case 6:
    case "EXCEPTION":
      return LHStatus.EXCEPTION;
    case -1:
    case "UNRECOGNIZED":
    default:
      return LHStatus.UNRECOGNIZED;
  }
}

export function lHStatusToNumber(object: LHStatus): number {
  switch (object) {
    case LHStatus.STARTING:
      return 0;
    case LHStatus.RUNNING:
      return 1;
    case LHStatus.COMPLETED:
      return 2;
    case LHStatus.HALTING:
      return 3;
    case LHStatus.HALTED:
      return 4;
    case LHStatus.ERROR:
      return 5;
    case LHStatus.EXCEPTION:
      return 6;
    case LHStatus.UNRECOGNIZED:
    default:
      return -1;
  }
}

/** Status of a Metadata Object, such as WfSpec or TaskDef */
export enum MetadataStatus {
  /** ACTIVE - ACTIVE means the object can be used. */
  ACTIVE = "ACTIVE",
  /**
   * ARCHIVED - An ARCHIVED WfSpec can no longer be used to create new WfRun's, but
   * existing WfRun's will be allowed to run to completion.
   */
  ARCHIVED = "ARCHIVED",
  /**
   * TERMINATING - A TERMINATING WfSpec is actively deleting all running WfRun's, and will
   * self-destruct once all of its child WfRun's are terminated.
   */
  TERMINATING = "TERMINATING",
  UNRECOGNIZED = "UNRECOGNIZED",
}

export function metadataStatusFromJSON(object: any): MetadataStatus {
  switch (object) {
    case 0:
    case "ACTIVE":
      return MetadataStatus.ACTIVE;
    case 1:
    case "ARCHIVED":
      return MetadataStatus.ARCHIVED;
    case 2:
    case "TERMINATING":
      return MetadataStatus.TERMINATING;
    case -1:
    case "UNRECOGNIZED":
    default:
      return MetadataStatus.UNRECOGNIZED;
  }
}

export function metadataStatusToNumber(object: MetadataStatus): number {
  switch (object) {
    case MetadataStatus.ACTIVE:
      return 0;
    case MetadataStatus.ARCHIVED:
      return 1;
    case MetadataStatus.TERMINATING:
      return 2;
    case MetadataStatus.UNRECOGNIZED:
    default:
      return -1;
  }
}

/** Status of a TaskRun. */
export enum TaskStatus {
  /** TASK_SCHEDULED - Scheduled in the Task Queue but not yet picked up by a Task Worker. */
  TASK_SCHEDULED = "TASK_SCHEDULED",
  /** TASK_RUNNING - Picked up by a Task Worker, but not yet reported or timed out. */
  TASK_RUNNING = "TASK_RUNNING",
  /** TASK_SUCCESS - Successfully completed. */
  TASK_SUCCESS = "TASK_SUCCESS",
  /** TASK_FAILED - Task Worker reported a technical failure while attempting to execute the TaskRun */
  TASK_FAILED = "TASK_FAILED",
  /** TASK_TIMEOUT - Task Worker did not report a result in time. */
  TASK_TIMEOUT = "TASK_TIMEOUT",
  /** TASK_OUTPUT_SERIALIZING_ERROR - Task Worker reported that it was unable to serialize the output of the TaskRun. */
  TASK_OUTPUT_SERIALIZING_ERROR = "TASK_OUTPUT_SERIALIZING_ERROR",
  /**
   * TASK_INPUT_VAR_SUB_ERROR - Task Worker was unable to deserialize the input variables into appropriate language-specific
   * objects to pass into the Task Function
   */
  TASK_INPUT_VAR_SUB_ERROR = "TASK_INPUT_VAR_SUB_ERROR",
  /** TASK_EXCEPTION - Task Function business logic determined that there was a business exception. */
  TASK_EXCEPTION = "TASK_EXCEPTION",
  /**
   * TASK_PENDING - Refers to a TaskAttempt that is not yet scheduled. This happens when using retries
   * with an ExponentialBackoffRetryPolicy: the TaskAttempt isn't supposed to be scheduled
   * until it "matures", but it does already exist.
   */
  TASK_PENDING = "TASK_PENDING",
  UNRECOGNIZED = "UNRECOGNIZED",
}

export function taskStatusFromJSON(object: any): TaskStatus {
  switch (object) {
    case 0:
    case "TASK_SCHEDULED":
      return TaskStatus.TASK_SCHEDULED;
    case 1:
    case "TASK_RUNNING":
      return TaskStatus.TASK_RUNNING;
    case 2:
    case "TASK_SUCCESS":
      return TaskStatus.TASK_SUCCESS;
    case 3:
    case "TASK_FAILED":
      return TaskStatus.TASK_FAILED;
    case 4:
    case "TASK_TIMEOUT":
      return TaskStatus.TASK_TIMEOUT;
    case 5:
    case "TASK_OUTPUT_SERIALIZING_ERROR":
      return TaskStatus.TASK_OUTPUT_SERIALIZING_ERROR;
    case 6:
    case "TASK_INPUT_VAR_SUB_ERROR":
      return TaskStatus.TASK_INPUT_VAR_SUB_ERROR;
    case 8:
    case "TASK_EXCEPTION":
      return TaskStatus.TASK_EXCEPTION;
    case 9:
    case "TASK_PENDING":
      return TaskStatus.TASK_PENDING;
    case -1:
    case "UNRECOGNIZED":
    default:
      return TaskStatus.UNRECOGNIZED;
  }
}

export function taskStatusToNumber(object: TaskStatus): number {
  switch (object) {
    case TaskStatus.TASK_SCHEDULED:
      return 0;
    case TaskStatus.TASK_RUNNING:
      return 1;
    case TaskStatus.TASK_SUCCESS:
      return 2;
    case TaskStatus.TASK_FAILED:
      return 3;
    case TaskStatus.TASK_TIMEOUT:
      return 4;
    case TaskStatus.TASK_OUTPUT_SERIALIZING_ERROR:
      return 5;
    case TaskStatus.TASK_INPUT_VAR_SUB_ERROR:
      return 6;
    case TaskStatus.TASK_EXCEPTION:
      return 8;
    case TaskStatus.TASK_PENDING:
      return 9;
    case TaskStatus.UNRECOGNIZED:
    default:
      return -1;
  }
}

/** Metrics */
export enum MetricsWindowLength {
  MINUTES_5 = "MINUTES_5",
  HOURS_2 = "HOURS_2",
  DAYS_1 = "DAYS_1",
  UNRECOGNIZED = "UNRECOGNIZED",
}

export function metricsWindowLengthFromJSON(object: any): MetricsWindowLength {
  switch (object) {
    case 0:
    case "MINUTES_5":
      return MetricsWindowLength.MINUTES_5;
    case 1:
    case "HOURS_2":
      return MetricsWindowLength.HOURS_2;
    case 2:
    case "DAYS_1":
      return MetricsWindowLength.DAYS_1;
    case -1:
    case "UNRECOGNIZED":
    default:
      return MetricsWindowLength.UNRECOGNIZED;
  }
}

export function metricsWindowLengthToNumber(object: MetricsWindowLength): number {
  switch (object) {
    case MetricsWindowLength.MINUTES_5:
      return 0;
    case MetricsWindowLength.HOURS_2:
      return 1;
    case MetricsWindowLength.DAYS_1:
      return 2;
    case MetricsWindowLength.UNRECOGNIZED:
    default:
      return -1;
  }
}

/**
 * Type of a Varaible in LittleHorse. Corresponds to the possible value type's of a
 * VariableValue.
 */
export enum VariableType {
  /** JSON_OBJ - An object represented as a json string. */
  JSON_OBJ = "JSON_OBJ",
  /** JSON_ARR - A list represented as a json array string. */
  JSON_ARR = "JSON_ARR",
  /** DOUBLE - A 64-bit floating point number. */
  DOUBLE = "DOUBLE",
  /** BOOL - A boolean */
  BOOL = "BOOL",
  /** STR - A string */
  STR = "STR",
  /** INT - A 64-bit integer */
  INT = "INT",
  /** BYTES - A byte array */
  BYTES = "BYTES",
  UNRECOGNIZED = "UNRECOGNIZED",
}

export function variableTypeFromJSON(object: any): VariableType {
  switch (object) {
    case 0:
    case "JSON_OBJ":
      return VariableType.JSON_OBJ;
    case 1:
    case "JSON_ARR":
      return VariableType.JSON_ARR;
    case 2:
    case "DOUBLE":
      return VariableType.DOUBLE;
    case 3:
    case "BOOL":
      return VariableType.BOOL;
    case 4:
    case "STR":
      return VariableType.STR;
    case 5:
    case "INT":
      return VariableType.INT;
    case 6:
    case "BYTES":
      return VariableType.BYTES;
    case -1:
    case "UNRECOGNIZED":
    default:
      return VariableType.UNRECOGNIZED;
  }
}

export function variableTypeToNumber(object: VariableType): number {
  switch (object) {
    case VariableType.JSON_OBJ:
      return 0;
    case VariableType.JSON_ARR:
      return 1;
    case VariableType.DOUBLE:
      return 2;
    case VariableType.BOOL:
      return 3;
    case VariableType.STR:
      return 4;
    case VariableType.INT:
      return 5;
    case VariableType.BYTES:
      return 6;
    case VariableType.UNRECOGNIZED:
    default:
      return -1;
  }
}

/** This enum is all of the types of technical failure that can occur in a WfRun. */
export enum LHErrorType {
  /** CHILD_FAILURE - A child ThreadRun failed with a technical ERROR. */
  CHILD_FAILURE = "CHILD_FAILURE",
  /** VAR_SUB_ERROR - Failed substituting input variables into a NodeRun. */
  VAR_SUB_ERROR = "VAR_SUB_ERROR",
  /** VAR_MUTATION_ERROR - Failed mutating variables after a NodeRun successfully completed. */
  VAR_MUTATION_ERROR = "VAR_MUTATION_ERROR",
  /** USER_TASK_CANCELLED - A UserTaskRun was cancelled (EVOLVING: this will become a Business EXCEPTION) */
  USER_TASK_CANCELLED = "USER_TASK_CANCELLED",
  /** TIMEOUT - A NodeRun failed due to a timeout. */
  TIMEOUT = "TIMEOUT",
  /** TASK_FAILURE - A TaskRun failed due to an unexpected error. */
  TASK_FAILURE = "TASK_FAILURE",
  /** VAR_ERROR - Wrapper for VAR_SUB_ERROR and VAR_MUTATION_ERROR */
  VAR_ERROR = "VAR_ERROR",
  /** TASK_ERROR - Wrapper for TASK_FALIURE and TIMEOUT */
  TASK_ERROR = "TASK_ERROR",
  /** INTERNAL_ERROR - An unexpected LittleHorse Internal error occurred. This is not expected to happen. */
  INTERNAL_ERROR = "INTERNAL_ERROR",
  UNRECOGNIZED = "UNRECOGNIZED",
}

export function lHErrorTypeFromJSON(object: any): LHErrorType {
  switch (object) {
    case 0:
    case "CHILD_FAILURE":
      return LHErrorType.CHILD_FAILURE;
    case 1:
    case "VAR_SUB_ERROR":
      return LHErrorType.VAR_SUB_ERROR;
    case 2:
    case "VAR_MUTATION_ERROR":
      return LHErrorType.VAR_MUTATION_ERROR;
    case 3:
    case "USER_TASK_CANCELLED":
      return LHErrorType.USER_TASK_CANCELLED;
    case 4:
    case "TIMEOUT":
      return LHErrorType.TIMEOUT;
    case 5:
    case "TASK_FAILURE":
      return LHErrorType.TASK_FAILURE;
    case 6:
    case "VAR_ERROR":
      return LHErrorType.VAR_ERROR;
    case 7:
    case "TASK_ERROR":
      return LHErrorType.TASK_ERROR;
    case 8:
    case "INTERNAL_ERROR":
      return LHErrorType.INTERNAL_ERROR;
    case -1:
    case "UNRECOGNIZED":
    default:
      return LHErrorType.UNRECOGNIZED;
  }
}

export function lHErrorTypeToNumber(object: LHErrorType): number {
  switch (object) {
    case LHErrorType.CHILD_FAILURE:
      return 0;
    case LHErrorType.VAR_SUB_ERROR:
      return 1;
    case LHErrorType.VAR_MUTATION_ERROR:
      return 2;
    case LHErrorType.USER_TASK_CANCELLED:
      return 3;
    case LHErrorType.TIMEOUT:
      return 4;
    case LHErrorType.TASK_FAILURE:
      return 5;
    case LHErrorType.VAR_ERROR:
      return 6;
    case LHErrorType.TASK_ERROR:
      return 7;
    case LHErrorType.INTERNAL_ERROR:
      return 8;
    case LHErrorType.UNRECOGNIZED:
    default:
      return -1;
  }
}
