/* eslint-disable */
import * as _m0 from "protobufjs/minimal";
import { VariableType, variableTypeFromJSON, variableTypeToJSON } from "./common_enums";
import { Timestamp } from "./google/protobuf/timestamp";
import { WfSpecId } from "./object_id";
import Long = require("long");

export const protobufPackage = "littlehorse";

export interface VariableValue {
  type: VariableType;
  jsonObj?: string | undefined;
  jsonArr?: string | undefined;
  double?: number | undefined;
  bool?: boolean | undefined;
  str?: string | undefined;
  int?: number | undefined;
  bytes?: Uint8Array | undefined;
}

export interface Variable {
  value: VariableValue | undefined;
  wfRunId: string;
  threadRunNumber: number;
  name: string;
  date: Date | undefined;
  wfSpecId: WfSpecId | undefined;
}

function createBaseVariableValue(): VariableValue {
  return {
    type: 0,
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
    if (message.type !== 0) {
      writer.uint32(8).int32(message.type);
    }
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
        case 1:
          if (tag !== 8) {
            break;
          }

          message.type = reader.int32() as any;
          continue;
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
      type: isSet(object.type) ? variableTypeFromJSON(object.type) : 0,
      jsonObj: isSet(object.jsonObj) ? String(object.jsonObj) : undefined,
      jsonArr: isSet(object.jsonArr) ? String(object.jsonArr) : undefined,
      double: isSet(object.double) ? Number(object.double) : undefined,
      bool: isSet(object.bool) ? Boolean(object.bool) : undefined,
      str: isSet(object.str) ? String(object.str) : undefined,
      int: isSet(object.int) ? Number(object.int) : undefined,
      bytes: isSet(object.bytes) ? bytesFromBase64(object.bytes) : undefined,
    };
  },

  toJSON(message: VariableValue): unknown {
    const obj: any = {};
    if (message.type !== 0) {
      obj.type = variableTypeToJSON(message.type);
    }
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
    message.type = object.type ?? 0;
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
  return { value: undefined, wfRunId: "", threadRunNumber: 0, name: "", date: undefined, wfSpecId: undefined };
}

export const Variable = {
  encode(message: Variable, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.value !== undefined) {
      VariableValue.encode(message.value, writer.uint32(10).fork()).ldelim();
    }
    if (message.wfRunId !== "") {
      writer.uint32(18).string(message.wfRunId);
    }
    if (message.threadRunNumber !== 0) {
      writer.uint32(24).int32(message.threadRunNumber);
    }
    if (message.name !== "") {
      writer.uint32(34).string(message.name);
    }
    if (message.date !== undefined) {
      Timestamp.encode(toTimestamp(message.date), writer.uint32(42).fork()).ldelim();
    }
    if (message.wfSpecId !== undefined) {
      WfSpecId.encode(message.wfSpecId, writer.uint32(50).fork()).ldelim();
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

          message.value = VariableValue.decode(reader, reader.uint32());
          continue;
        case 2:
          if (tag !== 18) {
            break;
          }

          message.wfRunId = reader.string();
          continue;
        case 3:
          if (tag !== 24) {
            break;
          }

          message.threadRunNumber = reader.int32();
          continue;
        case 4:
          if (tag !== 34) {
            break;
          }

          message.name = reader.string();
          continue;
        case 5:
          if (tag !== 42) {
            break;
          }

          message.date = fromTimestamp(Timestamp.decode(reader, reader.uint32()));
          continue;
        case 6:
          if (tag !== 50) {
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
      value: isSet(object.value) ? VariableValue.fromJSON(object.value) : undefined,
      wfRunId: isSet(object.wfRunId) ? String(object.wfRunId) : "",
      threadRunNumber: isSet(object.threadRunNumber) ? Number(object.threadRunNumber) : 0,
      name: isSet(object.name) ? String(object.name) : "",
      date: isSet(object.date) ? fromJsonTimestamp(object.date) : undefined,
      wfSpecId: isSet(object.wfSpecId) ? WfSpecId.fromJSON(object.wfSpecId) : undefined,
    };
  },

  toJSON(message: Variable): unknown {
    const obj: any = {};
    if (message.value !== undefined) {
      obj.value = VariableValue.toJSON(message.value);
    }
    if (message.wfRunId !== "") {
      obj.wfRunId = message.wfRunId;
    }
    if (message.threadRunNumber !== 0) {
      obj.threadRunNumber = Math.round(message.threadRunNumber);
    }
    if (message.name !== "") {
      obj.name = message.name;
    }
    if (message.date !== undefined) {
      obj.date = message.date.toISOString();
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
    message.value = (object.value !== undefined && object.value !== null)
      ? VariableValue.fromPartial(object.value)
      : undefined;
    message.wfRunId = object.wfRunId ?? "";
    message.threadRunNumber = object.threadRunNumber ?? 0;
    message.name = object.name ?? "";
    message.date = object.date ?? undefined;
    message.wfSpecId = (object.wfSpecId !== undefined && object.wfSpecId !== null)
      ? WfSpecId.fromPartial(object.wfSpecId)
      : undefined;
    return message;
  },
};

declare const self: any | undefined;
declare const window: any | undefined;
declare const global: any | undefined;
const tsProtoGlobalThis: any = (() => {
  if (typeof globalThis !== "undefined") {
    return globalThis;
  }
  if (typeof self !== "undefined") {
    return self;
  }
  if (typeof window !== "undefined") {
    return window;
  }
  if (typeof global !== "undefined") {
    return global;
  }
  throw "Unable to locate global object";
})();

function bytesFromBase64(b64: string): Uint8Array {
  if (tsProtoGlobalThis.Buffer) {
    return Uint8Array.from(tsProtoGlobalThis.Buffer.from(b64, "base64"));
  } else {
    const bin = tsProtoGlobalThis.atob(b64);
    const arr = new Uint8Array(bin.length);
    for (let i = 0; i < bin.length; ++i) {
      arr[i] = bin.charCodeAt(i);
    }
    return arr;
  }
}

function base64FromBytes(arr: Uint8Array): string {
  if (tsProtoGlobalThis.Buffer) {
    return tsProtoGlobalThis.Buffer.from(arr).toString("base64");
  } else {
    const bin: string[] = [];
    arr.forEach((byte) => {
      bin.push(String.fromCharCode(byte));
    });
    return tsProtoGlobalThis.btoa(bin.join(""));
  }
}

type Builtin = Date | Function | Uint8Array | string | number | boolean | undefined;

export type DeepPartial<T> = T extends Builtin ? T
  : T extends Array<infer U> ? Array<DeepPartial<U>> : T extends ReadonlyArray<infer U> ? ReadonlyArray<DeepPartial<U>>
  : T extends {} ? { [K in keyof T]?: DeepPartial<T[K]> }
  : Partial<T>;

type KeysOfUnion<T> = T extends T ? keyof T : never;
export type Exact<P, I extends P> = P extends Builtin ? P
  : P & { [K in keyof P]: Exact<P[K], I[K]> } & { [K in Exclude<keyof I, KeysOfUnion<P>>]: never };

function toTimestamp(date: Date): Timestamp {
  const seconds = date.getTime() / 1_000;
  const nanos = (date.getTime() % 1_000) * 1_000_000;
  return { seconds, nanos };
}

function fromTimestamp(t: Timestamp): Date {
  let millis = (t.seconds || 0) * 1_000;
  millis += (t.nanos || 0) / 1_000_000;
  return new Date(millis);
}

function fromJsonTimestamp(o: any): Date {
  if (o instanceof Date) {
    return o;
  } else if (typeof o === "string") {
    return new Date(o);
  } else {
    return fromTimestamp(Timestamp.fromJSON(o));
  }
}

function longToNumber(long: Long): number {
  if (long.gt(Number.MAX_SAFE_INTEGER)) {
    throw new tsProtoGlobalThis.Error("Value is larger than Number.MAX_SAFE_INTEGER");
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
