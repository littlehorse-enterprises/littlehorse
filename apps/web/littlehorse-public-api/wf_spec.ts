/* eslint-disable */
import * as _m0 from "protobufjs/minimal";
import {
  LHStatus,
  lHStatusFromJSON,
  lHStatusToJSON,
  lHStatusToNumber,
  WaitForThreadsPolicy,
  waitForThreadsPolicyFromJSON,
  waitForThreadsPolicyToJSON,
  waitForThreadsPolicyToNumber,
} from "./common_enums";
import {
  Comparator,
  comparatorFromJSON,
  comparatorToJSON,
  comparatorToNumber,
  TaskNode,
  UTActionTrigger,
  VariableAssignment,
  VariableDef,
  VariableMutation,
} from "./common_wfspec";
import { Timestamp } from "./google/protobuf/timestamp";

export const protobufPackage = "littlehorse";

export interface WfSpec {
  name: string;
  version: number;
  createdAt: string | undefined;
  status: LHStatus;
  threadSpecs: { [key: string]: ThreadSpec };
  entrypointThreadName: string;
  retentionHours: number;
}

export interface WfSpec_ThreadSpecsEntry {
  key: string;
  value: ThreadSpec | undefined;
}

export interface ThreadSpec {
  nodes: { [key: string]: Node };
  variableDefs: VariableDef[];
  interruptDefs: InterruptDef[];
}

export interface ThreadSpec_NodesEntry {
  key: string;
  value: Node | undefined;
}

export interface InterruptDef {
  externalEventDefName: string;
  handlerSpecName: string;
}

export interface StartThreadNode {
  threadSpecName: string;
  variables: { [key: string]: VariableAssignment };
}

export interface StartThreadNode_VariablesEntry {
  key: string;
  value: VariableAssignment | undefined;
}

export interface StartMultipleThreadsNode {
  threadSpecName: string;
  variables: { [key: string]: VariableAssignment };
  iterable: VariableAssignment | undefined;
}

export interface StartMultipleThreadsNode_VariablesEntry {
  key: string;
  value: VariableAssignment | undefined;
}

export interface FailureHandlerDef {
  handlerSpecName: string;
  specificFailure?: string | undefined;
  anyFailureOfType?: FailureHandlerDef_LHFailureType | undefined;
}

export enum FailureHandlerDef_LHFailureType {
  FAILURE_TYPE_ERROR = "FAILURE_TYPE_ERROR",
  FAILURE_TYPE_EXCEPTION = "FAILURE_TYPE_EXCEPTION",
  UNRECOGNIZED = "UNRECOGNIZED",
}

export function failureHandlerDef_LHFailureTypeFromJSON(object: any): FailureHandlerDef_LHFailureType {
  switch (object) {
    case 0:
    case "FAILURE_TYPE_ERROR":
      return FailureHandlerDef_LHFailureType.FAILURE_TYPE_ERROR;
    case 1:
    case "FAILURE_TYPE_EXCEPTION":
      return FailureHandlerDef_LHFailureType.FAILURE_TYPE_EXCEPTION;
    case -1:
    case "UNRECOGNIZED":
    default:
      return FailureHandlerDef_LHFailureType.UNRECOGNIZED;
  }
}

export function failureHandlerDef_LHFailureTypeToJSON(object: FailureHandlerDef_LHFailureType): string {
  switch (object) {
    case FailureHandlerDef_LHFailureType.FAILURE_TYPE_ERROR:
      return "FAILURE_TYPE_ERROR";
    case FailureHandlerDef_LHFailureType.FAILURE_TYPE_EXCEPTION:
      return "FAILURE_TYPE_EXCEPTION";
    case FailureHandlerDef_LHFailureType.UNRECOGNIZED:
    default:
      return "UNRECOGNIZED";
  }
}

export function failureHandlerDef_LHFailureTypeToNumber(object: FailureHandlerDef_LHFailureType): number {
  switch (object) {
    case FailureHandlerDef_LHFailureType.FAILURE_TYPE_ERROR:
      return 0;
    case FailureHandlerDef_LHFailureType.FAILURE_TYPE_EXCEPTION:
      return 1;
    case FailureHandlerDef_LHFailureType.UNRECOGNIZED:
    default:
      return -1;
  }
}

export interface WaitForThreadsNode {
  /**
   * Either 1 or 3 is set. Cannot put `repeated` into a oneof, and
   * for compatibility reasons, we cannot wrap it into a separate message.
   */
  threads: WaitForThreadsNode_ThreadToWaitFor[];
  threadList?: VariableAssignment | undefined;
  policy: WaitForThreadsPolicy;
}

export interface WaitForThreadsNode_ThreadToWaitFor {
  threadRunNumber: VariableAssignment | undefined;
}

export interface ExternalEventNode {
  externalEventDefName: string;
  timeoutSeconds: VariableAssignment | undefined;
}

export interface EntrypointNode {
}

export interface ExitNode {
  failureDef?: FailureDef | undefined;
}

export interface FailureDef {
  failureName: string;
  message: string;
  content?: VariableAssignment | undefined;
}

export interface Node {
  outgoingEdges: Edge[];
  variableMutations: VariableMutation[];
  failureHandlers: FailureHandlerDef[];
  entrypoint?: EntrypointNode | undefined;
  exit?: ExitNode | undefined;
  task?: TaskNode | undefined;
  externalEvent?: ExternalEventNode | undefined;
  startThread?: StartThreadNode | undefined;
  waitForThreads?: WaitForThreadsNode | undefined;
  nop?: NopNode | undefined;
  sleep?: SleepNode | undefined;
  userTask?: UserTaskNode | undefined;
  startMultipleThreads?: StartMultipleThreadsNode | undefined;
}

export interface UserTaskNode {
  userTaskDefName: string;
  /** to whom should the User Task Run be assigned? */
  userGroup?: VariableAssignment | undefined;
  userId?:
    | VariableAssignment
    | undefined;
  /**
   * This is used to, for example, send a push notification to a mobile app
   * to remind someone that they need to fill out a task, or to re-assign
   * the task to another group of people
   */
  actions: UTActionTrigger[];
  /**
   * So, once the WfSpec is created, this will be pinned to a version. Customer
   * can optionally specify a specific version or can leave it null, in which
   * case we just use the latest
   */
  userTaskDefVersion?:
    | number
    | undefined;
  /** Allow WfRun-specific notes for this User Task. */
  notes?: VariableAssignment | undefined;
}

export interface EdgeCondition {
  comparator: Comparator;
  left: VariableAssignment | undefined;
  right: VariableAssignment | undefined;
}

export interface Edge {
  sinkNodeName: string;
  condition?: EdgeCondition | undefined;
}

export interface NopNode {
}

export interface SleepNode {
  rawSeconds?: VariableAssignment | undefined;
  timestamp?: VariableAssignment | undefined;
  isoDate?: VariableAssignment | undefined;
}

function createBaseWfSpec(): WfSpec {
  return {
    name: "",
    version: 0,
    createdAt: undefined,
    status: LHStatus.STARTING,
    threadSpecs: {},
    entrypointThreadName: "",
    retentionHours: 0,
  };
}

export const WfSpec = {
  encode(message: WfSpec, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.name !== "") {
      writer.uint32(10).string(message.name);
    }
    if (message.version !== 0) {
      writer.uint32(16).int32(message.version);
    }
    if (message.createdAt !== undefined) {
      Timestamp.encode(toTimestamp(message.createdAt), writer.uint32(26).fork()).ldelim();
    }
    if (message.status !== LHStatus.STARTING) {
      writer.uint32(32).int32(lHStatusToNumber(message.status));
    }
    Object.entries(message.threadSpecs).forEach(([key, value]) => {
      WfSpec_ThreadSpecsEntry.encode({ key: key as any, value }, writer.uint32(42).fork()).ldelim();
    });
    if (message.entrypointThreadName !== "") {
      writer.uint32(50).string(message.entrypointThreadName);
    }
    if (message.retentionHours !== 0) {
      writer.uint32(56).int32(message.retentionHours);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): WfSpec {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseWfSpec();
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

          message.createdAt = fromTimestamp(Timestamp.decode(reader, reader.uint32()));
          continue;
        case 4:
          if (tag !== 32) {
            break;
          }

          message.status = lHStatusFromJSON(reader.int32());
          continue;
        case 5:
          if (tag !== 42) {
            break;
          }

          const entry5 = WfSpec_ThreadSpecsEntry.decode(reader, reader.uint32());
          if (entry5.value !== undefined) {
            message.threadSpecs[entry5.key] = entry5.value;
          }
          continue;
        case 6:
          if (tag !== 50) {
            break;
          }

          message.entrypointThreadName = reader.string();
          continue;
        case 7:
          if (tag !== 56) {
            break;
          }

          message.retentionHours = reader.int32();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  fromJSON(object: any): WfSpec {
    return {
      name: isSet(object.name) ? globalThis.String(object.name) : "",
      version: isSet(object.version) ? globalThis.Number(object.version) : 0,
      createdAt: isSet(object.createdAt) ? globalThis.String(object.createdAt) : undefined,
      status: isSet(object.status) ? lHStatusFromJSON(object.status) : LHStatus.STARTING,
      threadSpecs: isObject(object.threadSpecs)
        ? Object.entries(object.threadSpecs).reduce<{ [key: string]: ThreadSpec }>((acc, [key, value]) => {
          acc[key] = ThreadSpec.fromJSON(value);
          return acc;
        }, {})
        : {},
      entrypointThreadName: isSet(object.entrypointThreadName) ? globalThis.String(object.entrypointThreadName) : "",
      retentionHours: isSet(object.retentionHours) ? globalThis.Number(object.retentionHours) : 0,
    };
  },

  toJSON(message: WfSpec): unknown {
    const obj: any = {};
    if (message.name !== "") {
      obj.name = message.name;
    }
    if (message.version !== 0) {
      obj.version = Math.round(message.version);
    }
    if (message.createdAt !== undefined) {
      obj.createdAt = message.createdAt;
    }
    if (message.status !== LHStatus.STARTING) {
      obj.status = lHStatusToJSON(message.status);
    }
    if (message.threadSpecs) {
      const entries = Object.entries(message.threadSpecs);
      if (entries.length > 0) {
        obj.threadSpecs = {};
        entries.forEach(([k, v]) => {
          obj.threadSpecs[k] = ThreadSpec.toJSON(v);
        });
      }
    }
    if (message.entrypointThreadName !== "") {
      obj.entrypointThreadName = message.entrypointThreadName;
    }
    if (message.retentionHours !== 0) {
      obj.retentionHours = Math.round(message.retentionHours);
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<WfSpec>, I>>(base?: I): WfSpec {
    return WfSpec.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<WfSpec>, I>>(object: I): WfSpec {
    const message = createBaseWfSpec();
    message.name = object.name ?? "";
    message.version = object.version ?? 0;
    message.createdAt = object.createdAt ?? undefined;
    message.status = object.status ?? LHStatus.STARTING;
    message.threadSpecs = Object.entries(object.threadSpecs ?? {}).reduce<{ [key: string]: ThreadSpec }>(
      (acc, [key, value]) => {
        if (value !== undefined) {
          acc[key] = ThreadSpec.fromPartial(value);
        }
        return acc;
      },
      {},
    );
    message.entrypointThreadName = object.entrypointThreadName ?? "";
    message.retentionHours = object.retentionHours ?? 0;
    return message;
  },
};

function createBaseWfSpec_ThreadSpecsEntry(): WfSpec_ThreadSpecsEntry {
  return { key: "", value: undefined };
}

export const WfSpec_ThreadSpecsEntry = {
  encode(message: WfSpec_ThreadSpecsEntry, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.key !== "") {
      writer.uint32(10).string(message.key);
    }
    if (message.value !== undefined) {
      ThreadSpec.encode(message.value, writer.uint32(18).fork()).ldelim();
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): WfSpec_ThreadSpecsEntry {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseWfSpec_ThreadSpecsEntry();
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

          message.value = ThreadSpec.decode(reader, reader.uint32());
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  fromJSON(object: any): WfSpec_ThreadSpecsEntry {
    return {
      key: isSet(object.key) ? globalThis.String(object.key) : "",
      value: isSet(object.value) ? ThreadSpec.fromJSON(object.value) : undefined,
    };
  },

  toJSON(message: WfSpec_ThreadSpecsEntry): unknown {
    const obj: any = {};
    if (message.key !== "") {
      obj.key = message.key;
    }
    if (message.value !== undefined) {
      obj.value = ThreadSpec.toJSON(message.value);
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<WfSpec_ThreadSpecsEntry>, I>>(base?: I): WfSpec_ThreadSpecsEntry {
    return WfSpec_ThreadSpecsEntry.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<WfSpec_ThreadSpecsEntry>, I>>(object: I): WfSpec_ThreadSpecsEntry {
    const message = createBaseWfSpec_ThreadSpecsEntry();
    message.key = object.key ?? "";
    message.value = (object.value !== undefined && object.value !== null)
      ? ThreadSpec.fromPartial(object.value)
      : undefined;
    return message;
  },
};

function createBaseThreadSpec(): ThreadSpec {
  return { nodes: {}, variableDefs: [], interruptDefs: [] };
}

export const ThreadSpec = {
  encode(message: ThreadSpec, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    Object.entries(message.nodes).forEach(([key, value]) => {
      ThreadSpec_NodesEntry.encode({ key: key as any, value }, writer.uint32(10).fork()).ldelim();
    });
    for (const v of message.variableDefs) {
      VariableDef.encode(v!, writer.uint32(18).fork()).ldelim();
    }
    for (const v of message.interruptDefs) {
      InterruptDef.encode(v!, writer.uint32(26).fork()).ldelim();
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): ThreadSpec {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseThreadSpec();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          const entry1 = ThreadSpec_NodesEntry.decode(reader, reader.uint32());
          if (entry1.value !== undefined) {
            message.nodes[entry1.key] = entry1.value;
          }
          continue;
        case 2:
          if (tag !== 18) {
            break;
          }

          message.variableDefs.push(VariableDef.decode(reader, reader.uint32()));
          continue;
        case 3:
          if (tag !== 26) {
            break;
          }

          message.interruptDefs.push(InterruptDef.decode(reader, reader.uint32()));
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  fromJSON(object: any): ThreadSpec {
    return {
      nodes: isObject(object.nodes)
        ? Object.entries(object.nodes).reduce<{ [key: string]: Node }>((acc, [key, value]) => {
          acc[key] = Node.fromJSON(value);
          return acc;
        }, {})
        : {},
      variableDefs: globalThis.Array.isArray(object?.variableDefs)
        ? object.variableDefs.map((e: any) => VariableDef.fromJSON(e))
        : [],
      interruptDefs: globalThis.Array.isArray(object?.interruptDefs)
        ? object.interruptDefs.map((e: any) => InterruptDef.fromJSON(e))
        : [],
    };
  },

  toJSON(message: ThreadSpec): unknown {
    const obj: any = {};
    if (message.nodes) {
      const entries = Object.entries(message.nodes);
      if (entries.length > 0) {
        obj.nodes = {};
        entries.forEach(([k, v]) => {
          obj.nodes[k] = Node.toJSON(v);
        });
      }
    }
    if (message.variableDefs?.length) {
      obj.variableDefs = message.variableDefs.map((e) => VariableDef.toJSON(e));
    }
    if (message.interruptDefs?.length) {
      obj.interruptDefs = message.interruptDefs.map((e) => InterruptDef.toJSON(e));
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<ThreadSpec>, I>>(base?: I): ThreadSpec {
    return ThreadSpec.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<ThreadSpec>, I>>(object: I): ThreadSpec {
    const message = createBaseThreadSpec();
    message.nodes = Object.entries(object.nodes ?? {}).reduce<{ [key: string]: Node }>((acc, [key, value]) => {
      if (value !== undefined) {
        acc[key] = Node.fromPartial(value);
      }
      return acc;
    }, {});
    message.variableDefs = object.variableDefs?.map((e) => VariableDef.fromPartial(e)) || [];
    message.interruptDefs = object.interruptDefs?.map((e) => InterruptDef.fromPartial(e)) || [];
    return message;
  },
};

function createBaseThreadSpec_NodesEntry(): ThreadSpec_NodesEntry {
  return { key: "", value: undefined };
}

export const ThreadSpec_NodesEntry = {
  encode(message: ThreadSpec_NodesEntry, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.key !== "") {
      writer.uint32(10).string(message.key);
    }
    if (message.value !== undefined) {
      Node.encode(message.value, writer.uint32(18).fork()).ldelim();
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): ThreadSpec_NodesEntry {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseThreadSpec_NodesEntry();
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

          message.value = Node.decode(reader, reader.uint32());
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  fromJSON(object: any): ThreadSpec_NodesEntry {
    return {
      key: isSet(object.key) ? globalThis.String(object.key) : "",
      value: isSet(object.value) ? Node.fromJSON(object.value) : undefined,
    };
  },

  toJSON(message: ThreadSpec_NodesEntry): unknown {
    const obj: any = {};
    if (message.key !== "") {
      obj.key = message.key;
    }
    if (message.value !== undefined) {
      obj.value = Node.toJSON(message.value);
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<ThreadSpec_NodesEntry>, I>>(base?: I): ThreadSpec_NodesEntry {
    return ThreadSpec_NodesEntry.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<ThreadSpec_NodesEntry>, I>>(object: I): ThreadSpec_NodesEntry {
    const message = createBaseThreadSpec_NodesEntry();
    message.key = object.key ?? "";
    message.value = (object.value !== undefined && object.value !== null) ? Node.fromPartial(object.value) : undefined;
    return message;
  },
};

function createBaseInterruptDef(): InterruptDef {
  return { externalEventDefName: "", handlerSpecName: "" };
}

export const InterruptDef = {
  encode(message: InterruptDef, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.externalEventDefName !== "") {
      writer.uint32(10).string(message.externalEventDefName);
    }
    if (message.handlerSpecName !== "") {
      writer.uint32(18).string(message.handlerSpecName);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): InterruptDef {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseInterruptDef();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.externalEventDefName = reader.string();
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

  fromJSON(object: any): InterruptDef {
    return {
      externalEventDefName: isSet(object.externalEventDefName) ? globalThis.String(object.externalEventDefName) : "",
      handlerSpecName: isSet(object.handlerSpecName) ? globalThis.String(object.handlerSpecName) : "",
    };
  },

  toJSON(message: InterruptDef): unknown {
    const obj: any = {};
    if (message.externalEventDefName !== "") {
      obj.externalEventDefName = message.externalEventDefName;
    }
    if (message.handlerSpecName !== "") {
      obj.handlerSpecName = message.handlerSpecName;
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<InterruptDef>, I>>(base?: I): InterruptDef {
    return InterruptDef.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<InterruptDef>, I>>(object: I): InterruptDef {
    const message = createBaseInterruptDef();
    message.externalEventDefName = object.externalEventDefName ?? "";
    message.handlerSpecName = object.handlerSpecName ?? "";
    return message;
  },
};

function createBaseStartThreadNode(): StartThreadNode {
  return { threadSpecName: "", variables: {} };
}

export const StartThreadNode = {
  encode(message: StartThreadNode, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.threadSpecName !== "") {
      writer.uint32(10).string(message.threadSpecName);
    }
    Object.entries(message.variables).forEach(([key, value]) => {
      StartThreadNode_VariablesEntry.encode({ key: key as any, value }, writer.uint32(18).fork()).ldelim();
    });
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): StartThreadNode {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseStartThreadNode();
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
          if (tag !== 18) {
            break;
          }

          const entry2 = StartThreadNode_VariablesEntry.decode(reader, reader.uint32());
          if (entry2.value !== undefined) {
            message.variables[entry2.key] = entry2.value;
          }
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  fromJSON(object: any): StartThreadNode {
    return {
      threadSpecName: isSet(object.threadSpecName) ? globalThis.String(object.threadSpecName) : "",
      variables: isObject(object.variables)
        ? Object.entries(object.variables).reduce<{ [key: string]: VariableAssignment }>((acc, [key, value]) => {
          acc[key] = VariableAssignment.fromJSON(value);
          return acc;
        }, {})
        : {},
    };
  },

  toJSON(message: StartThreadNode): unknown {
    const obj: any = {};
    if (message.threadSpecName !== "") {
      obj.threadSpecName = message.threadSpecName;
    }
    if (message.variables) {
      const entries = Object.entries(message.variables);
      if (entries.length > 0) {
        obj.variables = {};
        entries.forEach(([k, v]) => {
          obj.variables[k] = VariableAssignment.toJSON(v);
        });
      }
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<StartThreadNode>, I>>(base?: I): StartThreadNode {
    return StartThreadNode.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<StartThreadNode>, I>>(object: I): StartThreadNode {
    const message = createBaseStartThreadNode();
    message.threadSpecName = object.threadSpecName ?? "";
    message.variables = Object.entries(object.variables ?? {}).reduce<{ [key: string]: VariableAssignment }>(
      (acc, [key, value]) => {
        if (value !== undefined) {
          acc[key] = VariableAssignment.fromPartial(value);
        }
        return acc;
      },
      {},
    );
    return message;
  },
};

function createBaseStartThreadNode_VariablesEntry(): StartThreadNode_VariablesEntry {
  return { key: "", value: undefined };
}

export const StartThreadNode_VariablesEntry = {
  encode(message: StartThreadNode_VariablesEntry, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.key !== "") {
      writer.uint32(10).string(message.key);
    }
    if (message.value !== undefined) {
      VariableAssignment.encode(message.value, writer.uint32(18).fork()).ldelim();
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): StartThreadNode_VariablesEntry {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseStartThreadNode_VariablesEntry();
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

          message.value = VariableAssignment.decode(reader, reader.uint32());
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  fromJSON(object: any): StartThreadNode_VariablesEntry {
    return {
      key: isSet(object.key) ? globalThis.String(object.key) : "",
      value: isSet(object.value) ? VariableAssignment.fromJSON(object.value) : undefined,
    };
  },

  toJSON(message: StartThreadNode_VariablesEntry): unknown {
    const obj: any = {};
    if (message.key !== "") {
      obj.key = message.key;
    }
    if (message.value !== undefined) {
      obj.value = VariableAssignment.toJSON(message.value);
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<StartThreadNode_VariablesEntry>, I>>(base?: I): StartThreadNode_VariablesEntry {
    return StartThreadNode_VariablesEntry.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<StartThreadNode_VariablesEntry>, I>>(
    object: I,
  ): StartThreadNode_VariablesEntry {
    const message = createBaseStartThreadNode_VariablesEntry();
    message.key = object.key ?? "";
    message.value = (object.value !== undefined && object.value !== null)
      ? VariableAssignment.fromPartial(object.value)
      : undefined;
    return message;
  },
};

function createBaseStartMultipleThreadsNode(): StartMultipleThreadsNode {
  return { threadSpecName: "", variables: {}, iterable: undefined };
}

export const StartMultipleThreadsNode = {
  encode(message: StartMultipleThreadsNode, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.threadSpecName !== "") {
      writer.uint32(10).string(message.threadSpecName);
    }
    Object.entries(message.variables).forEach(([key, value]) => {
      StartMultipleThreadsNode_VariablesEntry.encode({ key: key as any, value }, writer.uint32(18).fork()).ldelim();
    });
    if (message.iterable !== undefined) {
      VariableAssignment.encode(message.iterable, writer.uint32(26).fork()).ldelim();
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): StartMultipleThreadsNode {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseStartMultipleThreadsNode();
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
          if (tag !== 18) {
            break;
          }

          const entry2 = StartMultipleThreadsNode_VariablesEntry.decode(reader, reader.uint32());
          if (entry2.value !== undefined) {
            message.variables[entry2.key] = entry2.value;
          }
          continue;
        case 3:
          if (tag !== 26) {
            break;
          }

          message.iterable = VariableAssignment.decode(reader, reader.uint32());
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  fromJSON(object: any): StartMultipleThreadsNode {
    return {
      threadSpecName: isSet(object.threadSpecName) ? globalThis.String(object.threadSpecName) : "",
      variables: isObject(object.variables)
        ? Object.entries(object.variables).reduce<{ [key: string]: VariableAssignment }>((acc, [key, value]) => {
          acc[key] = VariableAssignment.fromJSON(value);
          return acc;
        }, {})
        : {},
      iterable: isSet(object.iterable) ? VariableAssignment.fromJSON(object.iterable) : undefined,
    };
  },

  toJSON(message: StartMultipleThreadsNode): unknown {
    const obj: any = {};
    if (message.threadSpecName !== "") {
      obj.threadSpecName = message.threadSpecName;
    }
    if (message.variables) {
      const entries = Object.entries(message.variables);
      if (entries.length > 0) {
        obj.variables = {};
        entries.forEach(([k, v]) => {
          obj.variables[k] = VariableAssignment.toJSON(v);
        });
      }
    }
    if (message.iterable !== undefined) {
      obj.iterable = VariableAssignment.toJSON(message.iterable);
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<StartMultipleThreadsNode>, I>>(base?: I): StartMultipleThreadsNode {
    return StartMultipleThreadsNode.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<StartMultipleThreadsNode>, I>>(object: I): StartMultipleThreadsNode {
    const message = createBaseStartMultipleThreadsNode();
    message.threadSpecName = object.threadSpecName ?? "";
    message.variables = Object.entries(object.variables ?? {}).reduce<{ [key: string]: VariableAssignment }>(
      (acc, [key, value]) => {
        if (value !== undefined) {
          acc[key] = VariableAssignment.fromPartial(value);
        }
        return acc;
      },
      {},
    );
    message.iterable = (object.iterable !== undefined && object.iterable !== null)
      ? VariableAssignment.fromPartial(object.iterable)
      : undefined;
    return message;
  },
};

function createBaseStartMultipleThreadsNode_VariablesEntry(): StartMultipleThreadsNode_VariablesEntry {
  return { key: "", value: undefined };
}

export const StartMultipleThreadsNode_VariablesEntry = {
  encode(message: StartMultipleThreadsNode_VariablesEntry, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.key !== "") {
      writer.uint32(10).string(message.key);
    }
    if (message.value !== undefined) {
      VariableAssignment.encode(message.value, writer.uint32(18).fork()).ldelim();
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): StartMultipleThreadsNode_VariablesEntry {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseStartMultipleThreadsNode_VariablesEntry();
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

          message.value = VariableAssignment.decode(reader, reader.uint32());
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  fromJSON(object: any): StartMultipleThreadsNode_VariablesEntry {
    return {
      key: isSet(object.key) ? globalThis.String(object.key) : "",
      value: isSet(object.value) ? VariableAssignment.fromJSON(object.value) : undefined,
    };
  },

  toJSON(message: StartMultipleThreadsNode_VariablesEntry): unknown {
    const obj: any = {};
    if (message.key !== "") {
      obj.key = message.key;
    }
    if (message.value !== undefined) {
      obj.value = VariableAssignment.toJSON(message.value);
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<StartMultipleThreadsNode_VariablesEntry>, I>>(
    base?: I,
  ): StartMultipleThreadsNode_VariablesEntry {
    return StartMultipleThreadsNode_VariablesEntry.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<StartMultipleThreadsNode_VariablesEntry>, I>>(
    object: I,
  ): StartMultipleThreadsNode_VariablesEntry {
    const message = createBaseStartMultipleThreadsNode_VariablesEntry();
    message.key = object.key ?? "";
    message.value = (object.value !== undefined && object.value !== null)
      ? VariableAssignment.fromPartial(object.value)
      : undefined;
    return message;
  },
};

function createBaseFailureHandlerDef(): FailureHandlerDef {
  return { handlerSpecName: "", specificFailure: undefined, anyFailureOfType: undefined };
}

export const FailureHandlerDef = {
  encode(message: FailureHandlerDef, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.handlerSpecName !== "") {
      writer.uint32(18).string(message.handlerSpecName);
    }
    if (message.specificFailure !== undefined) {
      writer.uint32(10).string(message.specificFailure);
    }
    if (message.anyFailureOfType !== undefined) {
      writer.uint32(24).int32(failureHandlerDef_LHFailureTypeToNumber(message.anyFailureOfType));
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): FailureHandlerDef {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseFailureHandlerDef();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 2:
          if (tag !== 18) {
            break;
          }

          message.handlerSpecName = reader.string();
          continue;
        case 1:
          if (tag !== 10) {
            break;
          }

          message.specificFailure = reader.string();
          continue;
        case 3:
          if (tag !== 24) {
            break;
          }

          message.anyFailureOfType = failureHandlerDef_LHFailureTypeFromJSON(reader.int32());
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  fromJSON(object: any): FailureHandlerDef {
    return {
      handlerSpecName: isSet(object.handlerSpecName) ? globalThis.String(object.handlerSpecName) : "",
      specificFailure: isSet(object.specificFailure) ? globalThis.String(object.specificFailure) : undefined,
      anyFailureOfType: isSet(object.anyFailureOfType)
        ? failureHandlerDef_LHFailureTypeFromJSON(object.anyFailureOfType)
        : undefined,
    };
  },

  toJSON(message: FailureHandlerDef): unknown {
    const obj: any = {};
    if (message.handlerSpecName !== "") {
      obj.handlerSpecName = message.handlerSpecName;
    }
    if (message.specificFailure !== undefined) {
      obj.specificFailure = message.specificFailure;
    }
    if (message.anyFailureOfType !== undefined) {
      obj.anyFailureOfType = failureHandlerDef_LHFailureTypeToJSON(message.anyFailureOfType);
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<FailureHandlerDef>, I>>(base?: I): FailureHandlerDef {
    return FailureHandlerDef.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<FailureHandlerDef>, I>>(object: I): FailureHandlerDef {
    const message = createBaseFailureHandlerDef();
    message.handlerSpecName = object.handlerSpecName ?? "";
    message.specificFailure = object.specificFailure ?? undefined;
    message.anyFailureOfType = object.anyFailureOfType ?? undefined;
    return message;
  },
};

function createBaseWaitForThreadsNode(): WaitForThreadsNode {
  return { threads: [], threadList: undefined, policy: WaitForThreadsPolicy.STOP_ON_FAILURE };
}

export const WaitForThreadsNode = {
  encode(message: WaitForThreadsNode, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    for (const v of message.threads) {
      WaitForThreadsNode_ThreadToWaitFor.encode(v!, writer.uint32(10).fork()).ldelim();
    }
    if (message.threadList !== undefined) {
      VariableAssignment.encode(message.threadList, writer.uint32(26).fork()).ldelim();
    }
    if (message.policy !== WaitForThreadsPolicy.STOP_ON_FAILURE) {
      writer.uint32(16).int32(waitForThreadsPolicyToNumber(message.policy));
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): WaitForThreadsNode {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseWaitForThreadsNode();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.threads.push(WaitForThreadsNode_ThreadToWaitFor.decode(reader, reader.uint32()));
          continue;
        case 3:
          if (tag !== 26) {
            break;
          }

          message.threadList = VariableAssignment.decode(reader, reader.uint32());
          continue;
        case 2:
          if (tag !== 16) {
            break;
          }

          message.policy = waitForThreadsPolicyFromJSON(reader.int32());
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  fromJSON(object: any): WaitForThreadsNode {
    return {
      threads: globalThis.Array.isArray(object?.threads)
        ? object.threads.map((e: any) => WaitForThreadsNode_ThreadToWaitFor.fromJSON(e))
        : [],
      threadList: isSet(object.threadList) ? VariableAssignment.fromJSON(object.threadList) : undefined,
      policy: isSet(object.policy) ? waitForThreadsPolicyFromJSON(object.policy) : WaitForThreadsPolicy.STOP_ON_FAILURE,
    };
  },

  toJSON(message: WaitForThreadsNode): unknown {
    const obj: any = {};
    if (message.threads?.length) {
      obj.threads = message.threads.map((e) => WaitForThreadsNode_ThreadToWaitFor.toJSON(e));
    }
    if (message.threadList !== undefined) {
      obj.threadList = VariableAssignment.toJSON(message.threadList);
    }
    if (message.policy !== WaitForThreadsPolicy.STOP_ON_FAILURE) {
      obj.policy = waitForThreadsPolicyToJSON(message.policy);
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<WaitForThreadsNode>, I>>(base?: I): WaitForThreadsNode {
    return WaitForThreadsNode.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<WaitForThreadsNode>, I>>(object: I): WaitForThreadsNode {
    const message = createBaseWaitForThreadsNode();
    message.threads = object.threads?.map((e) => WaitForThreadsNode_ThreadToWaitFor.fromPartial(e)) || [];
    message.threadList = (object.threadList !== undefined && object.threadList !== null)
      ? VariableAssignment.fromPartial(object.threadList)
      : undefined;
    message.policy = object.policy ?? WaitForThreadsPolicy.STOP_ON_FAILURE;
    return message;
  },
};

function createBaseWaitForThreadsNode_ThreadToWaitFor(): WaitForThreadsNode_ThreadToWaitFor {
  return { threadRunNumber: undefined };
}

export const WaitForThreadsNode_ThreadToWaitFor = {
  encode(message: WaitForThreadsNode_ThreadToWaitFor, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.threadRunNumber !== undefined) {
      VariableAssignment.encode(message.threadRunNumber, writer.uint32(10).fork()).ldelim();
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): WaitForThreadsNode_ThreadToWaitFor {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseWaitForThreadsNode_ThreadToWaitFor();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.threadRunNumber = VariableAssignment.decode(reader, reader.uint32());
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  fromJSON(object: any): WaitForThreadsNode_ThreadToWaitFor {
    return {
      threadRunNumber: isSet(object.threadRunNumber) ? VariableAssignment.fromJSON(object.threadRunNumber) : undefined,
    };
  },

  toJSON(message: WaitForThreadsNode_ThreadToWaitFor): unknown {
    const obj: any = {};
    if (message.threadRunNumber !== undefined) {
      obj.threadRunNumber = VariableAssignment.toJSON(message.threadRunNumber);
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<WaitForThreadsNode_ThreadToWaitFor>, I>>(
    base?: I,
  ): WaitForThreadsNode_ThreadToWaitFor {
    return WaitForThreadsNode_ThreadToWaitFor.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<WaitForThreadsNode_ThreadToWaitFor>, I>>(
    object: I,
  ): WaitForThreadsNode_ThreadToWaitFor {
    const message = createBaseWaitForThreadsNode_ThreadToWaitFor();
    message.threadRunNumber = (object.threadRunNumber !== undefined && object.threadRunNumber !== null)
      ? VariableAssignment.fromPartial(object.threadRunNumber)
      : undefined;
    return message;
  },
};

function createBaseExternalEventNode(): ExternalEventNode {
  return { externalEventDefName: "", timeoutSeconds: undefined };
}

export const ExternalEventNode = {
  encode(message: ExternalEventNode, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.externalEventDefName !== "") {
      writer.uint32(10).string(message.externalEventDefName);
    }
    if (message.timeoutSeconds !== undefined) {
      VariableAssignment.encode(message.timeoutSeconds, writer.uint32(18).fork()).ldelim();
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): ExternalEventNode {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseExternalEventNode();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.externalEventDefName = reader.string();
          continue;
        case 2:
          if (tag !== 18) {
            break;
          }

          message.timeoutSeconds = VariableAssignment.decode(reader, reader.uint32());
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  fromJSON(object: any): ExternalEventNode {
    return {
      externalEventDefName: isSet(object.externalEventDefName) ? globalThis.String(object.externalEventDefName) : "",
      timeoutSeconds: isSet(object.timeoutSeconds) ? VariableAssignment.fromJSON(object.timeoutSeconds) : undefined,
    };
  },

  toJSON(message: ExternalEventNode): unknown {
    const obj: any = {};
    if (message.externalEventDefName !== "") {
      obj.externalEventDefName = message.externalEventDefName;
    }
    if (message.timeoutSeconds !== undefined) {
      obj.timeoutSeconds = VariableAssignment.toJSON(message.timeoutSeconds);
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<ExternalEventNode>, I>>(base?: I): ExternalEventNode {
    return ExternalEventNode.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<ExternalEventNode>, I>>(object: I): ExternalEventNode {
    const message = createBaseExternalEventNode();
    message.externalEventDefName = object.externalEventDefName ?? "";
    message.timeoutSeconds = (object.timeoutSeconds !== undefined && object.timeoutSeconds !== null)
      ? VariableAssignment.fromPartial(object.timeoutSeconds)
      : undefined;
    return message;
  },
};

function createBaseEntrypointNode(): EntrypointNode {
  return {};
}

export const EntrypointNode = {
  encode(_: EntrypointNode, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): EntrypointNode {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseEntrypointNode();
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

  fromJSON(_: any): EntrypointNode {
    return {};
  },

  toJSON(_: EntrypointNode): unknown {
    const obj: any = {};
    return obj;
  },

  create<I extends Exact<DeepPartial<EntrypointNode>, I>>(base?: I): EntrypointNode {
    return EntrypointNode.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<EntrypointNode>, I>>(_: I): EntrypointNode {
    const message = createBaseEntrypointNode();
    return message;
  },
};

function createBaseExitNode(): ExitNode {
  return { failureDef: undefined };
}

export const ExitNode = {
  encode(message: ExitNode, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.failureDef !== undefined) {
      FailureDef.encode(message.failureDef, writer.uint32(10).fork()).ldelim();
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): ExitNode {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseExitNode();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.failureDef = FailureDef.decode(reader, reader.uint32());
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  fromJSON(object: any): ExitNode {
    return { failureDef: isSet(object.failureDef) ? FailureDef.fromJSON(object.failureDef) : undefined };
  },

  toJSON(message: ExitNode): unknown {
    const obj: any = {};
    if (message.failureDef !== undefined) {
      obj.failureDef = FailureDef.toJSON(message.failureDef);
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<ExitNode>, I>>(base?: I): ExitNode {
    return ExitNode.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<ExitNode>, I>>(object: I): ExitNode {
    const message = createBaseExitNode();
    message.failureDef = (object.failureDef !== undefined && object.failureDef !== null)
      ? FailureDef.fromPartial(object.failureDef)
      : undefined;
    return message;
  },
};

function createBaseFailureDef(): FailureDef {
  return { failureName: "", message: "", content: undefined };
}

export const FailureDef = {
  encode(message: FailureDef, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.failureName !== "") {
      writer.uint32(10).string(message.failureName);
    }
    if (message.message !== "") {
      writer.uint32(18).string(message.message);
    }
    if (message.content !== undefined) {
      VariableAssignment.encode(message.content, writer.uint32(26).fork()).ldelim();
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): FailureDef {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseFailureDef();
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

          message.content = VariableAssignment.decode(reader, reader.uint32());
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  fromJSON(object: any): FailureDef {
    return {
      failureName: isSet(object.failureName) ? globalThis.String(object.failureName) : "",
      message: isSet(object.message) ? globalThis.String(object.message) : "",
      content: isSet(object.content) ? VariableAssignment.fromJSON(object.content) : undefined,
    };
  },

  toJSON(message: FailureDef): unknown {
    const obj: any = {};
    if (message.failureName !== "") {
      obj.failureName = message.failureName;
    }
    if (message.message !== "") {
      obj.message = message.message;
    }
    if (message.content !== undefined) {
      obj.content = VariableAssignment.toJSON(message.content);
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<FailureDef>, I>>(base?: I): FailureDef {
    return FailureDef.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<FailureDef>, I>>(object: I): FailureDef {
    const message = createBaseFailureDef();
    message.failureName = object.failureName ?? "";
    message.message = object.message ?? "";
    message.content = (object.content !== undefined && object.content !== null)
      ? VariableAssignment.fromPartial(object.content)
      : undefined;
    return message;
  },
};

function createBaseNode(): Node {
  return {
    outgoingEdges: [],
    variableMutations: [],
    failureHandlers: [],
    entrypoint: undefined,
    exit: undefined,
    task: undefined,
    externalEvent: undefined,
    startThread: undefined,
    waitForThreads: undefined,
    nop: undefined,
    sleep: undefined,
    userTask: undefined,
    startMultipleThreads: undefined,
  };
}

export const Node = {
  encode(message: Node, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    for (const v of message.outgoingEdges) {
      Edge.encode(v!, writer.uint32(10).fork()).ldelim();
    }
    for (const v of message.variableMutations) {
      VariableMutation.encode(v!, writer.uint32(18).fork()).ldelim();
    }
    for (const v of message.failureHandlers) {
      FailureHandlerDef.encode(v!, writer.uint32(34).fork()).ldelim();
    }
    if (message.entrypoint !== undefined) {
      EntrypointNode.encode(message.entrypoint, writer.uint32(42).fork()).ldelim();
    }
    if (message.exit !== undefined) {
      ExitNode.encode(message.exit, writer.uint32(50).fork()).ldelim();
    }
    if (message.task !== undefined) {
      TaskNode.encode(message.task, writer.uint32(58).fork()).ldelim();
    }
    if (message.externalEvent !== undefined) {
      ExternalEventNode.encode(message.externalEvent, writer.uint32(66).fork()).ldelim();
    }
    if (message.startThread !== undefined) {
      StartThreadNode.encode(message.startThread, writer.uint32(74).fork()).ldelim();
    }
    if (message.waitForThreads !== undefined) {
      WaitForThreadsNode.encode(message.waitForThreads, writer.uint32(82).fork()).ldelim();
    }
    if (message.nop !== undefined) {
      NopNode.encode(message.nop, writer.uint32(90).fork()).ldelim();
    }
    if (message.sleep !== undefined) {
      SleepNode.encode(message.sleep, writer.uint32(98).fork()).ldelim();
    }
    if (message.userTask !== undefined) {
      UserTaskNode.encode(message.userTask, writer.uint32(106).fork()).ldelim();
    }
    if (message.startMultipleThreads !== undefined) {
      StartMultipleThreadsNode.encode(message.startMultipleThreads, writer.uint32(122).fork()).ldelim();
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): Node {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseNode();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.outgoingEdges.push(Edge.decode(reader, reader.uint32()));
          continue;
        case 2:
          if (tag !== 18) {
            break;
          }

          message.variableMutations.push(VariableMutation.decode(reader, reader.uint32()));
          continue;
        case 4:
          if (tag !== 34) {
            break;
          }

          message.failureHandlers.push(FailureHandlerDef.decode(reader, reader.uint32()));
          continue;
        case 5:
          if (tag !== 42) {
            break;
          }

          message.entrypoint = EntrypointNode.decode(reader, reader.uint32());
          continue;
        case 6:
          if (tag !== 50) {
            break;
          }

          message.exit = ExitNode.decode(reader, reader.uint32());
          continue;
        case 7:
          if (tag !== 58) {
            break;
          }

          message.task = TaskNode.decode(reader, reader.uint32());
          continue;
        case 8:
          if (tag !== 66) {
            break;
          }

          message.externalEvent = ExternalEventNode.decode(reader, reader.uint32());
          continue;
        case 9:
          if (tag !== 74) {
            break;
          }

          message.startThread = StartThreadNode.decode(reader, reader.uint32());
          continue;
        case 10:
          if (tag !== 82) {
            break;
          }

          message.waitForThreads = WaitForThreadsNode.decode(reader, reader.uint32());
          continue;
        case 11:
          if (tag !== 90) {
            break;
          }

          message.nop = NopNode.decode(reader, reader.uint32());
          continue;
        case 12:
          if (tag !== 98) {
            break;
          }

          message.sleep = SleepNode.decode(reader, reader.uint32());
          continue;
        case 13:
          if (tag !== 106) {
            break;
          }

          message.userTask = UserTaskNode.decode(reader, reader.uint32());
          continue;
        case 15:
          if (tag !== 122) {
            break;
          }

          message.startMultipleThreads = StartMultipleThreadsNode.decode(reader, reader.uint32());
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  fromJSON(object: any): Node {
    return {
      outgoingEdges: globalThis.Array.isArray(object?.outgoingEdges)
        ? object.outgoingEdges.map((e: any) => Edge.fromJSON(e))
        : [],
      variableMutations: globalThis.Array.isArray(object?.variableMutations)
        ? object.variableMutations.map((e: any) => VariableMutation.fromJSON(e))
        : [],
      failureHandlers: globalThis.Array.isArray(object?.failureHandlers)
        ? object.failureHandlers.map((e: any) => FailureHandlerDef.fromJSON(e))
        : [],
      entrypoint: isSet(object.entrypoint) ? EntrypointNode.fromJSON(object.entrypoint) : undefined,
      exit: isSet(object.exit) ? ExitNode.fromJSON(object.exit) : undefined,
      task: isSet(object.task) ? TaskNode.fromJSON(object.task) : undefined,
      externalEvent: isSet(object.externalEvent) ? ExternalEventNode.fromJSON(object.externalEvent) : undefined,
      startThread: isSet(object.startThread) ? StartThreadNode.fromJSON(object.startThread) : undefined,
      waitForThreads: isSet(object.waitForThreads) ? WaitForThreadsNode.fromJSON(object.waitForThreads) : undefined,
      nop: isSet(object.nop) ? NopNode.fromJSON(object.nop) : undefined,
      sleep: isSet(object.sleep) ? SleepNode.fromJSON(object.sleep) : undefined,
      userTask: isSet(object.userTask) ? UserTaskNode.fromJSON(object.userTask) : undefined,
      startMultipleThreads: isSet(object.startMultipleThreads)
        ? StartMultipleThreadsNode.fromJSON(object.startMultipleThreads)
        : undefined,
    };
  },

  toJSON(message: Node): unknown {
    const obj: any = {};
    if (message.outgoingEdges?.length) {
      obj.outgoingEdges = message.outgoingEdges.map((e) => Edge.toJSON(e));
    }
    if (message.variableMutations?.length) {
      obj.variableMutations = message.variableMutations.map((e) => VariableMutation.toJSON(e));
    }
    if (message.failureHandlers?.length) {
      obj.failureHandlers = message.failureHandlers.map((e) => FailureHandlerDef.toJSON(e));
    }
    if (message.entrypoint !== undefined) {
      obj.entrypoint = EntrypointNode.toJSON(message.entrypoint);
    }
    if (message.exit !== undefined) {
      obj.exit = ExitNode.toJSON(message.exit);
    }
    if (message.task !== undefined) {
      obj.task = TaskNode.toJSON(message.task);
    }
    if (message.externalEvent !== undefined) {
      obj.externalEvent = ExternalEventNode.toJSON(message.externalEvent);
    }
    if (message.startThread !== undefined) {
      obj.startThread = StartThreadNode.toJSON(message.startThread);
    }
    if (message.waitForThreads !== undefined) {
      obj.waitForThreads = WaitForThreadsNode.toJSON(message.waitForThreads);
    }
    if (message.nop !== undefined) {
      obj.nop = NopNode.toJSON(message.nop);
    }
    if (message.sleep !== undefined) {
      obj.sleep = SleepNode.toJSON(message.sleep);
    }
    if (message.userTask !== undefined) {
      obj.userTask = UserTaskNode.toJSON(message.userTask);
    }
    if (message.startMultipleThreads !== undefined) {
      obj.startMultipleThreads = StartMultipleThreadsNode.toJSON(message.startMultipleThreads);
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<Node>, I>>(base?: I): Node {
    return Node.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<Node>, I>>(object: I): Node {
    const message = createBaseNode();
    message.outgoingEdges = object.outgoingEdges?.map((e) => Edge.fromPartial(e)) || [];
    message.variableMutations = object.variableMutations?.map((e) => VariableMutation.fromPartial(e)) || [];
    message.failureHandlers = object.failureHandlers?.map((e) => FailureHandlerDef.fromPartial(e)) || [];
    message.entrypoint = (object.entrypoint !== undefined && object.entrypoint !== null)
      ? EntrypointNode.fromPartial(object.entrypoint)
      : undefined;
    message.exit = (object.exit !== undefined && object.exit !== null) ? ExitNode.fromPartial(object.exit) : undefined;
    message.task = (object.task !== undefined && object.task !== null) ? TaskNode.fromPartial(object.task) : undefined;
    message.externalEvent = (object.externalEvent !== undefined && object.externalEvent !== null)
      ? ExternalEventNode.fromPartial(object.externalEvent)
      : undefined;
    message.startThread = (object.startThread !== undefined && object.startThread !== null)
      ? StartThreadNode.fromPartial(object.startThread)
      : undefined;
    message.waitForThreads = (object.waitForThreads !== undefined && object.waitForThreads !== null)
      ? WaitForThreadsNode.fromPartial(object.waitForThreads)
      : undefined;
    message.nop = (object.nop !== undefined && object.nop !== null) ? NopNode.fromPartial(object.nop) : undefined;
    message.sleep = (object.sleep !== undefined && object.sleep !== null)
      ? SleepNode.fromPartial(object.sleep)
      : undefined;
    message.userTask = (object.userTask !== undefined && object.userTask !== null)
      ? UserTaskNode.fromPartial(object.userTask)
      : undefined;
    message.startMultipleThreads = (object.startMultipleThreads !== undefined && object.startMultipleThreads !== null)
      ? StartMultipleThreadsNode.fromPartial(object.startMultipleThreads)
      : undefined;
    return message;
  },
};

function createBaseUserTaskNode(): UserTaskNode {
  return {
    userTaskDefName: "",
    userGroup: undefined,
    userId: undefined,
    actions: [],
    userTaskDefVersion: undefined,
    notes: undefined,
  };
}

export const UserTaskNode = {
  encode(message: UserTaskNode, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.userTaskDefName !== "") {
      writer.uint32(10).string(message.userTaskDefName);
    }
    if (message.userGroup !== undefined) {
      VariableAssignment.encode(message.userGroup, writer.uint32(18).fork()).ldelim();
    }
    if (message.userId !== undefined) {
      VariableAssignment.encode(message.userId, writer.uint32(26).fork()).ldelim();
    }
    for (const v of message.actions) {
      UTActionTrigger.encode(v!, writer.uint32(34).fork()).ldelim();
    }
    if (message.userTaskDefVersion !== undefined) {
      writer.uint32(40).int32(message.userTaskDefVersion);
    }
    if (message.notes !== undefined) {
      VariableAssignment.encode(message.notes, writer.uint32(50).fork()).ldelim();
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): UserTaskNode {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseUserTaskNode();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.userTaskDefName = reader.string();
          continue;
        case 2:
          if (tag !== 18) {
            break;
          }

          message.userGroup = VariableAssignment.decode(reader, reader.uint32());
          continue;
        case 3:
          if (tag !== 26) {
            break;
          }

          message.userId = VariableAssignment.decode(reader, reader.uint32());
          continue;
        case 4:
          if (tag !== 34) {
            break;
          }

          message.actions.push(UTActionTrigger.decode(reader, reader.uint32()));
          continue;
        case 5:
          if (tag !== 40) {
            break;
          }

          message.userTaskDefVersion = reader.int32();
          continue;
        case 6:
          if (tag !== 50) {
            break;
          }

          message.notes = VariableAssignment.decode(reader, reader.uint32());
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  fromJSON(object: any): UserTaskNode {
    return {
      userTaskDefName: isSet(object.userTaskDefName) ? globalThis.String(object.userTaskDefName) : "",
      userGroup: isSet(object.userGroup) ? VariableAssignment.fromJSON(object.userGroup) : undefined,
      userId: isSet(object.userId) ? VariableAssignment.fromJSON(object.userId) : undefined,
      actions: globalThis.Array.isArray(object?.actions)
        ? object.actions.map((e: any) => UTActionTrigger.fromJSON(e))
        : [],
      userTaskDefVersion: isSet(object.userTaskDefVersion) ? globalThis.Number(object.userTaskDefVersion) : undefined,
      notes: isSet(object.notes) ? VariableAssignment.fromJSON(object.notes) : undefined,
    };
  },

  toJSON(message: UserTaskNode): unknown {
    const obj: any = {};
    if (message.userTaskDefName !== "") {
      obj.userTaskDefName = message.userTaskDefName;
    }
    if (message.userGroup !== undefined) {
      obj.userGroup = VariableAssignment.toJSON(message.userGroup);
    }
    if (message.userId !== undefined) {
      obj.userId = VariableAssignment.toJSON(message.userId);
    }
    if (message.actions?.length) {
      obj.actions = message.actions.map((e) => UTActionTrigger.toJSON(e));
    }
    if (message.userTaskDefVersion !== undefined) {
      obj.userTaskDefVersion = Math.round(message.userTaskDefVersion);
    }
    if (message.notes !== undefined) {
      obj.notes = VariableAssignment.toJSON(message.notes);
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<UserTaskNode>, I>>(base?: I): UserTaskNode {
    return UserTaskNode.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<UserTaskNode>, I>>(object: I): UserTaskNode {
    const message = createBaseUserTaskNode();
    message.userTaskDefName = object.userTaskDefName ?? "";
    message.userGroup = (object.userGroup !== undefined && object.userGroup !== null)
      ? VariableAssignment.fromPartial(object.userGroup)
      : undefined;
    message.userId = (object.userId !== undefined && object.userId !== null)
      ? VariableAssignment.fromPartial(object.userId)
      : undefined;
    message.actions = object.actions?.map((e) => UTActionTrigger.fromPartial(e)) || [];
    message.userTaskDefVersion = object.userTaskDefVersion ?? undefined;
    message.notes = (object.notes !== undefined && object.notes !== null)
      ? VariableAssignment.fromPartial(object.notes)
      : undefined;
    return message;
  },
};

function createBaseEdgeCondition(): EdgeCondition {
  return { comparator: Comparator.LESS_THAN, left: undefined, right: undefined };
}

export const EdgeCondition = {
  encode(message: EdgeCondition, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.comparator !== Comparator.LESS_THAN) {
      writer.uint32(8).int32(comparatorToNumber(message.comparator));
    }
    if (message.left !== undefined) {
      VariableAssignment.encode(message.left, writer.uint32(18).fork()).ldelim();
    }
    if (message.right !== undefined) {
      VariableAssignment.encode(message.right, writer.uint32(26).fork()).ldelim();
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): EdgeCondition {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseEdgeCondition();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 8) {
            break;
          }

          message.comparator = comparatorFromJSON(reader.int32());
          continue;
        case 2:
          if (tag !== 18) {
            break;
          }

          message.left = VariableAssignment.decode(reader, reader.uint32());
          continue;
        case 3:
          if (tag !== 26) {
            break;
          }

          message.right = VariableAssignment.decode(reader, reader.uint32());
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  fromJSON(object: any): EdgeCondition {
    return {
      comparator: isSet(object.comparator) ? comparatorFromJSON(object.comparator) : Comparator.LESS_THAN,
      left: isSet(object.left) ? VariableAssignment.fromJSON(object.left) : undefined,
      right: isSet(object.right) ? VariableAssignment.fromJSON(object.right) : undefined,
    };
  },

  toJSON(message: EdgeCondition): unknown {
    const obj: any = {};
    if (message.comparator !== Comparator.LESS_THAN) {
      obj.comparator = comparatorToJSON(message.comparator);
    }
    if (message.left !== undefined) {
      obj.left = VariableAssignment.toJSON(message.left);
    }
    if (message.right !== undefined) {
      obj.right = VariableAssignment.toJSON(message.right);
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<EdgeCondition>, I>>(base?: I): EdgeCondition {
    return EdgeCondition.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<EdgeCondition>, I>>(object: I): EdgeCondition {
    const message = createBaseEdgeCondition();
    message.comparator = object.comparator ?? Comparator.LESS_THAN;
    message.left = (object.left !== undefined && object.left !== null)
      ? VariableAssignment.fromPartial(object.left)
      : undefined;
    message.right = (object.right !== undefined && object.right !== null)
      ? VariableAssignment.fromPartial(object.right)
      : undefined;
    return message;
  },
};

function createBaseEdge(): Edge {
  return { sinkNodeName: "", condition: undefined };
}

export const Edge = {
  encode(message: Edge, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.sinkNodeName !== "") {
      writer.uint32(10).string(message.sinkNodeName);
    }
    if (message.condition !== undefined) {
      EdgeCondition.encode(message.condition, writer.uint32(18).fork()).ldelim();
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): Edge {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseEdge();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.sinkNodeName = reader.string();
          continue;
        case 2:
          if (tag !== 18) {
            break;
          }

          message.condition = EdgeCondition.decode(reader, reader.uint32());
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  fromJSON(object: any): Edge {
    return {
      sinkNodeName: isSet(object.sinkNodeName) ? globalThis.String(object.sinkNodeName) : "",
      condition: isSet(object.condition) ? EdgeCondition.fromJSON(object.condition) : undefined,
    };
  },

  toJSON(message: Edge): unknown {
    const obj: any = {};
    if (message.sinkNodeName !== "") {
      obj.sinkNodeName = message.sinkNodeName;
    }
    if (message.condition !== undefined) {
      obj.condition = EdgeCondition.toJSON(message.condition);
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<Edge>, I>>(base?: I): Edge {
    return Edge.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<Edge>, I>>(object: I): Edge {
    const message = createBaseEdge();
    message.sinkNodeName = object.sinkNodeName ?? "";
    message.condition = (object.condition !== undefined && object.condition !== null)
      ? EdgeCondition.fromPartial(object.condition)
      : undefined;
    return message;
  },
};

function createBaseNopNode(): NopNode {
  return {};
}

export const NopNode = {
  encode(_: NopNode, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): NopNode {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseNopNode();
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

  fromJSON(_: any): NopNode {
    return {};
  },

  toJSON(_: NopNode): unknown {
    const obj: any = {};
    return obj;
  },

  create<I extends Exact<DeepPartial<NopNode>, I>>(base?: I): NopNode {
    return NopNode.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<NopNode>, I>>(_: I): NopNode {
    const message = createBaseNopNode();
    return message;
  },
};

function createBaseSleepNode(): SleepNode {
  return { rawSeconds: undefined, timestamp: undefined, isoDate: undefined };
}

export const SleepNode = {
  encode(message: SleepNode, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.rawSeconds !== undefined) {
      VariableAssignment.encode(message.rawSeconds, writer.uint32(10).fork()).ldelim();
    }
    if (message.timestamp !== undefined) {
      VariableAssignment.encode(message.timestamp, writer.uint32(18).fork()).ldelim();
    }
    if (message.isoDate !== undefined) {
      VariableAssignment.encode(message.isoDate, writer.uint32(26).fork()).ldelim();
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): SleepNode {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseSleepNode();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.rawSeconds = VariableAssignment.decode(reader, reader.uint32());
          continue;
        case 2:
          if (tag !== 18) {
            break;
          }

          message.timestamp = VariableAssignment.decode(reader, reader.uint32());
          continue;
        case 3:
          if (tag !== 26) {
            break;
          }

          message.isoDate = VariableAssignment.decode(reader, reader.uint32());
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  fromJSON(object: any): SleepNode {
    return {
      rawSeconds: isSet(object.rawSeconds) ? VariableAssignment.fromJSON(object.rawSeconds) : undefined,
      timestamp: isSet(object.timestamp) ? VariableAssignment.fromJSON(object.timestamp) : undefined,
      isoDate: isSet(object.isoDate) ? VariableAssignment.fromJSON(object.isoDate) : undefined,
    };
  },

  toJSON(message: SleepNode): unknown {
    const obj: any = {};
    if (message.rawSeconds !== undefined) {
      obj.rawSeconds = VariableAssignment.toJSON(message.rawSeconds);
    }
    if (message.timestamp !== undefined) {
      obj.timestamp = VariableAssignment.toJSON(message.timestamp);
    }
    if (message.isoDate !== undefined) {
      obj.isoDate = VariableAssignment.toJSON(message.isoDate);
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<SleepNode>, I>>(base?: I): SleepNode {
    return SleepNode.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<SleepNode>, I>>(object: I): SleepNode {
    const message = createBaseSleepNode();
    message.rawSeconds = (object.rawSeconds !== undefined && object.rawSeconds !== null)
      ? VariableAssignment.fromPartial(object.rawSeconds)
      : undefined;
    message.timestamp = (object.timestamp !== undefined && object.timestamp !== null)
      ? VariableAssignment.fromPartial(object.timestamp)
      : undefined;
    message.isoDate = (object.isoDate !== undefined && object.isoDate !== null)
      ? VariableAssignment.fromPartial(object.isoDate)
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
  const seconds = date.getTime() / 1_000;
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
