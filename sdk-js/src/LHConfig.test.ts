import { afterAll, beforeEach, describe, expect, it, jest } from '@jest/globals'
import { readFileSync } from 'fs'
import { ChannelCredentials } from '@grpc/grpc-js'
import { LHConfig } from './LHConfig'

jest.mock('fs', () => ({
  readFileSync: jest.fn(),
}))

jest.mock('@grpc/grpc-js', () => ({
  ChannelCredentials: {
    createSsl: jest.fn(),
    createInsecure: jest.fn(() => 'insecure-creds'),
  },
}))

const mockTransports: Array<{ opts: unknown; close: ReturnType<typeof jest.fn> }> = []
jest.mock('@protobuf-ts/grpc-transport', () => ({
  GrpcTransport: class {
    close = jest.fn()
    constructor(public opts: unknown) {
      mockTransports.push(this as never)
    }
  },
}))

// from() reads LHC_* env, so the developer's shell must not leak into
// assertions. Scrubbed before every test and restored at the end.
const ORIGINAL_LHC_ENV = Object.fromEntries(Object.entries(process.env).filter(([key]) => key.startsWith('LHC_')))

function scrubLhcEnv() {
  for (const key of Object.keys(process.env)) {
    if (key.startsWith('LHC_')) delete process.env[key]
  }
}

beforeEach(() => {
  scrubLhcEnv()
  mockTransports.length = 0
})

afterAll(() => {
  scrubLhcEnv()
  Object.assign(process.env, ORIGINAL_LHC_ENV)
})

describe('LHConfig', () => {
  beforeEach(() => {
    jest.clearAllMocks()
    ;(ChannelCredentials.createInsecure as jest.Mock).mockReturnValue('insecure-creds')
  })

  it('uses insecure channel credentials by default', () => {
    const config = LHConfig.from({})

    expect(ChannelCredentials.createSsl).not.toHaveBeenCalled()
    expect(ChannelCredentials.createInsecure).toHaveBeenCalled()
    expect(config.getChannelCredentials()).toBe('insecure-creds')
  })

  it('creates TLS channel credentials with a CA certificate', () => {
    const caBuffer = Buffer.from('ca-cert')
    const tlsCreds = {} as ReturnType<typeof ChannelCredentials.createSsl>
    ;(readFileSync as jest.Mock).mockReturnValue(caBuffer)
    ;(ChannelCredentials.createSsl as jest.Mock).mockReturnValue(tlsCreds)

    const config = LHConfig.from({
      protocol: 'TLS',
      caCert: '/path/to/ca.crt',
    })

    expect(readFileSync).toHaveBeenCalledWith('/path/to/ca.crt')
    expect(ChannelCredentials.createSsl).toHaveBeenCalledWith(caBuffer)
    expect(config.getChannelCredentials()).toBe(tlsCreds)
  })

  it('creates mTLS channel credentials when client cert and key are provided', () => {
    const caBuffer = Buffer.from('ca-cert')
    const clientCertBuffer = Buffer.from('client-cert')
    const clientKeyBuffer = Buffer.from('client-key')
    ;(readFileSync as jest.Mock)
      .mockReturnValueOnce(caBuffer)
      .mockReturnValueOnce(clientCertBuffer)
      .mockReturnValueOnce(clientKeyBuffer)
    ;(ChannelCredentials.createSsl as jest.Mock).mockReturnValue('mtls-creds')

    LHConfig.from({
      protocol: 'TLS',
      caCert: '/path/to/ca.crt',
      clientCert: '/path/to/client.crt',
      clientKey: '/path/to/client.key',
    })

    expect(readFileSync).toHaveBeenNthCalledWith(1, '/path/to/ca.crt')
    expect(readFileSync).toHaveBeenNthCalledWith(2, '/path/to/client.crt')
    expect(readFileSync).toHaveBeenNthCalledWith(3, '/path/to/client.key')
    expect(ChannelCredentials.createSsl).toHaveBeenCalledWith(caBuffer, clientKeyBuffer, clientCertBuffer)
  })

  it('falls back to one-way TLS when only one client credential file is provided', () => {
    const clientCertBuffer = Buffer.from('client-cert')
    ;(readFileSync as jest.Mock).mockReturnValue(clientCertBuffer)
    ;(ChannelCredentials.createSsl as jest.Mock).mockReturnValue('tls-creds')

    LHConfig.from({
      protocol: 'TLS',
      clientCert: '/path/to/client.crt',
    })

    expect(readFileSync).toHaveBeenCalledWith('/path/to/client.crt')
    expect(ChannelCredentials.createSsl).toHaveBeenCalledWith(null)
  })

  it('applies keepalive defaults so idle connections are detected as dead', () => {
    const options = LHConfig.from({}).getClientOptions()
    expect(options['grpc.keepalive_time_ms']).toBe(45000)
    expect(options['grpc.keepalive_timeout_ms']).toBe(5000)
    expect(options['grpc.keepalive_permit_without_calls']).toBe(1)
  })

  it('lets explicit keepalive config override the defaults', () => {
    process.env.LHC_GRPC_KEEPALIVE_TIME_MS = '10000'
    const options = LHConfig.from({}).getClientOptions()
    expect(options['grpc.keepalive_time_ms']).toBe(10000)
    expect(options['grpc.keepalive_timeout_ms']).toBe(5000)
  })

  it('reuses one transport per host and closes it via close()', () => {
    const config = LHConfig.from({})
    config.getClient()
    config.getClient()
    expect(mockTransports).toHaveLength(1)

    config.close()
    expect(mockTransports[0].close).toHaveBeenCalled()

    config.getClient()
    expect(mockTransports).toHaveLength(2)
  })

  it('rejects an unknown protocol instead of falling back to plaintext', () => {
    expect(() => LHConfig.from({ protocol: 'tls' })).toThrow(/LHC_API_PROTOCOL/)
    expect(() => LHConfig.from({ protocol: 'SSL' })).toThrow(/LHC_API_PROTOCOL/)
  })

  it('enables resource exhausted retry by default', () => {
    const config = LHConfig.from({})

    expect(config.getResourceExhaustedRetryEnabled()).toBe(true)
  })

  it('allows disabling resource exhausted retry', () => {
    const config = LHConfig.from({
      grpcResourceExhaustedRetry: 'false',
    })

    expect(config.getResourceExhaustedRetryEnabled()).toBe(false)
  })

  it('leaves max receive message length unset by default', () => {
    const config = LHConfig.from({})

    expect(config.getGrpcMaxReceiveMessageLength()).toBeUndefined()
  })

  it('parses max receive message length', () => {
    const config = LHConfig.from({
      grpcMaxReceiveMessageLength: '8388608',
    })

    expect(config.getGrpcMaxReceiveMessageLength()).toBe(8388608)
  })

  it('accepts -1 as unlimited max receive message length', () => {
    const config = LHConfig.from({
      grpcMaxReceiveMessageLength: '-1',
    })

    expect(config.getGrpcMaxReceiveMessageLength()).toBe(-1)
  })

  it('rejects an invalid max receive message length', () => {
    expect(() =>
      LHConfig.from({
        grpcMaxReceiveMessageLength: 'lots',
      })
    ).toThrow(/LHC_GRPC_MAX_RECEIVE_MESSAGE_LENGTH/)

    expect(() =>
      LHConfig.from({
        grpcMaxReceiveMessageLength: '0',
      })
    ).toThrow(/LHC_GRPC_MAX_RECEIVE_MESSAGE_LENGTH/)
  })
})

describe('LHConfig.from environment layering', () => {
  it('reads LHC_* environment variables as the base layer', () => {
    process.env.LHC_API_HOST = 'env-host'
    process.env.LHC_API_PORT = '9999'
    const config = LHConfig.from({})
    expect(`${config.getApiBootstrapHost()}:${config.getApiBootstrapPort()}`).toBe('env-host:9999')
  })

  it('lets explicit args override the environment', () => {
    process.env.LHC_API_HOST = 'env-host'
    const config = LHConfig.from({ apiHost: 'arg-host' })
    expect(`${config.getApiBootstrapHost()}:${config.getApiBootstrapPort()}`).toBe('arg-host:2023')
  })

  it('ignores args passed with an undefined value', () => {
    process.env.LHC_API_HOST = 'env-host'
    const config = LHConfig.from({ apiHost: undefined, tenantId: undefined })
    expect(config.getApiBootstrapHost()).toBe('env-host')
    expect(config.getTenantId()).toBe('default')
  })

  it('falls back to defaults when an undefined arg has no env value', () => {
    const config = LHConfig.from({ apiHost: undefined })
    expect(`${config.getApiBootstrapHost()}:${config.getApiBootstrapPort()}`).toBe('localhost:2023')
  })
})

describe('LHConfig OAuth validation', () => {
  it('constructs despite a partial ambient OAuth trio', () => {
    process.env.LHC_OAUTH_CLIENT_ID = 'lhctl'
    const config = LHConfig.from({ apiHost: 'localhost', apiPort: '2023' })
    expect(config.getApiBootstrapHost()).toBe('localhost')
  })

  it('throws for a partial OAuth trio only when OAuth is used', () => {
    process.env.LHC_OAUTH_CLIENT_ID = 'lhctl'
    const config = LHConfig.from({})
    expect(() => config.isOauth()).toThrow(/LHC_OAUTH_CLIENT_SECRET/)
    expect(() => config.getOauthProvider()).toThrow(/LHC_OAUTH_CLIENT_SECRET/)
  })

  it('reports OAuth enabled for a complete trio', () => {
    process.env.LHC_OAUTH_CLIENT_ID = 'id'
    process.env.LHC_OAUTH_CLIENT_SECRET = 'secret'
    process.env.LHC_OAUTH_ACCESS_TOKEN_URL = 'https://idp/token'
    const config = LHConfig.from({})
    expect(config.isOauth()).toBe(true)
    expect(config.getOauthProvider()).toBeDefined()
  })

  it('reports OAuth disabled when no trio is present', () => {
    const config = LHConfig.from({})
    expect(config.isOauth()).toBe(false)
    expect(config.getOauthProvider()).toBeUndefined()
  })
})
