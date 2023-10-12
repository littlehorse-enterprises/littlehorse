/* eslint-disable */
import _m0 from "protobufjs/minimal";
import { Timestamp } from "./google/protobuf/timestamp";
import { VariableValue } from "./variable";

export const protobufPackage = "littlehorse";

export interface ExternalEvent {
  wfRunId: string;
  externalEventDefName: string;
  guid: string;
  createdAt: string | undefined;
  content: VariableValue | undefined;
  threadRunNumber?: number | undefined;
  nodeRunPosition?: number | undefined;
  claimed: boolean;
}

/** ExternalEventDef */
export interface ExternalEventDef {
  name: string;
  createdAt: string | undefined;
  retentionHours: number;
}

function createBaseExternalEvent(): ExternalEvent {
  return {
    wfRunId: "",
    externalEventDefName: "",
    guid: "",
    createdAt: undefined,
    content: undefined,
    threadRunNumber: undefined,
    nodeRunPosition: undefined,
    claimed: false,
  };
}

export const ExternalEvent = {
  encode(message: ExternalEvent, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.wfRunId !== "") {
      writer.uint32(10).string(message.wfRunId);
    }
    if (message.externalEventDefName !== "") {
      writer.uint32(18).string(message.externalEventDefName);
    }
    if (message.guid !== "") {
      writer.uint32(26).string(message.guid);
    }
    if (message.createdAt !== undefined) {
      Timestamp.encode(toTimestamp(message.createdAt), writer.uint32(34).fork()).ldelim();
    }
    if (message.content !== undefined) {
      VariableValue.encode(message.content, writer.uint32(42).fork()).ldelim();
    }
    if (message.threadRunNumber !== undefined) {
      writer.uint32(48).int32(message.threadRunNumber);
    }
    if (message.nodeRunPosition !== undefined) {
      writer.uint32(56).int32(message.nodeRunPosition);
    }
    if (message.claimed === true) {
      writer.uint32(64).bool(message.claimed);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): ExternalEvent {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseExternalEvent();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 10) {
            break;
          }

          message.wfRunId = reader.string();
          continue;
        case 2:
          if (tag !== 18) {
            break;
          }

          message.externalEventDefName = reader.string();
          continue;
        case 3:
          if (tag !== 26) {
            break;
          }

          message.guid = reader.string();
          continue;
        case 4:
          if (tag !== 34) {
            break;
          }

          message.createdAt = fromTimestamp(Timestamp.decode(reader, reader.uint32()));
          continue;
        case 5:
          if (tag !== 42) {
            break;
          }

          message.content = VariableValue.decode(reader, reader.uint32());
          continue;
        case 6:
          if (tag !== 48) {
            break;
          }

          message.threadRunNumber = reader.int32();
          continue;
        case 7:
          if (tag !== 56) {
            break;
          }

          message.nodeRunPosition = reader.int32();
          continue;
        case 8:
          if (tag !== 64) {
            break;
          }

          message.claimed = reader.bool();
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  fromJSON(object: any): ExternalEvent {
    return {
      wfRunId: isSet(object.wfRunId) ? String(object.wfRunId) : "",
      externalEventDefName: isSet(object.externalEventDefName) ? String(object.externalEventDefName) : "",
      guid: isSet(object.guid) ? String(object.guid) : "",
      createdAt: isSet(object.createdAt) ? String(object.createdAt) : undefined,
      content: isSet(object.content) ? VariableValue.fromJSON(object.content) : undefined,
      threadRunNumber: isSet(object.threadRunNumber) ? Number(object.threadRunNumber) : undefined,
      nodeRunPosition: isSet(object.nodeRunPosition) ? Number(object.nodeRunPosition) : undefined,
      claimed: isSet(object.claimed) ? Boolean(object.claimed) : false,
    };
  },

  toJSON(message: ExternalEvent): unknown {
    const obj: any = {};
    if (message.wfRunId !== "") {
      obj.wfRunId = message.wfRunId;
    }
    if (message.externalEventDefName !== "") {
      obj.externalEventDefName = message.externalEventDefName;
    }
    if (message.guid !== "") {
      obj.guid = message.guid;
    }
    if (message.createdAt !== undefined) {
      obj.createdAt = message.createdAt;
    }
    if (message.content !== undefined) {
      obj.content = VariableValue.toJSON(message.content);
    }
    if (message.threadRunNumber !== undefined) {
      obj.threadRunNumber = Math.round(message.threadRunNumber);
    }
    if (message.nodeRunPosition !== undefined) {
      obj.nodeRunPosition = Math.round(message.nodeRunPosition);
    }
    if (message.claimed === true) {
      obj.claimed = message.claimed;
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<ExternalEvent>, I>>(base?: I): ExternalEvent {
    return ExternalEvent.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<ExternalEvent>, I>>(object: I): ExternalEvent {
    const message = createBaseExternalEvent();
    message.wfRunId = object.wfRunId ?? "";
    message.externalEventDefName = object.externalEventDefName ?? "";
    message.guid = object.guid ?? "";
    message.createdAt = object.createdAt ?? undefined;
    message.content = (object.content !== undefined && object.content !== null)
      ? VariableValue.fromPartial(object.content)
      : undefined;
    message.threadRunNumber = object.threadRunNumber ?? undefined;
    message.nodeRunPosition = object.nodeRunPosition ?? undefined;
    message.claimed = object.claimed ?? false;
    return message;
  },
};

function createBaseExternalEventDef(): ExternalEventDef {
  return { name: "", createdAt: undefined, retentionHours: 0 };
}

export const ExternalEventDef = {
  encode(message: ExternalEventDef, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.name !== "") {
      writer.uint32(10).string(message.name);
    }
    if (message.createdAt !== undefined) {
      Timestamp.encode(toTimestamp(message.createdAt), writer.uint32(18).fork()).ldelim();
    }
    if (message.retentionHours !== 0) {
      writer.uint32(24).int32(message.retentionHours);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): ExternalEventDef {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseExternalEventDef();
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

          message.createdAt = fromTimestamp(Timestamp.decode(reader, reader.uint32()));
          continue;
        case 3:
          if (tag !== 24) {
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

  fromJSON(object: any): ExternalEventDef {
    return {
      name: isSet(object.name) ? String(object.name) : "",
      createdAt: isSet(object.createdAt) ? String(object.createdAt) : undefined,
      retentionHours: isSet(object.retentionHours) ? Number(object.retentionHours) : 0,
    };
  },

  toJSON(message: ExternalEventDef): unknown {
    const obj: any = {};
    if (message.name !== "") {
      obj.name = message.name;
    }
    if (message.createdAt !== undefined) {
      obj.createdAt = message.createdAt;
    }
    if (message.retentionHours !== 0) {
      obj.retentionHours = Math.round(message.retentionHours);
    }
    return obj;
  },

  create<I extends Exact<DeepPartial<ExternalEventDef>, I>>(base?: I): ExternalEventDef {
    return ExternalEventDef.fromPartial(base ?? ({} as any));
  },
  fromPartial<I extends Exact<DeepPartial<ExternalEventDef>, I>>(object: I): ExternalEventDef {
    const message = createBaseExternalEventDef();
    message.name = object.name ?? "";
    message.createdAt = object.createdAt ?? undefined;
    message.retentionHours = object.retentionHours ?? 0;
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
