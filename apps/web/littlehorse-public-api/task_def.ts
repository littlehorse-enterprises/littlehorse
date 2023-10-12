/* eslint-disable */
import _m0 from "protobufjs/minimal";
import { VariableDef } from "./common_wfspec";
import { Timestamp } from "./google/protobuf/timestamp";

export const protobufPackage = "littlehorse";

export interface TaskDef {
  name: string;
  inputVars: VariableDef[];
  createdAt: string | undefined;
}

function createBaseTaskDef(): TaskDef {
  return { name: "", inputVars: [], createdAt: undefined };
}

export const TaskDef = {
  encode(message: TaskDef, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.name !== "") {
      writer.uint32(10).string(message.name);
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

          message.name = reader.string();
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
      name: isSet(object.name) ? String(object.name) : "",
      inputVars: Array.isArray(object?.inputVars) ? object.inputVars.map((e: any) => VariableDef.fromJSON(e)) : [],
      createdAt: isSet(object.createdAt) ? String(object.createdAt) : undefined,
    };
  },

  toJSON(message: TaskDef): unknown {
    const obj: any = {};
    if (message.name !== "") {
      obj.name = message.name;
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
    message.name = object.name ?? "";
    message.inputVars = object.inputVars?.map((e) => VariableDef.fromPartial(e)) || [];
    message.createdAt = object.createdAt ?? undefined;
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

function toTimestamp(dateStr: string): Timestamp {
  const date = new Date(dateStr);
  const seconds = date.getTime() / 1_000;
  const nanos = (date.getTime() % 1_000) * 1_000_000;
  return { seconds, nanos };
}

function fromTimestamp(t: Timestamp): string {
  let millis = (t.seconds || 0) * 1_000;
  millis += (t.nanos || 0) / 1_000_000;
  return new Date(millis).toISOString();
}

function isSet(value: any): boolean {
  return value !== null && value !== undefined;
}
