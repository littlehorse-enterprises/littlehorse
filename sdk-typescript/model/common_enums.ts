/* eslint-disable */

export const protobufPackage = "littlehorse";

export enum LHStatus {
  STARTING = 0,
  RUNNING = 1,
  COMPLETED = 2,
  HALTING = 3,
  HALTED = 4,
  ERROR = 5,
  EXCEPTION = 6,
  UNRECOGNIZED = -1,
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

export enum TaskStatus {
  TASK_SCHEDULED = 0,
  TASK_RUNNING = 1,
  TASK_SUCCESS = 2,
  TASK_FAILED = 3,
  TASK_TIMEOUT = 4,
  TASK_OUTPUT_SERIALIZING_ERROR = 5,
  TASK_INPUT_VAR_SUB_ERROR = 6,
  TASK_CANCELLED = 7,
  UNRECOGNIZED = -1,
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
    case TaskStatus.UNRECOGNIZED:
    default:
      return "UNRECOGNIZED";
  }
}

/** Metrics */
export enum MetricsWindowLength {
  MINUTES_5 = 0,
  HOURS_2 = 1,
  DAYS_1 = 2,
  UNRECOGNIZED = -1,
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

export enum VariableType {
  JSON_OBJ = 0,
  JSON_ARR = 1,
  DOUBLE = 2,
  BOOL = 3,
  STR = 4,
  INT = 5,
  BYTES = 6,
  NULL = 7,
  UNRECOGNIZED = -1,
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

export enum WaitForThreadsPolicy {
  STOP_ON_FAILURE = 0,
  UNRECOGNIZED = -1,
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
