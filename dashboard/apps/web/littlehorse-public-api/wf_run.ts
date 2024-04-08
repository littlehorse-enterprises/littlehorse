/* eslint-disable */
import * as _m0 from "protobufjs/minimal";
import { LHStatus, lHStatusFromJSON, lHStatusToJSON, lHStatusToNumber } from "./common_enums";
import { Timestamp } from "./google/protobuf/timestamp";
import { ExternalEventId, WfRunId, WfSpecId } from "./object_id";

export const protobufPackage = "littlehorse";

/** The type of a ThreadRUn. */
export enum ThreadType {
  /** ENTRYPOINT - The ENTRYPOINT ThreadRun. Exactly one per WfRun. Always has number == 0. */
  ENTRYPOINT = "ENTRYPOINT",
  /**
   * CHILD - A ThreadRun explicitly created by another ThreadRun via a START_THREAD or START_MULTIPLE_THREADS
   * NodeRun.
   */
  CHILD = "CHILD",
  /** INTERRUPT - A ThreadRun that was created to handle an Interrupt. */
  INTERRUPT = "INTERRUPT",
  /** FAILURE_HANDLER - A ThreadRun that was created to handle a Failure. */
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

/** A WfRun is a running instance of a WfSpec. */
export interface WfRun {
  /** The ID of the WfRun. */
  id:
    | WfRunId
    | undefined;
  /** The ID of the WfSpec that this WfRun belongs to. */
  wfSpecId:
    | WfSpecId
    | undefined;
  /**
   * When a WfRun is migrated from an old verison of a WfSpec to a newer one, we add the
   * old WfSpecId to this list for historical auditing and debugging purposes.
   */
  oldWfSpecVersions: WfSpecId[];
  /** The status of this WfRun. */
  status: LHStatus;
  /**
   * The ID number of the greatest ThreadRUn in this WfRun. The total number of ThreadRuns
   * is given by greatest_thread_run_number + 1.
   *
   * Introduced now since with ThreadRun-level retention, we can't rely upon
   * thread_runs.size() to determine the number of ThreadRuns, as a ThreadRun is removed
   * from the thread_runs list once its retention period expires.
   */
  greatestThreadrunNumber: number;
  /** The time the WfRun was started. */
  startTime:
    | string
    | undefined;
  /** The time the WfRun failed or completed. */
  endTime?:
    | string
    | undefined;
  /**
   * A list of all active ThreadRun's and terminated ThreadRun's whose retention periods
   * have not yet expired.
   */
  threadRuns: ThreadRun[];
  /**
   * A list of Interrupt events that will fire once their appropriate ThreadRun's finish
   * halting.
   */
  pendingInterrupts: PendingInterrupt[];
  /**
   * A list of pending failure handlers which will fire once their appropriate ThreadRun's
   * finish halting.
   */
  pendingFailures: PendingFailureHandler[];
}

/** A ThreadRun is a running thread of execution within a WfRun. */
export interface ThreadRun {
  /**
   * The current WfSpecId of this ThreadRun. This must be set explicitly because
   * during a WfSpec Version Migration, it is possible for different ThreadSpec's to
   * have different WfSpec versions.
   */
  wfSpecId:
    | WfSpecId
    | undefined;
  /**
   * The number of the ThreadRun. This is an auto-incremented integer corresponding to
   * the chronological ordering of when the ThreadRun's were created. If you have not
   * configured any retention policy for the ThreadRun's (i.e. never clean them up), then
   * this also corresponds to the position of the ThreadRun in the WfRun's `thread_runs`
   * list.
   */
  number: number;
  /** The status of the ThreadRun. */
  status: LHStatus;
  /** The name of the ThreadSpec being run. */
  threadSpecName: string;
  /** The time the ThreadRun was started. */
  startTime:
    | string
    | undefined;
  /** The time the ThreadRun was completed or failed. Unset if still active. */
  endTime?:
    | string
    | undefined;
  /** Human-readable error message detailing what went wrong in the case of a failure. */
  errorMessage?:
    | string
    | undefined;
  /** List of thread_run_number's for all child thread_runs. */
  childThreadIds: number[];
  /** Set for every ThreadRun except the ENTRYPOINT. This is the id of the parent thread. */
  parentThreadId?:
    | number
    | undefined;
  /**
   * If the ThreadRun is HALTED, this contains a list of every reason for which the
   * ThreadRun is HALTED. Once every reason is "resolved" (and thus removed from the list),
   * then the ThreadRun will return to the RUNNING state.
   */
  haltReasons: ThreadHaltReason[];
  /**
   * If this ThreadRun is of type INTERRUPT_HANDLER, this field is set to the ID of the
   * ExternalEvent that caused the Interrupt.
   */
  interruptTriggerId?:
    | ExternalEventId
    | undefined;
  /**
   * If this ThreadRun is of type FAILURE_HANDLER, this field is set to the exact Failure
   * that is being handled by this ThreadRun.
   */
  failureBeingHandled?:
    | FailureBeingHandled
    | undefined;
  /**
   * This is the current `position` of the current NodeRun being run. This is an
   * auto-incremented field that gets incremented every time we run a new NodeRun.
   */
  currentNodePosition: number;
  /**
   * List of every child ThreadRun which both a) failed, and b) was properly handled by a
   * Failure Handler.
   *
   * This is important because at the EXIT node, if a Child ThreadRun was discovered to have
   * failed, then this ThreadRun (the parent) also fails with the same failure as the child.
   * If, however, a Failure Handler had previously "handled" the Child Failure, that ThreadRun's
   * number is appended to this list, and then the EXIT node ignores that ThreadRun.
   */
  handledFailedChildren: number[];
  /** The Type of this ThreadRun. */
  type: ThreadType;
}

/** Points to the Failure that is currently being handled in the ThreadRun. */
export interface FailureBeingHandled {
  /** The thread run number. */
  threadRunNumber: number;
  /** The position of the NodeRun causing the failure. */
  nodeRunPosition: number;
  /** The number of the failure. */
  failureNumber: number;
}

/**
 * Represents an ExternalEvent that has a registered Interrupt Handler for it
 * and which is pending to be sent to the relevant ThreadRun's.
 */
export interface PendingInterrupt {
  /** The ID of the ExternalEvent triggering the Interrupt. */
  externalEventId:
    | ExternalEventId
    | undefined;
  /** The name of the ThreadSpec to run to handle the Interrupt. */
  handlerSpecName: string;
  /**
   * The ID of the ThreadRun to interrupt. Must wait for this ThreadRun to be
   * HALTED before running the Interrupt Handler.
   */
  interruptedThreadId: number;
}

/** Represents a Failure Handler that is pending to be run. */
export interface PendingFailureHandler {
  /** The ThreadRun that failed. */
  failedThreadRun: number;
  /** The name of the ThreadSpec to run to handle the failure. */
  handlerSpecName: string;
}

/**
 * A Halt Reason denoting that a ThreadRun is halted while waiting for an Interrupt handler
 * to be run.
 */
export interface PendingInterruptHaltReason {
  /** The ExternalEventId that caused the Interrupt. */
  externalEventId: ExternalEventId | undefined;
}

/**
 * A Halt Reason denoting that a ThreadRun is halted while a Failure Handler is *enqueued* to be
 * run.
 */
export interface PendingFailureHandlerHaltReason {
  /** The position of the NodeRun which threw the failure. */
  nodeRunPosition: number;
}

/** A Halt Reason denoting that a ThreadRun is halted while a Failure Handler is being run. */
export interface HandlingFailureHaltReason {
  /** The ID of the Failure Handler ThreadRun. */
  handlerThreadId: number;
}

/** A Halt Reason denoting that a ThreadRun is halted because its parent is also HALTED. */
export interface ParentHalted {
  /** The ID of the halted parent. */
  parentThreadId: number;
}

/**
 * A Halt Reason denoting that a ThreadRun is halted because it is waiting for the
 * interrupt handler threadRun to run.
 */
export interface Interrupted {
  /** The ID of the Interrupt Handler ThreadRun. */
  interruptThreadId: number;
}

/** A Halt Reason denoting that a ThreadRun was halted manually, via the `rpc StopWfRun` request. */
export interface ManualHalt {
  /** Nothing to store. */
  meaningOfLife: boolean;
}

/** Denotes a reason why a ThreadRun is halted. See `ThreadRun.halt_reasons` for context. */
export interface ThreadHaltReason {
  /** Parent threadRun halted. */
  parentHalted?:
    | ParentHalted
    | undefined;
  /** Handling an Interrupt. */
  interrupted?:
    | Interrupted
    | undefined;
  /** Waiting to handle Interrupt. */
  pendingInterrupt?:
    | PendingInterruptHaltReason
    | undefined;
  /** Waiting to handle a failure. */
  pendingFailure?:
    | PendingFailureHandlerHaltReason
    | undefined;
  /** Handling a failure. */
  handlingFailure?:
    | HandlingFailureHaltReason
    | undefined;
  /** Manually stopped the WfRun. */
  manualHalt?: ManualHalt | undefined;
}

function createBaseWfRun(): WfRun {
  return {
    id: undefined,
    wfSpecId: undefined,
    oldWfSpecVersions: [],
    status: LHStatus.STARTING,
    greatestThreadrunNumber: 0,
    startTime: undefined,
    endTime: undefined,
    threadRuns: [],
    pendingInterrupts: [],
    pendingFailures: [],
  };
}

export const WfRun = {
  encode(message: WfRun, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.id !== undefined) {
      WfRunId.encode(message.id, writer.uint32(10).fork()).ldelim();
    }
    if (message.wfSpecId !== undefined) {
      WfSpecId.encode(message.wfSpecId, writer.uint32(18).fork()).ldelim();
    }
    for (const v of message.oldWfSpecVersions) {
      WfSpecId.encode(v!, writer.uint32(26).fork()).ldelim();
    }
    if (message.status !== LHStatus.STARTING) {
      writer.uint32(32).int32(lHStatusToNumber(message.status));
    }
    if (message.greatestThreadrunNumber !== 0) {
      writer.uint32(40).int32(message.greatestThreadrunNumber);
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

          message.id = WfRunId.decode(reader, reader.uint32());
          continue;
        case 2:
          if (tag !== 18) {
            break;
          }

          message.wfSpecId = WfSpecId.decode(reader, reader.uint32());
          continue;
        case 3:
          if (tag !== 26) {
            break;
          }

          message.oldWfSpecVersions.push(WfSpecId.decode(reader, reader.uint32()));
          continue;
        case 4:
          if (tag !== 32) {
            break;
          }

          message.status = lHStatusFromJSON(reader.int32());
          continue;
        case 5:
          if (tag !== 40) {
            break;
          }

          message.greatestThreadrunNumber = reader.int32();
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
      id: isSet(object.id) ? WfRunId.fromJSON(object.id) : undefined,
      wfSpecId: isSet(object.wfSpecId) ? WfSpecId.fromJSON(object.wfSpecId) : undefined,
      oldWfSpecVersions: globalThis.Array.isArray(object?.oldWfSpecVersions)
        ? object.oldWfSpecVersions.map((e: any) => WfSpecId.fromJSON(e))
        : [],
      status: isSet(object.status) ? lHStatusFromJSON(object.status) : LHStatus.STARTING,
      greatestThreadrunNumber: isSet(object.greatestThreadrunNumber)
        ? globalThis.Number(object.greatestThreadrunNumber)
        : 0,
      startTime: isSet(object.startTime) ? globalThis.String(object.startTime) : undefined,
      endTime: isSet(object.endTime) ? globalThis.String(object.endTime) : undefined,
      threadRuns: globalThis.Array.isArray(object?.threadRuns)
        ? object.threadRuns.map((e: any) => ThreadRun.fromJSON(e))
        : [],
      pendingInterrupts: globalThis.Array.isArray(object?.pendingInterrupts)
        ? object.pendingInterrupts.map((e: any) => PendingInterrupt.fromJSON(e))
        : [],
      pendingFailures: globalThis.Array.isArray(object?.pendingFailures)
        ? object.pendingFailures.map((e: any) => PendingFailureHandler.fromJSON(e))
        : [],
    };
  },

  toJSON(message: WfRun): unknown {
    const obj: any = {};
    if (message.id !== undefined) {
      obj.id = WfRunId.toJSON(message.id);
    }
    if (message.wfSpecId !== undefined) {
      obj.wfSpecId = WfSpecId.toJSON(message.wfSpecId);
    }
    if (message.oldWfSpecVersions?.length) {
      obj.oldWfSpecVersions = message.oldWfSpecVersions.map((e) => WfSpecId.toJSON(e));
    }
    if (message.status !== LHStatus.STARTING) {
      obj.status = lHStatusToJSON(message.status);
    }
    if (message.greatestThreadrunNumber !== 0) {
      obj.greatestThreadrunNumber = Math.round(message.greatestThreadrunNumber);
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
    message.id = (object.id !== undefined && object.id !== null) ? WfRunId.fromPartial(object.id) : undefined;
    message.wfSpecId = (object.wfSpecId !== undefined && object.wfSpecId !== null)
      ? WfSpecId.fromPartial(object.wfSpecId)
      : undefined;
    message.oldWfSpecVersions = object.oldWfSpecVersions?.map((e) => WfSpecId.fromPartial(e)) || [];
    message.status = object.status ?? LHStatus.STARTING;
    message.greatestThreadrunNumber = object.greatestThreadrunNumber ?? 0;
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
    wfSpecId: undefined,
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
    if (message.wfSpecId !== undefined) {
      WfSpecId.encode(message.wfSpecId, writer.uint32(10).fork()).ldelim();
    }
    if (message.number !== 0) {
      writer.uint32(16).int32(message.number);
    }
    if (message.status !== LHStatus.STARTING) {
      writer.uint32(24).int32(lHStatusToNumber(message.status));
    }
    if (message.threadSpecName !== "") {
      writer.uint32(34).string(message.threadSpecName);
    }
    if (message.startTime !== undefined) {
      Timestamp.encode(toTimestamp(message.startTime), writer.uint32(42).fork()).ldelim();
    }
    if (message.endTime !== undefined) {
      Timestamp.encode(toTimestamp(message.endTime), writer.uint32(50).fork()).ldelim();
    }
    if (message.errorMessage !== undefined) {
      writer.uint32(58).string(message.errorMessage);
    }
    writer.uint32(66).fork();
    for (const v of message.childThreadIds) {
      writer.int32(v);
    }
    writer.ldelim();
    if (message.parentThreadId !== undefined) {
      writer.uint32(72).int32(message.parentThreadId);
    }
    for (const v of message.haltReasons) {
      ThreadHaltReason.encode(v!, writer.uint32(82).fork()).ldelim();
    }
    if (message.interruptTriggerId !== undefined) {
      ExternalEventId.encode(message.interruptTriggerId, writer.uint32(90).fork()).ldelim();
    }
    if (message.failureBeingHandled !== undefined) {
      FailureBeingHandled.encode(message.failureBeingHandled, writer.uint32(98).fork()).ldelim();
    }
    if (message.currentNodePosition !== 0) {
      writer.uint32(104).int32(message.currentNodePosition);
    }
    writer.uint32(114).fork();
    for (const v of message.handledFailedChildren) {
      writer.int32(v);
    }
    writer.ldelim();
    if (message.type !== ThreadType.ENTRYPOINT) {
      writer.uint32(120).int32(threadTypeToNumber(message.type));
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
        case 1:
          if (tag !== 10) {
            break;
          }

          message.wfSpecId = WfSpecId.decode(reader, reader.uint32());
          continue;
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
        case 4:
          if (tag !== 34) {
            break;
          }

          message.threadSpecName = reader.string();
          continue;
        case 5:
          if (tag !== 42) {
            break;
          }

          message.startTime = fromTimestamp(Timestamp.decode(reader, reader.uint32()));
          continue;
        case 6:
          if (tag !== 50) {
            break;
          }

          message.endTime = fromTimestamp(Timestamp.decode(reader, reader.uint32()));
          continue;
        case 7:
          if (tag !== 58) {
            break;
          }

          message.errorMessage = reader.string();
          continue;
        case 8:
          if (tag === 64) {
            message.childThreadIds.push(reader.int32());

            continue;
          }

          if (tag === 66) {
            const end2 = reader.uint32() + reader.pos;
            while (reader.pos < end2) {
              message.childThreadIds.push(reader.int32());
            }

            continue;
          }

          break;
        case 9:
          if (tag !== 72) {
            break;
          }

          message.parentThreadId = reader.int32();
          continue;
        case 10:
          if (tag !== 82) {
            break;
          }

          message.haltReasons.push(ThreadHaltReason.decode(reader, reader.uint32()));
          continue;
        case 11:
          if (tag !== 90) {
            break;
          }

          message.interruptTriggerId = ExternalEventId.decode(reader, reader.uint32());
          continue;
        case 12:
          if (tag !== 98) {
            break;
          }

          message.failureBeingHandled = FailureBeingHandled.decode(reader, reader.uint32());
          continue;
        case 13:
          if (tag !== 104) {
            break;
          }

          message.currentNodePosition = reader.int32();
          continue;
        case 14:
          if (tag === 112) {
            message.handledFailedChildren.push(reader.int32());

            continue;
          }

          if (tag === 114) {
            const end2 = reader.uint32() + reader.pos;
            while (reader.pos < end2) {
              message.handledFailedChildren.push(reader.int32());
            }

            continue;
          }

          break;
        case 15:
          if (tag !== 120) {
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
      wfSpecId: isSet(object.wfSpecId) ? WfSpecId.fromJSON(object.wfSpecId) : undefined,
      number: isSet(object.number) ? globalThis.Number(object.number) : 0,
      status: isSet(object.status) ? lHStatusFromJSON(object.status) : LHStatus.STARTING,
      threadSpecName: isSet(object.threadSpecName) ? globalThis.String(object.threadSpecName) : "",
      startTime: isSet(object.startTime) ? globalThis.String(object.startTime) : undefined,
      endTime: isSet(object.endTime) ? globalThis.String(object.endTime) : undefined,
      errorMessage: isSet(object.errorMessage) ? globalThis.String(object.errorMessage) : undefined,
      childThreadIds: globalThis.Array.isArray(object?.childThreadIds)
        ? object.childThreadIds.map((e: any) => globalThis.Number(e))
        : [],
      parentThreadId: isSet(object.parentThreadId) ? globalThis.Number(object.parentThreadId) : undefined,
      haltReasons: globalThis.Array.isArray(object?.haltReasons)
        ? object.haltReasons.map((e: any) => ThreadHaltReason.fromJSON(e))
        : [],
      interruptTriggerId: isSet(object.interruptTriggerId)
        ? ExternalEventId.fromJSON(object.interruptTriggerId)
        : undefined,
      failureBeingHandled: isSet(object.failureBeingHandled)
        ? FailureBeingHandled.fromJSON(object.failureBeingHandled)
        : undefined,
      currentNodePosition: isSet(object.currentNodePosition) ? globalThis.Number(object.currentNodePosition) : 0,
      handledFailedChildren: globalThis.Array.isArray(object?.handledFailedChildren)
        ? object.handledFailedChildren.map((e: any) => globalThis.Number(e))
        : [],
      type: isSet(object.type) ? threadTypeFromJSON(object.type) : ThreadType.ENTRYPOINT,
    };
  },

  toJSON(message: ThreadRun): unknown {
    const obj: any = {};
    if (message.wfSpecId !== undefined) {
      obj.wfSpecId = WfSpecId.toJSON(message.wfSpecId);
    }
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
    message.wfSpecId = (object.wfSpecId !== undefined && object.wfSpecId !== null)
      ? WfSpecId.fromPartial(object.wfSpecId)
      : undefined;
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
      threadRunNumber: isSet(object.threadRunNumber) ? globalThis.Number(object.threadRunNumber) : 0,
      nodeRunPosition: isSet(object.nodeRunPosition) ? globalThis.Number(object.nodeRunPosition) : 0,
      failureNumber: isSet(object.failureNumber) ? globalThis.Number(object.failureNumber) : 0,
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
      handlerSpecName: isSet(object.handlerSpecName) ? globalThis.String(object.handlerSpecName) : "",
      interruptedThreadId: isSet(object.interruptedThreadId) ? globalThis.Number(object.interruptedThreadId) : 0,
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
      failedThreadRun: isSet(object.failedThreadRun) ? globalThis.Number(object.failedThreadRun) : 0,
      handlerSpecName: isSet(object.handlerSpecName) ? globalThis.String(object.handlerSpecName) : "",
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
    return { nodeRunPosition: isSet(object.nodeRunPosition) ? globalThis.Number(object.nodeRunPosition) : 0 };
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
    return { handlerThreadId: isSet(object.handlerThreadId) ? globalThis.Number(object.handlerThreadId) : 0 };
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
    return { parentThreadId: isSet(object.parentThreadId) ? globalThis.Number(object.parentThreadId) : 0 };
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
    return { interruptThreadId: isSet(object.interruptThreadId) ? globalThis.Number(object.interruptThreadId) : 0 };
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
    if (message.meaningOfLife !== false) {
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
    return { meaningOfLife: isSet(object.meaningOfLife) ? globalThis.Boolean(object.meaningOfLife) : false };
  },

  toJSON(message: ManualHalt): unknown {
    const obj: any = {};
    if (message.meaningOfLife !== false) {
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
