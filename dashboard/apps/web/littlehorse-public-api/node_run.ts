/* eslint-disable */
import * as _m0 from "protobufjs/minimal";
import {
  LHStatus,
  lHStatusFromJSON,
  lHStatusToJSON,
  lHStatusToNumber,
  WaitForThreadsPolicy,
  waitForThreadsPolicyFromJSON,
  waitForThreadsPolicyToJSON,
  waitForThreadsPolicyToNumber,
} from "./common_enums";
import { Timestamp } from "./google/protobuf/timestamp";
import { ExternalEventDefId, ExternalEventId, NodeRunId, TaskRunId, UserTaskRunId, WfSpecId } from "./object_id";
import { VariableValue } from "./variable";

export const protobufPackage = "littlehorse";

export interface NodeRun {
  id: NodeRunId | undefined;
  wfSpecId: WfSpecId | undefined;
  failureHandlerIds: number[];
  status: LHStatus;
  arrivalTime: string | undefined;
  endTime?: string | undefined;
  threadSpecName: string;
  nodeName: string;
  errorMessage?: string | undefined;
  failures: Failure[];
  task?: TaskNodeRun | undefined;
  externalEvent?: ExternalEventRun | undefined;
  entrypoint?: EntrypointRun | undefined;
  exit?: ExitRun | undefined;
  startThread?: StartThreadRun | undefined;
  waitThreads?: WaitForThreadsRun | undefined;
  sleep?: SleepNodeRun | undefined;
  userTask?: UserTaskNodeRun | undefined;
  startMultipleThreads?: StartMultipleThreadsRun | undefined;
}

export interface TaskNodeRun {
  taskRunId?: TaskRunId | undefined;
}

export interface UserTaskNodeRun {
  userTaskRunId?: UserTaskRunId | undefined;
}

export interface EntrypointRun {
}

export interface ExitRun {
}

export interface StartThreadRun {
  childThreadId?: number | undefined;
  threadSpecName: string;
}

export interface StartMultipleThreadsRun {
  threadSpecName: string;
}

export interface WaitForThreadsRun {
  threads: WaitForThreadsRun_WaitForThread[];
  policy: WaitForThreadsPolicy;
}

export interface WaitForThreadsRun_WaitForThread {
  threadEndTime?: string | undefined;
  threadStatus: LHStatus;
  threadRunNumber: number;
  alreadyHandled: boolean;
}

export interface ExternalEventRun {
  externalEventDefId: ExternalEventDefId | undefined;
  eventTime?: string | undefined;
  externalEventId?: ExternalEventId | undefined;
}

export interface SleepNodeRun {
  maturationTime: string | undefined;
}

export interface Failure {
  failureName: string;
  message: string;
  content?: VariableValue | undefined;
  wasProperlyHandled: boolean;
}

function createBaseNodeRun(): NodeRun {
  return {
    id: undefined,
    wfSpecId: undefined,
    failureHandlerIds: [],
    status: LHStatus.STARTING,
    arrivalTime: undefined,
    endTime: undefined,
    threadSpecName: "",
    nodeName: "",
    errorMessage: undefined,
    failures: [],
    task: undefined,
    externalEvent: undefined,
    entrypoint: undefined,
    exit: undefined,
    startThread: undefined,
    waitThreads: undefined,
    sleep: undefined,
    userTask: undefined,
    startMultipleThreads: undefined,
  };
}

export const NodeRun = {
  encode(message: NodeRun, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.id !== undefined) {
      NodeRunId.encode(message.id, writer.uint32(10).fork()).ldelim();
    }
    if (message.wfSpecId !== undefined) {
      WfSpecId.encode(message.wfSpecId, writer.uint32(34).fork()).ldelim();
    }
    writer.uint32(42).fork();
    for (const v of message.failureHandlerIds) {
      writer.int32(v);
    }
    writer.ldelim();
    if (message.status !== LHStatus.STARTING) {
      writer.uint32(48).int32(lHStatusToNumber(message.status));
    }
    if (message.arrivalTime !== undefined) {
      Timestamp.encode(toTimestamp(message.arrivalTime), writer.uint32(58).fork()).ldelim();
    }
    if (message.endTime !== undefined) {
      Timestamp.encode(toTimestamp(message.endTime), writer.uint32(66).fork()).ldelim();
    }
    if (message.threadSpecName !== "") {
      writer.uint32(74).string(message.threadSpecName);
    }
    if (message.nodeName !== "") {
      writer.uint32(82).string(message.nodeName);
    }
    if (message.errorMessage !== undefined) {
      writer.uint32(90).string(message.errorMessage);
    }
    for (const v of message.failures) {
      Failure.encode(v!, writer.uint32(98).fork()).ldelim();
    }
    if (message.task !== undefined) {
      TaskNodeRun.encode(message.task, writer.uint32(106).fork()).ldelim();
    }
    if (message.externalEvent !== undefined) {
      ExternalEventRun.encode(message.externalEvent, writer.uint32(114).fork()).ldelim();
    }
    if (message.entrypoint !== undefined) {
      EntrypointRun.encode(message.entrypoint, writer.uint32(122).fork()).ldelim();
    }
    if (message.exit !== undefined) {
      ExitRun.encode(message.exit, writer.uint32(130).fork()).ldelim();
    }
    if (message.startThread !== undefined) {
      StartThreadRun.encode(message.startThread, writer.uint32(138).fork()).ldelim();
    }
    if (message.waitThreads !== undefined) {
      WaitForThreadsRun.encode(message.waitThreads, writer.uint32(146).fork()).ldelim();
    }
    if (message.sleep !== undefined) {
      SleepNodeRun.encode(message.sleep, writer.uint32(154).fork()).ldelim();
    }
    if (message.userTask !== undefined) {
      UserTaskNodeRun.encode(message.userTask, writer.uint32(162).fork()).ldelim();
    }
    if (message.startMultipleThreads !== undefined) {
      StartMultipleThreadsRun.encode(message.startMultipleThreads, writer.uint32(170).fork()).ldelim();
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): NodeRun {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseNodeRun();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.id = NodeRunId.decode(reader, reader.uint32());
          continue;
        case 4:
          if (tag !== 34) {
            break;
          }

          message.wfSpecId = WfSpecId.decode(reader, reader.uint32());
          continue;
        case 5:
          if (tag === 40) {
            message.failureHandlerIds.push(reader.int32());

            continue;
          }

          if (tag === 42) {
            const end2 = reader.uint32() + reader.pos;
            while (reader.pos < end2) {
              message.failureHandlerIds.push(reader.int32());
            }

            continue;
          }

          break;
        case 6:
          if (tag !== 48) {
            break;
          }

          message.status = lHStatusFromJSON(reader.int32());
          continue;
        case 7:
          if (tag !== 58) {
            break;
          }

          message.arrivalTime = fromTimestamp(Timestamp.decode(reader, reader.uint32()));
          continue;
        case 8:
          if (tag !== 66) {
            break;
          }

          message.endTime = fromTimestamp(Timestamp.decode(reader, reader.uint32()));
          continue;
        case 9:
          if (tag !== 74) {
            break;
          }

          message.threadSpecName = reader.string();
          continue;
        case 10:
          if (tag !== 82) {
            break;
          }

          message.nodeName = reader.string();
          continue;
        case 11:
          if (tag !== 90) {
            break;
          }

          message.errorMessage = reader.string();
          continue;
        case 12:
          if (tag !== 98) {
            break;
          }

          message.failures.push(Failure.decode(reader, reader.uint32()));
          continue;
        case 13:
          if (tag !== 106) {
            break;
          }

          message.task = TaskNodeRun.decode(reader, reader.uint32());
          continue;
        case 14:
          if (tag !== 114) {
            break;
          }

          message.externalEvent = ExternalEventRun.decode(reader, reader.uint32());
          continue;
        case 15:
          if (tag !== 122) {
            break;
          }

          message.entrypoint = EntrypointRun.decode(reader, reader.uint32());
          continue;
        case 16:
          if (tag !== 130) {
            break;
          }

          message.exit = ExitRun.decode(reader, reader.uint32());
          continue;
        case 17:
          if (tag !== 138) {
            break;
          }

          message.startThread = StartThreadRun.decode(reader, reader.uint32());
          continue;
        case 18:
          if (tag !== 146) {
            break;
          }

          message.waitThreads = WaitForThreadsRun.decode(reader, reader.uint32());
          continue;
        case 19:
          if (tag !== 154) {
            break;
          }

          message.sleep = SleepNodeRun.decode(reader, reader.uint32());
          continue;
        case 20:
          if (tag !== 162) {
            break;
          }

          message.userTask = UserTaskNodeRun.decode(reader, reader.uint32());
          continue;
        case 21:
          if (tag !== 170) {
            break;
          }

          message.startMultipleThreads = StartMultipleThreadsRun.decode(reader, reader.uint32());
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  fromJSON(object: any): NodeRun {
    return {
      id: isSet(object.id) ? NodeRunId.fromJSON(object.id) : undefined,
      wfSpecId: isSet(object.wfSpecId) ? WfSpecId.fromJSON(object.wfSpecId) : undefined,
      failureHandlerIds: globalThis.Array.isArray(object?.failureHandlerIds)
        ? object.failureHandlerIds.map((e: any) => globalThis.Number(e))
        : [],
      status: isSet(object.status) ? lHStatusFromJSON(object.status) : LHStatus.STARTING,
      arrivalTime: isSet(object.arrivalTime) ? globalThis.String(object.arrivalTime) : undefined,
      endTime: isSet(object.endTime) ? globalThis.String(object.endTime) : undefined,
      threadSpecName: isSet(object.threadSpecName) ? globalThis.String(object.threadSpecName) : "",
      nodeName: isSet(object.nodeName) ? globalThis.String(object.nodeName) : "",
      errorMessage: isSet(object.errorMessage) ? globalThis.String(object.errorMessage) : undefined,
      failures: globalThis.Array.isArray(object?.failures) ? object.failures.map((e: any) => Failure.fromJSON(e)) : [],
      task: isSet(object.task) ? TaskNodeRun.fromJSON(object.task) : undefined,
      externalEvent: isSet(object.externalEvent) ? ExternalEventRun.fromJSON(object.externalEvent) : undefined,
      entrypoint: isSet(object.entrypoint) ? EntrypointRun.fromJSON(object.entrypoint) : undefined,
      exit: isSet(object.exit) ? ExitRun.fromJSON(object.exit) : undefined,
      startThread: isSet(object.startThread) ? StartThreadRun.fromJSON(object.startThread) : undefined,
      waitThreads: isSet(object.waitThreads) ? WaitForThreadsRun.fromJSON(object.waitThreads) : undefined,
      sleep: isSet(object.sleep) ? SleepNodeRun.fromJSON(object.sleep) : undefined,
      userTask: isSet(object.userTask) ? UserTaskNodeRun.fromJSON(object.userTask) : undefined,
      startMultipleThreads: isSet(object.startMultipleThreads)
        ? StartMultipleThreadsRun.fromJSON(object.startMultipleThreads)
        : undefined,
    };
  },

  toJSON(message: NodeRun): unknown {
    const obj: any = {};
    if (message.id !== undefined) {
      obj.id = NodeRunId.toJSON(message.id);
    }
    if (message.wfSpecId !== undefined) {
      obj.wfSpecId = WfSpecId.toJSON(message.wfSpecId);
    }
    if (message.failureHandlerIds?.length) {
      obj.failureHandlerIds = message.failureHandlerIds.map((e) => Math.round(e));
    }
    if (message.status !== LHStatus.STARTING) {
      obj.status = lHStatusToJSON(message.status);
    }
    if (message.arrivalTime !== undefined) {
      obj.arrivalTime = message.arrivalTime;
    }
    if (message.endTime !== undefined) {
      obj.endTime = message.endTime;
    }
    if (message.threadSpecName !== "") {
      obj.threadSpecName = message.threadSpecName;
    }
    if (message.nodeName !== "") {
      obj.nodeName = message.nodeName;
    }
    if (message.errorMessage !== undefined) {
      obj.errorMessage = message.errorMessage;
    }
    if (message.failures?.length) {
      obj.failures = message.failures.map((e) => Failure.toJSON(e));
    }
    if (message.task !== undefined) {
      obj.task = TaskNodeRun.toJSON(message.task);
    }
    if (message.externalEvent !== undefined) {
      obj.externalEvent = ExternalEventRun.toJSON(message.externalEvent);
    }
    if (message.entrypoint !== undefined) {
      obj.entrypoint = EntrypointRun.toJSON(message.entrypoint);
    }
    if (message.exit !== undefined) {
      obj.exit = ExitRun.toJSON(message.exit);
    }
    if (message.startThread !== undefined) {
      obj.startThread = StartThreadRun.toJSON(message.startThread);
    }
    if (message.waitThreads !== undefined) {
      obj.waitThreads = WaitForThreadsRun.toJSON(message.waitThreads);
    }
    if (message.sleep !== undefined) {
      obj.sleep = SleepNodeRun.toJSON(message.sleep);
    }
    if (message.userTask !== undefined) {
      obj.userTask = UserTaskNodeRun.toJSON(message.userTask);
    }
    if (message.startMultipleThreads !== undefined) {
      obj.startMultipleThreads = StartMultipleThreadsRun.toJSON(message.startMultipleThreads);
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<NodeRun>, I>>(base?: I): NodeRun {
    return NodeRun.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<NodeRun>, I>>(object: I): NodeRun {
    const message = createBaseNodeRun();
    message.id = (object.id !== undefined && object.id !== null) ? NodeRunId.fromPartial(object.id) : undefined;
    message.wfSpecId = (object.wfSpecId !== undefined && object.wfSpecId !== null)
      ? WfSpecId.fromPartial(object.wfSpecId)
      : undefined;
    message.failureHandlerIds = object.failureHandlerIds?.map((e) => e) || [];
    message.status = object.status ?? LHStatus.STARTING;
    message.arrivalTime = object.arrivalTime ?? undefined;
    message.endTime = object.endTime ?? undefined;
    message.threadSpecName = object.threadSpecName ?? "";
    message.nodeName = object.nodeName ?? "";
    message.errorMessage = object.errorMessage ?? undefined;
    message.failures = object.failures?.map((e) => Failure.fromPartial(e)) || [];
    message.task = (object.task !== undefined && object.task !== null)
      ? TaskNodeRun.fromPartial(object.task)
      : undefined;
    message.externalEvent = (object.externalEvent !== undefined && object.externalEvent !== null)
      ? ExternalEventRun.fromPartial(object.externalEvent)
      : undefined;
    message.entrypoint = (object.entrypoint !== undefined && object.entrypoint !== null)
      ? EntrypointRun.fromPartial(object.entrypoint)
      : undefined;
    message.exit = (object.exit !== undefined && object.exit !== null) ? ExitRun.fromPartial(object.exit) : undefined;
    message.startThread = (object.startThread !== undefined && object.startThread !== null)
      ? StartThreadRun.fromPartial(object.startThread)
      : undefined;
    message.waitThreads = (object.waitThreads !== undefined && object.waitThreads !== null)
      ? WaitForThreadsRun.fromPartial(object.waitThreads)
      : undefined;
    message.sleep = (object.sleep !== undefined && object.sleep !== null)
      ? SleepNodeRun.fromPartial(object.sleep)
      : undefined;
    message.userTask = (object.userTask !== undefined && object.userTask !== null)
      ? UserTaskNodeRun.fromPartial(object.userTask)
      : undefined;
    message.startMultipleThreads = (object.startMultipleThreads !== undefined && object.startMultipleThreads !== null)
      ? StartMultipleThreadsRun.fromPartial(object.startMultipleThreads)
      : undefined;
    return message;
  },
};

function createBaseTaskNodeRun(): TaskNodeRun {
  return { taskRunId: undefined };
}

export const TaskNodeRun = {
  encode(message: TaskNodeRun, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.taskRunId !== undefined) {
      TaskRunId.encode(message.taskRunId, writer.uint32(10).fork()).ldelim();
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): TaskNodeRun {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseTaskNodeRun();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.taskRunId = TaskRunId.decode(reader, reader.uint32());
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  fromJSON(object: any): TaskNodeRun {
    return { taskRunId: isSet(object.taskRunId) ? TaskRunId.fromJSON(object.taskRunId) : undefined };
  },

  toJSON(message: TaskNodeRun): unknown {
    const obj: any = {};
    if (message.taskRunId !== undefined) {
      obj.taskRunId = TaskRunId.toJSON(message.taskRunId);
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<TaskNodeRun>, I>>(base?: I): TaskNodeRun {
    return TaskNodeRun.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<TaskNodeRun>, I>>(object: I): TaskNodeRun {
    const message = createBaseTaskNodeRun();
    message.taskRunId = (object.taskRunId !== undefined && object.taskRunId !== null)
      ? TaskRunId.fromPartial(object.taskRunId)
      : undefined;
    return message;
  },
};

function createBaseUserTaskNodeRun(): UserTaskNodeRun {
  return { userTaskRunId: undefined };
}

export const UserTaskNodeRun = {
  encode(message: UserTaskNodeRun, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.userTaskRunId !== undefined) {
      UserTaskRunId.encode(message.userTaskRunId, writer.uint32(10).fork()).ldelim();
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): UserTaskNodeRun {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseUserTaskNodeRun();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.userTaskRunId = UserTaskRunId.decode(reader, reader.uint32());
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  fromJSON(object: any): UserTaskNodeRun {
    return { userTaskRunId: isSet(object.userTaskRunId) ? UserTaskRunId.fromJSON(object.userTaskRunId) : undefined };
  },

  toJSON(message: UserTaskNodeRun): unknown {
    const obj: any = {};
    if (message.userTaskRunId !== undefined) {
      obj.userTaskRunId = UserTaskRunId.toJSON(message.userTaskRunId);
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<UserTaskNodeRun>, I>>(base?: I): UserTaskNodeRun {
    return UserTaskNodeRun.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<UserTaskNodeRun>, I>>(object: I): UserTaskNodeRun {
    const message = createBaseUserTaskNodeRun();
    message.userTaskRunId = (object.userTaskRunId !== undefined && object.userTaskRunId !== null)
      ? UserTaskRunId.fromPartial(object.userTaskRunId)
      : undefined;
    return message;
  },
};

function createBaseEntrypointRun(): EntrypointRun {
  return {};
}

export const EntrypointRun = {
  encode(_: EntrypointRun, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): EntrypointRun {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseEntrypointRun();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  fromJSON(_: any): EntrypointRun {
    return {};
  },

  toJSON(_: EntrypointRun): unknown {
    const obj: any = {};
    return obj;
  },

  create<I extends Exact<DeepPartial<EntrypointRun>, I>>(base?: I): EntrypointRun {
    return EntrypointRun.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<EntrypointRun>, I>>(_: I): EntrypointRun {
    const message = createBaseEntrypointRun();
    return message;
  },
};

function createBaseExitRun(): ExitRun {
  return {};
}

export const ExitRun = {
  encode(_: ExitRun, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): ExitRun {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseExitRun();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  fromJSON(_: any): ExitRun {
    return {};
  },

  toJSON(_: ExitRun): unknown {
    const obj: any = {};
    return obj;
  },

  create<I extends Exact<DeepPartial<ExitRun>, I>>(base?: I): ExitRun {
    return ExitRun.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<ExitRun>, I>>(_: I): ExitRun {
    const message = createBaseExitRun();
    return message;
  },
};

function createBaseStartThreadRun(): StartThreadRun {
  return { childThreadId: undefined, threadSpecName: "" };
}

export const StartThreadRun = {
  encode(message: StartThreadRun, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.childThreadId !== undefined) {
      writer.uint32(8).int32(message.childThreadId);
    }
    if (message.threadSpecName !== "") {
      writer.uint32(18).string(message.threadSpecName);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): StartThreadRun {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseStartThreadRun();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 8) {
            break;
          }

          message.childThreadId = reader.int32();
          continue;
        case 2:
          if (tag !== 18) {
            break;
          }

          message.threadSpecName = reader.string();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  fromJSON(object: any): StartThreadRun {
    return {
      childThreadId: isSet(object.childThreadId) ? globalThis.Number(object.childThreadId) : undefined,
      threadSpecName: isSet(object.threadSpecName) ? globalThis.String(object.threadSpecName) : "",
    };
  },

  toJSON(message: StartThreadRun): unknown {
    const obj: any = {};
    if (message.childThreadId !== undefined) {
      obj.childThreadId = Math.round(message.childThreadId);
    }
    if (message.threadSpecName !== "") {
      obj.threadSpecName = message.threadSpecName;
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<StartThreadRun>, I>>(base?: I): StartThreadRun {
    return StartThreadRun.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<StartThreadRun>, I>>(object: I): StartThreadRun {
    const message = createBaseStartThreadRun();
    message.childThreadId = object.childThreadId ?? undefined;
    message.threadSpecName = object.threadSpecName ?? "";
    return message;
  },
};

function createBaseStartMultipleThreadsRun(): StartMultipleThreadsRun {
  return { threadSpecName: "" };
}

export const StartMultipleThreadsRun = {
  encode(message: StartMultipleThreadsRun, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.threadSpecName !== "") {
      writer.uint32(10).string(message.threadSpecName);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): StartMultipleThreadsRun {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseStartMultipleThreadsRun();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.threadSpecName = reader.string();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  fromJSON(object: any): StartMultipleThreadsRun {
    return { threadSpecName: isSet(object.threadSpecName) ? globalThis.String(object.threadSpecName) : "" };
  },

  toJSON(message: StartMultipleThreadsRun): unknown {
    const obj: any = {};
    if (message.threadSpecName !== "") {
      obj.threadSpecName = message.threadSpecName;
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<StartMultipleThreadsRun>, I>>(base?: I): StartMultipleThreadsRun {
    return StartMultipleThreadsRun.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<StartMultipleThreadsRun>, I>>(object: I): StartMultipleThreadsRun {
    const message = createBaseStartMultipleThreadsRun();
    message.threadSpecName = object.threadSpecName ?? "";
    return message;
  },
};

function createBaseWaitForThreadsRun(): WaitForThreadsRun {
  return { threads: [], policy: WaitForThreadsPolicy.STOP_ON_FAILURE };
}

export const WaitForThreadsRun = {
  encode(message: WaitForThreadsRun, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    for (const v of message.threads) {
      WaitForThreadsRun_WaitForThread.encode(v!, writer.uint32(10).fork()).ldelim();
    }
    if (message.policy !== WaitForThreadsPolicy.STOP_ON_FAILURE) {
      writer.uint32(16).int32(waitForThreadsPolicyToNumber(message.policy));
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): WaitForThreadsRun {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseWaitForThreadsRun();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.threads.push(WaitForThreadsRun_WaitForThread.decode(reader, reader.uint32()));
          continue;
        case 2:
          if (tag !== 16) {
            break;
          }

          message.policy = waitForThreadsPolicyFromJSON(reader.int32());
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  fromJSON(object: any): WaitForThreadsRun {
    return {
      threads: globalThis.Array.isArray(object?.threads)
        ? object.threads.map((e: any) => WaitForThreadsRun_WaitForThread.fromJSON(e))
        : [],
      policy: isSet(object.policy) ? waitForThreadsPolicyFromJSON(object.policy) : WaitForThreadsPolicy.STOP_ON_FAILURE,
    };
  },

  toJSON(message: WaitForThreadsRun): unknown {
    const obj: any = {};
    if (message.threads?.length) {
      obj.threads = message.threads.map((e) => WaitForThreadsRun_WaitForThread.toJSON(e));
    }
    if (message.policy !== WaitForThreadsPolicy.STOP_ON_FAILURE) {
      obj.policy = waitForThreadsPolicyToJSON(message.policy);
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<WaitForThreadsRun>, I>>(base?: I): WaitForThreadsRun {
    return WaitForThreadsRun.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<WaitForThreadsRun>, I>>(object: I): WaitForThreadsRun {
    const message = createBaseWaitForThreadsRun();
    message.threads = object.threads?.map((e) => WaitForThreadsRun_WaitForThread.fromPartial(e)) || [];
    message.policy = object.policy ?? WaitForThreadsPolicy.STOP_ON_FAILURE;
    return message;
  },
};

function createBaseWaitForThreadsRun_WaitForThread(): WaitForThreadsRun_WaitForThread {
  return { threadEndTime: undefined, threadStatus: LHStatus.STARTING, threadRunNumber: 0, alreadyHandled: false };
}

export const WaitForThreadsRun_WaitForThread = {
  encode(message: WaitForThreadsRun_WaitForThread, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.threadEndTime !== undefined) {
      Timestamp.encode(toTimestamp(message.threadEndTime), writer.uint32(10).fork()).ldelim();
    }
    if (message.threadStatus !== LHStatus.STARTING) {
      writer.uint32(16).int32(lHStatusToNumber(message.threadStatus));
    }
    if (message.threadRunNumber !== 0) {
      writer.uint32(24).int32(message.threadRunNumber);
    }
    if (message.alreadyHandled === true) {
      writer.uint32(40).bool(message.alreadyHandled);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): WaitForThreadsRun_WaitForThread {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseWaitForThreadsRun_WaitForThread();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.threadEndTime = fromTimestamp(Timestamp.decode(reader, reader.uint32()));
          continue;
        case 2:
          if (tag !== 16) {
            break;
          }

          message.threadStatus = lHStatusFromJSON(reader.int32());
          continue;
        case 3:
          if (tag !== 24) {
            break;
          }

          message.threadRunNumber = reader.int32();
          continue;
        case 5:
          if (tag !== 40) {
            break;
          }

          message.alreadyHandled = reader.bool();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  fromJSON(object: any): WaitForThreadsRun_WaitForThread {
    return {
      threadEndTime: isSet(object.threadEndTime) ? globalThis.String(object.threadEndTime) : undefined,
      threadStatus: isSet(object.threadStatus) ? lHStatusFromJSON(object.threadStatus) : LHStatus.STARTING,
      threadRunNumber: isSet(object.threadRunNumber) ? globalThis.Number(object.threadRunNumber) : 0,
      alreadyHandled: isSet(object.alreadyHandled) ? globalThis.Boolean(object.alreadyHandled) : false,
    };
  },

  toJSON(message: WaitForThreadsRun_WaitForThread): unknown {
    const obj: any = {};
    if (message.threadEndTime !== undefined) {
      obj.threadEndTime = message.threadEndTime;
    }
    if (message.threadStatus !== LHStatus.STARTING) {
      obj.threadStatus = lHStatusToJSON(message.threadStatus);
    }
    if (message.threadRunNumber !== 0) {
      obj.threadRunNumber = Math.round(message.threadRunNumber);
    }
    if (message.alreadyHandled === true) {
      obj.alreadyHandled = message.alreadyHandled;
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<WaitForThreadsRun_WaitForThread>, I>>(base?: I): WaitForThreadsRun_WaitForThread {
    return WaitForThreadsRun_WaitForThread.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<WaitForThreadsRun_WaitForThread>, I>>(
    object: I,
  ): WaitForThreadsRun_WaitForThread {
    const message = createBaseWaitForThreadsRun_WaitForThread();
    message.threadEndTime = object.threadEndTime ?? undefined;
    message.threadStatus = object.threadStatus ?? LHStatus.STARTING;
    message.threadRunNumber = object.threadRunNumber ?? 0;
    message.alreadyHandled = object.alreadyHandled ?? false;
    return message;
  },
};

function createBaseExternalEventRun(): ExternalEventRun {
  return { externalEventDefId: undefined, eventTime: undefined, externalEventId: undefined };
}

export const ExternalEventRun = {
  encode(message: ExternalEventRun, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.externalEventDefId !== undefined) {
      ExternalEventDefId.encode(message.externalEventDefId, writer.uint32(10).fork()).ldelim();
    }
    if (message.eventTime !== undefined) {
      Timestamp.encode(toTimestamp(message.eventTime), writer.uint32(18).fork()).ldelim();
    }
    if (message.externalEventId !== undefined) {
      ExternalEventId.encode(message.externalEventId, writer.uint32(26).fork()).ldelim();
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): ExternalEventRun {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseExternalEventRun();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.externalEventDefId = ExternalEventDefId.decode(reader, reader.uint32());
          continue;
        case 2:
          if (tag !== 18) {
            break;
          }

          message.eventTime = fromTimestamp(Timestamp.decode(reader, reader.uint32()));
          continue;
        case 3:
          if (tag !== 26) {
            break;
          }

          message.externalEventId = ExternalEventId.decode(reader, reader.uint32());
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  fromJSON(object: any): ExternalEventRun {
    return {
      externalEventDefId: isSet(object.externalEventDefId)
        ? ExternalEventDefId.fromJSON(object.externalEventDefId)
        : undefined,
      eventTime: isSet(object.eventTime) ? globalThis.String(object.eventTime) : undefined,
      externalEventId: isSet(object.externalEventId) ? ExternalEventId.fromJSON(object.externalEventId) : undefined,
    };
  },

  toJSON(message: ExternalEventRun): unknown {
    const obj: any = {};
    if (message.externalEventDefId !== undefined) {
      obj.externalEventDefId = ExternalEventDefId.toJSON(message.externalEventDefId);
    }
    if (message.eventTime !== undefined) {
      obj.eventTime = message.eventTime;
    }
    if (message.externalEventId !== undefined) {
      obj.externalEventId = ExternalEventId.toJSON(message.externalEventId);
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<ExternalEventRun>, I>>(base?: I): ExternalEventRun {
    return ExternalEventRun.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<ExternalEventRun>, I>>(object: I): ExternalEventRun {
    const message = createBaseExternalEventRun();
    message.externalEventDefId = (object.externalEventDefId !== undefined && object.externalEventDefId !== null)
      ? ExternalEventDefId.fromPartial(object.externalEventDefId)
      : undefined;
    message.eventTime = object.eventTime ?? undefined;
    message.externalEventId = (object.externalEventId !== undefined && object.externalEventId !== null)
      ? ExternalEventId.fromPartial(object.externalEventId)
      : undefined;
    return message;
  },
};

function createBaseSleepNodeRun(): SleepNodeRun {
  return { maturationTime: undefined };
}

export const SleepNodeRun = {
  encode(message: SleepNodeRun, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.maturationTime !== undefined) {
      Timestamp.encode(toTimestamp(message.maturationTime), writer.uint32(10).fork()).ldelim();
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): SleepNodeRun {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseSleepNodeRun();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.maturationTime = fromTimestamp(Timestamp.decode(reader, reader.uint32()));
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  fromJSON(object: any): SleepNodeRun {
    return { maturationTime: isSet(object.maturationTime) ? globalThis.String(object.maturationTime) : undefined };
  },

  toJSON(message: SleepNodeRun): unknown {
    const obj: any = {};
    if (message.maturationTime !== undefined) {
      obj.maturationTime = message.maturationTime;
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<SleepNodeRun>, I>>(base?: I): SleepNodeRun {
    return SleepNodeRun.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<SleepNodeRun>, I>>(object: I): SleepNodeRun {
    const message = createBaseSleepNodeRun();
    message.maturationTime = object.maturationTime ?? undefined;
    return message;
  },
};

function createBaseFailure(): Failure {
  return { failureName: "", message: "", content: undefined, wasProperlyHandled: false };
}

export const Failure = {
  encode(message: Failure, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.failureName !== "") {
      writer.uint32(10).string(message.failureName);
    }
    if (message.message !== "") {
      writer.uint32(18).string(message.message);
    }
    if (message.content !== undefined) {
      VariableValue.encode(message.content, writer.uint32(26).fork()).ldelim();
    }
    if (message.wasProperlyHandled === true) {
      writer.uint32(32).bool(message.wasProperlyHandled);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): Failure {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseFailure();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.failureName = reader.string();
          continue;
        case 2:
          if (tag !== 18) {
            break;
          }

          message.message = reader.string();
          continue;
        case 3:
          if (tag !== 26) {
            break;
          }

          message.content = VariableValue.decode(reader, reader.uint32());
          continue;
        case 4:
          if (tag !== 32) {
            break;
          }

          message.wasProperlyHandled = reader.bool();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  fromJSON(object: any): Failure {
    return {
      failureName: isSet(object.failureName) ? globalThis.String(object.failureName) : "",
      message: isSet(object.message) ? globalThis.String(object.message) : "",
      content: isSet(object.content) ? VariableValue.fromJSON(object.content) : undefined,
      wasProperlyHandled: isSet(object.wasProperlyHandled) ? globalThis.Boolean(object.wasProperlyHandled) : false,
    };
  },

  toJSON(message: Failure): unknown {
    const obj: any = {};
    if (message.failureName !== "") {
      obj.failureName = message.failureName;
    }
    if (message.message !== "") {
      obj.message = message.message;
    }
    if (message.content !== undefined) {
      obj.content = VariableValue.toJSON(message.content);
    }
    if (message.wasProperlyHandled === true) {
      obj.wasProperlyHandled = message.wasProperlyHandled;
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<Failure>, I>>(base?: I): Failure {
    return Failure.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<Failure>, I>>(object: I): Failure {
    const message = createBaseFailure();
    message.failureName = object.failureName ?? "";
    message.message = object.message ?? "";
    message.content = (object.content !== undefined && object.content !== null)
      ? VariableValue.fromPartial(object.content)
      : undefined;
    message.wasProperlyHandled = object.wasProperlyHandled ?? false;
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
  const seconds = date.getTime() / 1_000;
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
