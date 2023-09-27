/* eslint-disable */
import * as _m0 from "protobufjs/minimal";
import { VariableType, variableTypeFromJSON, variableTypeToJSON } from "./common_enums";
import { Timestamp } from "./google/protobuf/timestamp";
import { NodeRunId, TaskRunId, UserTaskDefId, UserTaskRunId, WfSpecId } from "./object_id";
import { VariableValue } from "./variable";

export const protobufPackage = "littlehorse";

export enum UserTaskRunStatus {
  UNASSIGNED = 0,
  ASSIGNED = 1,
  DONE = 3,
  CANCELLED = 4,
  UNRECOGNIZED = -1,
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

export interface UserTaskDef {
  name: string;
  version: number;
  description?: string | undefined;
  fields: UserTaskField[];
  createdAt: Date | undefined;
}

export interface UserTaskField {
  name: string;
  type: VariableType;
  description?: string | undefined;
  displayName: string;
  /**
   * Later versions will allow stuff such as:
   * 1. Validation (eg. email address, integer between 1-10, etc)
   * 2. Nested object structures
   * 3. Multi-Page forms (survey-js style)
   * 4. Conditional rendering of forms based on input (surveyjs style)
   * 5. Default values and optional fields
   */
  required: boolean;
}

export interface UserTaskRun {
  id: UserTaskRunId | undefined;
  userTaskDefId: UserTaskDefId | undefined;
  userGroup?: string | undefined;
  userId?: string | undefined;
  results: { [key: string]: VariableValue };
  status: UserTaskRunStatus;
  events: UserTaskEvent[];
  notes?: string | undefined;
  scheduledTime:
    | Date
    | undefined;
  /**
   * If we ever allow ad-hoc User Tasks, this will move to an optional
   * field, or a `oneof user_task_source` field. However, note that such
   * a change would be fine from the API Compatibility perspective.
   */
  nodeRunId: NodeRunId | undefined;
}

export interface UserTaskRun_ResultsEntry {
  key: string;
  value: VariableValue | undefined;
}

export interface AssignUserTaskRunRequest {
  userTaskRunId: UserTaskRunId | undefined;
  overrideClaim: boolean;
  userGroup?: string | undefined;
  userId?: string | undefined;
}

export interface CompleteUserTaskRunRequest {
  userTaskRunId: UserTaskRunId | undefined;
  results: { [key: string]: VariableValue };
  userId: string;
}

export interface CompleteUserTaskRunRequest_ResultsEntry {
  key: string;
  value: VariableValue | undefined;
}

export interface CancelUserTaskRunRequest {
  userTaskRunId: UserTaskRunId | undefined;
}

export interface UserTaskTriggerReference {
  nodeRunId: NodeRunId | undefined;
  userTaskEventNumber: number;
  wfSpecId: WfSpecId | undefined;
  userId?: string | undefined;
  userGroup?: string | undefined;
}

export interface UserTaskEvent {
  time: Date | undefined;
  taskExecuted?: UserTaskEvent_UTETaskExecuted | undefined;
  assigned?:
    | UserTaskEvent_UTEAssigned
    | undefined;
  /**
   * TODO: Add "save user task" and "complete user task" to the
   * audit log
   */
  cancelled?: UserTaskEvent_UTECancelled | undefined;
}

export interface UserTaskEvent_UTECancelled {
}

export interface UserTaskEvent_UTETaskExecuted {
  taskRun: TaskRunId | undefined;
}

export interface UserTaskEvent_UTEAssigned {
  oldUserId?: string | undefined;
  oldUserGroup?: string | undefined;
  newUserId?: string | undefined;
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
      name: isSet(object.name) ? String(object.name) : "",
      version: isSet(object.version) ? Number(object.version) : 0,
      description: isSet(object.description) ? String(object.description) : undefined,
      fields: Array.isArray(object?.fields) ? object.fields.map((e: any) => UserTaskField.fromJSON(e)) : [],
      createdAt: isSet(object.createdAt) ? fromJsonTimestamp(object.createdAt) : undefined,
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
      obj.createdAt = message.createdAt.toISOString();
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
  return { name: "", type: 0, description: undefined, displayName: "", required: false };
}

export const UserTaskField = {
  encode(message: UserTaskField, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.name !== "") {
      writer.uint32(10).string(message.name);
    }
    if (message.type !== 0) {
      writer.uint32(16).int32(message.type);
    }
    if (message.description !== undefined) {
      writer.uint32(26).string(message.description);
    }
    if (message.displayName !== "") {
      writer.uint32(34).string(message.displayName);
    }
    if (message.required === true) {
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

          message.type = reader.int32() as any;
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
      name: isSet(object.name) ? String(object.name) : "",
      type: isSet(object.type) ? variableTypeFromJSON(object.type) : 0,
      description: isSet(object.description) ? String(object.description) : undefined,
      displayName: isSet(object.displayName) ? String(object.displayName) : "",
      required: isSet(object.required) ? Boolean(object.required) : false,
    };
  },

  toJSON(message: UserTaskField): unknown {
    const obj: any = {};
    if (message.name !== "") {
      obj.name = message.name;
    }
    if (message.type !== 0) {
      obj.type = variableTypeToJSON(message.type);
    }
    if (message.description !== undefined) {
      obj.description = message.description;
    }
    if (message.displayName !== "") {
      obj.displayName = message.displayName;
    }
    if (message.required === true) {
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
    message.type = object.type ?? 0;
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
    status: 0,
    events: [],
    notes: undefined,
    scheduledTime: undefined,
    nodeRunId: undefined,
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
    if (message.status !== 0) {
      writer.uint32(56).int32(message.status);
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

          message.status = reader.int32() as any;
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
      userGroup: isSet(object.userGroup) ? String(object.userGroup) : undefined,
      userId: isSet(object.userId) ? String(object.userId) : undefined,
      results: isObject(object.results)
        ? Object.entries(object.results).reduce<{ [key: string]: VariableValue }>((acc, [key, value]) => {
          acc[key] = VariableValue.fromJSON(value);
          return acc;
        }, {})
        : {},
      status: isSet(object.status) ? userTaskRunStatusFromJSON(object.status) : 0,
      events: Array.isArray(object?.events) ? object.events.map((e: any) => UserTaskEvent.fromJSON(e)) : [],
      notes: isSet(object.notes) ? String(object.notes) : undefined,
      scheduledTime: isSet(object.scheduledTime) ? fromJsonTimestamp(object.scheduledTime) : undefined,
      nodeRunId: isSet(object.nodeRunId) ? NodeRunId.fromJSON(object.nodeRunId) : undefined,
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
    if (message.status !== 0) {
      obj.status = userTaskRunStatusToJSON(message.status);
    }
    if (message.events?.length) {
      obj.events = message.events.map((e) => UserTaskEvent.toJSON(e));
    }
    if (message.notes !== undefined) {
      obj.notes = message.notes;
    }
    if (message.scheduledTime !== undefined) {
      obj.scheduledTime = message.scheduledTime.toISOString();
    }
    if (message.nodeRunId !== undefined) {
      obj.nodeRunId = NodeRunId.toJSON(message.nodeRunId);
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
    message.status = object.status ?? 0;
    message.events = object.events?.map((e) => UserTaskEvent.fromPartial(e)) || [];
    message.notes = object.notes ?? undefined;
    message.scheduledTime = object.scheduledTime ?? undefined;
    message.nodeRunId = (object.nodeRunId !== undefined && object.nodeRunId !== null)
      ? NodeRunId.fromPartial(object.nodeRunId)
      : undefined;
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
      key: isSet(object.key) ? String(object.key) : "",
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
    if (message.overrideClaim === true) {
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
      overrideClaim: isSet(object.overrideClaim) ? Boolean(object.overrideClaim) : false,
      userGroup: isSet(object.userGroup) ? String(object.userGroup) : undefined,
      userId: isSet(object.userId) ? String(object.userId) : undefined,
    };
  },

  toJSON(message: AssignUserTaskRunRequest): unknown {
    const obj: any = {};
    if (message.userTaskRunId !== undefined) {
      obj.userTaskRunId = UserTaskRunId.toJSON(message.userTaskRunId);
    }
    if (message.overrideClaim === true) {
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
      userId: isSet(object.userId) ? String(object.userId) : "",
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
      key: isSet(object.key) ? String(object.key) : "",
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
  return { nodeRunId: undefined, userTaskEventNumber: 0, wfSpecId: undefined, userId: undefined, userGroup: undefined };
}

export const UserTaskTriggerReference = {
  encode(message: UserTaskTriggerReference, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.nodeRunId !== undefined) {
      NodeRunId.encode(message.nodeRunId, writer.uint32(10).fork()).ldelim();
    }
    if (message.userTaskEventNumber !== 0) {
      writer.uint32(16).int32(message.userTaskEventNumber);
    }
    if (message.wfSpecId !== undefined) {
      WfSpecId.encode(message.wfSpecId, writer.uint32(26).fork()).ldelim();
    }
    if (message.userId !== undefined) {
      writer.uint32(34).string(message.userId);
    }
    if (message.userGroup !== undefined) {
      writer.uint32(42).string(message.userGroup);
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

          message.wfSpecId = WfSpecId.decode(reader, reader.uint32());
          continue;
        case 4:
          if (tag !== 34) {
            break;
          }

          message.userId = reader.string();
          continue;
        case 5:
          if (tag !== 42) {
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
      userTaskEventNumber: isSet(object.userTaskEventNumber) ? Number(object.userTaskEventNumber) : 0,
      wfSpecId: isSet(object.wfSpecId) ? WfSpecId.fromJSON(object.wfSpecId) : undefined,
      userId: isSet(object.userId) ? String(object.userId) : undefined,
      userGroup: isSet(object.userGroup) ? String(object.userGroup) : undefined,
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
    if (message.wfSpecId !== undefined) {
      obj.wfSpecId = WfSpecId.toJSON(message.wfSpecId);
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
    message.wfSpecId = (object.wfSpecId !== undefined && object.wfSpecId !== null)
      ? WfSpecId.fromPartial(object.wfSpecId)
      : undefined;
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
      time: isSet(object.time) ? fromJsonTimestamp(object.time) : undefined,
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
      obj.time = message.time.toISOString();
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
  return {};
}

export const UserTaskEvent_UTECancelled = {
  encode(_: UserTaskEvent_UTECancelled, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): UserTaskEvent_UTECancelled {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseUserTaskEvent_UTECancelled();
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

  fromJSON(_: any): UserTaskEvent_UTECancelled {
    return {};
  },

  toJSON(_: UserTaskEvent_UTECancelled): unknown {
    const obj: any = {};
    return obj;
  },

  create<I extends Exact<DeepPartial<UserTaskEvent_UTECancelled>, I>>(base?: I): UserTaskEvent_UTECancelled {
    return UserTaskEvent_UTECancelled.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<UserTaskEvent_UTECancelled>, I>>(_: I): UserTaskEvent_UTECancelled {
    const message = createBaseUserTaskEvent_UTECancelled();
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
      oldUserId: isSet(object.oldUserId) ? String(object.oldUserId) : undefined,
      oldUserGroup: isSet(object.oldUserGroup) ? String(object.oldUserGroup) : undefined,
      newUserId: isSet(object.newUserId) ? String(object.newUserId) : undefined,
      newUserGroup: isSet(object.newUserGroup) ? String(object.newUserGroup) : undefined,
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
  : T extends Array<infer U> ? Array<DeepPartial<U>> : T extends ReadonlyArray<infer U> ? ReadonlyArray<DeepPartial<U>>
  : T extends {} ? { [K in keyof T]?: DeepPartial<T[K]> }
  : Partial<T>;

type KeysOfUnion<T> = T extends T ? keyof T : never;
export type Exact<P, I extends P> = P extends Builtin ? P
  : P & { [K in keyof P]: Exact<P[K], I[K]> } & { [K in Exclude<keyof I, KeysOfUnion<P>>]: never };

function toTimestamp(date: Date): Timestamp {
  const seconds = date.getTime() / 1_000;
  const nanos = (date.getTime() % 1_000) * 1_000_000;
  return { seconds, nanos };
}

function fromTimestamp(t: Timestamp): Date {
  let millis = (t.seconds || 0) * 1_000;
  millis += (t.nanos || 0) / 1_000_000;
  return new Date(millis);
}

function fromJsonTimestamp(o: any): Date {
  if (o instanceof Date) {
    return o;
  } else if (typeof o === "string") {
    return new Date(o);
  } else {
    return fromTimestamp(Timestamp.fromJSON(o));
  }
}

function isObject(value: any): boolean {
  return typeof value === "object" && value !== null;
}

function isSet(value: any): boolean {
  return value !== null && value !== undefined;
}
