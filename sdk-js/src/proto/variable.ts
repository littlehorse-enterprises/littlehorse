/* eslint-disable */
import Long from "long";
import _m0 from "protobufjs/minimal";
import { Timestamp } from "./google/protobuf/timestamp";
import { VariableId, WfSpecId } from "./object_id";

export const protobufPackage = "littlehorse";

/**
 * VariableValue is a structure containing a value in LittleHorse. It can be
 * used to pass input variables into a WfRun/ThreadRun/TaskRun/etc, as output
 * from a TaskRun, as the value of a WfRun's Variable, etc.
 */
export interface VariableValue {
  /** A String representing a serialized json object. */
  jsonObj?:
    | string
    | undefined;
  /** A String representing a serialized json list. */
  jsonArr?:
    | string
    | undefined;
  /** A 64-bit floating point number. */
  double?:
    | number
    | undefined;
  /** A boolean. */
  bool?:
    | boolean
    | undefined;
  /** A string. */
  str?:
    | string
    | undefined;
  /** A 64-bit integer. */
  int?:
    | number
    | undefined;
  /** An arbitrary String of bytes. */
  bytes?: Buffer | undefined;
}

/** A Variable is an instance of a variable assigned to a WfRun. */
export interface Variable {
  /**
   * ID of this Variable. Note that the VariableId contains the relevant
   * WfRunId inside it, the threadRunNumber, and the name of the Variabe.
   */
  id:
    | VariableId
    | undefined;
  /** The value of this Variable. */
  value:
    | VariableValue
    | undefined;
  /** When the Variable was created. */
  createdAt:
    | string
    | undefined;
  /** The ID of the WfSpec that this Variable belongs to. */
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

          message.bytes = reader.bytes() as Buffer;
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create(base?: DeepPartial<VariableValue>): VariableValue {
    return VariableValue.fromPartial(base ?? {});
  },
  fromPartial(object: DeepPartial<VariableValue>): VariableValue {
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

  create(base?: DeepPartial<Variable>): Variable {
    return Variable.fromPartial(base ?? {});
  },
  fromPartial(object: DeepPartial<Variable>): Variable {
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
