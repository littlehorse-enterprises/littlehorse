/* eslint-disable */
import * as _m0 from "protobufjs/minimal";
import { VariableType, variableTypeFromJSON, variableTypeToJSON, variableTypeToNumber } from "./common_enums";
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

export function userTaskRunStatusToJSON(object: UserTaskRunStatus): string {
  switch (object) {
    case UserTaskRunStatus.UNASSIGNED:
      return "UNASSIGNED";
    case UserTaskRunStatus.ASSIGNED:
      return "ASSIGNED";
    case UserTaskRunStatus.DONE:
      return "DONE";
    case UserTaskRunStatus.CANCELLED:
      return "CANCELLED";
    case UserTaskRunStatus.UNRECOGNIZED:
    default:
      return "UNRECOGNIZED";
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

  fromJSON(object: any): UserTaskDef {
    return {
      name: isSet(object.name) ? globalThis.String(object.name) : "",
      version: isSet(object.version) ? globalThis.Number(object.version) : 0,
      description: isSet(object.description) ? globalThis.String(object.description) : undefined,
      fields: globalThis.Array.isArray(object?.fields) ? object.fields.map((e: any) => UserTaskField.fromJSON(e)) : [],
      createdAt: isSet(object.createdAt) ? globalThis.String(object.createdAt) : undefined,
    };
  },

  toJSON(message: UserTaskDef): unknown {
    const obj: any = {};
    if (message.name !== "") {
      obj.name = message.name;
    }
    if (message.version !== 0) {
      obj.version = Math.round(message.version);
    }
    if (message.description !== undefined) {
      obj.description = message.description;
    }
    if (message.fields?.length) {
      obj.fields = message.fields.map((e) => UserTaskField.toJSON(e));
    }
    if (message.createdAt !== undefined) {
      obj.createdAt = message.createdAt;
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<UserTaskDef>, I>>(base?: I): UserTaskDef {
    return UserTaskDef.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<UserTaskDef>, I>>(object: I): UserTaskDef {
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

  fromJSON(object: any): UserTaskField {
    return {
      name: isSet(object.name) ? globalThis.String(object.name) : "",
      type: isSet(object.type) ? variableTypeFromJSON(object.type) : VariableType.JSON_OBJ,
      description: isSet(object.description) ? globalThis.String(object.description) : undefined,
      displayName: isSet(object.displayName) ? globalThis.String(object.displayName) : "",
      required: isSet(object.required) ? globalThis.Boolean(object.required) : false,
    };
  },

  toJSON(message: UserTaskField): unknown {
    const obj: any = {};
    if (message.name !== "") {
      obj.name = message.name;
    }
    if (message.type !== VariableType.JSON_OBJ) {
      obj.type = variableTypeToJSON(message.type);
    }
    if (message.description !== undefined) {
      obj.description = message.description;
    }
    if (message.displayName !== "") {
      obj.displayName = message.displayName;
    }
    if (message.required !== false) {
      obj.required = message.required;
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<UserTaskField>, I>>(base?: I): UserTaskField {
    return UserTaskField.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<UserTaskField>, I>>(object: I): UserTaskField {
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

  fromJSON(object: any): UserTaskRun {
    return {
      id: isSet(object.id) ? UserTaskRunId.fromJSON(object.id) : undefined,
      userTaskDefId: isSet(object.userTaskDefId) ? UserTaskDefId.fromJSON(object.userTaskDefId) : undefined,
      userGroup: isSet(object.userGroup) ? globalThis.String(object.userGroup) : undefined,
      userId: isSet(object.userId) ? globalThis.String(object.userId) : undefined,
      results: isObject(object.results)
        ? Object.entries(object.results).reduce<{ [key: string]: VariableValue }>((acc, [key, value]) => {
          acc[key] = VariableValue.fromJSON(value);
          return acc;
        }, {})
        : {},
      status: isSet(object.status) ? userTaskRunStatusFromJSON(object.status) : UserTaskRunStatus.UNASSIGNED,
      events: globalThis.Array.isArray(object?.events) ? object.events.map((e: any) => UserTaskEvent.fromJSON(e)) : [],
      notes: isSet(object.notes) ? globalThis.String(object.notes) : undefined,
      scheduledTime: isSet(object.scheduledTime) ? globalThis.String(object.scheduledTime) : undefined,
      nodeRunId: isSet(object.nodeRunId) ? NodeRunId.fromJSON(object.nodeRunId) : undefined,
      epoch: isSet(object.epoch) ? globalThis.Number(object.epoch) : 0,
    };
  },

  toJSON(message: UserTaskRun): unknown {
    const obj: any = {};
    if (message.id !== undefined) {
      obj.id = UserTaskRunId.toJSON(message.id);
    }
    if (message.userTaskDefId !== undefined) {
      obj.userTaskDefId = UserTaskDefId.toJSON(message.userTaskDefId);
    }
    if (message.userGroup !== undefined) {
      obj.userGroup = message.userGroup;
    }
    if (message.userId !== undefined) {
      obj.userId = message.userId;
    }
    if (message.results) {
      const entries = Object.entries(message.results);
      if (entries.length > 0) {
        obj.results = {};
        entries.forEach(([k, v]) => {
          obj.results[k] = VariableValue.toJSON(v);
        });
      }
    }
    if (message.status !== UserTaskRunStatus.UNASSIGNED) {
      obj.status = userTaskRunStatusToJSON(message.status);
    }
    if (message.events?.length) {
      obj.events = message.events.map((e) => UserTaskEvent.toJSON(e));
    }
    if (message.notes !== undefined) {
      obj.notes = message.notes;
    }
    if (message.scheduledTime !== undefined) {
      obj.scheduledTime = message.scheduledTime;
    }
    if (message.nodeRunId !== undefined) {
      obj.nodeRunId = NodeRunId.toJSON(message.nodeRunId);
    }
    if (message.epoch !== 0) {
      obj.epoch = Math.round(message.epoch);
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<UserTaskRun>, I>>(base?: I): UserTaskRun {
    return UserTaskRun.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<UserTaskRun>, I>>(object: I): UserTaskRun {
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

  fromJSON(object: any): UserTaskRun_ResultsEntry {
    return {
      key: isSet(object.key) ? globalThis.String(object.key) : "",
      value: isSet(object.value) ? VariableValue.fromJSON(object.value) : undefined,
    };
  },

  toJSON(message: UserTaskRun_ResultsEntry): unknown {
    const obj: any = {};
    if (message.key !== "") {
      obj.key = message.key;
    }
    if (message.value !== undefined) {
      obj.value = VariableValue.toJSON(message.value);
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<UserTaskRun_ResultsEntry>, I>>(base?: I): UserTaskRun_ResultsEntry {
    return UserTaskRun_ResultsEntry.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<UserTaskRun_ResultsEntry>, I>>(object: I): UserTaskRun_ResultsEntry {
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

  fromJSON(object: any): AssignUserTaskRunRequest {
    return {
      userTaskRunId: isSet(object.userTaskRunId) ? UserTaskRunId.fromJSON(object.userTaskRunId) : undefined,
      overrideClaim: isSet(object.overrideClaim) ? globalThis.Boolean(object.overrideClaim) : false,
      userGroup: isSet(object.userGroup) ? globalThis.String(object.userGroup) : undefined,
      userId: isSet(object.userId) ? globalThis.String(object.userId) : undefined,
    };
  },

  toJSON(message: AssignUserTaskRunRequest): unknown {
    const obj: any = {};
    if (message.userTaskRunId !== undefined) {
      obj.userTaskRunId = UserTaskRunId.toJSON(message.userTaskRunId);
    }
    if (message.overrideClaim !== false) {
      obj.overrideClaim = message.overrideClaim;
    }
    if (message.userGroup !== undefined) {
      obj.userGroup = message.userGroup;
    }
    if (message.userId !== undefined) {
      obj.userId = message.userId;
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<AssignUserTaskRunRequest>, I>>(base?: I): AssignUserTaskRunRequest {
    return AssignUserTaskRunRequest.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<AssignUserTaskRunRequest>, I>>(object: I): AssignUserTaskRunRequest {
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

  fromJSON(object: any): CompleteUserTaskRunRequest {
    return {
      userTaskRunId: isSet(object.userTaskRunId) ? UserTaskRunId.fromJSON(object.userTaskRunId) : undefined,
      results: isObject(object.results)
        ? Object.entries(object.results).reduce<{ [key: string]: VariableValue }>((acc, [key, value]) => {
          acc[key] = VariableValue.fromJSON(value);
          return acc;
        }, {})
        : {},
      userId: isSet(object.userId) ? globalThis.String(object.userId) : "",
    };
  },

  toJSON(message: CompleteUserTaskRunRequest): unknown {
    const obj: any = {};
    if (message.userTaskRunId !== undefined) {
      obj.userTaskRunId = UserTaskRunId.toJSON(message.userTaskRunId);
    }
    if (message.results) {
      const entries = Object.entries(message.results);
      if (entries.length > 0) {
        obj.results = {};
        entries.forEach(([k, v]) => {
          obj.results[k] = VariableValue.toJSON(v);
        });
      }
    }
    if (message.userId !== "") {
      obj.userId = message.userId;
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<CompleteUserTaskRunRequest>, I>>(base?: I): CompleteUserTaskRunRequest {
    return CompleteUserTaskRunRequest.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<CompleteUserTaskRunRequest>, I>>(object: I): CompleteUserTaskRunRequest {
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

  fromJSON(object: any): CompleteUserTaskRunRequest_ResultsEntry {
    return {
      key: isSet(object.key) ? globalThis.String(object.key) : "",
      value: isSet(object.value) ? VariableValue.fromJSON(object.value) : undefined,
    };
  },

  toJSON(message: CompleteUserTaskRunRequest_ResultsEntry): unknown {
    const obj: any = {};
    if (message.key !== "") {
      obj.key = message.key;
    }
    if (message.value !== undefined) {
      obj.value = VariableValue.toJSON(message.value);
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<CompleteUserTaskRunRequest_ResultsEntry>, I>>(
    base?: I,
  ): CompleteUserTaskRunRequest_ResultsEntry {
    return CompleteUserTaskRunRequest_ResultsEntry.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<CompleteUserTaskRunRequest_ResultsEntry>, I>>(
    object: I,
  ): CompleteUserTaskRunRequest_ResultsEntry {
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

  fromJSON(object: any): CancelUserTaskRunRequest {
    return { userTaskRunId: isSet(object.userTaskRunId) ? UserTaskRunId.fromJSON(object.userTaskRunId) : undefined };
  },

  toJSON(message: CancelUserTaskRunRequest): unknown {
    const obj: any = {};
    if (message.userTaskRunId !== undefined) {
      obj.userTaskRunId = UserTaskRunId.toJSON(message.userTaskRunId);
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<CancelUserTaskRunRequest>, I>>(base?: I): CancelUserTaskRunRequest {
    return CancelUserTaskRunRequest.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<CancelUserTaskRunRequest>, I>>(object: I): CancelUserTaskRunRequest {
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

  fromJSON(object: any): UserTaskTriggerReference {
    return {
      nodeRunId: isSet(object.nodeRunId) ? NodeRunId.fromJSON(object.nodeRunId) : undefined,
      userTaskEventNumber: isSet(object.userTaskEventNumber) ? globalThis.Number(object.userTaskEventNumber) : 0,
      userId: isSet(object.userId) ? globalThis.String(object.userId) : undefined,
      userGroup: isSet(object.userGroup) ? globalThis.String(object.userGroup) : undefined,
    };
  },

  toJSON(message: UserTaskTriggerReference): unknown {
    const obj: any = {};
    if (message.nodeRunId !== undefined) {
      obj.nodeRunId = NodeRunId.toJSON(message.nodeRunId);
    }
    if (message.userTaskEventNumber !== 0) {
      obj.userTaskEventNumber = Math.round(message.userTaskEventNumber);
    }
    if (message.userId !== undefined) {
      obj.userId = message.userId;
    }
    if (message.userGroup !== undefined) {
      obj.userGroup = message.userGroup;
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<UserTaskTriggerReference>, I>>(base?: I): UserTaskTriggerReference {
    return UserTaskTriggerReference.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<UserTaskTriggerReference>, I>>(object: I): UserTaskTriggerReference {
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

  fromJSON(object: any): UserTaskEvent {
    return {
      time: isSet(object.time) ? globalThis.String(object.time) : undefined,
      taskExecuted: isSet(object.taskExecuted)
        ? UserTaskEvent_UTETaskExecuted.fromJSON(object.taskExecuted)
        : undefined,
      assigned: isSet(object.assigned) ? UserTaskEvent_UTEAssigned.fromJSON(object.assigned) : undefined,
      cancelled: isSet(object.cancelled) ? UserTaskEvent_UTECancelled.fromJSON(object.cancelled) : undefined,
    };
  },

  toJSON(message: UserTaskEvent): unknown {
    const obj: any = {};
    if (message.time !== undefined) {
      obj.time = message.time;
    }
    if (message.taskExecuted !== undefined) {
      obj.taskExecuted = UserTaskEvent_UTETaskExecuted.toJSON(message.taskExecuted);
    }
    if (message.assigned !== undefined) {
      obj.assigned = UserTaskEvent_UTEAssigned.toJSON(message.assigned);
    }
    if (message.cancelled !== undefined) {
      obj.cancelled = UserTaskEvent_UTECancelled.toJSON(message.cancelled);
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<UserTaskEvent>, I>>(base?: I): UserTaskEvent {
    return UserTaskEvent.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<UserTaskEvent>, I>>(object: I): UserTaskEvent {
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

  fromJSON(object: any): UserTaskEvent_UTECancelled {
    return { message: isSet(object.message) ? globalThis.String(object.message) : "" };
  },

  toJSON(message: UserTaskEvent_UTECancelled): unknown {
    const obj: any = {};
    if (message.message !== "") {
      obj.message = message.message;
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<UserTaskEvent_UTECancelled>, I>>(base?: I): UserTaskEvent_UTECancelled {
    return UserTaskEvent_UTECancelled.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<UserTaskEvent_UTECancelled>, I>>(object: I): UserTaskEvent_UTECancelled {
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

  fromJSON(object: any): UserTaskEvent_UTETaskExecuted {
    return { taskRun: isSet(object.taskRun) ? TaskRunId.fromJSON(object.taskRun) : undefined };
  },

  toJSON(message: UserTaskEvent_UTETaskExecuted): unknown {
    const obj: any = {};
    if (message.taskRun !== undefined) {
      obj.taskRun = TaskRunId.toJSON(message.taskRun);
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<UserTaskEvent_UTETaskExecuted>, I>>(base?: I): UserTaskEvent_UTETaskExecuted {
    return UserTaskEvent_UTETaskExecuted.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<UserTaskEvent_UTETaskExecuted>, I>>(
    object: I,
  ): UserTaskEvent_UTETaskExecuted {
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

  fromJSON(object: any): UserTaskEvent_UTEAssigned {
    return {
      oldUserId: isSet(object.oldUserId) ? globalThis.String(object.oldUserId) : undefined,
      oldUserGroup: isSet(object.oldUserGroup) ? globalThis.String(object.oldUserGroup) : undefined,
      newUserId: isSet(object.newUserId) ? globalThis.String(object.newUserId) : undefined,
      newUserGroup: isSet(object.newUserGroup) ? globalThis.String(object.newUserGroup) : undefined,
    };
  },

  toJSON(message: UserTaskEvent_UTEAssigned): unknown {
    const obj: any = {};
    if (message.oldUserId !== undefined) {
      obj.oldUserId = message.oldUserId;
    }
    if (message.oldUserGroup !== undefined) {
      obj.oldUserGroup = message.oldUserGroup;
    }
    if (message.newUserId !== undefined) {
      obj.newUserId = message.newUserId;
    }
    if (message.newUserGroup !== undefined) {
      obj.newUserGroup = message.newUserGroup;
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<UserTaskEvent_UTEAssigned>, I>>(base?: I): UserTaskEvent_UTEAssigned {
    return UserTaskEvent_UTEAssigned.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<UserTaskEvent_UTEAssigned>, I>>(object: I): UserTaskEvent_UTEAssigned {
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

function isObject(value: any): boolean {
  return typeof value === "object" && value !== null;
}

function isSet(value: any): boolean {
  return value !== null && value !== undefined;
}
