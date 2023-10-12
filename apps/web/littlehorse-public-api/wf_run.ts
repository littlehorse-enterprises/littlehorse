/* eslint-disable */
import _m0 from "protobufjs/minimal";
import { LHStatus, lHStatusFromJSON, lHStatusToJSON, lHStatusToNumber } from "./common_enums";
import { Timestamp } from "./google/protobuf/timestamp";
import { ExternalEventId } from "./object_id";

export const protobufPackage = "littlehorse";

export enum ThreadType {
  ENTRYPOINT = "ENTRYPOINT",
  CHILD = "CHILD",
  INTERRUPT = "INTERRUPT",
  FAILURE_HANDLER = "FAILURE_HANDLER",
  UNRECOGNIZED = "UNRECOGNIZED",
}

export function threadTypeFromJSON(object: any): ThreadType {
  switch (object) {
    case 0:
    case "ENTRYPOINT":
      return ThreadType.ENTRYPOINT;
    case 1:
    case "CHILD":
      return ThreadType.CHILD;
    case 2:
    case "INTERRUPT":
      return ThreadType.INTERRUPT;
    case 3:
    case "FAILURE_HANDLER":
      return ThreadType.FAILURE_HANDLER;
    case -1:
    case "UNRECOGNIZED":
    default:
      return ThreadType.UNRECOGNIZED;
  }
}

export function threadTypeToJSON(object: ThreadType): string {
  switch (object) {
    case ThreadType.ENTRYPOINT:
      return "ENTRYPOINT";
    case ThreadType.CHILD:
      return "CHILD";
    case ThreadType.INTERRUPT:
      return "INTERRUPT";
    case ThreadType.FAILURE_HANDLER:
      return "FAILURE_HANDLER";
    case ThreadType.UNRECOGNIZED:
    default:
      return "UNRECOGNIZED";
  }
}

export function threadTypeToNumber(object: ThreadType): number {
  switch (object) {
    case ThreadType.ENTRYPOINT:
      return 0;
    case ThreadType.CHILD:
      return 1;
    case ThreadType.INTERRUPT:
      return 2;
    case ThreadType.FAILURE_HANDLER:
      return 3;
    case ThreadType.UNRECOGNIZED:
    default:
      return -1;
  }
}

export interface WfRun {
  id: string;
  wfSpecName: string;
  wfSpecVersion: number;
  status: LHStatus;
  startTime: string | undefined;
  endTime?: string | undefined;
  threadRuns: ThreadRun[];
  pendingInterrupts: PendingInterrupt[];
  pendingFailures: PendingFailureHandler[];
}

export interface ThreadRun {
  number: number;
  status: LHStatus;
  threadSpecName: string;
  startTime: string | undefined;
  endTime?: string | undefined;
  errorMessage?: string | undefined;
  childThreadIds: number[];
  parentThreadId?: number | undefined;
  haltReasons: ThreadHaltReason[];
  interruptTriggerId?: ExternalEventId | undefined;
  failureBeingHandled?: FailureBeingHandled | undefined;
  currentNodePosition: number;
  handledFailedChildren: number[];
  type: ThreadType;
}

export interface FailureBeingHandled {
  threadRunNumber: number;
  nodeRunPosition: number;
  failureNumber: number;
}

export interface PendingInterrupt {
  externalEventId: ExternalEventId | undefined;
  handlerSpecName: string;
  interruptedThreadId: number;
}

export interface PendingFailureHandler {
  failedThreadRun: number;
  handlerSpecName: string;
}

export interface PendingInterruptHaltReason {
  externalEventId: ExternalEventId | undefined;
}

export interface PendingFailureHandlerHaltReason {
  nodeRunPosition: number;
}

export interface HandlingFailureHaltReason {
  handlerThreadId: number;
}

export interface ParentHalted {
  parentThreadId: number;
}

export interface Interrupted {
  interruptThreadId: number;
}

export interface ManualHalt {
  /** Nothing to store. */
  meaningOfLife: boolean;
}

export interface ThreadHaltReason {
  parentHalted?: ParentHalted | undefined;
  interrupted?: Interrupted | undefined;
  pendingInterrupt?: PendingInterruptHaltReason | undefined;
  pendingFailure?: PendingFailureHandlerHaltReason | undefined;
  handlingFailure?: HandlingFailureHaltReason | undefined;
  manualHalt?: ManualHalt | undefined;
}

function createBaseWfRun(): WfRun {
  return {
    id: "",
    wfSpecName: "",
    wfSpecVersion: 0,
    status: LHStatus.STARTING,
    startTime: undefined,
    endTime: undefined,
    threadRuns: [],
    pendingInterrupts: [],
    pendingFailures: [],
  };
}

export const WfRun = {
  encode(message: WfRun, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.id !== "") {
      writer.uint32(10).string(message.id);
    }
    if (message.wfSpecName !== "") {
      writer.uint32(18).string(message.wfSpecName);
    }
    if (message.wfSpecVersion !== 0) {
      writer.uint32(24).int32(message.wfSpecVersion);
    }
    if (message.status !== LHStatus.STARTING) {
      writer.uint32(32).int32(lHStatusToNumber(message.status));
    }
    if (message.startTime !== undefined) {
      Timestamp.encode(toTimestamp(message.startTime), writer.uint32(50).fork()).ldelim();
    }
    if (message.endTime !== undefined) {
      Timestamp.encode(toTimestamp(message.endTime), writer.uint32(58).fork()).ldelim();
    }
    for (const v of message.threadRuns) {
      ThreadRun.encode(v!, writer.uint32(66).fork()).ldelim();
    }
    for (const v of message.pendingInterrupts) {
      PendingInterrupt.encode(v!, writer.uint32(74).fork()).ldelim();
    }
    for (const v of message.pendingFailures) {
      PendingFailureHandler.encode(v!, writer.uint32(82).fork()).ldelim();
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): WfRun {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseWfRun();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.id = reader.string();
          continue;
        case 2:
          if (tag !== 18) {
            break;
          }

          message.wfSpecName = reader.string();
          continue;
        case 3:
          if (tag !== 24) {
            break;
          }

          message.wfSpecVersion = reader.int32();
          continue;
        case 4:
          if (tag !== 32) {
            break;
          }

          message.status = lHStatusFromJSON(reader.int32());
          continue;
        case 6:
          if (tag !== 50) {
            break;
          }

          message.startTime = fromTimestamp(Timestamp.decode(reader, reader.uint32()));
          continue;
        case 7:
          if (tag !== 58) {
            break;
          }

          message.endTime = fromTimestamp(Timestamp.decode(reader, reader.uint32()));
          continue;
        case 8:
          if (tag !== 66) {
            break;
          }

          message.threadRuns.push(ThreadRun.decode(reader, reader.uint32()));
          continue;
        case 9:
          if (tag !== 74) {
            break;
          }

          message.pendingInterrupts.push(PendingInterrupt.decode(reader, reader.uint32()));
          continue;
        case 10:
          if (tag !== 82) {
            break;
          }

          message.pendingFailures.push(PendingFailureHandler.decode(reader, reader.uint32()));
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  fromJSON(object: any): WfRun {
    return {
      id: isSet(object.id) ? String(object.id) : "",
      wfSpecName: isSet(object.wfSpecName) ? String(object.wfSpecName) : "",
      wfSpecVersion: isSet(object.wfSpecVersion) ? Number(object.wfSpecVersion) : 0,
      status: isSet(object.status) ? lHStatusFromJSON(object.status) : LHStatus.STARTING,
      startTime: isSet(object.startTime) ? String(object.startTime) : undefined,
      endTime: isSet(object.endTime) ? String(object.endTime) : undefined,
      threadRuns: Array.isArray(object?.threadRuns) ? object.threadRuns.map((e: any) => ThreadRun.fromJSON(e)) : [],
      pendingInterrupts: Array.isArray(object?.pendingInterrupts)
        ? object.pendingInterrupts.map((e: any) => PendingInterrupt.fromJSON(e))
        : [],
      pendingFailures: Array.isArray(object?.pendingFailures)
        ? object.pendingFailures.map((e: any) => PendingFailureHandler.fromJSON(e))
        : [],
    };
  },

  toJSON(message: WfRun): unknown {
    const obj: any = {};
    if (message.id !== "") {
      obj.id = message.id;
    }
    if (message.wfSpecName !== "") {
      obj.wfSpecName = message.wfSpecName;
    }
    if (message.wfSpecVersion !== 0) {
      obj.wfSpecVersion = Math.round(message.wfSpecVersion);
    }
    if (message.status !== LHStatus.STARTING) {
      obj.status = lHStatusToJSON(message.status);
    }
    if (message.startTime !== undefined) {
      obj.startTime = message.startTime;
    }
    if (message.endTime !== undefined) {
      obj.endTime = message.endTime;
    }
    if (message.threadRuns?.length) {
      obj.threadRuns = message.threadRuns.map((e) => ThreadRun.toJSON(e));
    }
    if (message.pendingInterrupts?.length) {
      obj.pendingInterrupts = message.pendingInterrupts.map((e) => PendingInterrupt.toJSON(e));
    }
    if (message.pendingFailures?.length) {
      obj.pendingFailures = message.pendingFailures.map((e) => PendingFailureHandler.toJSON(e));
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<WfRun>, I>>(base?: I): WfRun {
    return WfRun.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<WfRun>, I>>(object: I): WfRun {
    const message = createBaseWfRun();
    message.id = object.id ?? "";
    message.wfSpecName = object.wfSpecName ?? "";
    message.wfSpecVersion = object.wfSpecVersion ?? 0;
    message.status = object.status ?? LHStatus.STARTING;
    message.startTime = object.startTime ?? undefined;
    message.endTime = object.endTime ?? undefined;
    message.threadRuns = object.threadRuns?.map((e) => ThreadRun.fromPartial(e)) || [];
    message.pendingInterrupts = object.pendingInterrupts?.map((e) => PendingInterrupt.fromPartial(e)) || [];
    message.pendingFailures = object.pendingFailures?.map((e) => PendingFailureHandler.fromPartial(e)) || [];
    return message;
  },
};

function createBaseThreadRun(): ThreadRun {
  return {
    number: 0,
    status: LHStatus.STARTING,
    threadSpecName: "",
    startTime: undefined,
    endTime: undefined,
    errorMessage: undefined,
    childThreadIds: [],
    parentThreadId: undefined,
    haltReasons: [],
    interruptTriggerId: undefined,
    failureBeingHandled: undefined,
    currentNodePosition: 0,
    handledFailedChildren: [],
    type: ThreadType.ENTRYPOINT,
  };
}

export const ThreadRun = {
  encode(message: ThreadRun, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.number !== 0) {
      writer.uint32(16).int32(message.number);
    }
    if (message.status !== LHStatus.STARTING) {
      writer.uint32(24).int32(lHStatusToNumber(message.status));
    }
    if (message.threadSpecName !== "") {
      writer.uint32(50).string(message.threadSpecName);
    }
    if (message.startTime !== undefined) {
      Timestamp.encode(toTimestamp(message.startTime), writer.uint32(58).fork()).ldelim();
    }
    if (message.endTime !== undefined) {
      Timestamp.encode(toTimestamp(message.endTime), writer.uint32(66).fork()).ldelim();
    }
    if (message.errorMessage !== undefined) {
      writer.uint32(74).string(message.errorMessage);
    }
    writer.uint32(90).fork();
    for (const v of message.childThreadIds) {
      writer.int32(v);
    }
    writer.ldelim();
    if (message.parentThreadId !== undefined) {
      writer.uint32(96).int32(message.parentThreadId);
    }
    for (const v of message.haltReasons) {
      ThreadHaltReason.encode(v!, writer.uint32(106).fork()).ldelim();
    }
    if (message.interruptTriggerId !== undefined) {
      ExternalEventId.encode(message.interruptTriggerId, writer.uint32(114).fork()).ldelim();
    }
    if (message.failureBeingHandled !== undefined) {
      FailureBeingHandled.encode(message.failureBeingHandled, writer.uint32(122).fork()).ldelim();
    }
    if (message.currentNodePosition !== 0) {
      writer.uint32(128).int32(message.currentNodePosition);
    }
    writer.uint32(138).fork();
    for (const v of message.handledFailedChildren) {
      writer.int32(v);
    }
    writer.ldelim();
    if (message.type !== ThreadType.ENTRYPOINT) {
      writer.uint32(144).int32(threadTypeToNumber(message.type));
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): ThreadRun {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseThreadRun();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 2:
          if (tag !== 16) {
            break;
          }

          message.number = reader.int32();
          continue;
        case 3:
          if (tag !== 24) {
            break;
          }

          message.status = lHStatusFromJSON(reader.int32());
          continue;
        case 6:
          if (tag !== 50) {
            break;
          }

          message.threadSpecName = reader.string();
          continue;
        case 7:
          if (tag !== 58) {
            break;
          }

          message.startTime = fromTimestamp(Timestamp.decode(reader, reader.uint32()));
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

          message.errorMessage = reader.string();
          continue;
        case 11:
          if (tag === 88) {
            message.childThreadIds.push(reader.int32());

            continue;
          }

          if (tag === 90) {
            const end2 = reader.uint32() + reader.pos;
            while (reader.pos < end2) {
              message.childThreadIds.push(reader.int32());
            }

            continue;
          }

          break;
        case 12:
          if (tag !== 96) {
            break;
          }

          message.parentThreadId = reader.int32();
          continue;
        case 13:
          if (tag !== 106) {
            break;
          }

          message.haltReasons.push(ThreadHaltReason.decode(reader, reader.uint32()));
          continue;
        case 14:
          if (tag !== 114) {
            break;
          }

          message.interruptTriggerId = ExternalEventId.decode(reader, reader.uint32());
          continue;
        case 15:
          if (tag !== 122) {
            break;
          }

          message.failureBeingHandled = FailureBeingHandled.decode(reader, reader.uint32());
          continue;
        case 16:
          if (tag !== 128) {
            break;
          }

          message.currentNodePosition = reader.int32();
          continue;
        case 17:
          if (tag === 136) {
            message.handledFailedChildren.push(reader.int32());

            continue;
          }

          if (tag === 138) {
            const end2 = reader.uint32() + reader.pos;
            while (reader.pos < end2) {
              message.handledFailedChildren.push(reader.int32());
            }

            continue;
          }

          break;
        case 18:
          if (tag !== 144) {
            break;
          }

          message.type = threadTypeFromJSON(reader.int32());
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  fromJSON(object: any): ThreadRun {
    return {
      number: isSet(object.number) ? Number(object.number) : 0,
      status: isSet(object.status) ? lHStatusFromJSON(object.status) : LHStatus.STARTING,
      threadSpecName: isSet(object.threadSpecName) ? String(object.threadSpecName) : "",
      startTime: isSet(object.startTime) ? String(object.startTime) : undefined,
      endTime: isSet(object.endTime) ? String(object.endTime) : undefined,
      errorMessage: isSet(object.errorMessage) ? String(object.errorMessage) : undefined,
      childThreadIds: Array.isArray(object?.childThreadIds) ? object.childThreadIds.map((e: any) => Number(e)) : [],
      parentThreadId: isSet(object.parentThreadId) ? Number(object.parentThreadId) : undefined,
      haltReasons: Array.isArray(object?.haltReasons)
        ? object.haltReasons.map((e: any) => ThreadHaltReason.fromJSON(e))
        : [],
      interruptTriggerId: isSet(object.interruptTriggerId)
        ? ExternalEventId.fromJSON(object.interruptTriggerId)
        : undefined,
      failureBeingHandled: isSet(object.failureBeingHandled)
        ? FailureBeingHandled.fromJSON(object.failureBeingHandled)
        : undefined,
      currentNodePosition: isSet(object.currentNodePosition) ? Number(object.currentNodePosition) : 0,
      handledFailedChildren: Array.isArray(object?.handledFailedChildren)
        ? object.handledFailedChildren.map((e: any) => Number(e))
        : [],
      type: isSet(object.type) ? threadTypeFromJSON(object.type) : ThreadType.ENTRYPOINT,
    };
  },

  toJSON(message: ThreadRun): unknown {
    const obj: any = {};
    if (message.number !== 0) {
      obj.number = Math.round(message.number);
    }
    if (message.status !== LHStatus.STARTING) {
      obj.status = lHStatusToJSON(message.status);
    }
    if (message.threadSpecName !== "") {
      obj.threadSpecName = message.threadSpecName;
    }
    if (message.startTime !== undefined) {
      obj.startTime = message.startTime;
    }
    if (message.endTime !== undefined) {
      obj.endTime = message.endTime;
    }
    if (message.errorMessage !== undefined) {
      obj.errorMessage = message.errorMessage;
    }
    if (message.childThreadIds?.length) {
      obj.childThreadIds = message.childThreadIds.map((e) => Math.round(e));
    }
    if (message.parentThreadId !== undefined) {
      obj.parentThreadId = Math.round(message.parentThreadId);
    }
    if (message.haltReasons?.length) {
      obj.haltReasons = message.haltReasons.map((e) => ThreadHaltReason.toJSON(e));
    }
    if (message.interruptTriggerId !== undefined) {
      obj.interruptTriggerId = ExternalEventId.toJSON(message.interruptTriggerId);
    }
    if (message.failureBeingHandled !== undefined) {
      obj.failureBeingHandled = FailureBeingHandled.toJSON(message.failureBeingHandled);
    }
    if (message.currentNodePosition !== 0) {
      obj.currentNodePosition = Math.round(message.currentNodePosition);
    }
    if (message.handledFailedChildren?.length) {
      obj.handledFailedChildren = message.handledFailedChildren.map((e) => Math.round(e));
    }
    if (message.type !== ThreadType.ENTRYPOINT) {
      obj.type = threadTypeToJSON(message.type);
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<ThreadRun>, I>>(base?: I): ThreadRun {
    return ThreadRun.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<ThreadRun>, I>>(object: I): ThreadRun {
    const message = createBaseThreadRun();
    message.number = object.number ?? 0;
    message.status = object.status ?? LHStatus.STARTING;
    message.threadSpecName = object.threadSpecName ?? "";
    message.startTime = object.startTime ?? undefined;
    message.endTime = object.endTime ?? undefined;
    message.errorMessage = object.errorMessage ?? undefined;
    message.childThreadIds = object.childThreadIds?.map((e) => e) || [];
    message.parentThreadId = object.parentThreadId ?? undefined;
    message.haltReasons = object.haltReasons?.map((e) => ThreadHaltReason.fromPartial(e)) || [];
    message.interruptTriggerId = (object.interruptTriggerId !== undefined && object.interruptTriggerId !== null)
      ? ExternalEventId.fromPartial(object.interruptTriggerId)
      : undefined;
    message.failureBeingHandled = (object.failureBeingHandled !== undefined && object.failureBeingHandled !== null)
      ? FailureBeingHandled.fromPartial(object.failureBeingHandled)
      : undefined;
    message.currentNodePosition = object.currentNodePosition ?? 0;
    message.handledFailedChildren = object.handledFailedChildren?.map((e) => e) || [];
    message.type = object.type ?? ThreadType.ENTRYPOINT;
    return message;
  },
};

function createBaseFailureBeingHandled(): FailureBeingHandled {
  return { threadRunNumber: 0, nodeRunPosition: 0, failureNumber: 0 };
}

export const FailureBeingHandled = {
  encode(message: FailureBeingHandled, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.threadRunNumber !== 0) {
      writer.uint32(8).int32(message.threadRunNumber);
    }
    if (message.nodeRunPosition !== 0) {
      writer.uint32(16).int32(message.nodeRunPosition);
    }
    if (message.failureNumber !== 0) {
      writer.uint32(24).int32(message.failureNumber);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): FailureBeingHandled {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseFailureBeingHandled();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 8) {
            break;
          }

          message.threadRunNumber = reader.int32();
          continue;
        case 2:
          if (tag !== 16) {
            break;
          }

          message.nodeRunPosition = reader.int32();
          continue;
        case 3:
          if (tag !== 24) {
            break;
          }

          message.failureNumber = reader.int32();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  fromJSON(object: any): FailureBeingHandled {
    return {
      threadRunNumber: isSet(object.threadRunNumber) ? Number(object.threadRunNumber) : 0,
      nodeRunPosition: isSet(object.nodeRunPosition) ? Number(object.nodeRunPosition) : 0,
      failureNumber: isSet(object.failureNumber) ? Number(object.failureNumber) : 0,
    };
  },

  toJSON(message: FailureBeingHandled): unknown {
    const obj: any = {};
    if (message.threadRunNumber !== 0) {
      obj.threadRunNumber = Math.round(message.threadRunNumber);
    }
    if (message.nodeRunPosition !== 0) {
      obj.nodeRunPosition = Math.round(message.nodeRunPosition);
    }
    if (message.failureNumber !== 0) {
      obj.failureNumber = Math.round(message.failureNumber);
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<FailureBeingHandled>, I>>(base?: I): FailureBeingHandled {
    return FailureBeingHandled.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<FailureBeingHandled>, I>>(object: I): FailureBeingHandled {
    const message = createBaseFailureBeingHandled();
    message.threadRunNumber = object.threadRunNumber ?? 0;
    message.nodeRunPosition = object.nodeRunPosition ?? 0;
    message.failureNumber = object.failureNumber ?? 0;
    return message;
  },
};

function createBasePendingInterrupt(): PendingInterrupt {
  return { externalEventId: undefined, handlerSpecName: "", interruptedThreadId: 0 };
}

export const PendingInterrupt = {
  encode(message: PendingInterrupt, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.externalEventId !== undefined) {
      ExternalEventId.encode(message.externalEventId, writer.uint32(10).fork()).ldelim();
    }
    if (message.handlerSpecName !== "") {
      writer.uint32(18).string(message.handlerSpecName);
    }
    if (message.interruptedThreadId !== 0) {
      writer.uint32(24).int32(message.interruptedThreadId);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): PendingInterrupt {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBasePendingInterrupt();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.externalEventId = ExternalEventId.decode(reader, reader.uint32());
          continue;
        case 2:
          if (tag !== 18) {
            break;
          }

          message.handlerSpecName = reader.string();
          continue;
        case 3:
          if (tag !== 24) {
            break;
          }

          message.interruptedThreadId = reader.int32();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  fromJSON(object: any): PendingInterrupt {
    return {
      externalEventId: isSet(object.externalEventId) ? ExternalEventId.fromJSON(object.externalEventId) : undefined,
      handlerSpecName: isSet(object.handlerSpecName) ? String(object.handlerSpecName) : "",
      interruptedThreadId: isSet(object.interruptedThreadId) ? Number(object.interruptedThreadId) : 0,
    };
  },

  toJSON(message: PendingInterrupt): unknown {
    const obj: any = {};
    if (message.externalEventId !== undefined) {
      obj.externalEventId = ExternalEventId.toJSON(message.externalEventId);
    }
    if (message.handlerSpecName !== "") {
      obj.handlerSpecName = message.handlerSpecName;
    }
    if (message.interruptedThreadId !== 0) {
      obj.interruptedThreadId = Math.round(message.interruptedThreadId);
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<PendingInterrupt>, I>>(base?: I): PendingInterrupt {
    return PendingInterrupt.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<PendingInterrupt>, I>>(object: I): PendingInterrupt {
    const message = createBasePendingInterrupt();
    message.externalEventId = (object.externalEventId !== undefined && object.externalEventId !== null)
      ? ExternalEventId.fromPartial(object.externalEventId)
      : undefined;
    message.handlerSpecName = object.handlerSpecName ?? "";
    message.interruptedThreadId = object.interruptedThreadId ?? 0;
    return message;
  },
};

function createBasePendingFailureHandler(): PendingFailureHandler {
  return { failedThreadRun: 0, handlerSpecName: "" };
}

export const PendingFailureHandler = {
  encode(message: PendingFailureHandler, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.failedThreadRun !== 0) {
      writer.uint32(8).int32(message.failedThreadRun);
    }
    if (message.handlerSpecName !== "") {
      writer.uint32(18).string(message.handlerSpecName);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): PendingFailureHandler {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBasePendingFailureHandler();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 8) {
            break;
          }

          message.failedThreadRun = reader.int32();
          continue;
        case 2:
          if (tag !== 18) {
            break;
          }

          message.handlerSpecName = reader.string();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  fromJSON(object: any): PendingFailureHandler {
    return {
      failedThreadRun: isSet(object.failedThreadRun) ? Number(object.failedThreadRun) : 0,
      handlerSpecName: isSet(object.handlerSpecName) ? String(object.handlerSpecName) : "",
    };
  },

  toJSON(message: PendingFailureHandler): unknown {
    const obj: any = {};
    if (message.failedThreadRun !== 0) {
      obj.failedThreadRun = Math.round(message.failedThreadRun);
    }
    if (message.handlerSpecName !== "") {
      obj.handlerSpecName = message.handlerSpecName;
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<PendingFailureHandler>, I>>(base?: I): PendingFailureHandler {
    return PendingFailureHandler.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<PendingFailureHandler>, I>>(object: I): PendingFailureHandler {
    const message = createBasePendingFailureHandler();
    message.failedThreadRun = object.failedThreadRun ?? 0;
    message.handlerSpecName = object.handlerSpecName ?? "";
    return message;
  },
};

function createBasePendingInterruptHaltReason(): PendingInterruptHaltReason {
  return { externalEventId: undefined };
}

export const PendingInterruptHaltReason = {
  encode(message: PendingInterruptHaltReason, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.externalEventId !== undefined) {
      ExternalEventId.encode(message.externalEventId, writer.uint32(10).fork()).ldelim();
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): PendingInterruptHaltReason {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBasePendingInterruptHaltReason();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
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

  fromJSON(object: any): PendingInterruptHaltReason {
    return {
      externalEventId: isSet(object.externalEventId) ? ExternalEventId.fromJSON(object.externalEventId) : undefined,
    };
  },

  toJSON(message: PendingInterruptHaltReason): unknown {
    const obj: any = {};
    if (message.externalEventId !== undefined) {
      obj.externalEventId = ExternalEventId.toJSON(message.externalEventId);
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<PendingInterruptHaltReason>, I>>(base?: I): PendingInterruptHaltReason {
    return PendingInterruptHaltReason.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<PendingInterruptHaltReason>, I>>(object: I): PendingInterruptHaltReason {
    const message = createBasePendingInterruptHaltReason();
    message.externalEventId = (object.externalEventId !== undefined && object.externalEventId !== null)
      ? ExternalEventId.fromPartial(object.externalEventId)
      : undefined;
    return message;
  },
};

function createBasePendingFailureHandlerHaltReason(): PendingFailureHandlerHaltReason {
  return { nodeRunPosition: 0 };
}

export const PendingFailureHandlerHaltReason = {
  encode(message: PendingFailureHandlerHaltReason, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.nodeRunPosition !== 0) {
      writer.uint32(8).int32(message.nodeRunPosition);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): PendingFailureHandlerHaltReason {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBasePendingFailureHandlerHaltReason();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 8) {
            break;
          }

          message.nodeRunPosition = reader.int32();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  fromJSON(object: any): PendingFailureHandlerHaltReason {
    return { nodeRunPosition: isSet(object.nodeRunPosition) ? Number(object.nodeRunPosition) : 0 };
  },

  toJSON(message: PendingFailureHandlerHaltReason): unknown {
    const obj: any = {};
    if (message.nodeRunPosition !== 0) {
      obj.nodeRunPosition = Math.round(message.nodeRunPosition);
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<PendingFailureHandlerHaltReason>, I>>(base?: I): PendingFailureHandlerHaltReason {
    return PendingFailureHandlerHaltReason.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<PendingFailureHandlerHaltReason>, I>>(
    object: I,
  ): PendingFailureHandlerHaltReason {
    const message = createBasePendingFailureHandlerHaltReason();
    message.nodeRunPosition = object.nodeRunPosition ?? 0;
    return message;
  },
};

function createBaseHandlingFailureHaltReason(): HandlingFailureHaltReason {
  return { handlerThreadId: 0 };
}

export const HandlingFailureHaltReason = {
  encode(message: HandlingFailureHaltReason, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.handlerThreadId !== 0) {
      writer.uint32(8).int32(message.handlerThreadId);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): HandlingFailureHaltReason {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseHandlingFailureHaltReason();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 8) {
            break;
          }

          message.handlerThreadId = reader.int32();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  fromJSON(object: any): HandlingFailureHaltReason {
    return { handlerThreadId: isSet(object.handlerThreadId) ? Number(object.handlerThreadId) : 0 };
  },

  toJSON(message: HandlingFailureHaltReason): unknown {
    const obj: any = {};
    if (message.handlerThreadId !== 0) {
      obj.handlerThreadId = Math.round(message.handlerThreadId);
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<HandlingFailureHaltReason>, I>>(base?: I): HandlingFailureHaltReason {
    return HandlingFailureHaltReason.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<HandlingFailureHaltReason>, I>>(object: I): HandlingFailureHaltReason {
    const message = createBaseHandlingFailureHaltReason();
    message.handlerThreadId = object.handlerThreadId ?? 0;
    return message;
  },
};

function createBaseParentHalted(): ParentHalted {
  return { parentThreadId: 0 };
}

export const ParentHalted = {
  encode(message: ParentHalted, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.parentThreadId !== 0) {
      writer.uint32(8).int32(message.parentThreadId);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): ParentHalted {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseParentHalted();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 8) {
            break;
          }

          message.parentThreadId = reader.int32();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  fromJSON(object: any): ParentHalted {
    return { parentThreadId: isSet(object.parentThreadId) ? Number(object.parentThreadId) : 0 };
  },

  toJSON(message: ParentHalted): unknown {
    const obj: any = {};
    if (message.parentThreadId !== 0) {
      obj.parentThreadId = Math.round(message.parentThreadId);
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<ParentHalted>, I>>(base?: I): ParentHalted {
    return ParentHalted.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<ParentHalted>, I>>(object: I): ParentHalted {
    const message = createBaseParentHalted();
    message.parentThreadId = object.parentThreadId ?? 0;
    return message;
  },
};

function createBaseInterrupted(): Interrupted {
  return { interruptThreadId: 0 };
}

export const Interrupted = {
  encode(message: Interrupted, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.interruptThreadId !== 0) {
      writer.uint32(8).int32(message.interruptThreadId);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): Interrupted {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseInterrupted();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 8) {
            break;
          }

          message.interruptThreadId = reader.int32();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  fromJSON(object: any): Interrupted {
    return { interruptThreadId: isSet(object.interruptThreadId) ? Number(object.interruptThreadId) : 0 };
  },

  toJSON(message: Interrupted): unknown {
    const obj: any = {};
    if (message.interruptThreadId !== 0) {
      obj.interruptThreadId = Math.round(message.interruptThreadId);
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<Interrupted>, I>>(base?: I): Interrupted {
    return Interrupted.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<Interrupted>, I>>(object: I): Interrupted {
    const message = createBaseInterrupted();
    message.interruptThreadId = object.interruptThreadId ?? 0;
    return message;
  },
};

function createBaseManualHalt(): ManualHalt {
  return { meaningOfLife: false };
}

export const ManualHalt = {
  encode(message: ManualHalt, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.meaningOfLife === true) {
      writer.uint32(1096).bool(message.meaningOfLife);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): ManualHalt {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseManualHalt();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 137:
          if (tag !== 1096) {
            break;
          }

          message.meaningOfLife = reader.bool();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  fromJSON(object: any): ManualHalt {
    return { meaningOfLife: isSet(object.meaningOfLife) ? Boolean(object.meaningOfLife) : false };
  },

  toJSON(message: ManualHalt): unknown {
    const obj: any = {};
    if (message.meaningOfLife === true) {
      obj.meaningOfLife = message.meaningOfLife;
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<ManualHalt>, I>>(base?: I): ManualHalt {
    return ManualHalt.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<ManualHalt>, I>>(object: I): ManualHalt {
    const message = createBaseManualHalt();
    message.meaningOfLife = object.meaningOfLife ?? false;
    return message;
  },
};

function createBaseThreadHaltReason(): ThreadHaltReason {
  return {
    parentHalted: undefined,
    interrupted: undefined,
    pendingInterrupt: undefined,
    pendingFailure: undefined,
    handlingFailure: undefined,
    manualHalt: undefined,
  };
}

export const ThreadHaltReason = {
  encode(message: ThreadHaltReason, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.parentHalted !== undefined) {
      ParentHalted.encode(message.parentHalted, writer.uint32(10).fork()).ldelim();
    }
    if (message.interrupted !== undefined) {
      Interrupted.encode(message.interrupted, writer.uint32(18).fork()).ldelim();
    }
    if (message.pendingInterrupt !== undefined) {
      PendingInterruptHaltReason.encode(message.pendingInterrupt, writer.uint32(26).fork()).ldelim();
    }
    if (message.pendingFailure !== undefined) {
      PendingFailureHandlerHaltReason.encode(message.pendingFailure, writer.uint32(34).fork()).ldelim();
    }
    if (message.handlingFailure !== undefined) {
      HandlingFailureHaltReason.encode(message.handlingFailure, writer.uint32(42).fork()).ldelim();
    }
    if (message.manualHalt !== undefined) {
      ManualHalt.encode(message.manualHalt, writer.uint32(50).fork()).ldelim();
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): ThreadHaltReason {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseThreadHaltReason();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.parentHalted = ParentHalted.decode(reader, reader.uint32());
          continue;
        case 2:
          if (tag !== 18) {
            break;
          }

          message.interrupted = Interrupted.decode(reader, reader.uint32());
          continue;
        case 3:
          if (tag !== 26) {
            break;
          }

          message.pendingInterrupt = PendingInterruptHaltReason.decode(reader, reader.uint32());
          continue;
        case 4:
          if (tag !== 34) {
            break;
          }

          message.pendingFailure = PendingFailureHandlerHaltReason.decode(reader, reader.uint32());
          continue;
        case 5:
          if (tag !== 42) {
            break;
          }

          message.handlingFailure = HandlingFailureHaltReason.decode(reader, reader.uint32());
          continue;
        case 6:
          if (tag !== 50) {
            break;
          }

          message.manualHalt = ManualHalt.decode(reader, reader.uint32());
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  fromJSON(object: any): ThreadHaltReason {
    return {
      parentHalted: isSet(object.parentHalted) ? ParentHalted.fromJSON(object.parentHalted) : undefined,
      interrupted: isSet(object.interrupted) ? Interrupted.fromJSON(object.interrupted) : undefined,
      pendingInterrupt: isSet(object.pendingInterrupt)
        ? PendingInterruptHaltReason.fromJSON(object.pendingInterrupt)
        : undefined,
      pendingFailure: isSet(object.pendingFailure)
        ? PendingFailureHandlerHaltReason.fromJSON(object.pendingFailure)
        : undefined,
      handlingFailure: isSet(object.handlingFailure)
        ? HandlingFailureHaltReason.fromJSON(object.handlingFailure)
        : undefined,
      manualHalt: isSet(object.manualHalt) ? ManualHalt.fromJSON(object.manualHalt) : undefined,
    };
  },

  toJSON(message: ThreadHaltReason): unknown {
    const obj: any = {};
    if (message.parentHalted !== undefined) {
      obj.parentHalted = ParentHalted.toJSON(message.parentHalted);
    }
    if (message.interrupted !== undefined) {
      obj.interrupted = Interrupted.toJSON(message.interrupted);
    }
    if (message.pendingInterrupt !== undefined) {
      obj.pendingInterrupt = PendingInterruptHaltReason.toJSON(message.pendingInterrupt);
    }
    if (message.pendingFailure !== undefined) {
      obj.pendingFailure = PendingFailureHandlerHaltReason.toJSON(message.pendingFailure);
    }
    if (message.handlingFailure !== undefined) {
      obj.handlingFailure = HandlingFailureHaltReason.toJSON(message.handlingFailure);
    }
    if (message.manualHalt !== undefined) {
      obj.manualHalt = ManualHalt.toJSON(message.manualHalt);
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<ThreadHaltReason>, I>>(base?: I): ThreadHaltReason {
    return ThreadHaltReason.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<ThreadHaltReason>, I>>(object: I): ThreadHaltReason {
    const message = createBaseThreadHaltReason();
    message.parentHalted = (object.parentHalted !== undefined && object.parentHalted !== null)
      ? ParentHalted.fromPartial(object.parentHalted)
      : undefined;
    message.interrupted = (object.interrupted !== undefined && object.interrupted !== null)
      ? Interrupted.fromPartial(object.interrupted)
      : undefined;
    message.pendingInterrupt = (object.pendingInterrupt !== undefined && object.pendingInterrupt !== null)
      ? PendingInterruptHaltReason.fromPartial(object.pendingInterrupt)
      : undefined;
    message.pendingFailure = (object.pendingFailure !== undefined && object.pendingFailure !== null)
      ? PendingFailureHandlerHaltReason.fromPartial(object.pendingFailure)
      : undefined;
    message.handlingFailure = (object.handlingFailure !== undefined && object.handlingFailure !== null)
      ? HandlingFailureHaltReason.fromPartial(object.handlingFailure)
      : undefined;
    message.manualHalt = (object.manualHalt !== undefined && object.manualHalt !== null)
      ? ManualHalt.fromPartial(object.manualHalt)
      : undefined;
    return message;
  },
};

type Builtin = Date | Function | Uint8Array | string | number | boolean | undefined;

export type DeepPartial<T> = T extends Builtin ? T
  : T extends Array<infer U> ? Array<DeepPartial<U>> : T extends ReadonlyArray<infer U> ? ReadonlyArray<DeepPartial<U>>
  : T extends {} ? { [K in keyof T]?: DeepPartial<T[K]> }
  : Partial<T>;

type KeysOfUnion<T> = T extends T ? keyof T : never;
export type Exact<P, I extends P> = P extends Builtin ? P
  : P & { [K in keyof P]: Exact<P[K], I[K]> } & { [K in Exclude<keyof I, KeysOfUnion<P>>]: never };

function toTimestamp(dateStr: string): Timestamp {
  const date = new Date(dateStr);
  const seconds = date.getTime() / 1_000;
  const nanos = (date.getTime() % 1_000) * 1_000_000;
  return { seconds, nanos };
}

function fromTimestamp(t: Timestamp): string {
  let millis = (t.seconds || 0) * 1_000;
  millis += (t.nanos || 0) / 1_000_000;
  return new Date(millis).toISOString();
}

function isSet(value: any): boolean {
  return value !== null && value !== undefined;
}
