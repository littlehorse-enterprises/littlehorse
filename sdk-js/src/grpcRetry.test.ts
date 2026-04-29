import _m0 from 'protobufjs/minimal'
import { extractRetryDelayMsFromMetadata } from './grpcRetry'

function encodeStatusDetails(delaySeconds: number, delayNanos = 0): Uint8Array {
  const durationWriter = _m0.Writer.create().uint32(8).int64(delaySeconds)
  if (delayNanos > 0) {
    durationWriter.uint32(16).int32(delayNanos)
  }

  const retryInfo = _m0.Writer.create().uint32(10).bytes(durationWriter.finish()).finish()
  const anyMessage = _m0.Writer.create()
    .uint32(10)
    .string('type.googleapis.com/google.rpc.RetryInfo')
    .uint32(18)
    .bytes(retryInfo)
    .finish()

  return _m0.Writer.create().uint32(26).bytes(anyMessage).finish()
}

describe('grpcRetry', () => {
  it('extracts retry delay from grpc status details metadata', () => {
    const statusDetails = encodeStatusDetails(1, 500_000_000)
    const metadata = {
      get(key: string) {
        return key === 'grpc-status-details-bin' ? [statusDetails] : []
      },
    }

    const retryDelayMs = extractRetryDelayMsFromMetadata(metadata)

    expect(retryDelayMs).toBe(1500)
  })

  it('returns undefined when retry info is missing', () => {
    const retryDelayMs = extractRetryDelayMsFromMetadata({
      get() {
        return []
      },
    })

    expect(retryDelayMs).toBeUndefined()
  })
})
