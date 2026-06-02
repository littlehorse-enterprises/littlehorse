import { ClientError } from 'nice-grpc'
import Long from 'long'
import _m0 from 'protobufjs/minimal'

const RESOURCE_EXHAUSTED_CODE = 8
const STATUS_DETAILS_KEY = 'grpc-status-details-bin'

type MetadataLike = {
  get(key: string): readonly (string | Uint8Array)[]
}

interface GoogleRpcStatus {
  details: AnyMessage[]
}

interface AnyMessage {
  typeUrl: string
  value: Uint8Array
}

interface RetryInfoMessage {
  retryDelay?: DurationMessage
}

interface DurationMessage {
  seconds: number
  nanos: number
}

export function extractRetryDelayMsFromMetadata(metadata?: MetadataLike): number | undefined {
  const statusDetails = getStatusDetails(metadata)
  if (!statusDetails) {
    return undefined
  }

  const status = decodeGoogleRpcStatus(statusDetails)
  for (const detail of status.details) {
    if (!detail.typeUrl.endsWith('/google.rpc.RetryInfo')) {
      continue
    }

    const retryInfo = decodeRetryInfo(detail.value)
    if (!retryInfo.retryDelay) {
      return undefined
    }

    const delayMs = retryInfo.retryDelay.seconds * 1_000 + retryInfo.retryDelay.nanos / 1_000_000
    return delayMs > 0 ? delayMs : undefined
  }

  return undefined
}

export function createResourceExhaustedRetryMiddleware() {
  return async function* (call: any, options: any) {
    if (call.method.requestStream || call.method.responseStream) {
      return yield* call.next(call.request, options)
    }

    while (true) {
      let trailingMetadata: MetadataLike | undefined
      const nextOptions = {
        ...options,
        onTrailers(trailers: MetadataLike) {
          trailingMetadata = trailers
          options.onTrailers?.(trailers)
        },
      }

      try {
        return yield* call.next(call.request, nextOptions)
      } catch (error) {
        const delayMs = getRetryDelayMs(error, trailingMetadata)
        if (delayMs === undefined) {
          throw error
        }

        await sleep(delayMs)
      }
    }
  }
}

const DEFAULT_RETRY_DELAY_MS = 500

function getRetryDelayMs(error: unknown, trailingMetadata?: MetadataLike): number | undefined {
  if (!(error instanceof ClientError) || error.code !== RESOURCE_EXHAUSTED_CODE) {
    return undefined
  }

  const fromMetadata = extractRetryDelayMsFromMetadata(trailingMetadata)
  if (fromMetadata !== undefined) {
    return fromMetadata
  }

  return DEFAULT_RETRY_DELAY_MS
}

function getStatusDetails(metadata?: MetadataLike): Uint8Array | undefined {
  const values = metadata?.get(STATUS_DETAILS_KEY)
  if (!values || values.length === 0) {
    return undefined
  }

  const value = values[0]
  if (typeof value === 'string') {
    return Buffer.from(value, 'base64')
  }

  return value
}

function decodeGoogleRpcStatus(input: Uint8Array): GoogleRpcStatus {
  const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input)
  const message: GoogleRpcStatus = { details: [] }
  while (reader.pos < reader.len) {
    const tag = reader.uint32()
    switch (tag >>> 3) {
      case 3:
        message.details.push(decodeAny(reader, reader.uint32()))
        break
      default:
        reader.skipType(tag & 7)
        break
    }
  }
  return message
}

function decodeAny(reader: _m0.Reader, length: number): AnyMessage {
  const end = reader.pos + length
  const message: AnyMessage = { typeUrl: '', value: new Uint8Array(0) }
  while (reader.pos < end) {
    const tag = reader.uint32()
    switch (tag >>> 3) {
      case 1:
        message.typeUrl = reader.string()
        break
      case 2:
        message.value = reader.bytes()
        break
      default:
        reader.skipType(tag & 7)
        break
    }
  }
  return message
}

function decodeRetryInfo(input: Uint8Array): RetryInfoMessage {
  const reader = input instanceof _m0.Reader ? input : _m0.Reader.create(input)
  const message: RetryInfoMessage = {}
  while (reader.pos < reader.len) {
    const tag = reader.uint32()
    switch (tag >>> 3) {
      case 1:
        message.retryDelay = decodeDuration(reader, reader.uint32())
        break
      default:
        reader.skipType(tag & 7)
        break
    }
  }
  return message
}

function decodeDuration(reader: _m0.Reader, length: number): DurationMessage {
  const end = reader.pos + length
  const message: DurationMessage = { seconds: 0, nanos: 0 }
  while (reader.pos < end) {
    const tag = reader.uint32()
    switch (tag >>> 3) {
      case 1:
        message.seconds = longToNumber(reader.int64() as Long | number)
        break
      case 2:
        message.nanos = reader.int32()
        break
      default:
        reader.skipType(tag & 7)
        break
    }
  }
  return message
}

function longToNumber(long: Long | number): number {
  if (typeof long === 'number') {
    return long
  }
  if (long.gt(Number.MAX_SAFE_INTEGER)) {
    throw new Error('Value exceeds Number.MAX_SAFE_INTEGER')
  }
  if (long.lt(Number.MIN_SAFE_INTEGER)) {
    throw new Error('Value is less than Number.MIN_SAFE_INTEGER')
  }
  return long.toNumber()
}

function sleep(delayMs: number): Promise<void> {
  return new Promise(resolve => setTimeout(resolve, delayMs))
}
