import { BinaryWriter, WireType } from '@protobuf-ts/runtime'
import { extractRetryDelayMsFromMetadata } from './grpcRetry'

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
})
