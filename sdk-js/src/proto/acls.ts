// Code generated by protoc-gen-ts_proto. DO NOT EDIT.
// versions:
//   protoc-gen-ts_proto  v1.174.0
//   protoc               v4.23.4
// source: acls.proto

/* eslint-disable */
import _m0 from "protobufjs/minimal";
import { Timestamp } from "./google/protobuf/timestamp";
import { PrincipalId, TenantId } from "./object_id";

export const protobufPackage = "littlehorse";

/** Defines a resource type for ACL's. */
export enum ACLResource {
  /** ACL_WORKFLOW - Refers to `WfSpec` and `WfRun` */
  ACL_WORKFLOW = "ACL_WORKFLOW",
  /** ACL_TASK - Refers to `TaskDef` and `TaskRun` */
  ACL_TASK = "ACL_TASK",
  /** ACL_EXTERNAL_EVENT - Refers to `ExternalEventDef` and `ExternalEvent` */
  ACL_EXTERNAL_EVENT = "ACL_EXTERNAL_EVENT",
  /** ACL_USER_TASK - Refers to `UserTaskDef` and `UserTaskRun` */
  ACL_USER_TASK = "ACL_USER_TASK",
  /**
   * ACL_PRINCIPAL - Refers to the `Principal` resource. Currently, the `ACL_PRINCIPAL` permission is only
   * valid in the `global_acls` field of the `Principal`. A `Principal` who only has access
   * to a specific Tenant cannot create othe Principals because a Principal is scoped
   * to the Cluster, and not to a Tenant.
   */
  ACL_PRINCIPAL = "ACL_PRINCIPAL",
  /**
   * ACL_TENANT - Refers to the `Tenant` resource. The `ACL_TENANT` permission is only valid in the
   * `global_acls` field of the `Principal`. This is because the `Tenant` resource is
   * cluste-rscoped.
   */
  ACL_TENANT = "ACL_TENANT",
  /**
   * ACL_ALL_RESOURCES - Refers to all resources. In the `global_acls` field, this includes `Principal` and `Tenant`
   * resources. In the `per_tenant_acls` field, this does not include `Principal` and `Tenant` since
   * those are cluster-scoped resources.
   */
  ACL_ALL_RESOURCES = "ACL_ALL_RESOURCES",
  /** ACL_TASK_WORKER_GROUP - Refers to the `TaskWorkerGroup` associated with a TaskDef */
  ACL_TASK_WORKER_GROUP = "ACL_TASK_WORKER_GROUP",
  UNRECOGNIZED = "UNRECOGNIZED",
}

export function aCLResourceFromJSON(object: any): ACLResource {
  switch (object) {
    case 0:
    case "ACL_WORKFLOW":
      return ACLResource.ACL_WORKFLOW;
    case 1:
    case "ACL_TASK":
      return ACLResource.ACL_TASK;
    case 2:
    case "ACL_EXTERNAL_EVENT":
      return ACLResource.ACL_EXTERNAL_EVENT;
    case 3:
    case "ACL_USER_TASK":
      return ACLResource.ACL_USER_TASK;
    case 4:
    case "ACL_PRINCIPAL":
      return ACLResource.ACL_PRINCIPAL;
    case 5:
    case "ACL_TENANT":
      return ACLResource.ACL_TENANT;
    case 6:
    case "ACL_ALL_RESOURCES":
      return ACLResource.ACL_ALL_RESOURCES;
    case 7:
    case "ACL_TASK_WORKER_GROUP":
      return ACLResource.ACL_TASK_WORKER_GROUP;
    case -1:
    case "UNRECOGNIZED":
    default:
      return ACLResource.UNRECOGNIZED;
  }
}

export function aCLResourceToNumber(object: ACLResource): number {
  switch (object) {
    case ACLResource.ACL_WORKFLOW:
      return 0;
    case ACLResource.ACL_TASK:
      return 1;
    case ACLResource.ACL_EXTERNAL_EVENT:
      return 2;
    case ACLResource.ACL_USER_TASK:
      return 3;
    case ACLResource.ACL_PRINCIPAL:
      return 4;
    case ACLResource.ACL_TENANT:
      return 5;
    case ACLResource.ACL_ALL_RESOURCES:
      return 6;
    case ACLResource.ACL_TASK_WORKER_GROUP:
      return 7;
    case ACLResource.UNRECOGNIZED:
    default:
      return -1;
  }
}

/** Describes an Action that can be taken over a specific set of resources. */
export enum ACLAction {
  /**
   * READ - Allows all RPC's that start with `Get`, `List`, and `Search` in relation to the
   * metadata (eg. `TaskDef` for `ACL_TASK`) or run data (eg. `TaskRun` for `ACL_TASK`)
   */
  READ = "READ",
  /**
   * RUN - Allows RPC's that are needed for mutating the _runs_ of the resource. For
   * example, `RUN` over `ACL_TASK` allows the `ReportTask` and `PollTask` RPC's,
   * and `RUN` over `ACL_WORKFLOW` allows the `RunWf`, `DeleteWfRun`, `StopWfRun`,
   * and `ResumeWfRun` RPC's.
   */
  RUN = "RUN",
  /**
   * WRITE_METADATA - Allows mutating metadata. For example, `WRITE_METADATA` over `ACL_WORKFLOW` allows
   * mutating `WfSpec`s, and `WRITE_METADATA` over `ACL_TASK` allows mutating `TaskDef`s.
   */
  WRITE_METADATA = "WRITE_METADATA",
  /** ALL_ACTIONS - Allows all actions related to a resource. */
  ALL_ACTIONS = "ALL_ACTIONS",
  UNRECOGNIZED = "UNRECOGNIZED",
}

export function aCLActionFromJSON(object: any): ACLAction {
  switch (object) {
    case 0:
    case "READ":
      return ACLAction.READ;
    case 1:
    case "RUN":
      return ACLAction.RUN;
    case 2:
    case "WRITE_METADATA":
      return ACLAction.WRITE_METADATA;
    case 3:
    case "ALL_ACTIONS":
      return ACLAction.ALL_ACTIONS;
    case -1:
    case "UNRECOGNIZED":
    default:
      return ACLAction.UNRECOGNIZED;
  }
}

export function aCLActionToNumber(object: ACLAction): number {
  switch (object) {
    case ACLAction.READ:
      return 0;
    case ACLAction.RUN:
      return 1;
    case ACLAction.WRITE_METADATA:
      return 2;
    case ACLAction.ALL_ACTIONS:
      return 3;
    case ACLAction.UNRECOGNIZED:
    default:
      return -1;
  }
}

/**
 * A Principal represents the identity of a client of LittleHorse, whether human or
 * machine. The ACL's on the Principal control what actions the client is allowed
 * to take.
 *
 * A Principal is not scoped to a Tenant; rather, a Principal is scoped to the Cluster
 * and may have access to one or more Tenants.
 */
export interface Principal {
  /**
   * The ID of the Principal. In OAuth for human users, this is the user_id. In
   * OAuth for machine clients, this is the Client ID.
   *
   * mTLS for Principal identification is not yet implemented.
   */
  id:
    | PrincipalId
    | undefined;
  /** The time at which the Principal was created. */
  createdAt:
    | string
    | undefined;
  /**
   * Maps a Tenant ID to a list of ACL's that the Principal has permission to
   * execute *within that Tenant*.
   */
  perTenantAcls: { [key: string]: ServerACLs };
  /** Sets permissions that this Principal has *for any Tenant* in the LH Cluster. */
  globalAcls: ServerACLs | undefined;
}

export interface Principal_PerTenantAclsEntry {
  key: string;
  value: ServerACLs | undefined;
}

/**
 * A Tenant is a logically isolated environment within LittleHorse. All workflows and
 * associated data (WfSpec, WfRun, TaskDef, TaskRun, NodeRun, etc) are scoped to within
 * a Tenant.
 *
 * Future versions will include quotas on a per-Tenant basis.
 */
export interface Tenant {
  /** The ID of the Tenant. */
  id:
    | TenantId
    | undefined;
  /** The time at which the Tenant was created. */
  createdAt: string | undefined;
}

/** List of ACL's for LittleHorse */
export interface ServerACLs {
  /** The associated ACL's */
  acls: ServerACL[];
}

/**
 * Represents a specific set of permissions over a specific set of objects
 * in a Tenant. This is a *positive* permission.
 */
export interface ServerACL {
  /** The resource types over which permission is granted. */
  resources: ACLResource[];
  /** The actions that are permitted. */
  allowedActions: ACLAction[];
  /**
   * If set, then only the resources with this exact name are allowed. For example,
   * the `READ` and `RUN` `allowed_actions` over `ACL_TASK` with `name` == `my-task`
   * allows a Task Worker to only execute the `my-task` TaskDef.
   *
   * If `name` and `prefix` are unset, then the ACL applies to all resources of the
   * specified types.
   */
  name?:
    | string
    | undefined;
  /**
   * If set, then only the resources whose names match this prefix are allowed.
   *
   * If `name` and `prefix` are unset, then the ACL applies to all resources of the
   * specified types.
   */
  prefix?: string | undefined;
}

/**
 * Creates or updates a Principal. If this request would remove admin privileges from the
 * last admin principal (i.e. `ALL_ACTIONS` over `ACL_ALL_RESOURCES` in the `global_acls`),
 * then the RPC throws `FAILED_PRECONDITION`.
 */
export interface PutPrincipalRequest {
  /** The ID of the Principal that we are creating. */
  id: string;
  /** The per-tenant ACL's for the Principal */
  perTenantAcls: { [key: string]: ServerACLs };
  /** The ACL's for the principal in all tenants */
  globalAcls:
    | ServerACLs
    | undefined;
  /**
   * If this is set to false and a `Principal` with the same `id` already exists *and*
   * has different ACL's configured, then the RPC throws `ALREADY_EXISTS`.
   *
   * If this is set to `true`, then the RPC will override hte
   */
  overwrite: boolean;
}

export interface PutPrincipalRequest_PerTenantAclsEntry {
  key: string;
  value: ServerACLs | undefined;
}

export interface DeletePrincipalRequest {
  id: string;
}

export interface PutTenantRequest {
  id: string;
}

function createBasePrincipal(): Principal {
  return { id: undefined, createdAt: undefined, perTenantAcls: {}, globalAcls: undefined };
}

export const Principal = {
  encode(message: Principal, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.id !== undefined) {
      PrincipalId.encode(message.id, writer.uint32(10).fork()).ldelim();
    }
    if (message.createdAt !== undefined) {
      Timestamp.encode(toTimestamp(message.createdAt), writer.uint32(18).fork()).ldelim();
    }
    Object.entries(message.perTenantAcls).forEach(([key, value]) => {
      Principal_PerTenantAclsEntry.encode({ key: key as any, value }, writer.uint32(26).fork()).ldelim();
    });
    if (message.globalAcls !== undefined) {
      ServerACLs.encode(message.globalAcls, writer.uint32(34).fork()).ldelim();
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): Principal {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBasePrincipal();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.id = PrincipalId.decode(reader, reader.uint32());
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

          const entry3 = Principal_PerTenantAclsEntry.decode(reader, reader.uint32());
          if (entry3.value !== undefined) {
            message.perTenantAcls[entry3.key] = entry3.value;
          }
          continue;
        case 4:
          if (tag !== 34) {
            break;
          }

          message.globalAcls = ServerACLs.decode(reader, reader.uint32());
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create(base?: DeepPartial<Principal>): Principal {
    return Principal.fromPartial(base ?? {});
  },
  fromPartial(object: DeepPartial<Principal>): Principal {
    const message = createBasePrincipal();
    message.id = (object.id !== undefined && object.id !== null) ? PrincipalId.fromPartial(object.id) : undefined;
    message.createdAt = object.createdAt ?? undefined;
    message.perTenantAcls = Object.entries(object.perTenantAcls ?? {}).reduce<{ [key: string]: ServerACLs }>(
      (acc, [key, value]) => {
        if (value !== undefined) {
          acc[key] = ServerACLs.fromPartial(value);
        }
        return acc;
      },
      {},
    );
    message.globalAcls = (object.globalAcls !== undefined && object.globalAcls !== null)
      ? ServerACLs.fromPartial(object.globalAcls)
      : undefined;
    return message;
  },
};

function createBasePrincipal_PerTenantAclsEntry(): Principal_PerTenantAclsEntry {
  return { key: "", value: undefined };
}

export const Principal_PerTenantAclsEntry = {
  encode(message: Principal_PerTenantAclsEntry, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.key !== "") {
      writer.uint32(10).string(message.key);
    }
    if (message.value !== undefined) {
      ServerACLs.encode(message.value, writer.uint32(18).fork()).ldelim();
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): Principal_PerTenantAclsEntry {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBasePrincipal_PerTenantAclsEntry();
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

          message.value = ServerACLs.decode(reader, reader.uint32());
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create(base?: DeepPartial<Principal_PerTenantAclsEntry>): Principal_PerTenantAclsEntry {
    return Principal_PerTenantAclsEntry.fromPartial(base ?? {});
  },
  fromPartial(object: DeepPartial<Principal_PerTenantAclsEntry>): Principal_PerTenantAclsEntry {
    const message = createBasePrincipal_PerTenantAclsEntry();
    message.key = object.key ?? "";
    message.value = (object.value !== undefined && object.value !== null)
      ? ServerACLs.fromPartial(object.value)
      : undefined;
    return message;
  },
};

function createBaseTenant(): Tenant {
  return { id: undefined, createdAt: undefined };
}

export const Tenant = {
  encode(message: Tenant, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.id !== undefined) {
      TenantId.encode(message.id, writer.uint32(10).fork()).ldelim();
    }
    if (message.createdAt !== undefined) {
      Timestamp.encode(toTimestamp(message.createdAt), writer.uint32(18).fork()).ldelim();
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): Tenant {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseTenant();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.id = TenantId.decode(reader, reader.uint32());
          continue;
        case 2:
          if (tag !== 18) {
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

  create(base?: DeepPartial<Tenant>): Tenant {
    return Tenant.fromPartial(base ?? {});
  },
  fromPartial(object: DeepPartial<Tenant>): Tenant {
    const message = createBaseTenant();
    message.id = (object.id !== undefined && object.id !== null) ? TenantId.fromPartial(object.id) : undefined;
    message.createdAt = object.createdAt ?? undefined;
    return message;
  },
};

function createBaseServerACLs(): ServerACLs {
  return { acls: [] };
}

export const ServerACLs = {
  encode(message: ServerACLs, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    for (const v of message.acls) {
      ServerACL.encode(v!, writer.uint32(10).fork()).ldelim();
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): ServerACLs {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseServerACLs();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.acls.push(ServerACL.decode(reader, reader.uint32()));
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create(base?: DeepPartial<ServerACLs>): ServerACLs {
    return ServerACLs.fromPartial(base ?? {});
  },
  fromPartial(object: DeepPartial<ServerACLs>): ServerACLs {
    const message = createBaseServerACLs();
    message.acls = object.acls?.map((e) => ServerACL.fromPartial(e)) || [];
    return message;
  },
};

function createBaseServerACL(): ServerACL {
  return { resources: [], allowedActions: [], name: undefined, prefix: undefined };
}

export const ServerACL = {
  encode(message: ServerACL, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    writer.uint32(10).fork();
    for (const v of message.resources) {
      writer.int32(aCLResourceToNumber(v));
    }
    writer.ldelim();
    writer.uint32(18).fork();
    for (const v of message.allowedActions) {
      writer.int32(aCLActionToNumber(v));
    }
    writer.ldelim();
    if (message.name !== undefined) {
      writer.uint32(26).string(message.name);
    }
    if (message.prefix !== undefined) {
      writer.uint32(34).string(message.prefix);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): ServerACL {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseServerACL();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag === 8) {
            message.resources.push(aCLResourceFromJSON(reader.int32()));

            continue;
          }

          if (tag === 10) {
            const end2 = reader.uint32() + reader.pos;
            while (reader.pos < end2) {
              message.resources.push(aCLResourceFromJSON(reader.int32()));
            }

            continue;
          }

          break;
        case 2:
          if (tag === 16) {
            message.allowedActions.push(aCLActionFromJSON(reader.int32()));

            continue;
          }

          if (tag === 18) {
            const end2 = reader.uint32() + reader.pos;
            while (reader.pos < end2) {
              message.allowedActions.push(aCLActionFromJSON(reader.int32()));
            }

            continue;
          }

          break;
        case 3:
          if (tag !== 26) {
            break;
          }

          message.name = reader.string();
          continue;
        case 4:
          if (tag !== 34) {
            break;
          }

          message.prefix = reader.string();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create(base?: DeepPartial<ServerACL>): ServerACL {
    return ServerACL.fromPartial(base ?? {});
  },
  fromPartial(object: DeepPartial<ServerACL>): ServerACL {
    const message = createBaseServerACL();
    message.resources = object.resources?.map((e) => e) || [];
    message.allowedActions = object.allowedActions?.map((e) => e) || [];
    message.name = object.name ?? undefined;
    message.prefix = object.prefix ?? undefined;
    return message;
  },
};

function createBasePutPrincipalRequest(): PutPrincipalRequest {
  return { id: "", perTenantAcls: {}, globalAcls: undefined, overwrite: false };
}

export const PutPrincipalRequest = {
  encode(message: PutPrincipalRequest, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.id !== "") {
      writer.uint32(10).string(message.id);
    }
    Object.entries(message.perTenantAcls).forEach(([key, value]) => {
      PutPrincipalRequest_PerTenantAclsEntry.encode({ key: key as any, value }, writer.uint32(18).fork()).ldelim();
    });
    if (message.globalAcls !== undefined) {
      ServerACLs.encode(message.globalAcls, writer.uint32(26).fork()).ldelim();
    }
    if (message.overwrite !== false) {
      writer.uint32(40).bool(message.overwrite);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): PutPrincipalRequest {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBasePutPrincipalRequest();
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

          const entry2 = PutPrincipalRequest_PerTenantAclsEntry.decode(reader, reader.uint32());
          if (entry2.value !== undefined) {
            message.perTenantAcls[entry2.key] = entry2.value;
          }
          continue;
        case 3:
          if (tag !== 26) {
            break;
          }

          message.globalAcls = ServerACLs.decode(reader, reader.uint32());
          continue;
        case 5:
          if (tag !== 40) {
            break;
          }

          message.overwrite = reader.bool();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create(base?: DeepPartial<PutPrincipalRequest>): PutPrincipalRequest {
    return PutPrincipalRequest.fromPartial(base ?? {});
  },
  fromPartial(object: DeepPartial<PutPrincipalRequest>): PutPrincipalRequest {
    const message = createBasePutPrincipalRequest();
    message.id = object.id ?? "";
    message.perTenantAcls = Object.entries(object.perTenantAcls ?? {}).reduce<{ [key: string]: ServerACLs }>(
      (acc, [key, value]) => {
        if (value !== undefined) {
          acc[key] = ServerACLs.fromPartial(value);
        }
        return acc;
      },
      {},
    );
    message.globalAcls = (object.globalAcls !== undefined && object.globalAcls !== null)
      ? ServerACLs.fromPartial(object.globalAcls)
      : undefined;
    message.overwrite = object.overwrite ?? false;
    return message;
  },
};

function createBasePutPrincipalRequest_PerTenantAclsEntry(): PutPrincipalRequest_PerTenantAclsEntry {
  return { key: "", value: undefined };
}

export const PutPrincipalRequest_PerTenantAclsEntry = {
  encode(message: PutPrincipalRequest_PerTenantAclsEntry, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.key !== "") {
      writer.uint32(10).string(message.key);
    }
    if (message.value !== undefined) {
      ServerACLs.encode(message.value, writer.uint32(18).fork()).ldelim();
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): PutPrincipalRequest_PerTenantAclsEntry {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBasePutPrincipalRequest_PerTenantAclsEntry();
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

          message.value = ServerACLs.decode(reader, reader.uint32());
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create(base?: DeepPartial<PutPrincipalRequest_PerTenantAclsEntry>): PutPrincipalRequest_PerTenantAclsEntry {
    return PutPrincipalRequest_PerTenantAclsEntry.fromPartial(base ?? {});
  },
  fromPartial(object: DeepPartial<PutPrincipalRequest_PerTenantAclsEntry>): PutPrincipalRequest_PerTenantAclsEntry {
    const message = createBasePutPrincipalRequest_PerTenantAclsEntry();
    message.key = object.key ?? "";
    message.value = (object.value !== undefined && object.value !== null)
      ? ServerACLs.fromPartial(object.value)
      : undefined;
    return message;
  },
};

function createBaseDeletePrincipalRequest(): DeletePrincipalRequest {
  return { id: "" };
}

export const DeletePrincipalRequest = {
  encode(message: DeletePrincipalRequest, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.id !== "") {
      writer.uint32(10).string(message.id);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): DeletePrincipalRequest {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseDeletePrincipalRequest();
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

  create(base?: DeepPartial<DeletePrincipalRequest>): DeletePrincipalRequest {
    return DeletePrincipalRequest.fromPartial(base ?? {});
  },
  fromPartial(object: DeepPartial<DeletePrincipalRequest>): DeletePrincipalRequest {
    const message = createBaseDeletePrincipalRequest();
    message.id = object.id ?? "";
    return message;
  },
};

function createBasePutTenantRequest(): PutTenantRequest {
  return { id: "" };
}

export const PutTenantRequest = {
  encode(message: PutTenantRequest, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.id !== "") {
      writer.uint32(10).string(message.id);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): PutTenantRequest {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBasePutTenantRequest();
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

  create(base?: DeepPartial<PutTenantRequest>): PutTenantRequest {
    return PutTenantRequest.fromPartial(base ?? {});
  },
  fromPartial(object: DeepPartial<PutTenantRequest>): PutTenantRequest {
    const message = createBasePutTenantRequest();
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
