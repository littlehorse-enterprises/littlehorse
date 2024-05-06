/* eslint-disable */
import Long from "long";
import * as _m0 from "protobufjs/minimal";
import {
  MetadataStatus,
  metadataStatusFromJSON,
  metadataStatusToJSON,
  metadataStatusToNumber,
  VariableType,
  variableTypeFromJSON,
  variableTypeToJSON,
  variableTypeToNumber,
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
import { ExternalEventDefId, WfSpecId, WorkflowEventDefId } from "./object_id";

export const protobufPackage = "littlehorse";

export enum WfRunVariableAccessLevel {
  PUBLIC_VAR = "PUBLIC_VAR",
  PRIVATE_VAR = "PRIVATE_VAR",
  INHERITED_VAR = "INHERITED_VAR",
  UNRECOGNIZED = "UNRECOGNIZED",
}

export function wfRunVariableAccessLevelFromJSON(object: any): WfRunVariableAccessLevel {
  switch (object) {
    case 0:
    case "PUBLIC_VAR":
      return WfRunVariableAccessLevel.PUBLIC_VAR;
    case 1:
    case "PRIVATE_VAR":
      return WfRunVariableAccessLevel.PRIVATE_VAR;
    case 2:
    case "INHERITED_VAR":
      return WfRunVariableAccessLevel.INHERITED_VAR;
    case -1:
    case "UNRECOGNIZED":
    default:
      return WfRunVariableAccessLevel.UNRECOGNIZED;
  }
}

export function wfRunVariableAccessLevelToJSON(object: WfRunVariableAccessLevel): string {
  switch (object) {
    case WfRunVariableAccessLevel.PUBLIC_VAR:
      return "PUBLIC_VAR";
    case WfRunVariableAccessLevel.PRIVATE_VAR:
      return "PRIVATE_VAR";
    case WfRunVariableAccessLevel.INHERITED_VAR:
      return "INHERITED_VAR";
    case WfRunVariableAccessLevel.UNRECOGNIZED:
    default:
      return "UNRECOGNIZED";
  }
}

export function wfRunVariableAccessLevelToNumber(object: WfRunVariableAccessLevel): number {
  switch (object) {
    case WfRunVariableAccessLevel.PUBLIC_VAR:
      return 0;
    case WfRunVariableAccessLevel.PRIVATE_VAR:
      return 1;
    case WfRunVariableAccessLevel.INHERITED_VAR:
      return 2;
    case WfRunVariableAccessLevel.UNRECOGNIZED:
    default:
      return -1;
  }
}

export interface WfSpec {
  id: WfSpecId | undefined;
  createdAt: string | undefined;
  frozenVariables: ThreadVarDef[];
  /** to be used for WfSpec Status, i.e. ACTIVE/TERMINATING/ARCHIVED */
  status: MetadataStatus;
  threadSpecs: { [key: string]: ThreadSpec };
  entrypointThreadName: string;
  retentionPolicy?: WorkflowRetentionPolicy | undefined;
  migration?:
    | WfSpecVersionMigration
    | undefined;
  /**
   * Reference to the parent WfSpec. If this is set, all WfRun's for this WfSpec must be the
   * child of a WfRun belonging to the referenced WfSpec.
   */
  parentWfSpec?: WfSpec_ParentWfSpecReference | undefined;
}

export interface WfSpec_ThreadSpecsEntry {
  key: string;
  value: ThreadSpec | undefined;
}

/**
 * Reference to another WfSpec. If a WfSpec has a ParentWfSpecReference, then all
 * WfRun's for that WfSpec *MUST* be the child of a WfRun of the provided WfSpec; meaning
 * that the RunWf RPC must provide a `parent_wf_run_id` that belongs to the specified
 * WfSpec.
 *
 * Currently, only reference by names is supported.
 */
export interface WfSpec_ParentWfSpecReference {
  /** Name of the Parent WfSpec */
  wfSpecName: string;
  /**
   * FOR NOW: no validation of variables on parent. In the future we will pass
   * wf_spec_major_version, but we should probably examine the rules for
   * evolution in the future.
   */
  wfSpecMajorVersion: number;
}

export interface WorkflowRetentionPolicy {
  /**
   * Delete all WfRun's X seconds after they terminate, regardless of
   * status.
   */
  secondsAfterWfTermination?: number | undefined;
}

export interface JsonIndex {
  fieldPath: string;
  fieldType: VariableType;
}

export interface SearchableVariableDef {
  /** Future: Add index information (local/remote/etc) */
  varDef: VariableDef | undefined;
}

export interface ThreadVarDef {
  varDef: VariableDef | undefined;
  required: boolean;
  searchable: boolean;
  jsonIndexes: JsonIndex[];
  accessLevel: WfRunVariableAccessLevel;
}

export interface ThreadSpec {
  nodes: { [key: string]: Node };
  variableDefs: ThreadVarDef[];
  interruptDefs: InterruptDef[];
  retentionPolicy?: ThreadRetentionPolicy | undefined;
}

export interface ThreadSpec_NodesEntry {
  key: string;
  value: Node | undefined;
}

export interface ThreadRetentionPolicy {
  /**
   * Delete associated ThreadRun's X seconds after they terminate, regardless
   * of status.
   */
  secondsAfterThreadTermination?: number | undefined;
}

export interface InterruptDef {
  externalEventDefId: ExternalEventDefId | undefined;
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
  threads?: WaitForThreadsNode_ThreadsToWaitFor | undefined;
  threadList?: VariableAssignment | undefined;
  perThreadFailureHandlers: FailureHandlerDef[];
}

export interface WaitForThreadsNode_ThreadToWaitFor {
  threadRunNumber: VariableAssignment | undefined;
}

export interface WaitForThreadsNode_ThreadsToWaitFor {
  threads: WaitForThreadsNode_ThreadToWaitFor[];
}

export interface ExternalEventNode {
  externalEventDefId: ExternalEventDefId | undefined;
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
  throwEvent?: ThrowEventNode | undefined;
}

/** A SubNode that throws a WorkflowEvent of a specific type. */
export interface ThrowEventNode {
  /** The WorkflowEventDefId of the WorkflowEvent that is thrown */
  eventDefId:
    | WorkflowEventDefId
    | undefined;
  /** A VariableAssignment defining the content of the WorkflowEvent that is thrown */
  content: VariableAssignment | undefined;
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
  notes?:
    | VariableAssignment
    | undefined;
  /** Specifies the name of the exception thrown when the User Task is canceled */
  onCancellationExceptionName?: VariableAssignment | undefined;
}

export interface EdgeCondition {
  comparator: Comparator;
  left: VariableAssignment | undefined;
  right: VariableAssignment | undefined;
}

export interface Edge {
  sinkNodeName: string;
  condition?: EdgeCondition | undefined;
  variableMutations: VariableMutation[];
}

export interface NopNode {
}

export interface SleepNode {
  rawSeconds?: VariableAssignment | undefined;
  timestamp?: VariableAssignment | undefined;
  isoDate?: VariableAssignment | undefined;
}

export interface WfSpecVersionMigration {
  newMajorVersion: number;
  newRevision: number;
  threadSpecMigrations: { [key: string]: ThreadSpecMigration };
}

export interface WfSpecVersionMigration_ThreadSpecMigrationsEntry {
  key: string;
  value: ThreadSpecMigration | undefined;
}

export interface ThreadSpecMigration {
  newThreadSpecName: string;
  nodeMigrations: { [key: string]: NodeMigration };
}

export interface ThreadSpecMigration_NodeMigrationsEntry {
  key: string;
  value: NodeMigration | undefined;
}

export interface NodeMigration {
  newNodeName: string;
}

function createBaseWfSpec(): WfSpec {
  return {
    id: undefined,
    createdAt: undefined,
    frozenVariables: [],
    status: MetadataStatus.ACTIVE,
    threadSpecs: {},
    entrypointThreadName: "",
    retentionPolicy: undefined,
    migration: undefined,
    parentWfSpec: undefined,
  };
}

export const WfSpec = {
  encode(message: WfSpec, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.id !== undefined) {
      WfSpecId.encode(message.id, writer.uint32(10).fork()).ldelim();
    }
    if (message.createdAt !== undefined) {
      Timestamp.encode(toTimestamp(message.createdAt), writer.uint32(18).fork()).ldelim();
    }
    for (const v of message.frozenVariables) {
      ThreadVarDef.encode(v!, writer.uint32(26).fork()).ldelim();
    }
    if (message.status !== MetadataStatus.ACTIVE) {
      writer.uint32(32).int32(metadataStatusToNumber(message.status));
    }
    Object.entries(message.threadSpecs).forEach(([key, value]) => {
      WfSpec_ThreadSpecsEntry.encode({ key: key as any, value }, writer.uint32(42).fork()).ldelim();
    });
    if (message.entrypointThreadName !== "") {
      writer.uint32(50).string(message.entrypointThreadName);
    }
    if (message.retentionPolicy !== undefined) {
      WorkflowRetentionPolicy.encode(message.retentionPolicy, writer.uint32(58).fork()).ldelim();
    }
    if (message.migration !== undefined) {
      WfSpecVersionMigration.encode(message.migration, writer.uint32(66).fork()).ldelim();
    }
    if (message.parentWfSpec !== undefined) {
      WfSpec_ParentWfSpecReference.encode(message.parentWfSpec, writer.uint32(74).fork()).ldelim();
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

          message.id = WfSpecId.decode(reader, reader.uint32());
          continue;
        case 2:
          if (tag !== 18) {
            break;
          }

          message.createdAt = fromTimestamp(Timestamp.decode(reader, reader.uint32()));
          continue;
        case 3:
          if (tag !== 26) {
            break;
          }

          message.frozenVariables.push(ThreadVarDef.decode(reader, reader.uint32()));
          continue;
        case 4:
          if (tag !== 32) {
            break;
          }

          message.status = metadataStatusFromJSON(reader.int32());
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
          if (tag !== 58) {
            break;
          }

          message.retentionPolicy = WorkflowRetentionPolicy.decode(reader, reader.uint32());
          continue;
        case 8:
          if (tag !== 66) {
            break;
          }

          message.migration = WfSpecVersionMigration.decode(reader, reader.uint32());
          continue;
        case 9:
          if (tag !== 74) {
            break;
          }

          message.parentWfSpec = WfSpec_ParentWfSpecReference.decode(reader, reader.uint32());
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
      id: isSet(object.id) ? WfSpecId.fromJSON(object.id) : undefined,
      createdAt: isSet(object.createdAt) ? globalThis.String(object.createdAt) : undefined,
      frozenVariables: globalThis.Array.isArray(object?.frozenVariables)
        ? object.frozenVariables.map((e: any) => ThreadVarDef.fromJSON(e))
        : [],
      status: isSet(object.status) ? metadataStatusFromJSON(object.status) : MetadataStatus.ACTIVE,
      threadSpecs: isObject(object.threadSpecs)
        ? Object.entries(object.threadSpecs).reduce<{ [key: string]: ThreadSpec }>((acc, [key, value]) => {
          acc[key] = ThreadSpec.fromJSON(value);
          return acc;
        }, {})
        : {},
      entrypointThreadName: isSet(object.entrypointThreadName) ? globalThis.String(object.entrypointThreadName) : "",
      retentionPolicy: isSet(object.retentionPolicy)
        ? WorkflowRetentionPolicy.fromJSON(object.retentionPolicy)
        : undefined,
      migration: isSet(object.migration) ? WfSpecVersionMigration.fromJSON(object.migration) : undefined,
      parentWfSpec: isSet(object.parentWfSpec) ? WfSpec_ParentWfSpecReference.fromJSON(object.parentWfSpec) : undefined,
    };
  },

  toJSON(message: WfSpec): unknown {
    const obj: any = {};
    if (message.id !== undefined) {
      obj.id = WfSpecId.toJSON(message.id);
    }
    if (message.createdAt !== undefined) {
      obj.createdAt = message.createdAt;
    }
    if (message.frozenVariables?.length) {
      obj.frozenVariables = message.frozenVariables.map((e) => ThreadVarDef.toJSON(e));
    }
    if (message.status !== MetadataStatus.ACTIVE) {
      obj.status = metadataStatusToJSON(message.status);
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
    if (message.retentionPolicy !== undefined) {
      obj.retentionPolicy = WorkflowRetentionPolicy.toJSON(message.retentionPolicy);
    }
    if (message.migration !== undefined) {
      obj.migration = WfSpecVersionMigration.toJSON(message.migration);
    }
    if (message.parentWfSpec !== undefined) {
      obj.parentWfSpec = WfSpec_ParentWfSpecReference.toJSON(message.parentWfSpec);
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<WfSpec>, I>>(base?: I): WfSpec {
    return WfSpec.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<WfSpec>, I>>(object: I): WfSpec {
    const message = createBaseWfSpec();
    message.id = (object.id !== undefined && object.id !== null) ? WfSpecId.fromPartial(object.id) : undefined;
    message.createdAt = object.createdAt ?? undefined;
    message.frozenVariables = object.frozenVariables?.map((e) => ThreadVarDef.fromPartial(e)) || [];
    message.status = object.status ?? MetadataStatus.ACTIVE;
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
    message.retentionPolicy = (object.retentionPolicy !== undefined && object.retentionPolicy !== null)
      ? WorkflowRetentionPolicy.fromPartial(object.retentionPolicy)
      : undefined;
    message.migration = (object.migration !== undefined && object.migration !== null)
      ? WfSpecVersionMigration.fromPartial(object.migration)
      : undefined;
    message.parentWfSpec = (object.parentWfSpec !== undefined && object.parentWfSpec !== null)
      ? WfSpec_ParentWfSpecReference.fromPartial(object.parentWfSpec)
      : undefined;
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

function createBaseWfSpec_ParentWfSpecReference(): WfSpec_ParentWfSpecReference {
  return { wfSpecName: "", wfSpecMajorVersion: 0 };
}

export const WfSpec_ParentWfSpecReference = {
  encode(message: WfSpec_ParentWfSpecReference, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.wfSpecName !== "") {
      writer.uint32(10).string(message.wfSpecName);
    }
    if (message.wfSpecMajorVersion !== 0) {
      writer.uint32(16).int32(message.wfSpecMajorVersion);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): WfSpec_ParentWfSpecReference {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseWfSpec_ParentWfSpecReference();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.wfSpecName = reader.string();
          continue;
        case 2:
          if (tag !== 16) {
            break;
          }

          message.wfSpecMajorVersion = reader.int32();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  fromJSON(object: any): WfSpec_ParentWfSpecReference {
    return {
      wfSpecName: isSet(object.wfSpecName) ? globalThis.String(object.wfSpecName) : "",
      wfSpecMajorVersion: isSet(object.wfSpecMajorVersion) ? globalThis.Number(object.wfSpecMajorVersion) : 0,
    };
  },

  toJSON(message: WfSpec_ParentWfSpecReference): unknown {
    const obj: any = {};
    if (message.wfSpecName !== "") {
      obj.wfSpecName = message.wfSpecName;
    }
    if (message.wfSpecMajorVersion !== 0) {
      obj.wfSpecMajorVersion = Math.round(message.wfSpecMajorVersion);
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<WfSpec_ParentWfSpecReference>, I>>(base?: I): WfSpec_ParentWfSpecReference {
    return WfSpec_ParentWfSpecReference.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<WfSpec_ParentWfSpecReference>, I>>(object: I): WfSpec_ParentWfSpecReference {
    const message = createBaseWfSpec_ParentWfSpecReference();
    message.wfSpecName = object.wfSpecName ?? "";
    message.wfSpecMajorVersion = object.wfSpecMajorVersion ?? 0;
    return message;
  },
};

function createBaseWorkflowRetentionPolicy(): WorkflowRetentionPolicy {
  return { secondsAfterWfTermination: undefined };
}

export const WorkflowRetentionPolicy = {
  encode(message: WorkflowRetentionPolicy, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.secondsAfterWfTermination !== undefined) {
      writer.uint32(8).int64(message.secondsAfterWfTermination);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): WorkflowRetentionPolicy {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseWorkflowRetentionPolicy();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 8) {
            break;
          }

          message.secondsAfterWfTermination = longToNumber(reader.int64() as Long);
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  fromJSON(object: any): WorkflowRetentionPolicy {
    return {
      secondsAfterWfTermination: isSet(object.secondsAfterWfTermination)
        ? globalThis.Number(object.secondsAfterWfTermination)
        : undefined,
    };
  },

  toJSON(message: WorkflowRetentionPolicy): unknown {
    const obj: any = {};
    if (message.secondsAfterWfTermination !== undefined) {
      obj.secondsAfterWfTermination = Math.round(message.secondsAfterWfTermination);
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<WorkflowRetentionPolicy>, I>>(base?: I): WorkflowRetentionPolicy {
    return WorkflowRetentionPolicy.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<WorkflowRetentionPolicy>, I>>(object: I): WorkflowRetentionPolicy {
    const message = createBaseWorkflowRetentionPolicy();
    message.secondsAfterWfTermination = object.secondsAfterWfTermination ?? undefined;
    return message;
  },
};

function createBaseJsonIndex(): JsonIndex {
  return { fieldPath: "", fieldType: VariableType.JSON_OBJ };
}

export const JsonIndex = {
  encode(message: JsonIndex, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.fieldPath !== "") {
      writer.uint32(10).string(message.fieldPath);
    }
    if (message.fieldType !== VariableType.JSON_OBJ) {
      writer.uint32(16).int32(variableTypeToNumber(message.fieldType));
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): JsonIndex {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseJsonIndex();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.fieldPath = reader.string();
          continue;
        case 2:
          if (tag !== 16) {
            break;
          }

          message.fieldType = variableTypeFromJSON(reader.int32());
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  fromJSON(object: any): JsonIndex {
    return {
      fieldPath: isSet(object.fieldPath) ? globalThis.String(object.fieldPath) : "",
      fieldType: isSet(object.fieldType) ? variableTypeFromJSON(object.fieldType) : VariableType.JSON_OBJ,
    };
  },

  toJSON(message: JsonIndex): unknown {
    const obj: any = {};
    if (message.fieldPath !== "") {
      obj.fieldPath = message.fieldPath;
    }
    if (message.fieldType !== VariableType.JSON_OBJ) {
      obj.fieldType = variableTypeToJSON(message.fieldType);
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<JsonIndex>, I>>(base?: I): JsonIndex {
    return JsonIndex.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<JsonIndex>, I>>(object: I): JsonIndex {
    const message = createBaseJsonIndex();
    message.fieldPath = object.fieldPath ?? "";
    message.fieldType = object.fieldType ?? VariableType.JSON_OBJ;
    return message;
  },
};

function createBaseSearchableVariableDef(): SearchableVariableDef {
  return { varDef: undefined };
}

export const SearchableVariableDef = {
  encode(message: SearchableVariableDef, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.varDef !== undefined) {
      VariableDef.encode(message.varDef, writer.uint32(10).fork()).ldelim();
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): SearchableVariableDef {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseSearchableVariableDef();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.varDef = VariableDef.decode(reader, reader.uint32());
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  fromJSON(object: any): SearchableVariableDef {
    return { varDef: isSet(object.varDef) ? VariableDef.fromJSON(object.varDef) : undefined };
  },

  toJSON(message: SearchableVariableDef): unknown {
    const obj: any = {};
    if (message.varDef !== undefined) {
      obj.varDef = VariableDef.toJSON(message.varDef);
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<SearchableVariableDef>, I>>(base?: I): SearchableVariableDef {
    return SearchableVariableDef.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<SearchableVariableDef>, I>>(object: I): SearchableVariableDef {
    const message = createBaseSearchableVariableDef();
    message.varDef = (object.varDef !== undefined && object.varDef !== null)
      ? VariableDef.fromPartial(object.varDef)
      : undefined;
    return message;
  },
};

function createBaseThreadVarDef(): ThreadVarDef {
  return {
    varDef: undefined,
    required: false,
    searchable: false,
    jsonIndexes: [],
    accessLevel: WfRunVariableAccessLevel.PUBLIC_VAR,
  };
}

export const ThreadVarDef = {
  encode(message: ThreadVarDef, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.varDef !== undefined) {
      VariableDef.encode(message.varDef, writer.uint32(10).fork()).ldelim();
    }
    if (message.required !== false) {
      writer.uint32(16).bool(message.required);
    }
    if (message.searchable !== false) {
      writer.uint32(24).bool(message.searchable);
    }
    for (const v of message.jsonIndexes) {
      JsonIndex.encode(v!, writer.uint32(34).fork()).ldelim();
    }
    if (message.accessLevel !== WfRunVariableAccessLevel.PUBLIC_VAR) {
      writer.uint32(40).int32(wfRunVariableAccessLevelToNumber(message.accessLevel));
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): ThreadVarDef {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseThreadVarDef();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.varDef = VariableDef.decode(reader, reader.uint32());
          continue;
        case 2:
          if (tag !== 16) {
            break;
          }

          message.required = reader.bool();
          continue;
        case 3:
          if (tag !== 24) {
            break;
          }

          message.searchable = reader.bool();
          continue;
        case 4:
          if (tag !== 34) {
            break;
          }

          message.jsonIndexes.push(JsonIndex.decode(reader, reader.uint32()));
          continue;
        case 5:
          if (tag !== 40) {
            break;
          }

          message.accessLevel = wfRunVariableAccessLevelFromJSON(reader.int32());
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  fromJSON(object: any): ThreadVarDef {
    return {
      varDef: isSet(object.varDef) ? VariableDef.fromJSON(object.varDef) : undefined,
      required: isSet(object.required) ? globalThis.Boolean(object.required) : false,
      searchable: isSet(object.searchable) ? globalThis.Boolean(object.searchable) : false,
      jsonIndexes: globalThis.Array.isArray(object?.jsonIndexes)
        ? object.jsonIndexes.map((e: any) => JsonIndex.fromJSON(e))
        : [],
      accessLevel: isSet(object.accessLevel)
        ? wfRunVariableAccessLevelFromJSON(object.accessLevel)
        : WfRunVariableAccessLevel.PUBLIC_VAR,
    };
  },

  toJSON(message: ThreadVarDef): unknown {
    const obj: any = {};
    if (message.varDef !== undefined) {
      obj.varDef = VariableDef.toJSON(message.varDef);
    }
    if (message.required !== false) {
      obj.required = message.required;
    }
    if (message.searchable !== false) {
      obj.searchable = message.searchable;
    }
    if (message.jsonIndexes?.length) {
      obj.jsonIndexes = message.jsonIndexes.map((e) => JsonIndex.toJSON(e));
    }
    if (message.accessLevel !== WfRunVariableAccessLevel.PUBLIC_VAR) {
      obj.accessLevel = wfRunVariableAccessLevelToJSON(message.accessLevel);
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<ThreadVarDef>, I>>(base?: I): ThreadVarDef {
    return ThreadVarDef.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<ThreadVarDef>, I>>(object: I): ThreadVarDef {
    const message = createBaseThreadVarDef();
    message.varDef = (object.varDef !== undefined && object.varDef !== null)
      ? VariableDef.fromPartial(object.varDef)
      : undefined;
    message.required = object.required ?? false;
    message.searchable = object.searchable ?? false;
    message.jsonIndexes = object.jsonIndexes?.map((e) => JsonIndex.fromPartial(e)) || [];
    message.accessLevel = object.accessLevel ?? WfRunVariableAccessLevel.PUBLIC_VAR;
    return message;
  },
};

function createBaseThreadSpec(): ThreadSpec {
  return { nodes: {}, variableDefs: [], interruptDefs: [], retentionPolicy: undefined };
}

export const ThreadSpec = {
  encode(message: ThreadSpec, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    Object.entries(message.nodes).forEach(([key, value]) => {
      ThreadSpec_NodesEntry.encode({ key: key as any, value }, writer.uint32(10).fork()).ldelim();
    });
    for (const v of message.variableDefs) {
      ThreadVarDef.encode(v!, writer.uint32(18).fork()).ldelim();
    }
    for (const v of message.interruptDefs) {
      InterruptDef.encode(v!, writer.uint32(26).fork()).ldelim();
    }
    if (message.retentionPolicy !== undefined) {
      ThreadRetentionPolicy.encode(message.retentionPolicy, writer.uint32(34).fork()).ldelim();
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

          message.variableDefs.push(ThreadVarDef.decode(reader, reader.uint32()));
          continue;
        case 3:
          if (tag !== 26) {
            break;
          }

          message.interruptDefs.push(InterruptDef.decode(reader, reader.uint32()));
          continue;
        case 4:
          if (tag !== 34) {
            break;
          }

          message.retentionPolicy = ThreadRetentionPolicy.decode(reader, reader.uint32());
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
        ? object.variableDefs.map((e: any) => ThreadVarDef.fromJSON(e))
        : [],
      interruptDefs: globalThis.Array.isArray(object?.interruptDefs)
        ? object.interruptDefs.map((e: any) => InterruptDef.fromJSON(e))
        : [],
      retentionPolicy: isSet(object.retentionPolicy)
        ? ThreadRetentionPolicy.fromJSON(object.retentionPolicy)
        : undefined,
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
      obj.variableDefs = message.variableDefs.map((e) => ThreadVarDef.toJSON(e));
    }
    if (message.interruptDefs?.length) {
      obj.interruptDefs = message.interruptDefs.map((e) => InterruptDef.toJSON(e));
    }
    if (message.retentionPolicy !== undefined) {
      obj.retentionPolicy = ThreadRetentionPolicy.toJSON(message.retentionPolicy);
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
    message.variableDefs = object.variableDefs?.map((e) => ThreadVarDef.fromPartial(e)) || [];
    message.interruptDefs = object.interruptDefs?.map((e) => InterruptDef.fromPartial(e)) || [];
    message.retentionPolicy = (object.retentionPolicy !== undefined && object.retentionPolicy !== null)
      ? ThreadRetentionPolicy.fromPartial(object.retentionPolicy)
      : undefined;
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

function createBaseThreadRetentionPolicy(): ThreadRetentionPolicy {
  return { secondsAfterThreadTermination: undefined };
}

export const ThreadRetentionPolicy = {
  encode(message: ThreadRetentionPolicy, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.secondsAfterThreadTermination !== undefined) {
      writer.uint32(8).int64(message.secondsAfterThreadTermination);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): ThreadRetentionPolicy {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseThreadRetentionPolicy();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 8) {
            break;
          }

          message.secondsAfterThreadTermination = longToNumber(reader.int64() as Long);
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  fromJSON(object: any): ThreadRetentionPolicy {
    return {
      secondsAfterThreadTermination: isSet(object.secondsAfterThreadTermination)
        ? globalThis.Number(object.secondsAfterThreadTermination)
        : undefined,
    };
  },

  toJSON(message: ThreadRetentionPolicy): unknown {
    const obj: any = {};
    if (message.secondsAfterThreadTermination !== undefined) {
      obj.secondsAfterThreadTermination = Math.round(message.secondsAfterThreadTermination);
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<ThreadRetentionPolicy>, I>>(base?: I): ThreadRetentionPolicy {
    return ThreadRetentionPolicy.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<ThreadRetentionPolicy>, I>>(object: I): ThreadRetentionPolicy {
    const message = createBaseThreadRetentionPolicy();
    message.secondsAfterThreadTermination = object.secondsAfterThreadTermination ?? undefined;
    return message;
  },
};

function createBaseInterruptDef(): InterruptDef {
  return { externalEventDefId: undefined, handlerSpecName: "" };
}

export const InterruptDef = {
  encode(message: InterruptDef, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.externalEventDefId !== undefined) {
      ExternalEventDefId.encode(message.externalEventDefId, writer.uint32(10).fork()).ldelim();
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

          message.externalEventDefId = ExternalEventDefId.decode(reader, reader.uint32());
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
      externalEventDefId: isSet(object.externalEventDefId)
        ? ExternalEventDefId.fromJSON(object.externalEventDefId)
        : undefined,
      handlerSpecName: isSet(object.handlerSpecName) ? globalThis.String(object.handlerSpecName) : "",
    };
  },

  toJSON(message: InterruptDef): unknown {
    const obj: any = {};
    if (message.externalEventDefId !== undefined) {
      obj.externalEventDefId = ExternalEventDefId.toJSON(message.externalEventDefId);
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
    message.externalEventDefId = (object.externalEventDefId !== undefined && object.externalEventDefId !== null)
      ? ExternalEventDefId.fromPartial(object.externalEventDefId)
      : undefined;
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
  return { threads: undefined, threadList: undefined, perThreadFailureHandlers: [] };
}

export const WaitForThreadsNode = {
  encode(message: WaitForThreadsNode, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.threads !== undefined) {
      WaitForThreadsNode_ThreadsToWaitFor.encode(message.threads, writer.uint32(10).fork()).ldelim();
    }
    if (message.threadList !== undefined) {
      VariableAssignment.encode(message.threadList, writer.uint32(18).fork()).ldelim();
    }
    for (const v of message.perThreadFailureHandlers) {
      FailureHandlerDef.encode(v!, writer.uint32(26).fork()).ldelim();
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

          message.threads = WaitForThreadsNode_ThreadsToWaitFor.decode(reader, reader.uint32());
          continue;
        case 2:
          if (tag !== 18) {
            break;
          }

          message.threadList = VariableAssignment.decode(reader, reader.uint32());
          continue;
        case 3:
          if (tag !== 26) {
            break;
          }

          message.perThreadFailureHandlers.push(FailureHandlerDef.decode(reader, reader.uint32()));
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
      threads: isSet(object.threads) ? WaitForThreadsNode_ThreadsToWaitFor.fromJSON(object.threads) : undefined,
      threadList: isSet(object.threadList) ? VariableAssignment.fromJSON(object.threadList) : undefined,
      perThreadFailureHandlers: globalThis.Array.isArray(object?.perThreadFailureHandlers)
        ? object.perThreadFailureHandlers.map((e: any) => FailureHandlerDef.fromJSON(e))
        : [],
    };
  },

  toJSON(message: WaitForThreadsNode): unknown {
    const obj: any = {};
    if (message.threads !== undefined) {
      obj.threads = WaitForThreadsNode_ThreadsToWaitFor.toJSON(message.threads);
    }
    if (message.threadList !== undefined) {
      obj.threadList = VariableAssignment.toJSON(message.threadList);
    }
    if (message.perThreadFailureHandlers?.length) {
      obj.perThreadFailureHandlers = message.perThreadFailureHandlers.map((e) => FailureHandlerDef.toJSON(e));
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<WaitForThreadsNode>, I>>(base?: I): WaitForThreadsNode {
    return WaitForThreadsNode.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<WaitForThreadsNode>, I>>(object: I): WaitForThreadsNode {
    const message = createBaseWaitForThreadsNode();
    message.threads = (object.threads !== undefined && object.threads !== null)
      ? WaitForThreadsNode_ThreadsToWaitFor.fromPartial(object.threads)
      : undefined;
    message.threadList = (object.threadList !== undefined && object.threadList !== null)
      ? VariableAssignment.fromPartial(object.threadList)
      : undefined;
    message.perThreadFailureHandlers = object.perThreadFailureHandlers?.map((e) => FailureHandlerDef.fromPartial(e)) ||
      [];
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

function createBaseWaitForThreadsNode_ThreadsToWaitFor(): WaitForThreadsNode_ThreadsToWaitFor {
  return { threads: [] };
}

export const WaitForThreadsNode_ThreadsToWaitFor = {
  encode(message: WaitForThreadsNode_ThreadsToWaitFor, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    for (const v of message.threads) {
      WaitForThreadsNode_ThreadToWaitFor.encode(v!, writer.uint32(10).fork()).ldelim();
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): WaitForThreadsNode_ThreadsToWaitFor {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseWaitForThreadsNode_ThreadsToWaitFor();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.threads.push(WaitForThreadsNode_ThreadToWaitFor.decode(reader, reader.uint32()));
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  fromJSON(object: any): WaitForThreadsNode_ThreadsToWaitFor {
    return {
      threads: globalThis.Array.isArray(object?.threads)
        ? object.threads.map((e: any) => WaitForThreadsNode_ThreadToWaitFor.fromJSON(e))
        : [],
    };
  },

  toJSON(message: WaitForThreadsNode_ThreadsToWaitFor): unknown {
    const obj: any = {};
    if (message.threads?.length) {
      obj.threads = message.threads.map((e) => WaitForThreadsNode_ThreadToWaitFor.toJSON(e));
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<WaitForThreadsNode_ThreadsToWaitFor>, I>>(
    base?: I,
  ): WaitForThreadsNode_ThreadsToWaitFor {
    return WaitForThreadsNode_ThreadsToWaitFor.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<WaitForThreadsNode_ThreadsToWaitFor>, I>>(
    object: I,
  ): WaitForThreadsNode_ThreadsToWaitFor {
    const message = createBaseWaitForThreadsNode_ThreadsToWaitFor();
    message.threads = object.threads?.map((e) => WaitForThreadsNode_ThreadToWaitFor.fromPartial(e)) || [];
    return message;
  },
};

function createBaseExternalEventNode(): ExternalEventNode {
  return { externalEventDefId: undefined, timeoutSeconds: undefined };
}

export const ExternalEventNode = {
  encode(message: ExternalEventNode, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.externalEventDefId !== undefined) {
      ExternalEventDefId.encode(message.externalEventDefId, writer.uint32(10).fork()).ldelim();
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

          message.externalEventDefId = ExternalEventDefId.decode(reader, reader.uint32());
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
      externalEventDefId: isSet(object.externalEventDefId)
        ? ExternalEventDefId.fromJSON(object.externalEventDefId)
        : undefined,
      timeoutSeconds: isSet(object.timeoutSeconds) ? VariableAssignment.fromJSON(object.timeoutSeconds) : undefined,
    };
  },

  toJSON(message: ExternalEventNode): unknown {
    const obj: any = {};
    if (message.externalEventDefId !== undefined) {
      obj.externalEventDefId = ExternalEventDefId.toJSON(message.externalEventDefId);
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
    message.externalEventDefId = (object.externalEventDefId !== undefined && object.externalEventDefId !== null)
      ? ExternalEventDefId.fromPartial(object.externalEventDefId)
      : undefined;
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
    throwEvent: undefined,
  };
}

export const Node = {
  encode(message: Node, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    for (const v of message.outgoingEdges) {
      Edge.encode(v!, writer.uint32(10).fork()).ldelim();
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
    if (message.throwEvent !== undefined) {
      ThrowEventNode.encode(message.throwEvent, writer.uint32(130).fork()).ldelim();
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
        case 16:
          if (tag !== 130) {
            break;
          }

          message.throwEvent = ThrowEventNode.decode(reader, reader.uint32());
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
      throwEvent: isSet(object.throwEvent) ? ThrowEventNode.fromJSON(object.throwEvent) : undefined,
    };
  },

  toJSON(message: Node): unknown {
    const obj: any = {};
    if (message.outgoingEdges?.length) {
      obj.outgoingEdges = message.outgoingEdges.map((e) => Edge.toJSON(e));
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
    if (message.throwEvent !== undefined) {
      obj.throwEvent = ThrowEventNode.toJSON(message.throwEvent);
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<Node>, I>>(base?: I): Node {
    return Node.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<Node>, I>>(object: I): Node {
    const message = createBaseNode();
    message.outgoingEdges = object.outgoingEdges?.map((e) => Edge.fromPartial(e)) || [];
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
    message.throwEvent = (object.throwEvent !== undefined && object.throwEvent !== null)
      ? ThrowEventNode.fromPartial(object.throwEvent)
      : undefined;
    return message;
  },
};

function createBaseThrowEventNode(): ThrowEventNode {
  return { eventDefId: undefined, content: undefined };
}

export const ThrowEventNode = {
  encode(message: ThrowEventNode, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.eventDefId !== undefined) {
      WorkflowEventDefId.encode(message.eventDefId, writer.uint32(10).fork()).ldelim();
    }
    if (message.content !== undefined) {
      VariableAssignment.encode(message.content, writer.uint32(18).fork()).ldelim();
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): ThrowEventNode {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseThrowEventNode();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.eventDefId = WorkflowEventDefId.decode(reader, reader.uint32());
          continue;
        case 2:
          if (tag !== 18) {
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

  fromJSON(object: any): ThrowEventNode {
    return {
      eventDefId: isSet(object.eventDefId) ? WorkflowEventDefId.fromJSON(object.eventDefId) : undefined,
      content: isSet(object.content) ? VariableAssignment.fromJSON(object.content) : undefined,
    };
  },

  toJSON(message: ThrowEventNode): unknown {
    const obj: any = {};
    if (message.eventDefId !== undefined) {
      obj.eventDefId = WorkflowEventDefId.toJSON(message.eventDefId);
    }
    if (message.content !== undefined) {
      obj.content = VariableAssignment.toJSON(message.content);
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<ThrowEventNode>, I>>(base?: I): ThrowEventNode {
    return ThrowEventNode.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<ThrowEventNode>, I>>(object: I): ThrowEventNode {
    const message = createBaseThrowEventNode();
    message.eventDefId = (object.eventDefId !== undefined && object.eventDefId !== null)
      ? WorkflowEventDefId.fromPartial(object.eventDefId)
      : undefined;
    message.content = (object.content !== undefined && object.content !== null)
      ? VariableAssignment.fromPartial(object.content)
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
    onCancellationExceptionName: undefined,
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
    if (message.onCancellationExceptionName !== undefined) {
      VariableAssignment.encode(message.onCancellationExceptionName, writer.uint32(58).fork()).ldelim();
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
        case 7:
          if (tag !== 58) {
            break;
          }

          message.onCancellationExceptionName = VariableAssignment.decode(reader, reader.uint32());
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
      onCancellationExceptionName: isSet(object.onCancellationExceptionName)
        ? VariableAssignment.fromJSON(object.onCancellationExceptionName)
        : undefined,
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
    if (message.onCancellationExceptionName !== undefined) {
      obj.onCancellationExceptionName = VariableAssignment.toJSON(message.onCancellationExceptionName);
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
    message.onCancellationExceptionName =
      (object.onCancellationExceptionName !== undefined && object.onCancellationExceptionName !== null)
        ? VariableAssignment.fromPartial(object.onCancellationExceptionName)
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
  return { sinkNodeName: "", condition: undefined, variableMutations: [] };
}

export const Edge = {
  encode(message: Edge, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.sinkNodeName !== "") {
      writer.uint32(10).string(message.sinkNodeName);
    }
    if (message.condition !== undefined) {
      EdgeCondition.encode(message.condition, writer.uint32(18).fork()).ldelim();
    }
    for (const v of message.variableMutations) {
      VariableMutation.encode(v!, writer.uint32(26).fork()).ldelim();
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
        case 3:
          if (tag !== 26) {
            break;
          }

          message.variableMutations.push(VariableMutation.decode(reader, reader.uint32()));
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
      variableMutations: globalThis.Array.isArray(object?.variableMutations)
        ? object.variableMutations.map((e: any) => VariableMutation.fromJSON(e))
        : [],
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
    if (message.variableMutations?.length) {
      obj.variableMutations = message.variableMutations.map((e) => VariableMutation.toJSON(e));
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
    message.variableMutations = object.variableMutations?.map((e) => VariableMutation.fromPartial(e)) || [];
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

function createBaseWfSpecVersionMigration(): WfSpecVersionMigration {
  return { newMajorVersion: 0, newRevision: 0, threadSpecMigrations: {} };
}

export const WfSpecVersionMigration = {
  encode(message: WfSpecVersionMigration, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.newMajorVersion !== 0) {
      writer.uint32(8).int32(message.newMajorVersion);
    }
    if (message.newRevision !== 0) {
      writer.uint32(16).int32(message.newRevision);
    }
    Object.entries(message.threadSpecMigrations).forEach(([key, value]) => {
      WfSpecVersionMigration_ThreadSpecMigrationsEntry.encode({ key: key as any, value }, writer.uint32(26).fork())
        .ldelim();
    });
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): WfSpecVersionMigration {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseWfSpecVersionMigration();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 8) {
            break;
          }

          message.newMajorVersion = reader.int32();
          continue;
        case 2:
          if (tag !== 16) {
            break;
          }

          message.newRevision = reader.int32();
          continue;
        case 3:
          if (tag !== 26) {
            break;
          }

          const entry3 = WfSpecVersionMigration_ThreadSpecMigrationsEntry.decode(reader, reader.uint32());
          if (entry3.value !== undefined) {
            message.threadSpecMigrations[entry3.key] = entry3.value;
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

  fromJSON(object: any): WfSpecVersionMigration {
    return {
      newMajorVersion: isSet(object.newMajorVersion) ? globalThis.Number(object.newMajorVersion) : 0,
      newRevision: isSet(object.newRevision) ? globalThis.Number(object.newRevision) : 0,
      threadSpecMigrations: isObject(object.threadSpecMigrations)
        ? Object.entries(object.threadSpecMigrations).reduce<{ [key: string]: ThreadSpecMigration }>(
          (acc, [key, value]) => {
            acc[key] = ThreadSpecMigration.fromJSON(value);
            return acc;
          },
          {},
        )
        : {},
    };
  },

  toJSON(message: WfSpecVersionMigration): unknown {
    const obj: any = {};
    if (message.newMajorVersion !== 0) {
      obj.newMajorVersion = Math.round(message.newMajorVersion);
    }
    if (message.newRevision !== 0) {
      obj.newRevision = Math.round(message.newRevision);
    }
    if (message.threadSpecMigrations) {
      const entries = Object.entries(message.threadSpecMigrations);
      if (entries.length > 0) {
        obj.threadSpecMigrations = {};
        entries.forEach(([k, v]) => {
          obj.threadSpecMigrations[k] = ThreadSpecMigration.toJSON(v);
        });
      }
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<WfSpecVersionMigration>, I>>(base?: I): WfSpecVersionMigration {
    return WfSpecVersionMigration.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<WfSpecVersionMigration>, I>>(object: I): WfSpecVersionMigration {
    const message = createBaseWfSpecVersionMigration();
    message.newMajorVersion = object.newMajorVersion ?? 0;
    message.newRevision = object.newRevision ?? 0;
    message.threadSpecMigrations = Object.entries(object.threadSpecMigrations ?? {}).reduce<
      { [key: string]: ThreadSpecMigration }
    >((acc, [key, value]) => {
      if (value !== undefined) {
        acc[key] = ThreadSpecMigration.fromPartial(value);
      }
      return acc;
    }, {});
    return message;
  },
};

function createBaseWfSpecVersionMigration_ThreadSpecMigrationsEntry(): WfSpecVersionMigration_ThreadSpecMigrationsEntry {
  return { key: "", value: undefined };
}

export const WfSpecVersionMigration_ThreadSpecMigrationsEntry = {
  encode(
    message: WfSpecVersionMigration_ThreadSpecMigrationsEntry,
    writer: _m0.Writer = _m0.Writer.create(),
  ): _m0.Writer {
    if (message.key !== "") {
      writer.uint32(10).string(message.key);
    }
    if (message.value !== undefined) {
      ThreadSpecMigration.encode(message.value, writer.uint32(18).fork()).ldelim();
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): WfSpecVersionMigration_ThreadSpecMigrationsEntry {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseWfSpecVersionMigration_ThreadSpecMigrationsEntry();
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

          message.value = ThreadSpecMigration.decode(reader, reader.uint32());
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  fromJSON(object: any): WfSpecVersionMigration_ThreadSpecMigrationsEntry {
    return {
      key: isSet(object.key) ? globalThis.String(object.key) : "",
      value: isSet(object.value) ? ThreadSpecMigration.fromJSON(object.value) : undefined,
    };
  },

  toJSON(message: WfSpecVersionMigration_ThreadSpecMigrationsEntry): unknown {
    const obj: any = {};
    if (message.key !== "") {
      obj.key = message.key;
    }
    if (message.value !== undefined) {
      obj.value = ThreadSpecMigration.toJSON(message.value);
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<WfSpecVersionMigration_ThreadSpecMigrationsEntry>, I>>(
    base?: I,
  ): WfSpecVersionMigration_ThreadSpecMigrationsEntry {
    return WfSpecVersionMigration_ThreadSpecMigrationsEntry.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<WfSpecVersionMigration_ThreadSpecMigrationsEntry>, I>>(
    object: I,
  ): WfSpecVersionMigration_ThreadSpecMigrationsEntry {
    const message = createBaseWfSpecVersionMigration_ThreadSpecMigrationsEntry();
    message.key = object.key ?? "";
    message.value = (object.value !== undefined && object.value !== null)
      ? ThreadSpecMigration.fromPartial(object.value)
      : undefined;
    return message;
  },
};

function createBaseThreadSpecMigration(): ThreadSpecMigration {
  return { newThreadSpecName: "", nodeMigrations: {} };
}

export const ThreadSpecMigration = {
  encode(message: ThreadSpecMigration, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.newThreadSpecName !== "") {
      writer.uint32(10).string(message.newThreadSpecName);
    }
    Object.entries(message.nodeMigrations).forEach(([key, value]) => {
      ThreadSpecMigration_NodeMigrationsEntry.encode({ key: key as any, value }, writer.uint32(18).fork()).ldelim();
    });
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): ThreadSpecMigration {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseThreadSpecMigration();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.newThreadSpecName = reader.string();
          continue;
        case 2:
          if (tag !== 18) {
            break;
          }

          const entry2 = ThreadSpecMigration_NodeMigrationsEntry.decode(reader, reader.uint32());
          if (entry2.value !== undefined) {
            message.nodeMigrations[entry2.key] = entry2.value;
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

  fromJSON(object: any): ThreadSpecMigration {
    return {
      newThreadSpecName: isSet(object.newThreadSpecName) ? globalThis.String(object.newThreadSpecName) : "",
      nodeMigrations: isObject(object.nodeMigrations)
        ? Object.entries(object.nodeMigrations).reduce<{ [key: string]: NodeMigration }>((acc, [key, value]) => {
          acc[key] = NodeMigration.fromJSON(value);
          return acc;
        }, {})
        : {},
    };
  },

  toJSON(message: ThreadSpecMigration): unknown {
    const obj: any = {};
    if (message.newThreadSpecName !== "") {
      obj.newThreadSpecName = message.newThreadSpecName;
    }
    if (message.nodeMigrations) {
      const entries = Object.entries(message.nodeMigrations);
      if (entries.length > 0) {
        obj.nodeMigrations = {};
        entries.forEach(([k, v]) => {
          obj.nodeMigrations[k] = NodeMigration.toJSON(v);
        });
      }
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<ThreadSpecMigration>, I>>(base?: I): ThreadSpecMigration {
    return ThreadSpecMigration.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<ThreadSpecMigration>, I>>(object: I): ThreadSpecMigration {
    const message = createBaseThreadSpecMigration();
    message.newThreadSpecName = object.newThreadSpecName ?? "";
    message.nodeMigrations = Object.entries(object.nodeMigrations ?? {}).reduce<{ [key: string]: NodeMigration }>(
      (acc, [key, value]) => {
        if (value !== undefined) {
          acc[key] = NodeMigration.fromPartial(value);
        }
        return acc;
      },
      {},
    );
    return message;
  },
};

function createBaseThreadSpecMigration_NodeMigrationsEntry(): ThreadSpecMigration_NodeMigrationsEntry {
  return { key: "", value: undefined };
}

export const ThreadSpecMigration_NodeMigrationsEntry = {
  encode(message: ThreadSpecMigration_NodeMigrationsEntry, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.key !== "") {
      writer.uint32(10).string(message.key);
    }
    if (message.value !== undefined) {
      NodeMigration.encode(message.value, writer.uint32(18).fork()).ldelim();
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): ThreadSpecMigration_NodeMigrationsEntry {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseThreadSpecMigration_NodeMigrationsEntry();
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

          message.value = NodeMigration.decode(reader, reader.uint32());
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  fromJSON(object: any): ThreadSpecMigration_NodeMigrationsEntry {
    return {
      key: isSet(object.key) ? globalThis.String(object.key) : "",
      value: isSet(object.value) ? NodeMigration.fromJSON(object.value) : undefined,
    };
  },

  toJSON(message: ThreadSpecMigration_NodeMigrationsEntry): unknown {
    const obj: any = {};
    if (message.key !== "") {
      obj.key = message.key;
    }
    if (message.value !== undefined) {
      obj.value = NodeMigration.toJSON(message.value);
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<ThreadSpecMigration_NodeMigrationsEntry>, I>>(
    base?: I,
  ): ThreadSpecMigration_NodeMigrationsEntry {
    return ThreadSpecMigration_NodeMigrationsEntry.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<ThreadSpecMigration_NodeMigrationsEntry>, I>>(
    object: I,
  ): ThreadSpecMigration_NodeMigrationsEntry {
    const message = createBaseThreadSpecMigration_NodeMigrationsEntry();
    message.key = object.key ?? "";
    message.value = (object.value !== undefined && object.value !== null)
      ? NodeMigration.fromPartial(object.value)
      : undefined;
    return message;
  },
};

function createBaseNodeMigration(): NodeMigration {
  return { newNodeName: "" };
}

export const NodeMigration = {
  encode(message: NodeMigration, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.newNodeName !== "") {
      writer.uint32(10).string(message.newNodeName);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): NodeMigration {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseNodeMigration();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.newNodeName = reader.string();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  fromJSON(object: any): NodeMigration {
    return { newNodeName: isSet(object.newNodeName) ? globalThis.String(object.newNodeName) : "" };
  },

  toJSON(message: NodeMigration): unknown {
    const obj: any = {};
    if (message.newNodeName !== "") {
      obj.newNodeName = message.newNodeName;
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<NodeMigration>, I>>(base?: I): NodeMigration {
    return NodeMigration.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<NodeMigration>, I>>(object: I): NodeMigration {
    const message = createBaseNodeMigration();
    message.newNodeName = object.newNodeName ?? "";
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

function longToNumber(long: Long): number {
  if (long.gt(globalThis.Number.MAX_SAFE_INTEGER)) {
    throw new globalThis.Error("Value is larger than Number.MAX_SAFE_INTEGER");
  }
  return long.toNumber();
}

if (_m0.util.Long !== Long) {
  _m0.util.Long = Long as any;
  _m0.configure();
}

function isObject(value: any): boolean {
  return typeof value === "object" && value !== null;
}

function isSet(value: any): boolean {
  return value !== null && value !== undefined;
}
