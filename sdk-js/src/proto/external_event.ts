/* eslint-disable */
import Long from "long";
import _m0 from "protobufjs/minimal";
import { Timestamp } from "./google/protobuf/timestamp";
import { ExternalEventDefId, ExternalEventId } from "./object_id";
import { VariableValue } from "./variable";

export const protobufPackage = "littlehorse";

/**
 * An ExternalEvent represents A Thing That Happened outside the context of a WfRun.
 * Generally, an ExternalEvent is used to represent a document getting signed, an incident
 * being resolved, an order being fulfilled, etc.
 *
 * ExternalEvent's are created via the 'rpc PutExternalEvent'
 *
 * For more context on ExternalEvents, check our documentation here:
 * https://littlehorse.dev/docs/concepts/external-events
 */
export interface ExternalEvent {
  /**
   * The ID of the ExternalEvent. This contains WfRunId, ExternalEventDefId,
   * and a unique guid which can be used for idempotency of the `PutExternalEvent`
   * rpc call.
   */
  id:
    | ExternalEventId
    | undefined;
  /** The time the ExternalEvent was registered with LittleHorse. */
  createdAt:
    | string
    | undefined;
  /** The payload of this ExternalEvent. */
  content:
    | VariableValue
    | undefined;
  /**
   * If the ExternalEvent was claimed by a specific ThreadRun (via Interrupt or
   * EXTERNAL_EVENT Node), this is set to the number of the relevant ThreadRun.
   */
  threadRunNumber?:
    | number
    | undefined;
  /**
   * If the ExternalEvent was claimed by a specific ThreadRun (via EXTERNAL_EVENT
   * Node; note that in the case of an Interrupt the node_run_position will never
   * be set), this is set to the number of the relevant NodeRun.
   */
  nodeRunPosition?:
    | number
    | undefined;
  /** Whether the ExternalEvent has been claimed by a WfRun. */
  claimed: boolean;
}

/** The ExternalEventDef defines the blueprint for an ExternalEvent. */
export interface ExternalEventDef {
  /** The id of the ExternalEventDef. */
  id:
    | ExternalEventDefId
    | undefined;
  /** When the ExternalEventDef was created. */
  createdAt:
    | string
    | undefined;
  /**
   * The retention policy for ExternalEvent's of this ExternalEventDef. This applies to the
   * ExternalEvent **only before** it is matched with a WfRun.
   */
  retentionPolicy: ExternalEventRetentionPolicy | undefined;
}

/**
 * Policy to determine how long an ExternalEvent is retained after creation if it
 * is not yet claimed by a WfRun. Note that once a WfRun has been matched with the
 * ExternalEvent, the ExternalEvent is deleted if/when that WfRun is deleted.
 * If not set, then ExternalEvent's are not deleted if they are not matched with
 * a WfRun.
 *
 * A future version of LittleHorse will allow changing the retention_policy, which
 * will trigger a cleanup of old `ExternalEvent`s.
 */
export interface ExternalEventRetentionPolicy {
  /**
   * Delete such an ExternalEvent X seconds after it has been registered if it
   * has not yet been claimed by a WfRun.
   */
  secondsAfterPut?: number | undefined;
}

function createBaseExternalEvent(): ExternalEvent {
  return {
    id: undefined,
    createdAt: undefined,
    content: undefined,
    threadRunNumber: undefined,
    nodeRunPosition: undefined,
    claimed: false,
  };
}

export const ExternalEvent = {
  encode(message: ExternalEvent, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.id !== undefined) {
      ExternalEventId.encode(message.id, writer.uint32(10).fork()).ldelim();
    }
    if (message.createdAt !== undefined) {
      Timestamp.encode(toTimestamp(message.createdAt), writer.uint32(18).fork()).ldelim();
    }
    if (message.content !== undefined) {
      VariableValue.encode(message.content, writer.uint32(26).fork()).ldelim();
    }
    if (message.threadRunNumber !== undefined) {
      writer.uint32(32).int32(message.threadRunNumber);
    }
    if (message.nodeRunPosition !== undefined) {
      writer.uint32(40).int32(message.nodeRunPosition);
    }
    if (message.claimed !== false) {
      writer.uint32(48).bool(message.claimed);
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

          message.id = ExternalEventId.decode(reader, reader.uint32());
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

          message.content = VariableValue.decode(reader, reader.uint32());
          continue;
        case 4:
          if (tag !== 32) {
            break;
          }

          message.threadRunNumber = reader.int32();
          continue;
        case 5:
          if (tag !== 40) {
            break;
          }

          message.nodeRunPosition = reader.int32();
          continue;
        case 6:
          if (tag !== 48) {
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

  create(base?: DeepPartial<ExternalEvent>): ExternalEvent {
    return ExternalEvent.fromPartial(base ?? {});
  },
  fromPartial(object: DeepPartial<ExternalEvent>): ExternalEvent {
    const message = createBaseExternalEvent();
    message.id = (object.id !== undefined && object.id !== null) ? ExternalEventId.fromPartial(object.id) : undefined;
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
  return { id: undefined, createdAt: undefined, retentionPolicy: undefined };
}

export const ExternalEventDef = {
  encode(message: ExternalEventDef, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.id !== undefined) {
      ExternalEventDefId.encode(message.id, writer.uint32(10).fork()).ldelim();
    }
    if (message.createdAt !== undefined) {
      Timestamp.encode(toTimestamp(message.createdAt), writer.uint32(18).fork()).ldelim();
    }
    if (message.retentionPolicy !== undefined) {
      ExternalEventRetentionPolicy.encode(message.retentionPolicy, writer.uint32(26).fork()).ldelim();
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

          message.id = ExternalEventDefId.decode(reader, reader.uint32());
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

          message.retentionPolicy = ExternalEventRetentionPolicy.decode(reader, reader.uint32());
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create(base?: DeepPartial<ExternalEventDef>): ExternalEventDef {
    return ExternalEventDef.fromPartial(base ?? {});
  },
  fromPartial(object: DeepPartial<ExternalEventDef>): ExternalEventDef {
    const message = createBaseExternalEventDef();
    message.id = (object.id !== undefined && object.id !== null)
      ? ExternalEventDefId.fromPartial(object.id)
      : undefined;
    message.createdAt = object.createdAt ?? undefined;
    message.retentionPolicy = (object.retentionPolicy !== undefined && object.retentionPolicy !== null)
      ? ExternalEventRetentionPolicy.fromPartial(object.retentionPolicy)
      : undefined;
    return message;
  },
};

function createBaseExternalEventRetentionPolicy(): ExternalEventRetentionPolicy {
  return { secondsAfterPut: undefined };
}

export const ExternalEventRetentionPolicy = {
  encode(message: ExternalEventRetentionPolicy, writer: _m0.Writer = _m0.Writer.create()): _m0.Writer {
    if (message.secondsAfterPut !== undefined) {
      writer.uint32(8).int64(message.secondsAfterPut);
    }
    return writer;
  },

  decode(input: _m0.Reader | Uint8Array, length?: number): ExternalEventRetentionPolicy {
    const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input);
    let end = length === undefined ? reader.len : reader.pos + length;
    const message = createBaseExternalEventRetentionPolicy();
    while (reader.pos < end) {
      const tag = reader.uint32();
      switch (tag >>> 3) {
        case 1:
          if (tag !== 8) {
            break;
          }

          message.secondsAfterPut = longToNumber(reader.int64() as Long);
          continue;
      }
      if ((tag & 7) === 4 || tag === 0) {
        break;
      }
      reader.skipType(tag & 7);
    }
    return message;
  },

  create(base?: DeepPartial<ExternalEventRetentionPolicy>): ExternalEventRetentionPolicy {
    return ExternalEventRetentionPolicy.fromPartial(base ?? {});
  },
  fromPartial(object: DeepPartial<ExternalEventRetentionPolicy>): ExternalEventRetentionPolicy {
    const message = createBaseExternalEventRetentionPolicy();
    message.secondsAfterPut = object.secondsAfterPut ?? undefined;
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
