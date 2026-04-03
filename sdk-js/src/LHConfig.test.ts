import { beforeEach, describe, expect, it, jest } from '@jest/globals'
import { readFileSync } from 'fs'
import { ChannelCredentials, createChannel } from 'nice-grpc'
import { LHConfig } from './LHConfig'

const createMock = jest.fn()
const useMock = jest.fn().mockReturnValue({ create: createMock })

jest.mock('fs', () => ({
  readFileSync: jest.fn(),
}))

jest.mock('nice-grpc', () => ({
  ChannelCredentials: {
    createSsl: jest.fn(),
  },
  createChannel: jest.fn(),
  createClientFactory: jest.fn(() => ({
    use: useMock,
  })),
  Metadata: jest.fn(() => ({
    append: jest.fn().mockReturnThis(),
  })),
}))

describe('LHConfig', () => {
  beforeEach(() => {
    jest.clearAllMocks()
  })

  it('creates a plaintext channel by default', () => {
    LHConfig.from({})

    expect(ChannelCredentials.createSsl).not.toHaveBeenCalled()
    expect(createChannel).toHaveBeenCalledWith('localhost:2023', undefined)
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
    expect(createChannel).toHaveBeenCalledWith('localhost:2023', tlsCreds)
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
    expect(ChannelCredentials.createSsl).toHaveBeenCalledWith(undefined)
  })
})
