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
  version: number;
}

export interface TaskDefId {
  name: string;
}

export interface ExternalEventDefId {
  name: string;
}

export interface GetLatestWfSpecRequest {
  name: string;
}

export interface UserTaskDefId {
  name: string;
  version: number;
}

export interface TaskWorkerGroupId {
  taskDefName: string;
}

export interface VariableId {
  wfRunId: string;
  threadRunNumber: number;
  name: string;
}

export interface ExternalEventId {
  wfRunId: string;
  externalEventDefName: string;
  guid: string;
}

export interface WfRunId {
  id: string;
}

export interface NodeRunId {
  wfRunId: string;
  threadRunNumber: number;
  position: number;
}

export interface TaskRunId {
  wfRunId: string;
  taskGuid: string;
}

export interface UserTaskRunId {
  wfRunId: string;
  userTaskGuid: string;
}

export interface TaskDefMetricsId {
  windowStart: string | undefined;
  windowType: MetricsWindowLength;
  taskDefName: string;
}

export interface WfSpecMetricsId {
  windowStart: string | undefined;
  windowType: MetricsWindowLength;
  wfSpecName: string;
  wfSpecVersion: number;
}

function createBaseWfSpecId(): WfSpecId {
  return { name: "", version: 0 };
}

export const WfSpecId = {
  encode(message: WfSpecId, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.name !== "") {
      writer.uint32(10).string(message.name);
    }
    if (message.version !== 0) {
      writer.uint32(16).int32(message.version);
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

  fromJSON(object: any): WfSpecId {
    return {
      name: isSet(object.name) ? globalThis.String(object.name) : "",
      version: isSet(object.version) ? globalThis.Number(object.version) : 0,
    };
  },

  toJSON(message: WfSpecId): unknown {
    const obj: any = {};
    if (message.name !== "") {
      obj.name = message.name;
    }
    if (message.version !== 0) {
      obj.version = Math.round(message.version);
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<WfSpecId>, I>>(base?: I): WfSpecId {
    return WfSpecId.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<WfSpecId>, I>>(object: I): WfSpecId {
    const message = createBaseWfSpecId();
    message.name = object.name ?? "";
    message.version = object.version ?? 0;
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

function createBaseGetLatestWfSpecRequest(): GetLatestWfSpecRequest {
  return { name: "" };
}

export const GetLatestWfSpecRequest = {
  encode(message: GetLatestWfSpecRequest, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.name !== "") {
      writer.uint32(10).string(message.name);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): GetLatestWfSpecRequest {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseGetLatestWfSpecRequest();
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

  fromJSON(object: any): GetLatestWfSpecRequest {
    return { name: isSet(object.name) ? globalThis.String(object.name) : "" };
  },

  toJSON(message: GetLatestWfSpecRequest): unknown {
    const obj: any = {};
    if (message.name !== "") {
      obj.name = message.name;
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<GetLatestWfSpecRequest>, I>>(base?: I): GetLatestWfSpecRequest {
    return GetLatestWfSpecRequest.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<GetLatestWfSpecRequest>, I>>(object: I): GetLatestWfSpecRequest {
    const message = createBaseGetLatestWfSpecRequest();
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
  return { taskDefName: "" };
}

export const TaskWorkerGroupId = {
  encode(message: TaskWorkerGroupId, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.taskDefName !== "") {
      writer.uint32(10).string(message.taskDefName);
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

          message.taskDefName = reader.string();
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
    return { taskDefName: isSet(object.taskDefName) ? globalThis.String(object.taskDefName) : "" };
  },

  toJSON(message: TaskWorkerGroupId): unknown {
    const obj: any = {};
    if (message.taskDefName !== "") {
      obj.taskDefName = message.taskDefName;
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<TaskWorkerGroupId>, I>>(base?: I): TaskWorkerGroupId {
    return TaskWorkerGroupId.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<TaskWorkerGroupId>, I>>(object: I): TaskWorkerGroupId {
    const message = createBaseTaskWorkerGroupId();
    message.taskDefName = object.taskDefName ?? "";
    return message;
  },
};

function createBaseVariableId(): VariableId {
  return { wfRunId: "", threadRunNumber: 0, name: "" };
}

export const VariableId = {
  encode(message: VariableId, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.wfRunId !== "") {
      writer.uint32(10).string(message.wfRunId);
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

          message.wfRunId = reader.string();
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
      wfRunId: isSet(object.wfRunId) ? globalThis.String(object.wfRunId) : "",
      threadRunNumber: isSet(object.threadRunNumber) ? globalThis.Number(object.threadRunNumber) : 0,
      name: isSet(object.name) ? globalThis.String(object.name) : "",
    };
  },

  toJSON(message: VariableId): unknown {
    const obj: any = {};
    if (message.wfRunId !== "") {
      obj.wfRunId = message.wfRunId;
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
    message.wfRunId = object.wfRunId ?? "";
    message.threadRunNumber = object.threadRunNumber ?? 0;
    message.name = object.name ?? "";
    return message;
  },
};

function createBaseExternalEventId(): ExternalEventId {
  return { wfRunId: "", externalEventDefName: "", guid: "" };
}

export const ExternalEventId = {
  encode(message: ExternalEventId, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.wfRunId !== "") {
      writer.uint32(10).string(message.wfRunId);
    }
    if (message.externalEventDefName !== "") {
      writer.uint32(18).string(message.externalEventDefName);
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

          message.wfRunId = reader.string();
          continue;
        case 2:
          if (tag !== 18) {
            break;
          }

          message.externalEventDefName = reader.string();
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
      wfRunId: isSet(object.wfRunId) ? globalThis.String(object.wfRunId) : "",
      externalEventDefName: isSet(object.externalEventDefName) ? globalThis.String(object.externalEventDefName) : "",
      guid: isSet(object.guid) ? globalThis.String(object.guid) : "",
    };
  },

  toJSON(message: ExternalEventId): unknown {
    const obj: any = {};
    if (message.wfRunId !== "") {
      obj.wfRunId = message.wfRunId;
    }
    if (message.externalEventDefName !== "") {
      obj.externalEventDefName = message.externalEventDefName;
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
    message.wfRunId = object.wfRunId ?? "";
    message.externalEventDefName = object.externalEventDefName ?? "";
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
  return { wfRunId: "", threadRunNumber: 0, position: 0 };
}

export const NodeRunId = {
  encode(message: NodeRunId, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.wfRunId !== "") {
      writer.uint32(10).string(message.wfRunId);
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

          message.wfRunId = reader.string();
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
      wfRunId: isSet(object.wfRunId) ? globalThis.String(object.wfRunId) : "",
      threadRunNumber: isSet(object.threadRunNumber) ? globalThis.Number(object.threadRunNumber) : 0,
      position: isSet(object.position) ? globalThis.Number(object.position) : 0,
    };
  },

  toJSON(message: NodeRunId): unknown {
    const obj: any = {};
    if (message.wfRunId !== "") {
      obj.wfRunId = message.wfRunId;
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
    message.wfRunId = object.wfRunId ?? "";
    message.threadRunNumber = object.threadRunNumber ?? 0;
    message.position = object.position ?? 0;
    return message;
  },
};

function createBaseTaskRunId(): TaskRunId {
  return { wfRunId: "", taskGuid: "" };
}

export const TaskRunId = {
  encode(message: TaskRunId, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.wfRunId !== "") {
      writer.uint32(10).string(message.wfRunId);
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

          message.wfRunId = reader.string();
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
      wfRunId: isSet(object.wfRunId) ? globalThis.String(object.wfRunId) : "",
      taskGuid: isSet(object.taskGuid) ? globalThis.String(object.taskGuid) : "",
    };
  },

  toJSON(message: TaskRunId): unknown {
    const obj: any = {};
    if (message.wfRunId !== "") {
      obj.wfRunId = message.wfRunId;
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
    message.wfRunId = object.wfRunId ?? "";
    message.taskGuid = object.taskGuid ?? "";
    return message;
  },
};

function createBaseUserTaskRunId(): UserTaskRunId {
  return { wfRunId: "", userTaskGuid: "" };
}

export const UserTaskRunId = {
  encode(message: UserTaskRunId, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.wfRunId !== "") {
      writer.uint32(10).string(message.wfRunId);
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

          message.wfRunId = reader.string();
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
      wfRunId: isSet(object.wfRunId) ? globalThis.String(object.wfRunId) : "",
      userTaskGuid: isSet(object.userTaskGuid) ? globalThis.String(object.userTaskGuid) : "",
    };
  },

  toJSON(message: UserTaskRunId): unknown {
    const obj: any = {};
    if (message.wfRunId !== "") {
      obj.wfRunId = message.wfRunId;
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
    message.wfRunId = object.wfRunId ?? "";
    message.userTaskGuid = object.userTaskGuid ?? "";
    return message;
  },
};

function createBaseTaskDefMetricsId(): TaskDefMetricsId {
  return { windowStart: undefined, windowType: MetricsWindowLength.MINUTES_5, taskDefName: "" };
}

export const TaskDefMetricsId = {
  encode(message: TaskDefMetricsId, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.windowStart !== undefined) {
      Timestamp.encode(toTimestamp(message.windowStart), writer.uint32(10).fork()).ldelim();
    }
    if (message.windowType !== MetricsWindowLength.MINUTES_5) {
      writer.uint32(16).int32(metricsWindowLengthToNumber(message.windowType));
    }
    if (message.taskDefName !== "") {
      writer.uint32(26).string(message.taskDefName);
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

          message.taskDefName = reader.string();
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
      taskDefName: isSet(object.taskDefName) ? globalThis.String(object.taskDefName) : "",
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
    if (message.taskDefName !== "") {
      obj.taskDefName = message.taskDefName;
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
    message.taskDefName = object.taskDefName ?? "";
    return message;
  },
};

function createBaseWfSpecMetricsId(): WfSpecMetricsId {
  return { windowStart: undefined, windowType: MetricsWindowLength.MINUTES_5, wfSpecName: "", wfSpecVersion: 0 };
}

export const WfSpecMetricsId = {
  encode(message: WfSpecMetricsId, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.windowStart !== undefined) {
      Timestamp.encode(toTimestamp(message.windowStart), writer.uint32(10).fork()).ldelim();
    }
    if (message.windowType !== MetricsWindowLength.MINUTES_5) {
      writer.uint32(16).int32(metricsWindowLengthToNumber(message.windowType));
    }
    if (message.wfSpecName !== "") {
      writer.uint32(26).string(message.wfSpecName);
    }
    if (message.wfSpecVersion !== 0) {
      writer.uint32(32).int32(message.wfSpecVersion);
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

          message.wfSpecName = reader.string();
          continue;
        case 4:
          if (tag !== 32) {
            break;
          }

          message.wfSpecVersion = reader.int32();
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
      wfSpecName: isSet(object.wfSpecName) ? globalThis.String(object.wfSpecName) : "",
      wfSpecVersion: isSet(object.wfSpecVersion) ? globalThis.Number(object.wfSpecVersion) : 0,
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
    if (message.wfSpecName !== "") {
      obj.wfSpecName = message.wfSpecName;
    }
    if (message.wfSpecVersion !== 0) {
      obj.wfSpecVersion = Math.round(message.wfSpecVersion);
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
    message.wfSpecName = object.wfSpecName ?? "";
    message.wfSpecVersion = object.wfSpecVersion ?? 0;
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
