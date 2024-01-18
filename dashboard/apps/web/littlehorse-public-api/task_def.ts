/* eslint-disable */
import * as _m0 from "protobufjs/minimal";
import { VariableDef } from "./common_wfspec";
import { Timestamp } from "./google/protobuf/timestamp";
import { TaskDefId } from "./object_id";

export const protobufPackage = "littlehorse";

/** A TaskDef defines a blueprint for a TaskRun that can be dispatched to Task Workers. */
export interface TaskDef {
  /** The ID of this TaskDef. */
  id:
    | TaskDefId
    | undefined;
  /** The input variables required to execute this TaskDef. */
  inputVars: VariableDef[];
  /** The time at which this TaskDef was created. */
  createdAt: string | undefined;
}

function createBaseTaskDef(): TaskDef {
  return { id: undefined, inputVars: [], createdAt: undefined };
}

export const TaskDef = {
  encode(message: TaskDef, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.id !== undefined) {
      TaskDefId.encode(message.id, writer.uint32(10).fork()).ldelim();
    }
    for (const v of message.inputVars) {
      VariableDef.encode(v!, writer.uint32(18).fork()).ldelim();
    }
    if (message.createdAt !== undefined) {
      Timestamp.encode(toTimestamp(message.createdAt), writer.uint32(26).fork()).ldelim();
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): TaskDef {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseTaskDef();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.id = TaskDefId.decode(reader, reader.uint32());
          continue;
        case 2:
          if (tag !== 18) {
            break;
          }

          message.inputVars.push(VariableDef.decode(reader, reader.uint32()));
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

  fromJSON(object: any): TaskDef {
    return {
      id: isSet(object.id) ? TaskDefId.fromJSON(object.id) : undefined,
      inputVars: globalThis.Array.isArray(object?.inputVars)
        ? object.inputVars.map((e: any) => VariableDef.fromJSON(e))
        : [],
      createdAt: isSet(object.createdAt) ? globalThis.String(object.createdAt) : undefined,
    };
  },

  toJSON(message: TaskDef): unknown {
    const obj: any = {};
    if (message.id !== undefined) {
      obj.id = TaskDefId.toJSON(message.id);
    }
    if (message.inputVars?.length) {
      obj.inputVars = message.inputVars.map((e) => VariableDef.toJSON(e));
    }
    if (message.createdAt !== undefined) {
      obj.createdAt = message.createdAt;
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<TaskDef>, I>>(base?: I): TaskDef {
    return TaskDef.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<TaskDef>, I>>(object: I): TaskDef {
    const message = createBaseTaskDef();
    message.id = (object.id !== undefined && object.id !== null) ? TaskDefId.fromPartial(object.id) : undefined;
    message.inputVars = object.inputVars?.map((e) => VariableDef.fromPartial(e)) || [];
    message.createdAt = object.createdAt ?? undefined;
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
