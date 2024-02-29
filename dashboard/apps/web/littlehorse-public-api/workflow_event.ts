/* eslint-disable */
import * as _m0 from "protobufjs/minimal";
import { VariableType, variableTypeFromJSON, variableTypeToJSON, variableTypeToNumber } from "./common_enums";
import { Timestamp } from "./google/protobuf/timestamp";
import { WorkflowEventDefId, WorkflowEventId } from "./object_id";
import { VariableValue } from "./variable";

export const protobufPackage = "littlehorse";

/**
 * A WorkflowEvent represents a "Thing That Happened" *INSIDE* a WfRun. It is DIFFERENT from
 * an ExternalEvent, because an ExternalEvent represents something that happened OUTSIDE the WfRun,
 * and is used to send information to the WfRun.
 *
 * In contrast, a WorkflowEvent is thrown by the WfRun and is used to send information to the outside
 * world.
 */
export interface WorkflowEvent {
  /** The ID of the WorkflowEvent. Contains WfRunId and WorkflowEventDefId. */
  id:
    | WorkflowEventId
    | undefined;
  /** The content of the WorkflowEvent. */
  content:
    | VariableValue
    | undefined;
  /** The time that the WorkflowEvent was created. */
  createdAt: string | undefined;
}

export interface WorkflowEventDef {
  id: WorkflowEventDefId | undefined;
  createdAt: string | undefined;
  type: VariableType;
}

function createBaseWorkflowEvent(): WorkflowEvent {
  return { id: undefined, content: undefined, createdAt: undefined };
}

export const WorkflowEvent = {
  encode(message: WorkflowEvent, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.id !== undefined) {
      WorkflowEventId.encode(message.id, writer.uint32(10).fork()).ldelim();
    }
    if (message.content !== undefined) {
      VariableValue.encode(message.content, writer.uint32(18).fork()).ldelim();
    }
    if (message.createdAt !== undefined) {
      Timestamp.encode(toTimestamp(message.createdAt), writer.uint32(26).fork()).ldelim();
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): WorkflowEvent {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseWorkflowEvent();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.id = WorkflowEventId.decode(reader, reader.uint32());
          continue;
        case 2:
          if (tag !== 18) {
            break;
          }

          message.content = VariableValue.decode(reader, reader.uint32());
          continue;
        case 3:
          if (tag !== 26) {
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

  fromJSON(object: any): WorkflowEvent {
    return {
      id: isSet(object.id) ? WorkflowEventId.fromJSON(object.id) : undefined,
      content: isSet(object.content) ? VariableValue.fromJSON(object.content) : undefined,
      createdAt: isSet(object.createdAt) ? globalThis.String(object.createdAt) : undefined,
    };
  },

  toJSON(message: WorkflowEvent): unknown {
    const obj: any = {};
    if (message.id !== undefined) {
      obj.id = WorkflowEventId.toJSON(message.id);
    }
    if (message.content !== undefined) {
      obj.content = VariableValue.toJSON(message.content);
    }
    if (message.createdAt !== undefined) {
      obj.createdAt = message.createdAt;
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<WorkflowEvent>, I>>(base?: I): WorkflowEvent {
    return WorkflowEvent.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<WorkflowEvent>, I>>(object: I): WorkflowEvent {
    const message = createBaseWorkflowEvent();
    message.id = (object.id !== undefined && object.id !== null) ? WorkflowEventId.fromPartial(object.id) : undefined;
    message.content = (object.content !== undefined && object.content !== null)
      ? VariableValue.fromPartial(object.content)
      : undefined;
    message.createdAt = object.createdAt ?? undefined;
    return message;
  },
};

function createBaseWorkflowEventDef(): WorkflowEventDef {
  return { id: undefined, createdAt: undefined, type: VariableType.JSON_OBJ };
}

export const WorkflowEventDef = {
  encode(message: WorkflowEventDef, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.id !== undefined) {
      WorkflowEventDefId.encode(message.id, writer.uint32(10).fork()).ldelim();
    }
    if (message.createdAt !== undefined) {
      Timestamp.encode(toTimestamp(message.createdAt), writer.uint32(18).fork()).ldelim();
    }
    if (message.type !== VariableType.JSON_OBJ) {
      writer.uint32(24).int32(variableTypeToNumber(message.type));
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): WorkflowEventDef {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseWorkflowEventDef();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.id = WorkflowEventDefId.decode(reader, reader.uint32());
          continue;
        case 2:
          if (tag !== 18) {
            break;
          }

          message.createdAt = fromTimestamp(Timestamp.decode(reader, reader.uint32()));
          continue;
        case 3:
          if (tag !== 24) {
            break;
          }

          message.type = variableTypeFromJSON(reader.int32());
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  fromJSON(object: any): WorkflowEventDef {
    return {
      id: isSet(object.id) ? WorkflowEventDefId.fromJSON(object.id) : undefined,
      createdAt: isSet(object.createdAt) ? globalThis.String(object.createdAt) : undefined,
      type: isSet(object.type) ? variableTypeFromJSON(object.type) : VariableType.JSON_OBJ,
    };
  },

  toJSON(message: WorkflowEventDef): unknown {
    const obj: any = {};
    if (message.id !== undefined) {
      obj.id = WorkflowEventDefId.toJSON(message.id);
    }
    if (message.createdAt !== undefined) {
      obj.createdAt = message.createdAt;
    }
    if (message.type !== VariableType.JSON_OBJ) {
      obj.type = variableTypeToJSON(message.type);
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<WorkflowEventDef>, I>>(base?: I): WorkflowEventDef {
    return WorkflowEventDef.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<WorkflowEventDef>, I>>(object: I): WorkflowEventDef {
    const message = createBaseWorkflowEventDef();
    message.id = (object.id !== undefined && object.id !== null)
      ? WorkflowEventDefId.fromPartial(object.id)
      : undefined;
    message.createdAt = object.createdAt ?? undefined;
    message.type = object.type ?? VariableType.JSON_OBJ;
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

function isSet(value: any): boolean {
  return value !== null && value !== undefined;
}
