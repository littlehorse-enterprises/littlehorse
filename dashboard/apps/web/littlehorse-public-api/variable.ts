/* eslint-disable */
import Long from "long";
import * as _m0 from "protobufjs/minimal";
import { Timestamp } from "./google/protobuf/timestamp";
import { VariableId, WfSpecId } from "./object_id";

export const protobufPackage = "littlehorse";

export interface VariableValue {
  jsonObj?: string | undefined;
  jsonArr?: string | undefined;
  double?: number | undefined;
  bool?: boolean | undefined;
  str?: string | undefined;
  int?: number | undefined;
  bytes?: Uint8Array | undefined;
}

export interface Variable {
  id: VariableId | undefined;
  value: VariableValue | undefined;
  createdAt: string | undefined;
  wfSpecId: WfSpecId | undefined;
}

function createBaseVariableValue(): VariableValue {
  return {
    jsonObj: undefined,
    jsonArr: undefined,
    double: undefined,
    bool: undefined,
    str: undefined,
    int: undefined,
    bytes: undefined,
  };
}

export const VariableValue = {
  encode(message: VariableValue, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.jsonObj !== undefined) {
      writer.uint32(18).string(message.jsonObj);
    }
    if (message.jsonArr !== undefined) {
      writer.uint32(26).string(message.jsonArr);
    }
    if (message.double !== undefined) {
      writer.uint32(33).double(message.double);
    }
    if (message.bool !== undefined) {
      writer.uint32(40).bool(message.bool);
    }
    if (message.str !== undefined) {
      writer.uint32(50).string(message.str);
    }
    if (message.int !== undefined) {
      writer.uint32(56).int64(message.int);
    }
    if (message.bytes !== undefined) {
      writer.uint32(66).bytes(message.bytes);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): VariableValue {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseVariableValue();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 2:
          if (tag !== 18) {
            break;
          }

          message.jsonObj = reader.string();
          continue;
        case 3:
          if (tag !== 26) {
            break;
          }

          message.jsonArr = reader.string();
          continue;
        case 4:
          if (tag !== 33) {
            break;
          }

          message.double = reader.double();
          continue;
        case 5:
          if (tag !== 40) {
            break;
          }

          message.bool = reader.bool();
          continue;
        case 6:
          if (tag !== 50) {
            break;
          }

          message.str = reader.string();
          continue;
        case 7:
          if (tag !== 56) {
            break;
          }

          message.int = longToNumber(reader.int64() as Long);
          continue;
        case 8:
          if (tag !== 66) {
            break;
          }

          message.bytes = reader.bytes();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  fromJSON(object: any): VariableValue {
    return {
      jsonObj: isSet(object.jsonObj) ? globalThis.String(object.jsonObj) : undefined,
      jsonArr: isSet(object.jsonArr) ? globalThis.String(object.jsonArr) : undefined,
      double: isSet(object.double) ? globalThis.Number(object.double) : undefined,
      bool: isSet(object.bool) ? globalThis.Boolean(object.bool) : undefined,
      str: isSet(object.str) ? globalThis.String(object.str) : undefined,
      int: isSet(object.int) ? globalThis.Number(object.int) : undefined,
      bytes: isSet(object.bytes) ? bytesFromBase64(object.bytes) : undefined,
    };
  },

  toJSON(message: VariableValue): unknown {
    const obj: any = {};
    if (message.jsonObj !== undefined) {
      obj.jsonObj = message.jsonObj;
    }
    if (message.jsonArr !== undefined) {
      obj.jsonArr = message.jsonArr;
    }
    if (message.double !== undefined) {
      obj.double = message.double;
    }
    if (message.bool !== undefined) {
      obj.bool = message.bool;
    }
    if (message.str !== undefined) {
      obj.str = message.str;
    }
    if (message.int !== undefined) {
      obj.int = Math.round(message.int);
    }
    if (message.bytes !== undefined) {
      obj.bytes = base64FromBytes(message.bytes);
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<VariableValue>, I>>(base?: I): VariableValue {
    return VariableValue.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<VariableValue>, I>>(object: I): VariableValue {
    const message = createBaseVariableValue();
    message.jsonObj = object.jsonObj ?? undefined;
    message.jsonArr = object.jsonArr ?? undefined;
    message.double = object.double ?? undefined;
    message.bool = object.bool ?? undefined;
    message.str = object.str ?? undefined;
    message.int = object.int ?? undefined;
    message.bytes = object.bytes ?? undefined;
    return message;
  },
};

function createBaseVariable(): Variable {
  return { id: undefined, value: undefined, createdAt: undefined, wfSpecId: undefined };
}

export const Variable = {
  encode(message: Variable, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.id !== undefined) {
      VariableId.encode(message.id, writer.uint32(10).fork()).ldelim();
    }
    if (message.value !== undefined) {
      VariableValue.encode(message.value, writer.uint32(18).fork()).ldelim();
    }
    if (message.createdAt !== undefined) {
      Timestamp.encode(toTimestamp(message.createdAt), writer.uint32(26).fork()).ldelim();
    }
    if (message.wfSpecId !== undefined) {
      WfSpecId.encode(message.wfSpecId, writer.uint32(34).fork()).ldelim();
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): Variable {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseVariable();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.id = VariableId.decode(reader, reader.uint32());
          continue;
        case 2:
          if (tag !== 18) {
            break;
          }

          message.value = VariableValue.decode(reader, reader.uint32());
          continue;
        case 3:
          if (tag !== 26) {
            break;
          }

          message.createdAt = fromTimestamp(Timestamp.decode(reader, reader.uint32()));
          continue;
        case 4:
          if (tag !== 34) {
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

  fromJSON(object: any): Variable {
    return {
      id: isSet(object.id) ? VariableId.fromJSON(object.id) : undefined,
      value: isSet(object.value) ? VariableValue.fromJSON(object.value) : undefined,
      createdAt: isSet(object.createdAt) ? globalThis.String(object.createdAt) : undefined,
      wfSpecId: isSet(object.wfSpecId) ? WfSpecId.fromJSON(object.wfSpecId) : undefined,
    };
  },

  toJSON(message: Variable): unknown {
    const obj: any = {};
    if (message.id !== undefined) {
      obj.id = VariableId.toJSON(message.id);
    }
    if (message.value !== undefined) {
      obj.value = VariableValue.toJSON(message.value);
    }
    if (message.createdAt !== undefined) {
      obj.createdAt = message.createdAt;
    }
    if (message.wfSpecId !== undefined) {
      obj.wfSpecId = WfSpecId.toJSON(message.wfSpecId);
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<Variable>, I>>(base?: I): Variable {
    return Variable.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<Variable>, I>>(object: I): Variable {
    const message = createBaseVariable();
    message.id = (object.id !== undefined && object.id !== null) ? VariableId.fromPartial(object.id) : undefined;
    message.value = (object.value !== undefined && object.value !== null)
      ? VariableValue.fromPartial(object.value)
      : undefined;
    message.createdAt = object.createdAt ?? undefined;
    message.wfSpecId = (object.wfSpecId !== undefined && object.wfSpecId !== null)
      ? WfSpecId.fromPartial(object.wfSpecId)
      : undefined;
    return message;
  },
};

function bytesFromBase64(b64: string): Uint8Array {
  if (globalThis.Buffer) {
    return Uint8Array.from(globalThis.Buffer.from(b64, "base64"));
  } else {
    const bin = globalThis.atob(b64);
    const arr = new Uint8Array(bin.length);
    for (let i = 0; i < bin.length; ++i) {
      arr[i] = bin.charCodeAt(i);
    }
    return arr;
  }
}

function base64FromBytes(arr: Uint8Array): string {
  if (globalThis.Buffer) {
    return globalThis.Buffer.from(arr).toString("base64");
  } else {
    const bin: string[] = [];
    arr.forEach((byte) => {
      bin.push(globalThis.String.fromCharCode(byte));
    });
    return globalThis.btoa(bin.join(""));
  }
}

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

function isSet(value: any): boolean {
  return value !== null && value !== undefined;
}
