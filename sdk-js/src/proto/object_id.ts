/* eslint-disable */
import _m0 from "protobufjs/minimal";
import { MetricsWindowLength, metricsWindowLengthFromJSON, metricsWindowLengthToNumber } from "./common_enums";
import { Timestamp } from "./google/protobuf/timestamp";

export const protobufPackage = "littlehorse";

/** The ID of a WfSpec. */
export interface WfSpecId {
  /** Name of the WfSpec. */
  name: string;
  /**
   * Major Version of a WfSpec.
   *
   * Note that WfSpec's are versioned. Creating a new WfSpec with the same name
   * and no breaking changes to the public Variables API results in a new WfSpec
   * being created with the same MajorVersion and a new revision. Creating a
   * WfSpec with a breaking change to the public Variables API results in a
   * new WfSpec being created with the same name, an incremented major_version,
   * and revision = 0.
   */
  majorVersion: number;
  /**
   * Revision of a WfSpec.
   *
   * Note that WfSpec's are versioned. Creating a new WfSpec with the same name
   * and no breaking changes to the public Variables API results in a new WfSpec
   * being created with the same MajorVersion and a new revision. Creating a
   * WfSpec with a breaking change to the public Variables API results in a
   * new WfSpec being created with the same name, an incremented major_version,
   * and revision = 0.
   */
  revision: number;
}

/** ID for a TaskDef. */
export interface TaskDefId {
  /** TaskDef's are uniquely identified by their name. */
  name: string;
}

/** ID for ExternalEventDef */
export interface ExternalEventDefId {
  /** ExternalEventDef's are uniquedly identified by their name. */
  name: string;
}

/** ID for a UserTaskDef */
export interface UserTaskDefId {
  /** The name of a UserTaskDef */
  name: string;
  /** Note that UserTaskDef's use simple versioning. */
  version: number;
}

/** ID for a WorkflowEventDef. */
export interface WorkflowEventDefId {
  /** The name of the WorkflowEventDef */
  name: string;
}

/** ID for a TaskWorkerGroup. */
export interface TaskWorkerGroupId {
  /** TaskWorkerGroups are uniquely identified by their TaskDefId. */
  taskDefId: TaskDefId | undefined;
}

/** Id for a Variable. */
export interface VariableId {
  /**
   * WfRunId for the variable. Note that every Variable is associated with
   * a WfRun.
   */
  wfRunId:
    | WfRunId
    | undefined;
  /**
   * Each Variable is owned by a specific ThreadRun inside the WfRun it belongs
   * to. This is that ThreadRun's number.
   */
  threadRunNumber: number;
  /** The name of the variable. */
  name: string;
}

/** ID for an ExternalEvent. */
export interface ExternalEventId {
  /**
   * WfRunId for the ExternalEvent. Note that every ExternalEvent is associated
   * with a WfRun.
   */
  wfRunId:
    | WfRunId
    | undefined;
  /** The ExternalEventDef for this ExternalEvent. */
  externalEventDefId:
    | ExternalEventDefId
    | undefined;
  /**
   * A unique guid allowing for distinguishing this ExternalEvent from other events
   * of the same ExternalEventDef and WfRun.
   */
  guid: string;
}

/** ID for a WfRun */
export interface WfRunId {
  /** The ID for this WfRun instance. */
  id: string;
  /** A WfRun may have a parent WfRun. If so, this field is set to the parent's ID. */
  parentWfRunId?: WfRunId | undefined;
}

/** ID for a NodeRun. */
export interface NodeRunId {
  /**
   * ID of the WfRun for this NodeRun. Note that every NodeRun is associated with
   * a WfRun.
   */
  wfRunId:
    | WfRunId
    | undefined;
  /** ThreadRun of this NodeRun. Note that each NodeRun belongs to a ThreadRun. */
  threadRunNumber: number;
  /** Position of this NodeRun within its ThreadRun. */
  position: number;
}

/** An ID for a WorkflowEvent. */
export interface WorkflowEventId {
  /** The Id of the WfRun that threw the event. */
  wfRunId:
    | WfRunId
    | undefined;
  /** The ID of the WorkflowEventDef that this WorkflowEvent is a member of. */
  workflowEventDefId:
    | WorkflowEventDefId
    | undefined;
  /**
   * An ID that makes the WorkflowEventId unique among all WorkflowEvent's of the
   * same type thrown by the WfRun. This field starts at zero and is incremented every
   * time a WorkflowEvent of the same type is thrown by the same WfRun.
   */
  number: number;
}

/** ID for a TaskRun. */
export interface TaskRunId {
  /**
   * WfRunId for this TaskRun. Note that every TaskRun is associated with
   * a WfRun.
   */
  wfRunId:
    | WfRunId
    | undefined;
  /** Unique identifier for this TaskRun. Unique among the WfRun. */
  taskGuid: string;
}

/** ID for a UserTaskRun */
export interface UserTaskRunId {
  /**
   * WfRunId for this UserTaskRun. Note that every UserTaskRun is associated
   * with a WfRun.
   */
  wfRunId:
    | WfRunId
    | undefined;
  /** Unique identifier for this UserTaskRun. */
  userTaskGuid: string;
}

/** ID for a specific window of TaskDef metrics. */
export interface TaskDefMetricsId {
  /** The timestamp at which this metrics window starts. */
  windowStart:
    | string
    | undefined;
  /** The length of this window. */
  windowType: MetricsWindowLength;
  /** The TaskDefId that this metrics window reports on. */
  taskDefId: TaskDefId | undefined;
}

/** ID for a specific window of WfSpec metrics. */
export interface WfSpecMetricsId {
  /** The timestamp at which this metrics window starts. */
  windowStart:
    | string
    | undefined;
  /** The length of this window. */
  windowType: MetricsWindowLength;
  /** The WfSpecId that this metrics window reports on. */
  wfSpecId: WfSpecId | undefined;
}

/** ID for a Principal. */
export interface PrincipalId {
  /**
   * The id of this principal. In OAuth, this is the OAuth Client ID (for
   * machine principals) or the OAuth User Id (for human principals).
   */
  id: string;
}

/** ID for a Tenant. */
export interface TenantId {
  /** The Tenant ID. */
  id: string;
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

  create(base?: DeepPartial<WfSpecId>): WfSpecId {
    return WfSpecId.fromPartial(base ?? {});
  },
  fromPartial(object: DeepPartial<WfSpecId>): WfSpecId {
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

  create(base?: DeepPartial<TaskDefId>): TaskDefId {
    return TaskDefId.fromPartial(base ?? {});
  },
  fromPartial(object: DeepPartial<TaskDefId>): TaskDefId {
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

  create(base?: DeepPartial<ExternalEventDefId>): ExternalEventDefId {
    return ExternalEventDefId.fromPartial(base ?? {});
  },
  fromPartial(object: DeepPartial<ExternalEventDefId>): ExternalEventDefId {
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

  create(base?: DeepPartial<UserTaskDefId>): UserTaskDefId {
    return UserTaskDefId.fromPartial(base ?? {});
  },
  fromPartial(object: DeepPartial<UserTaskDefId>): UserTaskDefId {
    const message = createBaseUserTaskDefId();
    message.name = object.name ?? "";
    message.version = object.version ?? 0;
    return message;
  },
};

function createBaseWorkflowEventDefId(): WorkflowEventDefId {
  return { name: "" };
}

export const WorkflowEventDefId = {
  encode(message: WorkflowEventDefId, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.name !== "") {
      writer.uint32(10).string(message.name);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): WorkflowEventDefId {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseWorkflowEventDefId();
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

  create(base?: DeepPartial<WorkflowEventDefId>): WorkflowEventDefId {
    return WorkflowEventDefId.fromPartial(base ?? {});
  },
  fromPartial(object: DeepPartial<WorkflowEventDefId>): WorkflowEventDefId {
    const message = createBaseWorkflowEventDefId();
    message.name = object.name ?? "";
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

  create(base?: DeepPartial<TaskWorkerGroupId>): TaskWorkerGroupId {
    return TaskWorkerGroupId.fromPartial(base ?? {});
  },
  fromPartial(object: DeepPartial<TaskWorkerGroupId>): TaskWorkerGroupId {
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

  create(base?: DeepPartial<VariableId>): VariableId {
    return VariableId.fromPartial(base ?? {});
  },
  fromPartial(object: DeepPartial<VariableId>): VariableId {
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

  create(base?: DeepPartial<ExternalEventId>): ExternalEventId {
    return ExternalEventId.fromPartial(base ?? {});
  },
  fromPartial(object: DeepPartial<ExternalEventId>): ExternalEventId {
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
  return { id: "", parentWfRunId: undefined };
}

export const WfRunId = {
  encode(message: WfRunId, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.id !== "") {
      writer.uint32(10).string(message.id);
    }
    if (message.parentWfRunId !== undefined) {
      WfRunId.encode(message.parentWfRunId, writer.uint32(18).fork()).ldelim();
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
        case 2:
          if (tag !== 18) {
            break;
          }

          message.parentWfRunId = WfRunId.decode(reader, reader.uint32());
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create(base?: DeepPartial<WfRunId>): WfRunId {
    return WfRunId.fromPartial(base ?? {});
  },
  fromPartial(object: DeepPartial<WfRunId>): WfRunId {
    const message = createBaseWfRunId();
    message.id = object.id ?? "";
    message.parentWfRunId = (object.parentWfRunId !== undefined && object.parentWfRunId !== null)
      ? WfRunId.fromPartial(object.parentWfRunId)
      : undefined;
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

  create(base?: DeepPartial<NodeRunId>): NodeRunId {
    return NodeRunId.fromPartial(base ?? {});
  },
  fromPartial(object: DeepPartial<NodeRunId>): NodeRunId {
    const message = createBaseNodeRunId();
    message.wfRunId = (object.wfRunId !== undefined && object.wfRunId !== null)
      ? WfRunId.fromPartial(object.wfRunId)
      : undefined;
    message.threadRunNumber = object.threadRunNumber ?? 0;
    message.position = object.position ?? 0;
    return message;
  },
};

function createBaseWorkflowEventId(): WorkflowEventId {
  return { wfRunId: undefined, workflowEventDefId: undefined, number: 0 };
}

export const WorkflowEventId = {
  encode(message: WorkflowEventId, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.wfRunId !== undefined) {
      WfRunId.encode(message.wfRunId, writer.uint32(10).fork()).ldelim();
    }
    if (message.workflowEventDefId !== undefined) {
      WorkflowEventDefId.encode(message.workflowEventDefId, writer.uint32(18).fork()).ldelim();
    }
    if (message.number !== 0) {
      writer.uint32(24).int32(message.number);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): WorkflowEventId {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseWorkflowEventId();
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

          message.workflowEventDefId = WorkflowEventDefId.decode(reader, reader.uint32());
          continue;
        case 3:
          if (tag !== 24) {
            break;
          }

          message.number = reader.int32();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create(base?: DeepPartial<WorkflowEventId>): WorkflowEventId {
    return WorkflowEventId.fromPartial(base ?? {});
  },
  fromPartial(object: DeepPartial<WorkflowEventId>): WorkflowEventId {
    const message = createBaseWorkflowEventId();
    message.wfRunId = (object.wfRunId !== undefined && object.wfRunId !== null)
      ? WfRunId.fromPartial(object.wfRunId)
      : undefined;
    message.workflowEventDefId = (object.workflowEventDefId !== undefined && object.workflowEventDefId !== null)
      ? WorkflowEventDefId.fromPartial(object.workflowEventDefId)
      : undefined;
    message.number = object.number ?? 0;
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

  create(base?: DeepPartial<TaskRunId>): TaskRunId {
    return TaskRunId.fromPartial(base ?? {});
  },
  fromPartial(object: DeepPartial<TaskRunId>): TaskRunId {
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

  create(base?: DeepPartial<UserTaskRunId>): UserTaskRunId {
    return UserTaskRunId.fromPartial(base ?? {});
  },
  fromPartial(object: DeepPartial<UserTaskRunId>): UserTaskRunId {
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

  create(base?: DeepPartial<TaskDefMetricsId>): TaskDefMetricsId {
    return TaskDefMetricsId.fromPartial(base ?? {});
  },
  fromPartial(object: DeepPartial<TaskDefMetricsId>): TaskDefMetricsId {
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

  create(base?: DeepPartial<WfSpecMetricsId>): WfSpecMetricsId {
    return WfSpecMetricsId.fromPartial(base ?? {});
  },
  fromPartial(object: DeepPartial<WfSpecMetricsId>): WfSpecMetricsId {
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

  create(base?: DeepPartial<PrincipalId>): PrincipalId {
    return PrincipalId.fromPartial(base ?? {});
  },
  fromPartial(object: DeepPartial<PrincipalId>): PrincipalId {
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

  create(base?: DeepPartial<TenantId>): TenantId {
    return TenantId.fromPartial(base ?? {});
  },
  fromPartial(object: DeepPartial<TenantId>): TenantId {
    const message = createBaseTenantId();
    message.id = object.id ?? "";
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
