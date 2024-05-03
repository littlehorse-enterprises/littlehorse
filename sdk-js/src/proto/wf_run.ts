/* eslint-disable */
import _m0 from "protobufjs/minimal";
import { LHStatus, lHStatusFromJSON, lHStatusToNumber } from "./common_enums";
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

  create(base?: DeepPartial<WfRun>): WfRun {
    return WfRun.fromPartial(base ?? {});
  },
  fromPartial(object: DeepPartial<WfRun>): WfRun {
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

  create(base?: DeepPartial<ThreadRun>): ThreadRun {
    return ThreadRun.fromPartial(base ?? {});
  },
  fromPartial(object: DeepPartial<ThreadRun>): ThreadRun {
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

  create(base?: DeepPartial<FailureBeingHandled>): FailureBeingHandled {
    return FailureBeingHandled.fromPartial(base ?? {});
  },
  fromPartial(object: DeepPartial<FailureBeingHandled>): FailureBeingHandled {
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

  create(base?: DeepPartial<PendingInterrupt>): PendingInterrupt {
    return PendingInterrupt.fromPartial(base ?? {});
  },
  fromPartial(object: DeepPartial<PendingInterrupt>): PendingInterrupt {
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

  create(base?: DeepPartial<PendingFailureHandler>): PendingFailureHandler {
    return PendingFailureHandler.fromPartial(base ?? {});
  },
  fromPartial(object: DeepPartial<PendingFailureHandler>): PendingFailureHandler {
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

  create(base?: DeepPartial<PendingInterruptHaltReason>): PendingInterruptHaltReason {
    return PendingInterruptHaltReason.fromPartial(base ?? {});
  },
  fromPartial(object: DeepPartial<PendingInterruptHaltReason>): PendingInterruptHaltReason {
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

  create(base?: DeepPartial<PendingFailureHandlerHaltReason>): PendingFailureHandlerHaltReason {
    return PendingFailureHandlerHaltReason.fromPartial(base ?? {});
  },
  fromPartial(object: DeepPartial<PendingFailureHandlerHaltReason>): PendingFailureHandlerHaltReason {
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

  create(base?: DeepPartial<HandlingFailureHaltReason>): HandlingFailureHaltReason {
    return HandlingFailureHaltReason.fromPartial(base ?? {});
  },
  fromPartial(object: DeepPartial<HandlingFailureHaltReason>): HandlingFailureHaltReason {
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

  create(base?: DeepPartial<ParentHalted>): ParentHalted {
    return ParentHalted.fromPartial(base ?? {});
  },
  fromPartial(object: DeepPartial<ParentHalted>): ParentHalted {
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

  create(base?: DeepPartial<Interrupted>): Interrupted {
    return Interrupted.fromPartial(base ?? {});
  },
  fromPartial(object: DeepPartial<Interrupted>): Interrupted {
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

  create(base?: DeepPartial<ManualHalt>): ManualHalt {
    return ManualHalt.fromPartial(base ?? {});
  },
  fromPartial(object: DeepPartial<ManualHalt>): ManualHalt {
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

  create(base?: DeepPartial<ThreadHaltReason>): ThreadHaltReason {
    return ThreadHaltReason.fromPartial(base ?? {});
  },
  fromPartial(object: DeepPartial<ThreadHaltReason>): ThreadHaltReason {
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
