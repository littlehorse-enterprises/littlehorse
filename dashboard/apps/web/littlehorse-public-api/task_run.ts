/* eslint-disable */
import * as _m0 from "protobufjs/minimal";
import {
  LHErrorType,
  lHErrorTypeFromJSON,
  lHErrorTypeToJSON,
  lHErrorTypeToNumber,
  TaskStatus,
  taskStatusFromJSON,
  taskStatusToJSON,
  taskStatusToNumber,
} from "./common_enums";
import { Timestamp } from "./google/protobuf/timestamp";
import { NodeRunId, TaskDefId, TaskRunId, WfSpecId } from "./object_id";
import { UserTaskTriggerReference } from "./user_tasks";
import { VariableValue } from "./variable";

export const protobufPackage = "littlehorse";

export interface TaskRun {
  id: TaskRunId | undefined;
  taskDefId: TaskDefId | undefined;
  attempts: TaskAttempt[];
  maxAttempts: number;
  inputVariables: VarNameAndVal[];
  source: TaskRunSource | undefined;
  scheduledAt: string | undefined;
  status: TaskStatus;
  timeoutSeconds: number;
}

export interface VarNameAndVal {
  varName: string;
  value: VariableValue | undefined;
}

export interface TaskAttempt {
  logOutput?: VariableValue | undefined;
  scheduleTime?: string | undefined;
  startTime?: string | undefined;
  endTime?: string | undefined;
  taskWorkerId: string;
  taskWorkerVersion?: string | undefined;
  status: TaskStatus;
  output?: VariableValue | undefined;
  error?: LHTaskError | undefined;
  exception?: LHTaskException | undefined;
}

export interface TaskRunSource {
  taskNode?: TaskNodeReference | undefined;
  userTaskTrigger?: UserTaskTriggerReference | undefined;
  wfSpecId?: WfSpecId | undefined;
}

export interface TaskNodeReference {
  nodeRunId: NodeRunId | undefined;
}

export interface LHTaskError {
  type: LHErrorType;
  message: string;
}

export interface LHTaskException {
  name: string;
  message: string;
}

function createBaseTaskRun(): TaskRun {
  return {
    id: undefined,
    taskDefId: undefined,
    attempts: [],
    maxAttempts: 0,
    inputVariables: [],
    source: undefined,
    scheduledAt: undefined,
    status: TaskStatus.TASK_SCHEDULED,
    timeoutSeconds: 0,
  };
}

export const TaskRun = {
  encode(message: TaskRun, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.id !== undefined) {
      TaskRunId.encode(message.id, writer.uint32(10).fork()).ldelim();
    }
    if (message.taskDefId !== undefined) {
      TaskDefId.encode(message.taskDefId, writer.uint32(18).fork()).ldelim();
    }
    for (const v of message.attempts) {
      TaskAttempt.encode(v!, writer.uint32(26).fork()).ldelim();
    }
    if (message.maxAttempts !== 0) {
      writer.uint32(32).int32(message.maxAttempts);
    }
    for (const v of message.inputVariables) {
      VarNameAndVal.encode(v!, writer.uint32(42).fork()).ldelim();
    }
    if (message.source !== undefined) {
      TaskRunSource.encode(message.source, writer.uint32(50).fork()).ldelim();
    }
    if (message.scheduledAt !== undefined) {
      Timestamp.encode(toTimestamp(message.scheduledAt), writer.uint32(58).fork()).ldelim();
    }
    if (message.status !== TaskStatus.TASK_SCHEDULED) {
      writer.uint32(64).int32(taskStatusToNumber(message.status));
    }
    if (message.timeoutSeconds !== 0) {
      writer.uint32(72).int32(message.timeoutSeconds);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): TaskRun {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseTaskRun();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.id = TaskRunId.decode(reader, reader.uint32());
          continue;
        case 2:
          if (tag !== 18) {
            break;
          }

          message.taskDefId = TaskDefId.decode(reader, reader.uint32());
          continue;
        case 3:
          if (tag !== 26) {
            break;
          }

          message.attempts.push(TaskAttempt.decode(reader, reader.uint32()));
          continue;
        case 4:
          if (tag !== 32) {
            break;
          }

          message.maxAttempts = reader.int32();
          continue;
        case 5:
          if (tag !== 42) {
            break;
          }

          message.inputVariables.push(VarNameAndVal.decode(reader, reader.uint32()));
          continue;
        case 6:
          if (tag !== 50) {
            break;
          }

          message.source = TaskRunSource.decode(reader, reader.uint32());
          continue;
        case 7:
          if (tag !== 58) {
            break;
          }

          message.scheduledAt = fromTimestamp(Timestamp.decode(reader, reader.uint32()));
          continue;
        case 8:
          if (tag !== 64) {
            break;
          }

          message.status = taskStatusFromJSON(reader.int32());
          continue;
        case 9:
          if (tag !== 72) {
            break;
          }

          message.timeoutSeconds = reader.int32();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  fromJSON(object: any): TaskRun {
    return {
      id: isSet(object.id) ? TaskRunId.fromJSON(object.id) : undefined,
      taskDefId: isSet(object.taskDefId) ? TaskDefId.fromJSON(object.taskDefId) : undefined,
      attempts: globalThis.Array.isArray(object?.attempts)
        ? object.attempts.map((e: any) => TaskAttempt.fromJSON(e))
        : [],
      maxAttempts: isSet(object.maxAttempts) ? globalThis.Number(object.maxAttempts) : 0,
      inputVariables: globalThis.Array.isArray(object?.inputVariables)
        ? object.inputVariables.map((e: any) => VarNameAndVal.fromJSON(e))
        : [],
      source: isSet(object.source) ? TaskRunSource.fromJSON(object.source) : undefined,
      scheduledAt: isSet(object.scheduledAt) ? globalThis.String(object.scheduledAt) : undefined,
      status: isSet(object.status) ? taskStatusFromJSON(object.status) : TaskStatus.TASK_SCHEDULED,
      timeoutSeconds: isSet(object.timeoutSeconds) ? globalThis.Number(object.timeoutSeconds) : 0,
    };
  },

  toJSON(message: TaskRun): unknown {
    const obj: any = {};
    if (message.id !== undefined) {
      obj.id = TaskRunId.toJSON(message.id);
    }
    if (message.taskDefId !== undefined) {
      obj.taskDefId = TaskDefId.toJSON(message.taskDefId);
    }
    if (message.attempts?.length) {
      obj.attempts = message.attempts.map((e) => TaskAttempt.toJSON(e));
    }
    if (message.maxAttempts !== 0) {
      obj.maxAttempts = Math.round(message.maxAttempts);
    }
    if (message.inputVariables?.length) {
      obj.inputVariables = message.inputVariables.map((e) => VarNameAndVal.toJSON(e));
    }
    if (message.source !== undefined) {
      obj.source = TaskRunSource.toJSON(message.source);
    }
    if (message.scheduledAt !== undefined) {
      obj.scheduledAt = message.scheduledAt;
    }
    if (message.status !== TaskStatus.TASK_SCHEDULED) {
      obj.status = taskStatusToJSON(message.status);
    }
    if (message.timeoutSeconds !== 0) {
      obj.timeoutSeconds = Math.round(message.timeoutSeconds);
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<TaskRun>, I>>(base?: I): TaskRun {
    return TaskRun.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<TaskRun>, I>>(object: I): TaskRun {
    const message = createBaseTaskRun();
    message.id = (object.id !== undefined && object.id !== null) ? TaskRunId.fromPartial(object.id) : undefined;
    message.taskDefId = (object.taskDefId !== undefined && object.taskDefId !== null)
      ? TaskDefId.fromPartial(object.taskDefId)
      : undefined;
    message.attempts = object.attempts?.map((e) => TaskAttempt.fromPartial(e)) || [];
    message.maxAttempts = object.maxAttempts ?? 0;
    message.inputVariables = object.inputVariables?.map((e) => VarNameAndVal.fromPartial(e)) || [];
    message.source = (object.source !== undefined && object.source !== null)
      ? TaskRunSource.fromPartial(object.source)
      : undefined;
    message.scheduledAt = object.scheduledAt ?? undefined;
    message.status = object.status ?? TaskStatus.TASK_SCHEDULED;
    message.timeoutSeconds = object.timeoutSeconds ?? 0;
    return message;
  },
};

function createBaseVarNameAndVal(): VarNameAndVal {
  return { varName: "", value: undefined };
}

export const VarNameAndVal = {
  encode(message: VarNameAndVal, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.varName !== "") {
      writer.uint32(10).string(message.varName);
    }
    if (message.value !== undefined) {
      VariableValue.encode(message.value, writer.uint32(18).fork()).ldelim();
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): VarNameAndVal {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseVarNameAndVal();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.varName = reader.string();
          continue;
        case 2:
          if (tag !== 18) {
            break;
          }

          message.value = VariableValue.decode(reader, reader.uint32());
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  fromJSON(object: any): VarNameAndVal {
    return {
      varName: isSet(object.varName) ? globalThis.String(object.varName) : "",
      value: isSet(object.value) ? VariableValue.fromJSON(object.value) : undefined,
    };
  },

  toJSON(message: VarNameAndVal): unknown {
    const obj: any = {};
    if (message.varName !== "") {
      obj.varName = message.varName;
    }
    if (message.value !== undefined) {
      obj.value = VariableValue.toJSON(message.value);
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<VarNameAndVal>, I>>(base?: I): VarNameAndVal {
    return VarNameAndVal.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<VarNameAndVal>, I>>(object: I): VarNameAndVal {
    const message = createBaseVarNameAndVal();
    message.varName = object.varName ?? "";
    message.value = (object.value !== undefined && object.value !== null)
      ? VariableValue.fromPartial(object.value)
      : undefined;
    return message;
  },
};

function createBaseTaskAttempt(): TaskAttempt {
  return {
    logOutput: undefined,
    scheduleTime: undefined,
    startTime: undefined,
    endTime: undefined,
    taskWorkerId: "",
    taskWorkerVersion: undefined,
    status: TaskStatus.TASK_SCHEDULED,
    output: undefined,
    error: undefined,
    exception: undefined,
  };
}

export const TaskAttempt = {
  encode(message: TaskAttempt, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.logOutput !== undefined) {
      VariableValue.encode(message.logOutput, writer.uint32(18).fork()).ldelim();
    }
    if (message.scheduleTime !== undefined) {
      Timestamp.encode(toTimestamp(message.scheduleTime), writer.uint32(26).fork()).ldelim();
    }
    if (message.startTime !== undefined) {
      Timestamp.encode(toTimestamp(message.startTime), writer.uint32(34).fork()).ldelim();
    }
    if (message.endTime !== undefined) {
      Timestamp.encode(toTimestamp(message.endTime), writer.uint32(42).fork()).ldelim();
    }
    if (message.taskWorkerId !== "") {
      writer.uint32(58).string(message.taskWorkerId);
    }
    if (message.taskWorkerVersion !== undefined) {
      writer.uint32(66).string(message.taskWorkerVersion);
    }
    if (message.status !== TaskStatus.TASK_SCHEDULED) {
      writer.uint32(72).int32(taskStatusToNumber(message.status));
    }
    if (message.output !== undefined) {
      VariableValue.encode(message.output, writer.uint32(10).fork()).ldelim();
    }
    if (message.error !== undefined) {
      LHTaskError.encode(message.error, writer.uint32(82).fork()).ldelim();
    }
    if (message.exception !== undefined) {
      LHTaskException.encode(message.exception, writer.uint32(90).fork()).ldelim();
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): TaskAttempt {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseTaskAttempt();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 2:
          if (tag !== 18) {
            break;
          }

          message.logOutput = VariableValue.decode(reader, reader.uint32());
          continue;
        case 3:
          if (tag !== 26) {
            break;
          }

          message.scheduleTime = fromTimestamp(Timestamp.decode(reader, reader.uint32()));
          continue;
        case 4:
          if (tag !== 34) {
            break;
          }

          message.startTime = fromTimestamp(Timestamp.decode(reader, reader.uint32()));
          continue;
        case 5:
          if (tag !== 42) {
            break;
          }

          message.endTime = fromTimestamp(Timestamp.decode(reader, reader.uint32()));
          continue;
        case 7:
          if (tag !== 58) {
            break;
          }

          message.taskWorkerId = reader.string();
          continue;
        case 8:
          if (tag !== 66) {
            break;
          }

          message.taskWorkerVersion = reader.string();
          continue;
        case 9:
          if (tag !== 72) {
            break;
          }

          message.status = taskStatusFromJSON(reader.int32());
          continue;
        case 1:
          if (tag !== 10) {
            break;
          }

          message.output = VariableValue.decode(reader, reader.uint32());
          continue;
        case 10:
          if (tag !== 82) {
            break;
          }

          message.error = LHTaskError.decode(reader, reader.uint32());
          continue;
        case 11:
          if (tag !== 90) {
            break;
          }

          message.exception = LHTaskException.decode(reader, reader.uint32());
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  fromJSON(object: any): TaskAttempt {
    return {
      logOutput: isSet(object.logOutput) ? VariableValue.fromJSON(object.logOutput) : undefined,
      scheduleTime: isSet(object.scheduleTime) ? globalThis.String(object.scheduleTime) : undefined,
      startTime: isSet(object.startTime) ? globalThis.String(object.startTime) : undefined,
      endTime: isSet(object.endTime) ? globalThis.String(object.endTime) : undefined,
      taskWorkerId: isSet(object.taskWorkerId) ? globalThis.String(object.taskWorkerId) : "",
      taskWorkerVersion: isSet(object.taskWorkerVersion) ? globalThis.String(object.taskWorkerVersion) : undefined,
      status: isSet(object.status) ? taskStatusFromJSON(object.status) : TaskStatus.TASK_SCHEDULED,
      output: isSet(object.output) ? VariableValue.fromJSON(object.output) : undefined,
      error: isSet(object.error) ? LHTaskError.fromJSON(object.error) : undefined,
      exception: isSet(object.exception) ? LHTaskException.fromJSON(object.exception) : undefined,
    };
  },

  toJSON(message: TaskAttempt): unknown {
    const obj: any = {};
    if (message.logOutput !== undefined) {
      obj.logOutput = VariableValue.toJSON(message.logOutput);
    }
    if (message.scheduleTime !== undefined) {
      obj.scheduleTime = message.scheduleTime;
    }
    if (message.startTime !== undefined) {
      obj.startTime = message.startTime;
    }
    if (message.endTime !== undefined) {
      obj.endTime = message.endTime;
    }
    if (message.taskWorkerId !== "") {
      obj.taskWorkerId = message.taskWorkerId;
    }
    if (message.taskWorkerVersion !== undefined) {
      obj.taskWorkerVersion = message.taskWorkerVersion;
    }
    if (message.status !== TaskStatus.TASK_SCHEDULED) {
      obj.status = taskStatusToJSON(message.status);
    }
    if (message.output !== undefined) {
      obj.output = VariableValue.toJSON(message.output);
    }
    if (message.error !== undefined) {
      obj.error = LHTaskError.toJSON(message.error);
    }
    if (message.exception !== undefined) {
      obj.exception = LHTaskException.toJSON(message.exception);
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<TaskAttempt>, I>>(base?: I): TaskAttempt {
    return TaskAttempt.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<TaskAttempt>, I>>(object: I): TaskAttempt {
    const message = createBaseTaskAttempt();
    message.logOutput = (object.logOutput !== undefined && object.logOutput !== null)
      ? VariableValue.fromPartial(object.logOutput)
      : undefined;
    message.scheduleTime = object.scheduleTime ?? undefined;
    message.startTime = object.startTime ?? undefined;
    message.endTime = object.endTime ?? undefined;
    message.taskWorkerId = object.taskWorkerId ?? "";
    message.taskWorkerVersion = object.taskWorkerVersion ?? undefined;
    message.status = object.status ?? TaskStatus.TASK_SCHEDULED;
    message.output = (object.output !== undefined && object.output !== null)
      ? VariableValue.fromPartial(object.output)
      : undefined;
    message.error = (object.error !== undefined && object.error !== null)
      ? LHTaskError.fromPartial(object.error)
      : undefined;
    message.exception = (object.exception !== undefined && object.exception !== null)
      ? LHTaskException.fromPartial(object.exception)
      : undefined;
    return message;
  },
};

function createBaseTaskRunSource(): TaskRunSource {
  return { taskNode: undefined, userTaskTrigger: undefined, wfSpecId: undefined };
}

export const TaskRunSource = {
  encode(message: TaskRunSource, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.taskNode !== undefined) {
      TaskNodeReference.encode(message.taskNode, writer.uint32(10).fork()).ldelim();
    }
    if (message.userTaskTrigger !== undefined) {
      UserTaskTriggerReference.encode(message.userTaskTrigger, writer.uint32(18).fork()).ldelim();
    }
    if (message.wfSpecId !== undefined) {
      WfSpecId.encode(message.wfSpecId, writer.uint32(26).fork()).ldelim();
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): TaskRunSource {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseTaskRunSource();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.taskNode = TaskNodeReference.decode(reader, reader.uint32());
          continue;
        case 2:
          if (tag !== 18) {
            break;
          }

          message.userTaskTrigger = UserTaskTriggerReference.decode(reader, reader.uint32());
          continue;
        case 3:
          if (tag !== 26) {
            break;
          }

          message.wfSpecId = WfSpecId.decode(reader, reader.uint32());
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  fromJSON(object: any): TaskRunSource {
    return {
      taskNode: isSet(object.taskNode) ? TaskNodeReference.fromJSON(object.taskNode) : undefined,
      userTaskTrigger: isSet(object.userTaskTrigger)
        ? UserTaskTriggerReference.fromJSON(object.userTaskTrigger)
        : undefined,
      wfSpecId: isSet(object.wfSpecId) ? WfSpecId.fromJSON(object.wfSpecId) : undefined,
    };
  },

  toJSON(message: TaskRunSource): unknown {
    const obj: any = {};
    if (message.taskNode !== undefined) {
      obj.taskNode = TaskNodeReference.toJSON(message.taskNode);
    }
    if (message.userTaskTrigger !== undefined) {
      obj.userTaskTrigger = UserTaskTriggerReference.toJSON(message.userTaskTrigger);
    }
    if (message.wfSpecId !== undefined) {
      obj.wfSpecId = WfSpecId.toJSON(message.wfSpecId);
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<TaskRunSource>, I>>(base?: I): TaskRunSource {
    return TaskRunSource.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<TaskRunSource>, I>>(object: I): TaskRunSource {
    const message = createBaseTaskRunSource();
    message.taskNode = (object.taskNode !== undefined && object.taskNode !== null)
      ? TaskNodeReference.fromPartial(object.taskNode)
      : undefined;
    message.userTaskTrigger = (object.userTaskTrigger !== undefined && object.userTaskTrigger !== null)
      ? UserTaskTriggerReference.fromPartial(object.userTaskTrigger)
      : undefined;
    message.wfSpecId = (object.wfSpecId !== undefined && object.wfSpecId !== null)
      ? WfSpecId.fromPartial(object.wfSpecId)
      : undefined;
    return message;
  },
};

function createBaseTaskNodeReference(): TaskNodeReference {
  return { nodeRunId: undefined };
}

export const TaskNodeReference = {
  encode(message: TaskNodeReference, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.nodeRunId !== undefined) {
      NodeRunId.encode(message.nodeRunId, writer.uint32(10).fork()).ldelim();
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): TaskNodeReference {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseTaskNodeReference();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.nodeRunId = NodeRunId.decode(reader, reader.uint32());
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  fromJSON(object: any): TaskNodeReference {
    return { nodeRunId: isSet(object.nodeRunId) ? NodeRunId.fromJSON(object.nodeRunId) : undefined };
  },

  toJSON(message: TaskNodeReference): unknown {
    const obj: any = {};
    if (message.nodeRunId !== undefined) {
      obj.nodeRunId = NodeRunId.toJSON(message.nodeRunId);
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<TaskNodeReference>, I>>(base?: I): TaskNodeReference {
    return TaskNodeReference.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<TaskNodeReference>, I>>(object: I): TaskNodeReference {
    const message = createBaseTaskNodeReference();
    message.nodeRunId = (object.nodeRunId !== undefined && object.nodeRunId !== null)
      ? NodeRunId.fromPartial(object.nodeRunId)
      : undefined;
    return message;
  },
};

function createBaseLHTaskError(): LHTaskError {
  return { type: LHErrorType.CHILD_FAILURE, message: "" };
}

export const LHTaskError = {
  encode(message: LHTaskError, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.type !== LHErrorType.CHILD_FAILURE) {
      writer.uint32(8).int32(lHErrorTypeToNumber(message.type));
    }
    if (message.message !== "") {
      writer.uint32(18).string(message.message);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): LHTaskError {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseLHTaskError();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 8) {
            break;
          }

          message.type = lHErrorTypeFromJSON(reader.int32());
          continue;
        case 2:
          if (tag !== 18) {
            break;
          }

          message.message = reader.string();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  fromJSON(object: any): LHTaskError {
    return {
      type: isSet(object.type) ? lHErrorTypeFromJSON(object.type) : LHErrorType.CHILD_FAILURE,
      message: isSet(object.message) ? globalThis.String(object.message) : "",
    };
  },

  toJSON(message: LHTaskError): unknown {
    const obj: any = {};
    if (message.type !== LHErrorType.CHILD_FAILURE) {
      obj.type = lHErrorTypeToJSON(message.type);
    }
    if (message.message !== "") {
      obj.message = message.message;
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<LHTaskError>, I>>(base?: I): LHTaskError {
    return LHTaskError.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<LHTaskError>, I>>(object: I): LHTaskError {
    const message = createBaseLHTaskError();
    message.type = object.type ?? LHErrorType.CHILD_FAILURE;
    message.message = object.message ?? "";
    return message;
  },
};

function createBaseLHTaskException(): LHTaskException {
  return { name: "", message: "" };
}

export const LHTaskException = {
  encode(message: LHTaskException, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.name !== "") {
      writer.uint32(10).string(message.name);
    }
    if (message.message !== "") {
      writer.uint32(18).string(message.message);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): LHTaskException {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseLHTaskException();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.name = reader.string();
          continue;
        case 2:
          if (tag !== 18) {
            break;
          }

          message.message = reader.string();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  fromJSON(object: any): LHTaskException {
    return {
      name: isSet(object.name) ? globalThis.String(object.name) : "",
      message: isSet(object.message) ? globalThis.String(object.message) : "",
    };
  },

  toJSON(message: LHTaskException): unknown {
    const obj: any = {};
    if (message.name !== "") {
      obj.name = message.name;
    }
    if (message.message !== "") {
      obj.message = message.message;
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<LHTaskException>, I>>(base?: I): LHTaskException {
    return LHTaskException.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<LHTaskException>, I>>(object: I): LHTaskException {
    const message = createBaseLHTaskException();
    message.name = object.name ?? "";
    message.message = object.message ?? "";
    return message;
  },
};

type Builtin = Date | Function | Uint8Array | string | number | boolean | undefined;

export type DeepPartial<T> = T extends Builtin ? T
  : T extends globalThis.Array<infer U> ? globalThis.Array<DeepPartial<U>>
  : T extends ReadonlyArray<infer U> ? ReadonlyArray<DeepPartial<U>>
  : T extends {} ? { [K in keyof T]?: DeepPartial<T[K]> }
  : Partial<T>;

type KeysOfUnion<T> = T extends T ? keyof T : never;
export type Exact<P, I extends P> = P extends Builtin ? P
  : P & { [K in keyof P]: Exact<P[K], I[K]> } & { [K in Exclude<keyof I, KeysOfUnion<P>>]: never };

function toTimestamp(dateStr: string): Timestamp {
  const date = new globalThis.Date(dateStr);
  const seconds = Math.trunc(date.getTime() / 1_000);
  const nanos = (date.getTime() % 1_000) * 1_000_000;
  return { seconds, nanos };
}

function fromTimestamp(t: Timestamp): string {
  let millis = (t.seconds || 0) * 1_000;
  millis += (t.nanos || 0) / 1_000_000;
  return new globalThis.Date(millis).toISOString();
}

function isSet(value: any): boolean {
  return value !== null && value !== undefined;
}
