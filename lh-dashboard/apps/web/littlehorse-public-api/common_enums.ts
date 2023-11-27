/* eslint-disable */

export const protobufPackage = "littlehorse";

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

export function lHStatusToJSON(object: LHStatus): string {
  switch (object) {
    case LHStatus.STARTING:
      return "STARTING";
    case LHStatus.RUNNING:
      return "RUNNING";
    case LHStatus.COMPLETED:
      return "COMPLETED";
    case LHStatus.HALTING:
      return "HALTING";
    case LHStatus.HALTED:
      return "HALTED";
    case LHStatus.ERROR:
      return "ERROR";
    case LHStatus.EXCEPTION:
      return "EXCEPTION";
    case LHStatus.UNRECOGNIZED:
    default:
      return "UNRECOGNIZED";
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

export enum TaskStatus {
  TASK_SCHEDULED = "TASK_SCHEDULED",
  TASK_RUNNING = "TASK_RUNNING",
  TASK_SUCCESS = "TASK_SUCCESS",
  TASK_FAILED = "TASK_FAILED",
  TASK_TIMEOUT = "TASK_TIMEOUT",
  TASK_OUTPUT_SERIALIZING_ERROR = "TASK_OUTPUT_SERIALIZING_ERROR",
  TASK_INPUT_VAR_SUB_ERROR = "TASK_INPUT_VAR_SUB_ERROR",
  TASK_CANCELLED = "TASK_CANCELLED",
  TASK_EXCEPTION = "TASK_EXCEPTION",
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
    case 7:
    case "TASK_CANCELLED":
      return TaskStatus.TASK_CANCELLED;
    case 8:
    case "TASK_EXCEPTION":
      return TaskStatus.TASK_EXCEPTION;
    case -1:
    case "UNRECOGNIZED":
    default:
      return TaskStatus.UNRECOGNIZED;
  }
}

export function taskStatusToJSON(object: TaskStatus): string {
  switch (object) {
    case TaskStatus.TASK_SCHEDULED:
      return "TASK_SCHEDULED";
    case TaskStatus.TASK_RUNNING:
      return "TASK_RUNNING";
    case TaskStatus.TASK_SUCCESS:
      return "TASK_SUCCESS";
    case TaskStatus.TASK_FAILED:
      return "TASK_FAILED";
    case TaskStatus.TASK_TIMEOUT:
      return "TASK_TIMEOUT";
    case TaskStatus.TASK_OUTPUT_SERIALIZING_ERROR:
      return "TASK_OUTPUT_SERIALIZING_ERROR";
    case TaskStatus.TASK_INPUT_VAR_SUB_ERROR:
      return "TASK_INPUT_VAR_SUB_ERROR";
    case TaskStatus.TASK_CANCELLED:
      return "TASK_CANCELLED";
    case TaskStatus.TASK_EXCEPTION:
      return "TASK_EXCEPTION";
    case TaskStatus.UNRECOGNIZED:
    default:
      return "UNRECOGNIZED";
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
    case TaskStatus.TASK_CANCELLED:
      return 7;
    case TaskStatus.TASK_EXCEPTION:
      return 8;
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

export function metricsWindowLengthToJSON(object: MetricsWindowLength): string {
  switch (object) {
    case MetricsWindowLength.MINUTES_5:
      return "MINUTES_5";
    case MetricsWindowLength.HOURS_2:
      return "HOURS_2";
    case MetricsWindowLength.DAYS_1:
      return "DAYS_1";
    case MetricsWindowLength.UNRECOGNIZED:
    default:
      return "UNRECOGNIZED";
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

export enum VariableType {
  JSON_OBJ = "JSON_OBJ",
  JSON_ARR = "JSON_ARR",
  DOUBLE = "DOUBLE",
  BOOL = "BOOL",
  STR = "STR",
  INT = "INT",
  BYTES = "BYTES",
  NULL = "NULL",
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
    case 7:
    case "NULL":
      return VariableType.NULL;
    case -1:
    case "UNRECOGNIZED":
    default:
      return VariableType.UNRECOGNIZED;
  }
}

export function variableTypeToJSON(object: VariableType): string {
  switch (object) {
    case VariableType.JSON_OBJ:
      return "JSON_OBJ";
    case VariableType.JSON_ARR:
      return "JSON_ARR";
    case VariableType.DOUBLE:
      return "DOUBLE";
    case VariableType.BOOL:
      return "BOOL";
    case VariableType.STR:
      return "STR";
    case VariableType.INT:
      return "INT";
    case VariableType.BYTES:
      return "BYTES";
    case VariableType.NULL:
      return "NULL";
    case VariableType.UNRECOGNIZED:
    default:
      return "UNRECOGNIZED";
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
    case VariableType.NULL:
      return 7;
    case VariableType.UNRECOGNIZED:
    default:
      return -1;
  }
}

export enum LHErrorType {
  CHILD_FAILURE = "CHILD_FAILURE",
  VAR_SUB_ERROR = "VAR_SUB_ERROR",
  VAR_MUTATION_ERROR = "VAR_MUTATION_ERROR",
  USER_TASK_CANCELLED = "USER_TASK_CANCELLED",
  TIMEOUT = "TIMEOUT",
  TASK_FAILURE = "TASK_FAILURE",
  VAR_ERROR = "VAR_ERROR",
  TASK_ERROR = "TASK_ERROR",
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

export function lHErrorTypeToJSON(object: LHErrorType): string {
  switch (object) {
    case LHErrorType.CHILD_FAILURE:
      return "CHILD_FAILURE";
    case LHErrorType.VAR_SUB_ERROR:
      return "VAR_SUB_ERROR";
    case LHErrorType.VAR_MUTATION_ERROR:
      return "VAR_MUTATION_ERROR";
    case LHErrorType.USER_TASK_CANCELLED:
      return "USER_TASK_CANCELLED";
    case LHErrorType.TIMEOUT:
      return "TIMEOUT";
    case LHErrorType.TASK_FAILURE:
      return "TASK_FAILURE";
    case LHErrorType.VAR_ERROR:
      return "VAR_ERROR";
    case LHErrorType.TASK_ERROR:
      return "TASK_ERROR";
    case LHErrorType.INTERNAL_ERROR:
      return "INTERNAL_ERROR";
    case LHErrorType.UNRECOGNIZED:
    default:
      return "UNRECOGNIZED";
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

export enum WaitForThreadsPolicy {
  STOP_ON_FAILURE = "STOP_ON_FAILURE",
  UNRECOGNIZED = "UNRECOGNIZED",
}

export function waitForThreadsPolicyFromJSON(object: any): WaitForThreadsPolicy {
  switch (object) {
    case 0:
    case "STOP_ON_FAILURE":
      return WaitForThreadsPolicy.STOP_ON_FAILURE;
    case -1:
    case "UNRECOGNIZED":
    default:
      return WaitForThreadsPolicy.UNRECOGNIZED;
  }
}

export function waitForThreadsPolicyToJSON(object: WaitForThreadsPolicy): string {
  switch (object) {
    case WaitForThreadsPolicy.STOP_ON_FAILURE:
      return "STOP_ON_FAILURE";
    case WaitForThreadsPolicy.UNRECOGNIZED:
    default:
      return "UNRECOGNIZED";
  }
}

export function waitForThreadsPolicyToNumber(object: WaitForThreadsPolicy): number {
  switch (object) {
    case WaitForThreadsPolicy.STOP_ON_FAILURE:
      return 0;
    case WaitForThreadsPolicy.UNRECOGNIZED:
    default:
      return -1;
  }
}
