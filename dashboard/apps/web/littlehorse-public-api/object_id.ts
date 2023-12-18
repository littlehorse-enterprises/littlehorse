/* eslint-disable */
import * as _m0 from "protobufjs/minimal";
import {
  MetricsWindowLength,
  metricsWindowLengthFromJSON,
  metricsWindowLengthToJSON,
  metricsWindowLengthToNumber,
} from "./common_enums";
import { Timestamp } from "./google/protobuf/timestamp";

export const protobufPackage = "littlehorse";

export interface WfSpecId {
  name: string;
  majorVersion: number;
  revision: number;
}

export interface TaskDefId {
  name: string;
}

export interface ExternalEventDefId {
  name: string;
}

export interface UserTaskDefId {
  name: string;
  version: number;
}

export interface TaskWorkerGroupId {
  taskDefId: TaskDefId | undefined;
}

export interface VariableId {
  wfRunId: WfRunId | undefined;
  threadRunNumber: number;
  name: string;
}

export interface ExternalEventId {
  wfRunId: WfRunId | undefined;
  externalEventDefId: ExternalEventDefId | undefined;
  guid: string;
}

export interface WfRunId {
  id: string;
}

export interface NodeRunId {
  wfRunId: WfRunId | undefined;
  threadRunNumber: number;
  position: number;
}

export interface TaskRunId {
  wfRunId: WfRunId | undefined;
  taskGuid: string;
}

export interface UserTaskRunId {
  wfRunId: WfRunId | undefined;
  userTaskGuid: string;
}

export interface TaskDefMetricsId {
  windowStart: string | undefined;
  windowType: MetricsWindowLength;
  taskDefId: TaskDefId | undefined;
}

export interface WfSpecMetricsId {
  windowStart: string | undefined;
  windowType: MetricsWindowLength;
  wfSpecId: WfSpecId | undefined;
}

export interface PrincipalId {
  id: string;
}

export interface TenantId {
  id: string;
}

export interface AggregatedMetricId {
  wfSpecId?: WfSpecId | undefined;
  specificId?: string | undefined;
}

function createBaseWfSpecId(): WfSpecId {
  return { name: "", majorVersion: 0, revision: 0 };
}

export const WfSpecId = {
  encode(message: WfSpecId, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.name !== "") {
      writer.uint32(10).string(message.name);
    }
    if (message.majorVersion !== 0) {
      writer.uint32(16).int32(message.majorVersion);
    }
    if (message.revision !== 0) {
      writer.uint32(24).int32(message.revision);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): WfSpecId {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseWfSpecId();
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

          message.majorVersion = reader.int32();
          continue;
        case 3:
          if (tag !== 24) {
            break;
          }

          message.revision = reader.int32();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  fromJSON(object: any): WfSpecId {
    return {
      name: isSet(object.name) ? globalThis.String(object.name) : "",
      majorVersion: isSet(object.majorVersion) ? globalThis.Number(object.majorVersion) : 0,
      revision: isSet(object.revision) ? globalThis.Number(object.revision) : 0,
    };
  },

  toJSON(message: WfSpecId): unknown {
    const obj: any = {};
    if (message.name !== "") {
      obj.name = message.name;
    }
    if (message.majorVersion !== 0) {
      obj.majorVersion = Math.round(message.majorVersion);
    }
    if (message.revision !== 0) {
      obj.revision = Math.round(message.revision);
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<WfSpecId>, I>>(base?: I): WfSpecId {
    return WfSpecId.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<WfSpecId>, I>>(object: I): WfSpecId {
    const message = createBaseWfSpecId();
    message.name = object.name ?? "";
    message.majorVersion = object.majorVersion ?? 0;
    message.revision = object.revision ?? 0;
    return message;
  },
};

function createBaseTaskDefId(): TaskDefId {
  return { name: "" };
}

export const TaskDefId = {
  encode(message: TaskDefId, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.name !== "") {
      writer.uint32(10).string(message.name);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): TaskDefId {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseTaskDefId();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.name = reader.string();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  fromJSON(object: any): TaskDefId {
    return { name: isSet(object.name) ? globalThis.String(object.name) : "" };
  },

  toJSON(message: TaskDefId): unknown {
    const obj: any = {};
    if (message.name !== "") {
      obj.name = message.name;
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<TaskDefId>, I>>(base?: I): TaskDefId {
    return TaskDefId.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<TaskDefId>, I>>(object: I): TaskDefId {
    const message = createBaseTaskDefId();
    message.name = object.name ?? "";
    return message;
  },
};

function createBaseExternalEventDefId(): ExternalEventDefId {
  return { name: "" };
}

export const ExternalEventDefId = {
  encode(message: ExternalEventDefId, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.name !== "") {
      writer.uint32(10).string(message.name);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): ExternalEventDefId {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseExternalEventDefId();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.name = reader.string();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  fromJSON(object: any): ExternalEventDefId {
    return { name: isSet(object.name) ? globalThis.String(object.name) : "" };
  },

  toJSON(message: ExternalEventDefId): unknown {
    const obj: any = {};
    if (message.name !== "") {
      obj.name = message.name;
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<ExternalEventDefId>, I>>(base?: I): ExternalEventDefId {
    return ExternalEventDefId.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<ExternalEventDefId>, I>>(object: I): ExternalEventDefId {
    const message = createBaseExternalEventDefId();
    message.name = object.name ?? "";
    return message;
  },
};

function createBaseUserTaskDefId(): UserTaskDefId {
  return { name: "", version: 0 };
}

export const UserTaskDefId = {
  encode(message: UserTaskDefId, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.name !== "") {
      writer.uint32(10).string(message.name);
    }
    if (message.version !== 0) {
      writer.uint32(16).int32(message.version);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): UserTaskDefId {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseUserTaskDefId();
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
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  fromJSON(object: any): UserTaskDefId {
    return {
      name: isSet(object.name) ? globalThis.String(object.name) : "",
      version: isSet(object.version) ? globalThis.Number(object.version) : 0,
    };
  },

  toJSON(message: UserTaskDefId): unknown {
    const obj: any = {};
    if (message.name !== "") {
      obj.name = message.name;
    }
    if (message.version !== 0) {
      obj.version = Math.round(message.version);
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<UserTaskDefId>, I>>(base?: I): UserTaskDefId {
    return UserTaskDefId.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<UserTaskDefId>, I>>(object: I): UserTaskDefId {
    const message = createBaseUserTaskDefId();
    message.name = object.name ?? "";
    message.version = object.version ?? 0;
    return message;
  },
};

function createBaseTaskWorkerGroupId(): TaskWorkerGroupId {
  return { taskDefId: undefined };
}

export const TaskWorkerGroupId = {
  encode(message: TaskWorkerGroupId, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.taskDefId !== undefined) {
      TaskDefId.encode(message.taskDefId, writer.uint32(10).fork()).ldelim();
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): TaskWorkerGroupId {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseTaskWorkerGroupId();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.taskDefId = TaskDefId.decode(reader, reader.uint32());
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  fromJSON(object: any): TaskWorkerGroupId {
    return { taskDefId: isSet(object.taskDefId) ? TaskDefId.fromJSON(object.taskDefId) : undefined };
  },

  toJSON(message: TaskWorkerGroupId): unknown {
    const obj: any = {};
    if (message.taskDefId !== undefined) {
      obj.taskDefId = TaskDefId.toJSON(message.taskDefId);
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<TaskWorkerGroupId>, I>>(base?: I): TaskWorkerGroupId {
    return TaskWorkerGroupId.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<TaskWorkerGroupId>, I>>(object: I): TaskWorkerGroupId {
    const message = createBaseTaskWorkerGroupId();
    message.taskDefId = (object.taskDefId !== undefined && object.taskDefId !== null)
      ? TaskDefId.fromPartial(object.taskDefId)
      : undefined;
    return message;
  },
};

function createBaseVariableId(): VariableId {
  return { wfRunId: undefined, threadRunNumber: 0, name: "" };
}

export const VariableId = {
  encode(message: VariableId, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.wfRunId !== undefined) {
      WfRunId.encode(message.wfRunId, writer.uint32(10).fork()).ldelim();
    }
    if (message.threadRunNumber !== 0) {
      writer.uint32(16).int32(message.threadRunNumber);
    }
    if (message.name !== "") {
      writer.uint32(26).string(message.name);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): VariableId {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseVariableId();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.wfRunId = WfRunId.decode(reader, reader.uint32());
          continue;
        case 2:
          if (tag !== 16) {
            break;
          }

          message.threadRunNumber = reader.int32();
          continue;
        case 3:
          if (tag !== 26) {
            break;
          }

          message.name = reader.string();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  fromJSON(object: any): VariableId {
    return {
      wfRunId: isSet(object.wfRunId) ? WfRunId.fromJSON(object.wfRunId) : undefined,
      threadRunNumber: isSet(object.threadRunNumber) ? globalThis.Number(object.threadRunNumber) : 0,
      name: isSet(object.name) ? globalThis.String(object.name) : "",
    };
  },

  toJSON(message: VariableId): unknown {
    const obj: any = {};
    if (message.wfRunId !== undefined) {
      obj.wfRunId = WfRunId.toJSON(message.wfRunId);
    }
    if (message.threadRunNumber !== 0) {
      obj.threadRunNumber = Math.round(message.threadRunNumber);
    }
    if (message.name !== "") {
      obj.name = message.name;
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<VariableId>, I>>(base?: I): VariableId {
    return VariableId.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<VariableId>, I>>(object: I): VariableId {
    const message = createBaseVariableId();
    message.wfRunId = (object.wfRunId !== undefined && object.wfRunId !== null)
      ? WfRunId.fromPartial(object.wfRunId)
      : undefined;
    message.threadRunNumber = object.threadRunNumber ?? 0;
    message.name = object.name ?? "";
    return message;
  },
};

function createBaseExternalEventId(): ExternalEventId {
  return { wfRunId: undefined, externalEventDefId: undefined, guid: "" };
}

export const ExternalEventId = {
  encode(message: ExternalEventId, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.wfRunId !== undefined) {
      WfRunId.encode(message.wfRunId, writer.uint32(10).fork()).ldelim();
    }
    if (message.externalEventDefId !== undefined) {
      ExternalEventDefId.encode(message.externalEventDefId, writer.uint32(18).fork()).ldelim();
    }
    if (message.guid !== "") {
      writer.uint32(26).string(message.guid);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): ExternalEventId {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseExternalEventId();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.wfRunId = WfRunId.decode(reader, reader.uint32());
          continue;
        case 2:
          if (tag !== 18) {
            break;
          }

          message.externalEventDefId = ExternalEventDefId.decode(reader, reader.uint32());
          continue;
        case 3:
          if (tag !== 26) {
            break;
          }

          message.guid = reader.string();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  fromJSON(object: any): ExternalEventId {
    return {
      wfRunId: isSet(object.wfRunId) ? WfRunId.fromJSON(object.wfRunId) : undefined,
      externalEventDefId: isSet(object.externalEventDefId)
        ? ExternalEventDefId.fromJSON(object.externalEventDefId)
        : undefined,
      guid: isSet(object.guid) ? globalThis.String(object.guid) : "",
    };
  },

  toJSON(message: ExternalEventId): unknown {
    const obj: any = {};
    if (message.wfRunId !== undefined) {
      obj.wfRunId = WfRunId.toJSON(message.wfRunId);
    }
    if (message.externalEventDefId !== undefined) {
      obj.externalEventDefId = ExternalEventDefId.toJSON(message.externalEventDefId);
    }
    if (message.guid !== "") {
      obj.guid = message.guid;
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<ExternalEventId>, I>>(base?: I): ExternalEventId {
    return ExternalEventId.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<ExternalEventId>, I>>(object: I): ExternalEventId {
    const message = createBaseExternalEventId();
    message.wfRunId = (object.wfRunId !== undefined && object.wfRunId !== null)
      ? WfRunId.fromPartial(object.wfRunId)
      : undefined;
    message.externalEventDefId = (object.externalEventDefId !== undefined && object.externalEventDefId !== null)
      ? ExternalEventDefId.fromPartial(object.externalEventDefId)
      : undefined;
    message.guid = object.guid ?? "";
    return message;
  },
};

function createBaseWfRunId(): WfRunId {
  return { id: "" };
}

export const WfRunId = {
  encode(message: WfRunId, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.id !== "") {
      writer.uint32(10).string(message.id);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): WfRunId {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseWfRunId();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.id = reader.string();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  fromJSON(object: any): WfRunId {
    return { id: isSet(object.id) ? globalThis.String(object.id) : "" };
  },

  toJSON(message: WfRunId): unknown {
    const obj: any = {};
    if (message.id !== "") {
      obj.id = message.id;
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<WfRunId>, I>>(base?: I): WfRunId {
    return WfRunId.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<WfRunId>, I>>(object: I): WfRunId {
    const message = createBaseWfRunId();
    message.id = object.id ?? "";
    return message;
  },
};

function createBaseNodeRunId(): NodeRunId {
  return { wfRunId: undefined, threadRunNumber: 0, position: 0 };
}

export const NodeRunId = {
  encode(message: NodeRunId, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.wfRunId !== undefined) {
      WfRunId.encode(message.wfRunId, writer.uint32(10).fork()).ldelim();
    }
    if (message.threadRunNumber !== 0) {
      writer.uint32(16).int32(message.threadRunNumber);
    }
    if (message.position !== 0) {
      writer.uint32(24).int32(message.position);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): NodeRunId {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseNodeRunId();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.wfRunId = WfRunId.decode(reader, reader.uint32());
          continue;
        case 2:
          if (tag !== 16) {
            break;
          }

          message.threadRunNumber = reader.int32();
          continue;
        case 3:
          if (tag !== 24) {
            break;
          }

          message.position = reader.int32();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  fromJSON(object: any): NodeRunId {
    return {
      wfRunId: isSet(object.wfRunId) ? WfRunId.fromJSON(object.wfRunId) : undefined,
      threadRunNumber: isSet(object.threadRunNumber) ? globalThis.Number(object.threadRunNumber) : 0,
      position: isSet(object.position) ? globalThis.Number(object.position) : 0,
    };
  },

  toJSON(message: NodeRunId): unknown {
    const obj: any = {};
    if (message.wfRunId !== undefined) {
      obj.wfRunId = WfRunId.toJSON(message.wfRunId);
    }
    if (message.threadRunNumber !== 0) {
      obj.threadRunNumber = Math.round(message.threadRunNumber);
    }
    if (message.position !== 0) {
      obj.position = Math.round(message.position);
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<NodeRunId>, I>>(base?: I): NodeRunId {
    return NodeRunId.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<NodeRunId>, I>>(object: I): NodeRunId {
    const message = createBaseNodeRunId();
    message.wfRunId = (object.wfRunId !== undefined && object.wfRunId !== null)
      ? WfRunId.fromPartial(object.wfRunId)
      : undefined;
    message.threadRunNumber = object.threadRunNumber ?? 0;
    message.position = object.position ?? 0;
    return message;
  },
};

function createBaseTaskRunId(): TaskRunId {
  return { wfRunId: undefined, taskGuid: "" };
}

export const TaskRunId = {
  encode(message: TaskRunId, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.wfRunId !== undefined) {
      WfRunId.encode(message.wfRunId, writer.uint32(10).fork()).ldelim();
    }
    if (message.taskGuid !== "") {
      writer.uint32(18).string(message.taskGuid);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): TaskRunId {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseTaskRunId();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.wfRunId = WfRunId.decode(reader, reader.uint32());
          continue;
        case 2:
          if (tag !== 18) {
            break;
          }

          message.taskGuid = reader.string();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  fromJSON(object: any): TaskRunId {
    return {
      wfRunId: isSet(object.wfRunId) ? WfRunId.fromJSON(object.wfRunId) : undefined,
      taskGuid: isSet(object.taskGuid) ? globalThis.String(object.taskGuid) : "",
    };
  },

  toJSON(message: TaskRunId): unknown {
    const obj: any = {};
    if (message.wfRunId !== undefined) {
      obj.wfRunId = WfRunId.toJSON(message.wfRunId);
    }
    if (message.taskGuid !== "") {
      obj.taskGuid = message.taskGuid;
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<TaskRunId>, I>>(base?: I): TaskRunId {
    return TaskRunId.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<TaskRunId>, I>>(object: I): TaskRunId {
    const message = createBaseTaskRunId();
    message.wfRunId = (object.wfRunId !== undefined && object.wfRunId !== null)
      ? WfRunId.fromPartial(object.wfRunId)
      : undefined;
    message.taskGuid = object.taskGuid ?? "";
    return message;
  },
};

function createBaseUserTaskRunId(): UserTaskRunId {
  return { wfRunId: undefined, userTaskGuid: "" };
}

export const UserTaskRunId = {
  encode(message: UserTaskRunId, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.wfRunId !== undefined) {
      WfRunId.encode(message.wfRunId, writer.uint32(10).fork()).ldelim();
    }
    if (message.userTaskGuid !== "") {
      writer.uint32(18).string(message.userTaskGuid);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): UserTaskRunId {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseUserTaskRunId();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.wfRunId = WfRunId.decode(reader, reader.uint32());
          continue;
        case 2:
          if (tag !== 18) {
            break;
          }

          message.userTaskGuid = reader.string();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  fromJSON(object: any): UserTaskRunId {
    return {
      wfRunId: isSet(object.wfRunId) ? WfRunId.fromJSON(object.wfRunId) : undefined,
      userTaskGuid: isSet(object.userTaskGuid) ? globalThis.String(object.userTaskGuid) : "",
    };
  },

  toJSON(message: UserTaskRunId): unknown {
    const obj: any = {};
    if (message.wfRunId !== undefined) {
      obj.wfRunId = WfRunId.toJSON(message.wfRunId);
    }
    if (message.userTaskGuid !== "") {
      obj.userTaskGuid = message.userTaskGuid;
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<UserTaskRunId>, I>>(base?: I): UserTaskRunId {
    return UserTaskRunId.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<UserTaskRunId>, I>>(object: I): UserTaskRunId {
    const message = createBaseUserTaskRunId();
    message.wfRunId = (object.wfRunId !== undefined && object.wfRunId !== null)
      ? WfRunId.fromPartial(object.wfRunId)
      : undefined;
    message.userTaskGuid = object.userTaskGuid ?? "";
    return message;
  },
};

function createBaseTaskDefMetricsId(): TaskDefMetricsId {
  return { windowStart: undefined, windowType: MetricsWindowLength.MINUTES_5, taskDefId: undefined };
}

export const TaskDefMetricsId = {
  encode(message: TaskDefMetricsId, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.windowStart !== undefined) {
      Timestamp.encode(toTimestamp(message.windowStart), writer.uint32(10).fork()).ldelim();
    }
    if (message.windowType !== MetricsWindowLength.MINUTES_5) {
      writer.uint32(16).int32(metricsWindowLengthToNumber(message.windowType));
    }
    if (message.taskDefId !== undefined) {
      TaskDefId.encode(message.taskDefId, writer.uint32(26).fork()).ldelim();
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): TaskDefMetricsId {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseTaskDefMetricsId();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.windowStart = fromTimestamp(Timestamp.decode(reader, reader.uint32()));
          continue;
        case 2:
          if (tag !== 16) {
            break;
          }

          message.windowType = metricsWindowLengthFromJSON(reader.int32());
          continue;
        case 3:
          if (tag !== 26) {
            break;
          }

          message.taskDefId = TaskDefId.decode(reader, reader.uint32());
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  fromJSON(object: any): TaskDefMetricsId {
    return {
      windowStart: isSet(object.windowStart) ? globalThis.String(object.windowStart) : undefined,
      windowType: isSet(object.windowType)
        ? metricsWindowLengthFromJSON(object.windowType)
        : MetricsWindowLength.MINUTES_5,
      taskDefId: isSet(object.taskDefId) ? TaskDefId.fromJSON(object.taskDefId) : undefined,
    };
  },

  toJSON(message: TaskDefMetricsId): unknown {
    const obj: any = {};
    if (message.windowStart !== undefined) {
      obj.windowStart = message.windowStart;
    }
    if (message.windowType !== MetricsWindowLength.MINUTES_5) {
      obj.windowType = metricsWindowLengthToJSON(message.windowType);
    }
    if (message.taskDefId !== undefined) {
      obj.taskDefId = TaskDefId.toJSON(message.taskDefId);
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<TaskDefMetricsId>, I>>(base?: I): TaskDefMetricsId {
    return TaskDefMetricsId.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<TaskDefMetricsId>, I>>(object: I): TaskDefMetricsId {
    const message = createBaseTaskDefMetricsId();
    message.windowStart = object.windowStart ?? undefined;
    message.windowType = object.windowType ?? MetricsWindowLength.MINUTES_5;
    message.taskDefId = (object.taskDefId !== undefined && object.taskDefId !== null)
      ? TaskDefId.fromPartial(object.taskDefId)
      : undefined;
    return message;
  },
};

function createBaseWfSpecMetricsId(): WfSpecMetricsId {
  return { windowStart: undefined, windowType: MetricsWindowLength.MINUTES_5, wfSpecId: undefined };
}

export const WfSpecMetricsId = {
  encode(message: WfSpecMetricsId, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.windowStart !== undefined) {
      Timestamp.encode(toTimestamp(message.windowStart), writer.uint32(10).fork()).ldelim();
    }
    if (message.windowType !== MetricsWindowLength.MINUTES_5) {
      writer.uint32(16).int32(metricsWindowLengthToNumber(message.windowType));
    }
    if (message.wfSpecId !== undefined) {
      WfSpecId.encode(message.wfSpecId, writer.uint32(26).fork()).ldelim();
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): WfSpecMetricsId {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseWfSpecMetricsId();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.windowStart = fromTimestamp(Timestamp.decode(reader, reader.uint32()));
          continue;
        case 2:
          if (tag !== 16) {
            break;
          }

          message.windowType = metricsWindowLengthFromJSON(reader.int32());
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

  fromJSON(object: any): WfSpecMetricsId {
    return {
      windowStart: isSet(object.windowStart) ? globalThis.String(object.windowStart) : undefined,
      windowType: isSet(object.windowType)
        ? metricsWindowLengthFromJSON(object.windowType)
        : MetricsWindowLength.MINUTES_5,
      wfSpecId: isSet(object.wfSpecId) ? WfSpecId.fromJSON(object.wfSpecId) : undefined,
    };
  },

  toJSON(message: WfSpecMetricsId): unknown {
    const obj: any = {};
    if (message.windowStart !== undefined) {
      obj.windowStart = message.windowStart;
    }
    if (message.windowType !== MetricsWindowLength.MINUTES_5) {
      obj.windowType = metricsWindowLengthToJSON(message.windowType);
    }
    if (message.wfSpecId !== undefined) {
      obj.wfSpecId = WfSpecId.toJSON(message.wfSpecId);
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<WfSpecMetricsId>, I>>(base?: I): WfSpecMetricsId {
    return WfSpecMetricsId.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<WfSpecMetricsId>, I>>(object: I): WfSpecMetricsId {
    const message = createBaseWfSpecMetricsId();
    message.windowStart = object.windowStart ?? undefined;
    message.windowType = object.windowType ?? MetricsWindowLength.MINUTES_5;
    message.wfSpecId = (object.wfSpecId !== undefined && object.wfSpecId !== null)
      ? WfSpecId.fromPartial(object.wfSpecId)
      : undefined;
    return message;
  },
};

function createBasePrincipalId(): PrincipalId {
  return { id: "" };
}

export const PrincipalId = {
  encode(message: PrincipalId, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.id !== "") {
      writer.uint32(10).string(message.id);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): PrincipalId {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBasePrincipalId();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.id = reader.string();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  fromJSON(object: any): PrincipalId {
    return { id: isSet(object.id) ? globalThis.String(object.id) : "" };
  },

  toJSON(message: PrincipalId): unknown {
    const obj: any = {};
    if (message.id !== "") {
      obj.id = message.id;
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<PrincipalId>, I>>(base?: I): PrincipalId {
    return PrincipalId.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<PrincipalId>, I>>(object: I): PrincipalId {
    const message = createBasePrincipalId();
    message.id = object.id ?? "";
    return message;
  },
};

function createBaseTenantId(): TenantId {
  return { id: "" };
}

export const TenantId = {
  encode(message: TenantId, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.id !== "") {
      writer.uint32(10).string(message.id);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): TenantId {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseTenantId();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.id = reader.string();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  fromJSON(object: any): TenantId {
    return { id: isSet(object.id) ? globalThis.String(object.id) : "" };
  },

  toJSON(message: TenantId): unknown {
    const obj: any = {};
    if (message.id !== "") {
      obj.id = message.id;
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<TenantId>, I>>(base?: I): TenantId {
    return TenantId.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<TenantId>, I>>(object: I): TenantId {
    const message = createBaseTenantId();
    message.id = object.id ?? "";
    return message;
  },
};

function createBaseAggregatedMetricId(): AggregatedMetricId {
  return { wfSpecId: undefined, specificId: undefined };
}

export const AggregatedMetricId = {
  encode(message: AggregatedMetricId, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.wfSpecId !== undefined) {
      WfSpecId.encode(message.wfSpecId, writer.uint32(10).fork()).ldelim();
    }
    if (message.specificId !== undefined) {
      writer.uint32(18).string(message.specificId);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): AggregatedMetricId {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseAggregatedMetricId();
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
          if (tag !== 18) {
            break;
          }

          message.specificId = reader.string();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  fromJSON(object: any): AggregatedMetricId {
    return {
      wfSpecId: isSet(object.wfSpecId) ? WfSpecId.fromJSON(object.wfSpecId) : undefined,
      specificId: isSet(object.specificId) ? globalThis.String(object.specificId) : undefined,
    };
  },

  toJSON(message: AggregatedMetricId): unknown {
    const obj: any = {};
    if (message.wfSpecId !== undefined) {
      obj.wfSpecId = WfSpecId.toJSON(message.wfSpecId);
    }
    if (message.specificId !== undefined) {
      obj.specificId = message.specificId;
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<AggregatedMetricId>, I>>(base?: I): AggregatedMetricId {
    return AggregatedMetricId.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<AggregatedMetricId>, I>>(object: I): AggregatedMetricId {
    const message = createBaseAggregatedMetricId();
    message.wfSpecId = (object.wfSpecId !== undefined && object.wfSpecId !== null)
      ? WfSpecId.fromPartial(object.wfSpecId)
      : undefined;
    message.specificId = object.specificId ?? undefined;
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
