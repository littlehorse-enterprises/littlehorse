/* eslint-disable */
import _m0 from "protobufjs/minimal";
import { VariableType, variableTypeFromJSON, variableTypeToNumber } from "./common_enums";
import { Timestamp } from "./google/protobuf/timestamp";
import { NodeRunId, TaskRunId, UserTaskDefId, UserTaskRunId } from "./object_id";
import { VariableValue } from "./variable";

export const protobufPackage = "littlehorse";

/** The status that a UserTaskRun can be in. */
export enum UserTaskRunStatus {
  /** UNASSIGNED - Not assigned to a specific user yet. */
  UNASSIGNED = "UNASSIGNED",
  /** ASSIGNED - Assigned to a specific user, but not completed or cancelled yet. */
  ASSIGNED = "ASSIGNED",
  /** DONE - Done. */
  DONE = "DONE",
  /** CANCELLED - Cancelled. */
  CANCELLED = "CANCELLED",
  UNRECOGNIZED = "UNRECOGNIZED",
}

export function userTaskRunStatusFromJSON(object: any): UserTaskRunStatus {
  switch (object) {
    case 0:
    case "UNASSIGNED":
      return UserTaskRunStatus.UNASSIGNED;
    case 1:
    case "ASSIGNED":
      return UserTaskRunStatus.ASSIGNED;
    case 3:
    case "DONE":
      return UserTaskRunStatus.DONE;
    case 4:
    case "CANCELLED":
      return UserTaskRunStatus.CANCELLED;
    case -1:
    case "UNRECOGNIZED":
    default:
      return UserTaskRunStatus.UNRECOGNIZED;
  }
}

export function userTaskRunStatusToNumber(object: UserTaskRunStatus): number {
  switch (object) {
    case UserTaskRunStatus.UNASSIGNED:
      return 0;
    case UserTaskRunStatus.ASSIGNED:
      return 1;
    case UserTaskRunStatus.DONE:
      return 3;
    case UserTaskRunStatus.CANCELLED:
      return 4;
    case UserTaskRunStatus.UNRECOGNIZED:
    default:
      return -1;
  }
}

/** UserTaskDef is the metadata blueprint for UserTaskRuns. */
export interface UserTaskDef {
  /** The name of the `UserTaskDef` */
  name: string;
  /** The version of the `UserTaskDef`. Only simple versioning is supported. */
  version: number;
  /**
   * Metadata field that does not impact WfRun execution. Useful for providing
   * context on the UserTaskRun, for example when displaying it on a general-purpose
   * task manager application.
   */
  description?:
    | string
    | undefined;
  /**
   * These are the fields comprise the User Task. A User Task Manager application, or
   * any application used to complete a UserTaskRun, should inspect these fields and
   * display form entries for each one.
   */
  fields: UserTaskField[];
  /** The time the UserTaskRun was created. */
  createdAt: string | undefined;
}

/** A UserTaskField is a specific field of data to be entered into a UserTaskRun. */
export interface UserTaskField {
  /**
   * The name of the field. When a UserTaskRun is completed, the NodeOutput is a
   * single-level JSON_OBJ. Each key is the name of the field. Must be unique.
   */
  name: string;
  /** The type of the output. Must be a primitive type (STR, BOOL, INT, DOUBLE). */
  type: VariableType;
  /**
   * Optional description which can be displayed by the User Task UI application.
   * Does not affect WfRun execution.
   */
  description?:
    | string
    | undefined;
  /**
   * The name to be displayed by the User Task UI application. Does not affect
   * WfRun execution.
   */
  displayName: string;
  /** Whether this field is required for UserTaskRun completion. */
  required: boolean;
}

/**
 * A UserTaskRun is a running instance of a UserTaskDef. It is created when a
 * ThreadRun arrives at a Node of type `USER_TASK`.
 */
export interface UserTaskRun {
  /** The ID of the UserTaskRun. */
  id:
    | UserTaskRunId
    | undefined;
  /** The ID of the UserTaskDef that this UserTaskRun comes from. */
  userTaskDefId:
    | UserTaskDefId
    | undefined;
  /**
   * The user_group to which this UserTaskRun is assigned. Not Set if not assigned
   * to a group. At least one of user_group or user_id will be set for any given
   * UserTaskRun.
   */
  userGroup?:
    | string
    | undefined;
  /**
   * The user_id to which this UserTaskRun is assigned. Not Set if not assigned
   * to a user. At least one of user_group or user_id will be set for any given
   * UserTaskRun. If user_id is set, then the UserTaskRun cannot be in the
   * UNASSIGNED status.
   */
  userId?:
    | string
    | undefined;
  /**
   * The results of the UserTaskRun. Empty if the UserTaskRun has not yet been completed.
   * Each key in this map is the `name` of a corresponding `UserTaskField` on the
   * UserTaskDef.
   */
  results: { [key: string]: VariableValue };
  /** Status of the UserTaskRun. Can be UNASSIGNED, ASSIGNED, DONE, or CANCELLED. */
  status: UserTaskRunStatus;
  /** A list of events that have happened. Used for auditing information. */
  events: UserTaskEvent[];
  /**
   * Notes about this UserTaskRun that are **specific to the WfRun**. These notes
   * are set by the WfSpec based on variables inside the specific `WfRun` and are
   * intended to be displayed on the User Task Manager application. They do not
   * affect WfRun execution.
   */
  notes?:
    | string
    | undefined;
  /** The time that the UserTaskRun was created/scheduled. */
  scheduledTime:
    | string
    | undefined;
  /** The NodeRun with which the UserTaskRun is associated. */
  nodeRunId:
    | NodeRunId
    | undefined;
  /**
   * Current observed epoch of the UserTaskRun, related to the number of times it has been
   * updated or re-assigned. Used internally to implement automated reassignment and reminder
   * tasks.
   */
  epoch: number;
}

export interface UserTaskRun_ResultsEntry {
  key: string;
  value: VariableValue | undefined;
}

/** Re-Assigns a UserTaskRun to a specific userId or userGroup. */
export interface AssignUserTaskRunRequest {
  /** The UserTaskRun to assign to a new user_id or user_group. */
  userTaskRunId:
    | UserTaskRunId
    | undefined;
  /**
   * If override_claim is set to false and the UserTaskRun is already assigned to
   * a user_id, then the request throws a FAILED_PRECONDITION error. If set to
   * true, then the old claim is overriden and the UserTaskRun is assigned to
   * the new user.
   */
  overrideClaim: boolean;
  /**
   * The new user_group to which the UserTaskRun is assigned. If not set, then
   * the user_group of the UserTaskRun is actively unset by this request. At least
   * one of the user_group and user_id must be set.
   */
  userGroup?:
    | string
    | undefined;
  /**
   * The new user_id to which the UserTaskRun is assigned. If not set, then
   * the user_id of the UserTaskRun is actively unset by this request. At least
   * one of the user_group and user_id must be set.
   */
  userId?: string | undefined;
}

/** Completes a UserTaskRun with provided values. */
export interface CompleteUserTaskRunRequest {
  /** The id of UserTaskRun to complete. */
  userTaskRunId:
    | UserTaskRunId
    | undefined;
  /**
   * A map from UserTaskField.name to a VariableValue containing the results of the
   * user filling out the form.
   */
  results: { [key: string]: VariableValue };
  /** The ID of the user who executed the task. */
  userId: string;
}

export interface CompleteUserTaskRunRequest_ResultsEntry {
  key: string;
  value: VariableValue | undefined;
}

/** Cancels a UserTaskRun. */
export interface CancelUserTaskRunRequest {
  /** The id of the UserTaskRun to cancel. */
  userTaskRunId: UserTaskRunId | undefined;
}

/**
 * All TaskRun's have a "trigger reference" which refers to the WfRun Element that
 * caused the TaskRun to be scheduled. For example, a TaskRun on a regular TASK_NODE
 * has a TaskNodeReference.
 *
 * The UserTaskTriggerReference serves as the "Trigger Reference" for a TaskRun that
 * was scheduled by a lifecycle hook on a UserTaskRun (eg. a reminder task).
 *
 * The UserTaskTriggerReference is most useful in the WorkerContext of the Task Worker
 * SDK, which allows the Task Method to determine where the TaskRun comes from.
 */
export interface UserTaskTriggerReference {
  /** Is the NodeRun that the UserTaskRun belongs to. */
  nodeRunId:
    | NodeRunId
    | undefined;
  /**
   * Is the index in the `events` field of the UserTaskRun that the TaskRun corresponds
   * to.
   */
  userTaskEventNumber: number;
  /**
   * Is the user_id that the UserTaskRun is assigned to. Unset if UserTaskRun is not
   * asigned to a specific user_id.
   */
  userId?:
    | string
    | undefined;
  /**
   * Is the user_id that the UserTaskRun is assigned to. Unset if UserTaskRun is not
   * asigned to a specific user_id.
   */
  userGroup?: string | undefined;
}

/**
 * This is an event stored in the audit log of a `UserTaskRun` purely for observability
 * purposes.
 */
export interface UserTaskEvent {
  /** the time the event occurred. */
  time:
    | string
    | undefined;
  /** Denotes that a TaskRun was scheduled via a trigger. */
  taskExecuted?:
    | UserTaskEvent_UTETaskExecuted
    | undefined;
  /** Denotes that the UserTaskRun was assigned. */
  assigned?:
    | UserTaskEvent_UTEAssigned
    | undefined;
  /** Denotes that the UserTaskRun was cancelled. */
  cancelled?: UserTaskEvent_UTECancelled | undefined;
}

/** Empty message used to denote that the `UserTaskRun` was cancelled. */
export interface UserTaskEvent_UTECancelled {
  message: string;
}

/** Message to denote that a `TaskRun` was scheduled by a trigger for this UserTaskRun. */
export interface UserTaskEvent_UTETaskExecuted {
  /** The `TaskRunId` of the scheduled `TaskRun` */
  taskRun: TaskRunId | undefined;
}

/** Message denoting that the UserTaskRun was assigned. */
export interface UserTaskEvent_UTEAssigned {
  /** The user_id before the ownership change, if set. */
  oldUserId?:
    | string
    | undefined;
  /** The user_group before the ownership change, if set. */
  oldUserGroup?:
    | string
    | undefined;
  /** The user_id after the ownership change, if set. */
  newUserId?:
    | string
    | undefined;
  /** The user_group after the ownership change, if set. */
  newUserGroup?: string | undefined;
}

function createBaseUserTaskDef(): UserTaskDef {
  return { name: "", version: 0, description: undefined, fields: [], createdAt: undefined };
}

export const UserTaskDef = {
  encode(message: UserTaskDef, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.name !== "") {
      writer.uint32(10).string(message.name);
    }
    if (message.version !== 0) {
      writer.uint32(16).int32(message.version);
    }
    if (message.description !== undefined) {
      writer.uint32(26).string(message.description);
    }
    for (const v of message.fields) {
      UserTaskField.encode(v!, writer.uint32(34).fork()).ldelim();
    }
    if (message.createdAt !== undefined) {
      Timestamp.encode(toTimestamp(message.createdAt), writer.uint32(42).fork()).ldelim();
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): UserTaskDef {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseUserTaskDef();
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
          if (tag !== 16) {
            break;
          }

          message.version = reader.int32();
          continue;
        case 3:
          if (tag !== 26) {
            break;
          }

          message.description = reader.string();
          continue;
        case 4:
          if (tag !== 34) {
            break;
          }

          message.fields.push(UserTaskField.decode(reader, reader.uint32()));
          continue;
        case 5:
          if (tag !== 42) {
            break;
          }

          message.createdAt = fromTimestamp(Timestamp.decode(reader, reader.uint32()));
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create(base?: DeepPartial<UserTaskDef>): UserTaskDef {
    return UserTaskDef.fromPartial(base ?? {});
  },
  fromPartial(object: DeepPartial<UserTaskDef>): UserTaskDef {
    const message = createBaseUserTaskDef();
    message.name = object.name ?? "";
    message.version = object.version ?? 0;
    message.description = object.description ?? undefined;
    message.fields = object.fields?.map((e) => UserTaskField.fromPartial(e)) || [];
    message.createdAt = object.createdAt ?? undefined;
    return message;
  },
};

function createBaseUserTaskField(): UserTaskField {
  return { name: "", type: VariableType.JSON_OBJ, description: undefined, displayName: "", required: false };
}

export const UserTaskField = {
  encode(message: UserTaskField, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.name !== "") {
      writer.uint32(10).string(message.name);
    }
    if (message.type !== VariableType.JSON_OBJ) {
      writer.uint32(16).int32(variableTypeToNumber(message.type));
    }
    if (message.description !== undefined) {
      writer.uint32(26).string(message.description);
    }
    if (message.displayName !== "") {
      writer.uint32(34).string(message.displayName);
    }
    if (message.required !== false) {
      writer.uint32(40).bool(message.required);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): UserTaskField {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseUserTaskField();
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
          if (tag !== 16) {
            break;
          }

          message.type = variableTypeFromJSON(reader.int32());
          continue;
        case 3:
          if (tag !== 26) {
            break;
          }

          message.description = reader.string();
          continue;
        case 4:
          if (tag !== 34) {
            break;
          }

          message.displayName = reader.string();
          continue;
        case 5:
          if (tag !== 40) {
            break;
          }

          message.required = reader.bool();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create(base?: DeepPartial<UserTaskField>): UserTaskField {
    return UserTaskField.fromPartial(base ?? {});
  },
  fromPartial(object: DeepPartial<UserTaskField>): UserTaskField {
    const message = createBaseUserTaskField();
    message.name = object.name ?? "";
    message.type = object.type ?? VariableType.JSON_OBJ;
    message.description = object.description ?? undefined;
    message.displayName = object.displayName ?? "";
    message.required = object.required ?? false;
    return message;
  },
};

function createBaseUserTaskRun(): UserTaskRun {
  return {
    id: undefined,
    userTaskDefId: undefined,
    userGroup: undefined,
    userId: undefined,
    results: {},
    status: UserTaskRunStatus.UNASSIGNED,
    events: [],
    notes: undefined,
    scheduledTime: undefined,
    nodeRunId: undefined,
    epoch: 0,
  };
}

export const UserTaskRun = {
  encode(message: UserTaskRun, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.id !== undefined) {
      UserTaskRunId.encode(message.id, writer.uint32(10).fork()).ldelim();
    }
    if (message.userTaskDefId !== undefined) {
      UserTaskDefId.encode(message.userTaskDefId, writer.uint32(18).fork()).ldelim();
    }
    if (message.userGroup !== undefined) {
      writer.uint32(26).string(message.userGroup);
    }
    if (message.userId !== undefined) {
      writer.uint32(34).string(message.userId);
    }
    Object.entries(message.results).forEach(([key, value]) => {
      UserTaskRun_ResultsEntry.encode({ key: key as any, value }, writer.uint32(50).fork()).ldelim();
    });
    if (message.status !== UserTaskRunStatus.UNASSIGNED) {
      writer.uint32(56).int32(userTaskRunStatusToNumber(message.status));
    }
    for (const v of message.events) {
      UserTaskEvent.encode(v!, writer.uint32(66).fork()).ldelim();
    }
    if (message.notes !== undefined) {
      writer.uint32(74).string(message.notes);
    }
    if (message.scheduledTime !== undefined) {
      Timestamp.encode(toTimestamp(message.scheduledTime), writer.uint32(82).fork()).ldelim();
    }
    if (message.nodeRunId !== undefined) {
      NodeRunId.encode(message.nodeRunId, writer.uint32(90).fork()).ldelim();
    }
    if (message.epoch !== 0) {
      writer.uint32(96).int32(message.epoch);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): UserTaskRun {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseUserTaskRun();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.id = UserTaskRunId.decode(reader, reader.uint32());
          continue;
        case 2:
          if (tag !== 18) {
            break;
          }

          message.userTaskDefId = UserTaskDefId.decode(reader, reader.uint32());
          continue;
        case 3:
          if (tag !== 26) {
            break;
          }

          message.userGroup = reader.string();
          continue;
        case 4:
          if (tag !== 34) {
            break;
          }

          message.userId = reader.string();
          continue;
        case 6:
          if (tag !== 50) {
            break;
          }

          const entry6 = UserTaskRun_ResultsEntry.decode(reader, reader.uint32());
          if (entry6.value !== undefined) {
            message.results[entry6.key] = entry6.value;
          }
          continue;
        case 7:
          if (tag !== 56) {
            break;
          }

          message.status = userTaskRunStatusFromJSON(reader.int32());
          continue;
        case 8:
          if (tag !== 66) {
            break;
          }

          message.events.push(UserTaskEvent.decode(reader, reader.uint32()));
          continue;
        case 9:
          if (tag !== 74) {
            break;
          }

          message.notes = reader.string();
          continue;
        case 10:
          if (tag !== 82) {
            break;
          }

          message.scheduledTime = fromTimestamp(Timestamp.decode(reader, reader.uint32()));
          continue;
        case 11:
          if (tag !== 90) {
            break;
          }

          message.nodeRunId = NodeRunId.decode(reader, reader.uint32());
          continue;
        case 12:
          if (tag !== 96) {
            break;
          }

          message.epoch = reader.int32();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create(base?: DeepPartial<UserTaskRun>): UserTaskRun {
    return UserTaskRun.fromPartial(base ?? {});
  },
  fromPartial(object: DeepPartial<UserTaskRun>): UserTaskRun {
    const message = createBaseUserTaskRun();
    message.id = (object.id !== undefined && object.id !== null) ? UserTaskRunId.fromPartial(object.id) : undefined;
    message.userTaskDefId = (object.userTaskDefId !== undefined && object.userTaskDefId !== null)
      ? UserTaskDefId.fromPartial(object.userTaskDefId)
      : undefined;
    message.userGroup = object.userGroup ?? undefined;
    message.userId = object.userId ?? undefined;
    message.results = Object.entries(object.results ?? {}).reduce<{ [key: string]: VariableValue }>(
      (acc, [key, value]) => {
        if (value !== undefined) {
          acc[key] = VariableValue.fromPartial(value);
        }
        return acc;
      },
      {},
    );
    message.status = object.status ?? UserTaskRunStatus.UNASSIGNED;
    message.events = object.events?.map((e) => UserTaskEvent.fromPartial(e)) || [];
    message.notes = object.notes ?? undefined;
    message.scheduledTime = object.scheduledTime ?? undefined;
    message.nodeRunId = (object.nodeRunId !== undefined && object.nodeRunId !== null)
      ? NodeRunId.fromPartial(object.nodeRunId)
      : undefined;
    message.epoch = object.epoch ?? 0;
    return message;
  },
};

function createBaseUserTaskRun_ResultsEntry(): UserTaskRun_ResultsEntry {
  return { key: "", value: undefined };
}

export const UserTaskRun_ResultsEntry = {
  encode(message: UserTaskRun_ResultsEntry, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.key !== "") {
      writer.uint32(10).string(message.key);
    }
    if (message.value !== undefined) {
      VariableValue.encode(message.value, writer.uint32(18).fork()).ldelim();
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): UserTaskRun_ResultsEntry {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseUserTaskRun_ResultsEntry();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.key = reader.string();
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

  create(base?: DeepPartial<UserTaskRun_ResultsEntry>): UserTaskRun_ResultsEntry {
    return UserTaskRun_ResultsEntry.fromPartial(base ?? {});
  },
  fromPartial(object: DeepPartial<UserTaskRun_ResultsEntry>): UserTaskRun_ResultsEntry {
    const message = createBaseUserTaskRun_ResultsEntry();
    message.key = object.key ?? "";
    message.value = (object.value !== undefined && object.value !== null)
      ? VariableValue.fromPartial(object.value)
      : undefined;
    return message;
  },
};

function createBaseAssignUserTaskRunRequest(): AssignUserTaskRunRequest {
  return { userTaskRunId: undefined, overrideClaim: false, userGroup: undefined, userId: undefined };
}

export const AssignUserTaskRunRequest = {
  encode(message: AssignUserTaskRunRequest, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.userTaskRunId !== undefined) {
      UserTaskRunId.encode(message.userTaskRunId, writer.uint32(10).fork()).ldelim();
    }
    if (message.overrideClaim !== false) {
      writer.uint32(16).bool(message.overrideClaim);
    }
    if (message.userGroup !== undefined) {
      writer.uint32(26).string(message.userGroup);
    }
    if (message.userId !== undefined) {
      writer.uint32(34).string(message.userId);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): AssignUserTaskRunRequest {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseAssignUserTaskRunRequest();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.userTaskRunId = UserTaskRunId.decode(reader, reader.uint32());
          continue;
        case 2:
          if (tag !== 16) {
            break;
          }

          message.overrideClaim = reader.bool();
          continue;
        case 3:
          if (tag !== 26) {
            break;
          }

          message.userGroup = reader.string();
          continue;
        case 4:
          if (tag !== 34) {
            break;
          }

          message.userId = reader.string();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create(base?: DeepPartial<AssignUserTaskRunRequest>): AssignUserTaskRunRequest {
    return AssignUserTaskRunRequest.fromPartial(base ?? {});
  },
  fromPartial(object: DeepPartial<AssignUserTaskRunRequest>): AssignUserTaskRunRequest {
    const message = createBaseAssignUserTaskRunRequest();
    message.userTaskRunId = (object.userTaskRunId !== undefined && object.userTaskRunId !== null)
      ? UserTaskRunId.fromPartial(object.userTaskRunId)
      : undefined;
    message.overrideClaim = object.overrideClaim ?? false;
    message.userGroup = object.userGroup ?? undefined;
    message.userId = object.userId ?? undefined;
    return message;
  },
};

function createBaseCompleteUserTaskRunRequest(): CompleteUserTaskRunRequest {
  return { userTaskRunId: undefined, results: {}, userId: "" };
}

export const CompleteUserTaskRunRequest = {
  encode(message: CompleteUserTaskRunRequest, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.userTaskRunId !== undefined) {
      UserTaskRunId.encode(message.userTaskRunId, writer.uint32(10).fork()).ldelim();
    }
    Object.entries(message.results).forEach(([key, value]) => {
      CompleteUserTaskRunRequest_ResultsEntry.encode({ key: key as any, value }, writer.uint32(18).fork()).ldelim();
    });
    if (message.userId !== "") {
      writer.uint32(26).string(message.userId);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): CompleteUserTaskRunRequest {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseCompleteUserTaskRunRequest();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.userTaskRunId = UserTaskRunId.decode(reader, reader.uint32());
          continue;
        case 2:
          if (tag !== 18) {
            break;
          }

          const entry2 = CompleteUserTaskRunRequest_ResultsEntry.decode(reader, reader.uint32());
          if (entry2.value !== undefined) {
            message.results[entry2.key] = entry2.value;
          }
          continue;
        case 3:
          if (tag !== 26) {
            break;
          }

          message.userId = reader.string();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create(base?: DeepPartial<CompleteUserTaskRunRequest>): CompleteUserTaskRunRequest {
    return CompleteUserTaskRunRequest.fromPartial(base ?? {});
  },
  fromPartial(object: DeepPartial<CompleteUserTaskRunRequest>): CompleteUserTaskRunRequest {
    const message = createBaseCompleteUserTaskRunRequest();
    message.userTaskRunId = (object.userTaskRunId !== undefined && object.userTaskRunId !== null)
      ? UserTaskRunId.fromPartial(object.userTaskRunId)
      : undefined;
    message.results = Object.entries(object.results ?? {}).reduce<{ [key: string]: VariableValue }>(
      (acc, [key, value]) => {
        if (value !== undefined) {
          acc[key] = VariableValue.fromPartial(value);
        }
        return acc;
      },
      {},
    );
    message.userId = object.userId ?? "";
    return message;
  },
};

function createBaseCompleteUserTaskRunRequest_ResultsEntry(): CompleteUserTaskRunRequest_ResultsEntry {
  return { key: "", value: undefined };
}

export const CompleteUserTaskRunRequest_ResultsEntry = {
  encode(message: CompleteUserTaskRunRequest_ResultsEntry, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.key !== "") {
      writer.uint32(10).string(message.key);
    }
    if (message.value !== undefined) {
      VariableValue.encode(message.value, writer.uint32(18).fork()).ldelim();
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): CompleteUserTaskRunRequest_ResultsEntry {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseCompleteUserTaskRunRequest_ResultsEntry();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.key = reader.string();
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

  create(base?: DeepPartial<CompleteUserTaskRunRequest_ResultsEntry>): CompleteUserTaskRunRequest_ResultsEntry {
    return CompleteUserTaskRunRequest_ResultsEntry.fromPartial(base ?? {});
  },
  fromPartial(object: DeepPartial<CompleteUserTaskRunRequest_ResultsEntry>): CompleteUserTaskRunRequest_ResultsEntry {
    const message = createBaseCompleteUserTaskRunRequest_ResultsEntry();
    message.key = object.key ?? "";
    message.value = (object.value !== undefined && object.value !== null)
      ? VariableValue.fromPartial(object.value)
      : undefined;
    return message;
  },
};

function createBaseCancelUserTaskRunRequest(): CancelUserTaskRunRequest {
  return { userTaskRunId: undefined };
}

export const CancelUserTaskRunRequest = {
  encode(message: CancelUserTaskRunRequest, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.userTaskRunId !== undefined) {
      UserTaskRunId.encode(message.userTaskRunId, writer.uint32(10).fork()).ldelim();
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): CancelUserTaskRunRequest {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseCancelUserTaskRunRequest();
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

  create(base?: DeepPartial<CancelUserTaskRunRequest>): CancelUserTaskRunRequest {
    return CancelUserTaskRunRequest.fromPartial(base ?? {});
  },
  fromPartial(object: DeepPartial<CancelUserTaskRunRequest>): CancelUserTaskRunRequest {
    const message = createBaseCancelUserTaskRunRequest();
    message.userTaskRunId = (object.userTaskRunId !== undefined && object.userTaskRunId !== null)
      ? UserTaskRunId.fromPartial(object.userTaskRunId)
      : undefined;
    return message;
  },
};

function createBaseUserTaskTriggerReference(): UserTaskTriggerReference {
  return { nodeRunId: undefined, userTaskEventNumber: 0, userId: undefined, userGroup: undefined };
}

export const UserTaskTriggerReference = {
  encode(message: UserTaskTriggerReference, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.nodeRunId !== undefined) {
      NodeRunId.encode(message.nodeRunId, writer.uint32(10).fork()).ldelim();
    }
    if (message.userTaskEventNumber !== 0) {
      writer.uint32(16).int32(message.userTaskEventNumber);
    }
    if (message.userId !== undefined) {
      writer.uint32(26).string(message.userId);
    }
    if (message.userGroup !== undefined) {
      writer.uint32(34).string(message.userGroup);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): UserTaskTriggerReference {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseUserTaskTriggerReference();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.nodeRunId = NodeRunId.decode(reader, reader.uint32());
          continue;
        case 2:
          if (tag !== 16) {
            break;
          }

          message.userTaskEventNumber = reader.int32();
          continue;
        case 3:
          if (tag !== 26) {
            break;
          }

          message.userId = reader.string();
          continue;
        case 4:
          if (tag !== 34) {
            break;
          }

          message.userGroup = reader.string();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create(base?: DeepPartial<UserTaskTriggerReference>): UserTaskTriggerReference {
    return UserTaskTriggerReference.fromPartial(base ?? {});
  },
  fromPartial(object: DeepPartial<UserTaskTriggerReference>): UserTaskTriggerReference {
    const message = createBaseUserTaskTriggerReference();
    message.nodeRunId = (object.nodeRunId !== undefined && object.nodeRunId !== null)
      ? NodeRunId.fromPartial(object.nodeRunId)
      : undefined;
    message.userTaskEventNumber = object.userTaskEventNumber ?? 0;
    message.userId = object.userId ?? undefined;
    message.userGroup = object.userGroup ?? undefined;
    return message;
  },
};

function createBaseUserTaskEvent(): UserTaskEvent {
  return { time: undefined, taskExecuted: undefined, assigned: undefined, cancelled: undefined };
}

export const UserTaskEvent = {
  encode(message: UserTaskEvent, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.time !== undefined) {
      Timestamp.encode(toTimestamp(message.time), writer.uint32(10).fork()).ldelim();
    }
    if (message.taskExecuted !== undefined) {
      UserTaskEvent_UTETaskExecuted.encode(message.taskExecuted, writer.uint32(18).fork()).ldelim();
    }
    if (message.assigned !== undefined) {
      UserTaskEvent_UTEAssigned.encode(message.assigned, writer.uint32(26).fork()).ldelim();
    }
    if (message.cancelled !== undefined) {
      UserTaskEvent_UTECancelled.encode(message.cancelled, writer.uint32(34).fork()).ldelim();
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): UserTaskEvent {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseUserTaskEvent();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.time = fromTimestamp(Timestamp.decode(reader, reader.uint32()));
          continue;
        case 2:
          if (tag !== 18) {
            break;
          }

          message.taskExecuted = UserTaskEvent_UTETaskExecuted.decode(reader, reader.uint32());
          continue;
        case 3:
          if (tag !== 26) {
            break;
          }

          message.assigned = UserTaskEvent_UTEAssigned.decode(reader, reader.uint32());
          continue;
        case 4:
          if (tag !== 34) {
            break;
          }

          message.cancelled = UserTaskEvent_UTECancelled.decode(reader, reader.uint32());
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create(base?: DeepPartial<UserTaskEvent>): UserTaskEvent {
    return UserTaskEvent.fromPartial(base ?? {});
  },
  fromPartial(object: DeepPartial<UserTaskEvent>): UserTaskEvent {
    const message = createBaseUserTaskEvent();
    message.time = object.time ?? undefined;
    message.taskExecuted = (object.taskExecuted !== undefined && object.taskExecuted !== null)
      ? UserTaskEvent_UTETaskExecuted.fromPartial(object.taskExecuted)
      : undefined;
    message.assigned = (object.assigned !== undefined && object.assigned !== null)
      ? UserTaskEvent_UTEAssigned.fromPartial(object.assigned)
      : undefined;
    message.cancelled = (object.cancelled !== undefined && object.cancelled !== null)
      ? UserTaskEvent_UTECancelled.fromPartial(object.cancelled)
      : undefined;
    return message;
  },
};

function createBaseUserTaskEvent_UTECancelled(): UserTaskEvent_UTECancelled {
  return { message: "" };
}

export const UserTaskEvent_UTECancelled = {
  encode(message: UserTaskEvent_UTECancelled, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.message !== "") {
      writer.uint32(10).string(message.message);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): UserTaskEvent_UTECancelled {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseUserTaskEvent_UTECancelled();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
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

  create(base?: DeepPartial<UserTaskEvent_UTECancelled>): UserTaskEvent_UTECancelled {
    return UserTaskEvent_UTECancelled.fromPartial(base ?? {});
  },
  fromPartial(object: DeepPartial<UserTaskEvent_UTECancelled>): UserTaskEvent_UTECancelled {
    const message = createBaseUserTaskEvent_UTECancelled();
    message.message = object.message ?? "";
    return message;
  },
};

function createBaseUserTaskEvent_UTETaskExecuted(): UserTaskEvent_UTETaskExecuted {
  return { taskRun: undefined };
}

export const UserTaskEvent_UTETaskExecuted = {
  encode(message: UserTaskEvent_UTETaskExecuted, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.taskRun !== undefined) {
      TaskRunId.encode(message.taskRun, writer.uint32(10).fork()).ldelim();
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): UserTaskEvent_UTETaskExecuted {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseUserTaskEvent_UTETaskExecuted();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.taskRun = TaskRunId.decode(reader, reader.uint32());
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create(base?: DeepPartial<UserTaskEvent_UTETaskExecuted>): UserTaskEvent_UTETaskExecuted {
    return UserTaskEvent_UTETaskExecuted.fromPartial(base ?? {});
  },
  fromPartial(object: DeepPartial<UserTaskEvent_UTETaskExecuted>): UserTaskEvent_UTETaskExecuted {
    const message = createBaseUserTaskEvent_UTETaskExecuted();
    message.taskRun = (object.taskRun !== undefined && object.taskRun !== null)
      ? TaskRunId.fromPartial(object.taskRun)
      : undefined;
    return message;
  },
};

function createBaseUserTaskEvent_UTEAssigned(): UserTaskEvent_UTEAssigned {
  return { oldUserId: undefined, oldUserGroup: undefined, newUserId: undefined, newUserGroup: undefined };
}

export const UserTaskEvent_UTEAssigned = {
  encode(message: UserTaskEvent_UTEAssigned, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.oldUserId !== undefined) {
      writer.uint32(10).string(message.oldUserId);
    }
    if (message.oldUserGroup !== undefined) {
      writer.uint32(18).string(message.oldUserGroup);
    }
    if (message.newUserId !== undefined) {
      writer.uint32(26).string(message.newUserId);
    }
    if (message.newUserGroup !== undefined) {
      writer.uint32(34).string(message.newUserGroup);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): UserTaskEvent_UTEAssigned {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseUserTaskEvent_UTEAssigned();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.oldUserId = reader.string();
          continue;
        case 2:
          if (tag !== 18) {
            break;
          }

          message.oldUserGroup = reader.string();
          continue;
        case 3:
          if (tag !== 26) {
            break;
          }

          message.newUserId = reader.string();
          continue;
        case 4:
          if (tag !== 34) {
            break;
          }

          message.newUserGroup = reader.string();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create(base?: DeepPartial<UserTaskEvent_UTEAssigned>): UserTaskEvent_UTEAssigned {
    return UserTaskEvent_UTEAssigned.fromPartial(base ?? {});
  },
  fromPartial(object: DeepPartial<UserTaskEvent_UTEAssigned>): UserTaskEvent_UTEAssigned {
    const message = createBaseUserTaskEvent_UTEAssigned();
    message.oldUserId = object.oldUserId ?? undefined;
    message.oldUserGroup = object.oldUserGroup ?? undefined;
    message.newUserId = object.newUserId ?? undefined;
    message.newUserGroup = object.newUserGroup ?? undefined;
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
