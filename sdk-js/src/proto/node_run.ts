/* eslint-disable */
import _m0 from "protobufjs/minimal";
import { LHStatus, lHStatusFromJSON, lHStatusToNumber } from "./common_enums";
import { Timestamp } from "./google/protobuf/timestamp";
import {
  ExternalEventDefId,
  ExternalEventId,
  NodeRunId,
  TaskRunId,
  UserTaskRunId,
  WfSpecId,
  WorkflowEventId,
} from "./object_id";
import { VariableValue } from "./variable";

export const protobufPackage = "littlehorse";

/**
 * A NodeRun is a running instance of a Node in a ThreadRun. Note that a NodeRun
 * is a Getable object, meaning it can be retried from the LittleHorse grpc API.
 */
export interface NodeRun {
  /**
   * The ID of the NodeRun. Note that the NodeRunId contains the WfRunId, the
   * ThreadRun's number, and the position of the NodeRun within that ThreadRun.
   */
  id:
    | NodeRunId
    | undefined;
  /**
   * The ID of the WfSpec that this NodeRun is from. This is not _always_ the same
   * as the ThreadRun it belongs to because of the upcoming WfSpec Version Migration
   * feature.
   */
  wfSpecId:
    | WfSpecId
    | undefined;
  /** A list of all ThreadRun's that ran to handle a failure thrown by this NodeRun. */
  failureHandlerIds: number[];
  /** The status of this NodeRun. */
  status: LHStatus;
  /** The time the ThreadRun arrived at this NodeRun. */
  arrivalTime:
    | string
    | undefined;
  /** The time the NodeRun was terminated (failed or completed). */
  endTime?:
    | string
    | undefined;
  /** The name of the ThreadSpec to which this NodeRun belongs. */
  threadSpecName: string;
  /** The name of the Node in the ThreadSpec that this NodeRun belongs to. */
  nodeName: string;
  /**
   * A human-readable error message intended to help developers diagnose WfSpec
   * problems.
   */
  errorMessage?:
    | string
    | undefined;
  /** A list of Failures thrown by this NodeRun. */
  failures: Failure[];
  /** Denotes a TASK node, which runs a TaskRun. */
  task?:
    | TaskNodeRun
    | undefined;
  /** An EXTERNAL_EVENT node blocks until an ExternalEvent arrives. */
  externalEvent?:
    | ExternalEventRun
    | undefined;
  /** An ENTRYPOINT node is the first thing that runs in a ThreadRun. */
  entrypoint?:
    | EntrypointRun
    | undefined;
  /** An EXIT node completes a ThreadRun. */
  exit?:
    | ExitRun
    | undefined;
  /** A START_THREAD node starts a child ThreadRun. */
  startThread?:
    | StartThreadRun
    | undefined;
  /** A WAIT_THREADS node waits for one or more child ThreadRun's to complete. */
  waitThreads?:
    | WaitForThreadsRun
    | undefined;
  /** A SLEEP node makes the ThreadRun block for a certain amount of time. */
  sleep?:
    | SleepNodeRun
    | undefined;
  /** A USER_TASK node waits until a human executes some work and reports the result. */
  userTask?:
    | UserTaskNodeRun
    | undefined;
  /**
   * A START_MULTIPLE_THREADS node iterates over a JSON_ARR variable and spawns a
   * child ThreadRun for each element in the list.
   */
  startMultipleThreads?: StartMultipleThreadsRun | undefined;
  throwEvent?: ThrowEventNodeRun | undefined;
}

/** The sub-node structure for a TASK NodeRun. */
export interface TaskNodeRun {
  /**
   * The ID of the TaskRun. Note that if the ThreadRun was halted when it arrived
   * at this TASK Node, then the task_run_id will be unset.
   */
  taskRunId?: TaskRunId | undefined;
}

export interface ThrowEventNodeRun {
  workflowEventId: WorkflowEventId | undefined;
}

/** The sub-node structure for a USER_TASK NodeRun. */
export interface UserTaskNodeRun {
  /**
   * The ID of the UserTaskRun. Note that if the ThreadRun was halted when it arrived
   * at this USER_TASK node, then the user_task_run_id will be unset.
   */
  userTaskRunId?: UserTaskRunId | undefined;
}

/** The sub-node structure for an ENTRYPOINT NodeRun. Currently Empty. */
export interface EntrypointRun {
}

/**
 * The sub-node structure for an EXIT NodeRun. Currently Empty, will contain info
 * about ThreadRun Outputs once those are added in the future.
 */
export interface ExitRun {
}

/** The sub-node structure for a START_THREAD NodeRun. */
export interface StartThreadRun {
  /**
   * Contains the thread_run_number of the created Child ThreadRun, if it has
   * been created already.
   */
  childThreadId?:
    | number
    | undefined;
  /** The thread_spec_name of the child thread_run. */
  threadSpecName: string;
}

/**
 * The sub-node structure for a START_MULTIPLE_THREADS NodeRun.
 *
 * Note: the output of this NodeRun, which can be used to mutate Variables,
 * is a JSON_ARR variable containing the ID's of all the child threadRuns.
 */
export interface StartMultipleThreadsRun {
  /** The thread_spec_name of the child thread_runs. */
  threadSpecName: string;
  /** The list of all created child ThreadRun's */
  childThreadIds: number[];
}

/** The sub-node structure for a WAIT_FOR_THREADS NodeRun. */
export interface WaitForThreadsRun {
  /** The threads that are being waited for. */
  threads: WaitForThreadsRun_WaitForThread[];
}

/** The status of a single ThreadRun that we are waiting for. */
export enum WaitForThreadsRun_WaitingThreadStatus {
  /** THREAD_IN_PROGRESS - The ThreadRun is in progress (i.e. not COMPLETED nor EXCEPTION nor ERROR) */
  THREAD_IN_PROGRESS = "THREAD_IN_PROGRESS",
  /**
   * THREAD_HANDLING_FAILURE - The ThreadRun failed with some failure, and the FailureHandler is running
   * for that Failure.
   */
  THREAD_HANDLING_FAILURE = "THREAD_HANDLING_FAILURE",
  /**
   * THREAD_COMPLETED_OR_FAILURE_HANDLED - We can mark this ThreadRun as "already waited for", meaning that either:
   * 1. It completed successfully, OR
   * 2. It failed, and the Failure Handler successfully completed
   */
  THREAD_COMPLETED_OR_FAILURE_HANDLED = "THREAD_COMPLETED_OR_FAILURE_HANDLED",
  /**
   * THREAD_UNSUCCESSFUL - The ThreadRun did not complete successfully, and there wasn't a successful
   * run of a Failure Handler for the Failure that was thrown.
   */
  THREAD_UNSUCCESSFUL = "THREAD_UNSUCCESSFUL",
  UNRECOGNIZED = "UNRECOGNIZED",
}

export function waitForThreadsRun_WaitingThreadStatusFromJSON(object: any): WaitForThreadsRun_WaitingThreadStatus {
  switch (object) {
    case 0:
    case "THREAD_IN_PROGRESS":
      return WaitForThreadsRun_WaitingThreadStatus.THREAD_IN_PROGRESS;
    case 1:
    case "THREAD_HANDLING_FAILURE":
      return WaitForThreadsRun_WaitingThreadStatus.THREAD_HANDLING_FAILURE;
    case 2:
    case "THREAD_COMPLETED_OR_FAILURE_HANDLED":
      return WaitForThreadsRun_WaitingThreadStatus.THREAD_COMPLETED_OR_FAILURE_HANDLED;
    case 3:
    case "THREAD_UNSUCCESSFUL":
      return WaitForThreadsRun_WaitingThreadStatus.THREAD_UNSUCCESSFUL;
    case -1:
    case "UNRECOGNIZED":
    default:
      return WaitForThreadsRun_WaitingThreadStatus.UNRECOGNIZED;
  }
}

export function waitForThreadsRun_WaitingThreadStatusToNumber(object: WaitForThreadsRun_WaitingThreadStatus): number {
  switch (object) {
    case WaitForThreadsRun_WaitingThreadStatus.THREAD_IN_PROGRESS:
      return 0;
    case WaitForThreadsRun_WaitingThreadStatus.THREAD_HANDLING_FAILURE:
      return 1;
    case WaitForThreadsRun_WaitingThreadStatus.THREAD_COMPLETED_OR_FAILURE_HANDLED:
      return 2;
    case WaitForThreadsRun_WaitingThreadStatus.THREAD_UNSUCCESSFUL:
      return 3;
    case WaitForThreadsRun_WaitingThreadStatus.UNRECOGNIZED:
    default:
      return -1;
  }
}

/** A 'WaitForThread' structure defines a thread that is being waited for. */
export interface WaitForThreadsRun_WaitForThread {
  /**
   * The time at which the ThreadRun ended (successfully or not). Not set if the ThreadRun
   * is still RUNNING, HALTED, or HALTING.
   */
  threadEndTime?:
    | string
    | undefined;
  /** The current status of the ThreadRun being waited for. */
  threadStatus: LHStatus;
  /** The number of the ThreadRun being waited for. */
  threadRunNumber: number;
  /**
   * The "waiting status" of this specific thread: whether it's still running,
   * already done, handling a failure, or completely failed.
   */
  waitingStatus: WaitForThreadsRun_WaitingThreadStatus;
  /**
   * If there is a failure on the ThreadRun, and we have a failure handler defined
   * for it, then we will start a failure handler for this threadrun. This field
   * is the id of that threadRun.
   */
  failureHandlerThreadRunId?: number | undefined;
}

/** The sub-node structure for an EXTERNAL_EVENT NodeRun. */
export interface ExternalEventRun {
  /** The ExternalEventDefId that we are waiting for. */
  externalEventDefId:
    | ExternalEventDefId
    | undefined;
  /** The time that the ExternalEvent arrived. Unset if still waiting. */
  eventTime?:
    | string
    | undefined;
  /** The ExternalEventId of the ExternalEvent. Unset if still waiting. */
  externalEventId?:
    | ExternalEventId
    | undefined;
  /** Whether we had a timeout while waiting for the ExternalEvent to come. */
  timedOut: boolean;
}

/** The sub-node structure for a SLEEP NodeRun. */
export interface SleepNodeRun {
  /**
   * The time at which the NodeRun is *SCHEDULED TO* wake up. In rare cases, if
   * the LH Server is back-pressuring clients due to extreme load, the timer
   * event which marks the sleep node as "matured" may come in slightly late.
   */
  maturationTime:
    | string
    | undefined;
  /** Whether the SleepNodeRun has been matured. */
  matured: boolean;
}

/**
 * Denotes a failure that happened during execution of a NodeRun or the outgoing
 * edges.
 */
export interface Failure {
  /**
   * The name of the failure. LittleHorse has certain built-in failures, all named in
   * UPPER_UNDERSCORE_CASE. Such failures correspond with the `LHStatus.ERROR`.
   *
   * Any Failure named in `kebab-case` is a user-defined business `EXCEPTION`, treated
   * as an `LHStatus.EXCEPTION`.
   */
  failureName: string;
  /** The human-readable message associated with this Failure. */
  message: string;
  /**
   * A user-defined Failure can have a value; for example, in Java an Exception is an
   * Object with arbitrary properties and behaviors.
   *
   * Future versions of LH will allow FailureHandler threads to accept that value as
   * an input variable.
   */
  content?:
    | VariableValue
    | undefined;
  /** A boolean denoting whether a Failure Handler ThreadRun properly handled the Failure. */
  wasProperlyHandled: boolean;
  /**
   * If there is a defined failure handler for the NodeRun, then this field is set to the
   * id of the failure handler thread run.
   */
  failureHandlerThreadrunId?: number | undefined;
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
    throwEvent: undefined,
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
    if (message.throwEvent !== undefined) {
      ThrowEventNodeRun.encode(message.throwEvent, writer.uint32(178).fork()).ldelim();
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
        case 22:
          if (tag !== 178) {
            break;
          }

          message.throwEvent = ThrowEventNodeRun.decode(reader, reader.uint32());
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create(base?: DeepPartial<NodeRun>): NodeRun {
    return NodeRun.fromPartial(base ?? {});
  },
  fromPartial(object: DeepPartial<NodeRun>): NodeRun {
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
    message.throwEvent = (object.throwEvent !== undefined && object.throwEvent !== null)
      ? ThrowEventNodeRun.fromPartial(object.throwEvent)
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

  create(base?: DeepPartial<TaskNodeRun>): TaskNodeRun {
    return TaskNodeRun.fromPartial(base ?? {});
  },
  fromPartial(object: DeepPartial<TaskNodeRun>): TaskNodeRun {
    const message = createBaseTaskNodeRun();
    message.taskRunId = (object.taskRunId !== undefined && object.taskRunId !== null)
      ? TaskRunId.fromPartial(object.taskRunId)
      : undefined;
    return message;
  },
};

function createBaseThrowEventNodeRun(): ThrowEventNodeRun {
  return { workflowEventId: undefined };
}

export const ThrowEventNodeRun = {
  encode(message: ThrowEventNodeRun, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.workflowEventId !== undefined) {
      WorkflowEventId.encode(message.workflowEventId, writer.uint32(10).fork()).ldelim();
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): ThrowEventNodeRun {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseThrowEventNodeRun();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.workflowEventId = WorkflowEventId.decode(reader, reader.uint32());
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create(base?: DeepPartial<ThrowEventNodeRun>): ThrowEventNodeRun {
    return ThrowEventNodeRun.fromPartial(base ?? {});
  },
  fromPartial(object: DeepPartial<ThrowEventNodeRun>): ThrowEventNodeRun {
    const message = createBaseThrowEventNodeRun();
    message.workflowEventId = (object.workflowEventId !== undefined && object.workflowEventId !== null)
      ? WorkflowEventId.fromPartial(object.workflowEventId)
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

  create(base?: DeepPartial<UserTaskNodeRun>): UserTaskNodeRun {
    return UserTaskNodeRun.fromPartial(base ?? {});
  },
  fromPartial(object: DeepPartial<UserTaskNodeRun>): UserTaskNodeRun {
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

  create(base?: DeepPartial<EntrypointRun>): EntrypointRun {
    return EntrypointRun.fromPartial(base ?? {});
  },
  fromPartial(_: DeepPartial<EntrypointRun>): EntrypointRun {
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

  create(base?: DeepPartial<ExitRun>): ExitRun {
    return ExitRun.fromPartial(base ?? {});
  },
  fromPartial(_: DeepPartial<ExitRun>): ExitRun {
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

  create(base?: DeepPartial<StartThreadRun>): StartThreadRun {
    return StartThreadRun.fromPartial(base ?? {});
  },
  fromPartial(object: DeepPartial<StartThreadRun>): StartThreadRun {
    const message = createBaseStartThreadRun();
    message.childThreadId = object.childThreadId ?? undefined;
    message.threadSpecName = object.threadSpecName ?? "";
    return message;
  },
};

function createBaseStartMultipleThreadsRun(): StartMultipleThreadsRun {
  return { threadSpecName: "", childThreadIds: [] };
}

export const StartMultipleThreadsRun = {
  encode(message: StartMultipleThreadsRun, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.threadSpecName !== "") {
      writer.uint32(10).string(message.threadSpecName);
    }
    writer.uint32(18).fork();
    for (const v of message.childThreadIds) {
      writer.int32(v);
    }
    writer.ldelim();
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
        case 2:
          if (tag === 16) {
            message.childThreadIds.push(reader.int32());

            continue;
          }

          if (tag === 18) {
            const end2 = reader.uint32() + reader.pos;
            while (reader.pos < end2) {
              message.childThreadIds.push(reader.int32());
            }

            continue;
          }

          break;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create(base?: DeepPartial<StartMultipleThreadsRun>): StartMultipleThreadsRun {
    return StartMultipleThreadsRun.fromPartial(base ?? {});
  },
  fromPartial(object: DeepPartial<StartMultipleThreadsRun>): StartMultipleThreadsRun {
    const message = createBaseStartMultipleThreadsRun();
    message.threadSpecName = object.threadSpecName ?? "";
    message.childThreadIds = object.childThreadIds?.map((e) => e) || [];
    return message;
  },
};

function createBaseWaitForThreadsRun(): WaitForThreadsRun {
  return { threads: [] };
}

export const WaitForThreadsRun = {
  encode(message: WaitForThreadsRun, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    for (const v of message.threads) {
      WaitForThreadsRun_WaitForThread.encode(v!, writer.uint32(10).fork()).ldelim();
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
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create(base?: DeepPartial<WaitForThreadsRun>): WaitForThreadsRun {
    return WaitForThreadsRun.fromPartial(base ?? {});
  },
  fromPartial(object: DeepPartial<WaitForThreadsRun>): WaitForThreadsRun {
    const message = createBaseWaitForThreadsRun();
    message.threads = object.threads?.map((e) => WaitForThreadsRun_WaitForThread.fromPartial(e)) || [];
    return message;
  },
};

function createBaseWaitForThreadsRun_WaitForThread(): WaitForThreadsRun_WaitForThread {
  return {
    threadEndTime: undefined,
    threadStatus: LHStatus.STARTING,
    threadRunNumber: 0,
    waitingStatus: WaitForThreadsRun_WaitingThreadStatus.THREAD_IN_PROGRESS,
    failureHandlerThreadRunId: undefined,
  };
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
    if (message.waitingStatus !== WaitForThreadsRun_WaitingThreadStatus.THREAD_IN_PROGRESS) {
      writer.uint32(32).int32(waitForThreadsRun_WaitingThreadStatusToNumber(message.waitingStatus));
    }
    if (message.failureHandlerThreadRunId !== undefined) {
      writer.uint32(40).int32(message.failureHandlerThreadRunId);
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
        case 4:
          if (tag !== 32) {
            break;
          }

          message.waitingStatus = waitForThreadsRun_WaitingThreadStatusFromJSON(reader.int32());
          continue;
        case 5:
          if (tag !== 40) {
            break;
          }

          message.failureHandlerThreadRunId = reader.int32();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create(base?: DeepPartial<WaitForThreadsRun_WaitForThread>): WaitForThreadsRun_WaitForThread {
    return WaitForThreadsRun_WaitForThread.fromPartial(base ?? {});
  },
  fromPartial(object: DeepPartial<WaitForThreadsRun_WaitForThread>): WaitForThreadsRun_WaitForThread {
    const message = createBaseWaitForThreadsRun_WaitForThread();
    message.threadEndTime = object.threadEndTime ?? undefined;
    message.threadStatus = object.threadStatus ?? LHStatus.STARTING;
    message.threadRunNumber = object.threadRunNumber ?? 0;
    message.waitingStatus = object.waitingStatus ?? WaitForThreadsRun_WaitingThreadStatus.THREAD_IN_PROGRESS;
    message.failureHandlerThreadRunId = object.failureHandlerThreadRunId ?? undefined;
    return message;
  },
};

function createBaseExternalEventRun(): ExternalEventRun {
  return { externalEventDefId: undefined, eventTime: undefined, externalEventId: undefined, timedOut: false };
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
    if (message.timedOut !== false) {
      writer.uint32(32).bool(message.timedOut);
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
        case 4:
          if (tag !== 32) {
            break;
          }

          message.timedOut = reader.bool();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create(base?: DeepPartial<ExternalEventRun>): ExternalEventRun {
    return ExternalEventRun.fromPartial(base ?? {});
  },
  fromPartial(object: DeepPartial<ExternalEventRun>): ExternalEventRun {
    const message = createBaseExternalEventRun();
    message.externalEventDefId = (object.externalEventDefId !== undefined && object.externalEventDefId !== null)
      ? ExternalEventDefId.fromPartial(object.externalEventDefId)
      : undefined;
    message.eventTime = object.eventTime ?? undefined;
    message.externalEventId = (object.externalEventId !== undefined && object.externalEventId !== null)
      ? ExternalEventId.fromPartial(object.externalEventId)
      : undefined;
    message.timedOut = object.timedOut ?? false;
    return message;
  },
};

function createBaseSleepNodeRun(): SleepNodeRun {
  return { maturationTime: undefined, matured: false };
}

export const SleepNodeRun = {
  encode(message: SleepNodeRun, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.maturationTime !== undefined) {
      Timestamp.encode(toTimestamp(message.maturationTime), writer.uint32(10).fork()).ldelim();
    }
    if (message.matured !== false) {
      writer.uint32(16).bool(message.matured);
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
        case 2:
          if (tag !== 16) {
            break;
          }

          message.matured = reader.bool();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create(base?: DeepPartial<SleepNodeRun>): SleepNodeRun {
    return SleepNodeRun.fromPartial(base ?? {});
  },
  fromPartial(object: DeepPartial<SleepNodeRun>): SleepNodeRun {
    const message = createBaseSleepNodeRun();
    message.maturationTime = object.maturationTime ?? undefined;
    message.matured = object.matured ?? false;
    return message;
  },
};

function createBaseFailure(): Failure {
  return {
    failureName: "",
    message: "",
    content: undefined,
    wasProperlyHandled: false,
    failureHandlerThreadrunId: undefined,
  };
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
    if (message.wasProperlyHandled !== false) {
      writer.uint32(32).bool(message.wasProperlyHandled);
    }
    if (message.failureHandlerThreadrunId !== undefined) {
      writer.uint32(40).int32(message.failureHandlerThreadrunId);
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
        case 5:
          if (tag !== 40) {
            break;
          }

          message.failureHandlerThreadrunId = reader.int32();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create(base?: DeepPartial<Failure>): Failure {
    return Failure.fromPartial(base ?? {});
  },
  fromPartial(object: DeepPartial<Failure>): Failure {
    const message = createBaseFailure();
    message.failureName = object.failureName ?? "";
    message.message = object.message ?? "";
    message.content = (object.content !== undefined && object.content !== null)
      ? VariableValue.fromPartial(object.content)
      : undefined;
    message.wasProperlyHandled = object.wasProperlyHandled ?? false;
    message.failureHandlerThreadrunId = object.failureHandlerThreadrunId ?? undefined;
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
