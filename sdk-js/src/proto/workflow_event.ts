/* eslint-disable */
import _m0 from "protobufjs/minimal";
import { VariableType, variableTypeFromJSON, variableTypeToNumber } from "./common_enums";
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

  create(base?: DeepPartial<WorkflowEvent>): WorkflowEvent {
    return WorkflowEvent.fromPartial(base ?? {});
  },
  fromPartial(object: DeepPartial<WorkflowEvent>): WorkflowEvent {
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

  create(base?: DeepPartial<WorkflowEventDef>): WorkflowEventDef {
    return WorkflowEventDef.fromPartial(base ?? {});
  },
  fromPartial(object: DeepPartial<WorkflowEventDef>): WorkflowEventDef {
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
