import { BinaryReader, WireType } from '@protobuf-ts/runtime'
import { RpcError } from '@protobuf-ts/runtime-rpc'
import type { RpcMetadata } from '@protobuf-ts/runtime-rpc'

const STATUS_DETAILS_KEY = 'grpc-status-details-bin'
const DEFAULT_RETRY_DELAY_MS = 500

interface AnyMessage {
  typeUrl: string
  value: Uint8Array
}

/**
 * Returns true when the given error is a gRPC RESOURCE_EXHAUSTED error.
 */
export function isResourceExhausted(error: unknown): boolean {
  return error instanceof RpcError && error.code === 'RESOURCE_EXHAUSTED'
}

/**
 * Computes how long to wait before retrying a RESOURCE_EXHAUSTED error. Honors
 * the server-provided `google.rpc.RetryInfo` when present, otherwise falls back
 * to a default delay.
 */
export function getRetryDelayMs(error: unknown): number | undefined {
  if (!isResourceExhausted(error)) {
    return undefined
  }

  const fromMetadata = extractRetryDelayMsFromMetadata((error as RpcError).meta)
  if (fromMetadata !== undefined) {
    return fromMetadata
  }

  return DEFAULT_RETRY_DELAY_MS
}

export function extractRetryDelayMsFromMetadata(metadata?: RpcMetadata): number | undefined {
  const statusDetails = getStatusDetails(metadata)
  if (!statusDetails) {
    return undefined
  }

  const details = decodeGoogleRpcStatusDetails(statusDetails)
  for (const detail of details) {
    if (!detail.typeUrl.endsWith('/google.rpc.RetryInfo')) {
      continue
    }

    const delayMs = decodeRetryInfoDelayMs(detail.value)
    return delayMs !== undefined && delayMs > 0 ? delayMs : undefined
  }

  return undefined
}

function getStatusDetails(metadata?: RpcMetadata): Uint8Array | undefined {
  const value = metadata?.[STATUS_DETAILS_KEY]
  if (!value) {
    return undefined
  }

  const raw = Array.isArray(value) ? value[0] : value
  if (!raw) {
    return undefined
  }

  return Buffer.from(raw, 'base64')
}

function decodeGoogleRpcStatusDetails(input: Uint8Array): AnyMessage[] {
  const reader = new BinaryReader(input)
  const details: AnyMessage[] = []
  while (reader.pos < reader.len) {
    const [fieldNo, wireType] = reader.tag()
    if (fieldNo === 3) {
      details.push(decodeAny(reader.bytes()))
    } else {
      reader.skip(wireType)
    }
  }
  return details
}

function decodeAny(input: Uint8Array): AnyMessage {
  const reader = new BinaryReader(input)
  const message: AnyMessage = { typeUrl: '', value: new Uint8Array(0) }
  while (reader.pos < reader.len) {
    const [fieldNo, wireType] = reader.tag()
    switch (fieldNo) {
      case 1:
        message.typeUrl = reader.string()
        break
      case 2:
        message.value = reader.bytes()
        break
      default:
        reader.skip(wireType)
        break
    }
  }
  return message
}

function decodeRetryInfoDelayMs(input: Uint8Array): number | undefined {
  const reader = new BinaryReader(input)
  while (reader.pos < reader.len) {
    const [fieldNo, wireType] = reader.tag()
    if (fieldNo === 1 && wireType === WireType.LengthDelimited) {
      // google.protobuf.Duration retry_delay = 1;
      const durationReader = new BinaryReader(reader.bytes())
      let seconds = 0
      let nanos = 0
      while (durationReader.pos < durationReader.len) {
        const [durFieldNo, durWireType] = durationReader.tag()
        switch (durFieldNo) {
          case 1:
            seconds = durationReader.int64().toNumber()
            break
          case 2:
            nanos = durationReader.int32()
            break
          default:
            durationReader.skip(durWireType)
            break
        }
      }
      return seconds * 1_000 + nanos / 1_000_000
    }
    reader.skip(wireType)
  }
  return undefined
}
