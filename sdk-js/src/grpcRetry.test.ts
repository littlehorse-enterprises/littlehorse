import { BinaryWriter, WireType } from '@protobuf-ts/runtime'
import { RpcError } from '@protobuf-ts/runtime-rpc'
import { extractRetryDelayMsFromMetadata, getRetryDelayMs } from './grpcRetry'

function encodeStatusDetails(delaySeconds: number, delayNanos = 0): string {
  const duration = new BinaryWriter().tag(1, WireType.Varint).int64(delaySeconds)
  if (delayNanos > 0) {
    duration.tag(2, WireType.Varint).int32(delayNanos)
  }

  const retryInfo = new BinaryWriter().tag(1, WireType.LengthDelimited).bytes(duration.finish()).finish()

  const anyMessage = new BinaryWriter()
    .tag(1, WireType.LengthDelimited)
    .string('type.googleapis.com/google.rpc.RetryInfo')
    .tag(2, WireType.LengthDelimited)
    .bytes(retryInfo)
    .finish()

  const status = new BinaryWriter().tag(3, WireType.LengthDelimited).bytes(anyMessage).finish()

  return Buffer.from(status).toString('base64')
}

describe('grpcRetry', () => {
  it('extracts retry delay from grpc status details metadata', () => {
    const statusDetails = encodeStatusDetails(1, 500_000_000)
    const retryDelayMs = extractRetryDelayMsFromMetadata({ 'grpc-status-details-bin': statusDetails })

    expect(retryDelayMs).toBe(1500)
  })

  it('returns undefined when retry info is missing', () => {
    const retryDelayMs = extractRetryDelayMsFromMetadata({})

    expect(retryDelayMs).toBeUndefined()
  })

  describe('getRetryDelayMs', () => {
    it('honors the RetryInfo delay from a server throttle error', () => {
      const error = new RpcError('Quota exceeded. Retry after 1500ms.', 'RESOURCE_EXHAUSTED', {
        'grpc-status-details-bin': encodeStatusDetails(1, 500_000_000),
      })

      expect(getRetryDelayMs(error)).toBe(1500)
    })

    it('does not retry client-generated message-size errors', () => {
      const error = new RpcError('Received message larger than max (4500420 vs 4194304)', 'RESOURCE_EXHAUSTED', {})

      expect(getRetryDelayMs(error)).toBeUndefined()
    })

    it('falls back to the default delay when server details lack RetryInfo', () => {
      const otherDetail = new BinaryWriter()
        .tag(1, WireType.LengthDelimited)
        .string('type.googleapis.com/google.rpc.ErrorInfo')
        .finish()
      const status = new BinaryWriter().tag(3, WireType.LengthDelimited).bytes(otherDetail).finish()
      const error = new RpcError('Quota exceeded.', 'RESOURCE_EXHAUSTED', {
        'grpc-status-details-bin': Buffer.from(status).toString('base64'),
      })

      expect(getRetryDelayMs(error)).toBe(500)
    })

    it('does not retry other error codes', () => {
      const error = new RpcError('not found', 'NOT_FOUND', {})

      expect(getRetryDelayMs(error)).toBeUndefined()
    })
  })
})
