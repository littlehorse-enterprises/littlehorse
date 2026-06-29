import { ChannelCredentials } from '@grpc/grpc-js'
import { GrpcTransport } from '@protobuf-ts/grpc-transport'
import type { RpcMetadata } from '@protobuf-ts/runtime-rpc'
import { LittleHorseClient } from './proto/service.client'
import getPropertiesFile from './utils/getPropertiesFile'
import getPropertiesArgs, { ConfigArgs } from './utils/getPropertiesArgs'
import { readFileSync } from 'fs'
import type { LHPublicClient } from './client'
import { promisifyClient } from './client'

export const CONFIG_NAMES = [
  'LHC_API_HOST',
  'LHC_API_PORT',
  'LHC_API_PROTOCOL',
  'LHC_GRPC_RESOURCE_EXHAUSTED_RETRY',
  'LHC_TENANT_ID',
  'LHC_CA_CERT',
  'LHC_CLIENT_CERT',
  'LHC_CLIENT_KEY',
] as const

export type Config = {
  [key in ConfigName]?: string
}

function isResourceExhaustedRetryEnabled(config?: string): boolean {
  return config?.toLowerCase() !== 'false'
}
export type ConfigName = (typeof CONFIG_NAMES)[number]

const DEFAULT_CONFIG: Config = {
  LHC_API_HOST: 'localhost',
  LHC_API_PORT: '2023',
  LHC_TENANT_ID: 'default',
  LHC_API_PROTOCOL: 'PLAINTEXT',
}

export class LHConfig {
  private apiHost?: string = 'localhost'
  private apiPort?: string = '2023'
  private protocol?: string = 'PLAINTEXT'
  private tenantId?: string = 'default'
  private caCert?: string
  private clientCert?: string
  private clientKey?: string
  private resourceExhaustedRetryEnabled: boolean = true

  private channelCredentials: ChannelCredentials

  private constructor(config: Config) {
    const mergedConfig = { ...DEFAULT_CONFIG, ...config } as Config
    this.apiHost = mergedConfig.LHC_API_HOST
    this.apiPort = mergedConfig.LHC_API_PORT
    this.protocol = mergedConfig.LHC_API_PROTOCOL
    this.tenantId = mergedConfig.LHC_TENANT_ID
    this.caCert = mergedConfig.LHC_CA_CERT
    this.clientCert = mergedConfig.LHC_CLIENT_CERT
    this.clientKey = mergedConfig.LHC_CLIENT_KEY
    this.resourceExhaustedRetryEnabled = isResourceExhaustedRetryEnabled(mergedConfig.LHC_GRPC_RESOURCE_EXHAUSTED_RETRY)

    if (this.protocol === 'TLS') {
      const rootCa = this.caCert ? readFileSync(this.caCert) : null
      const clientCert = this.clientCert ? readFileSync(this.clientCert) : null
      const clientKey = this.clientKey ? readFileSync(this.clientKey) : null

      if (clientCert && clientKey) {
        this.channelCredentials = ChannelCredentials.createSsl(rootCa, clientKey, clientCert)
      } else {
        this.channelCredentials = ChannelCredentials.createSsl(rootCa)
      }
    } else {
      this.channelCredentials = ChannelCredentials.createInsecure()
    }
  }

  /**
   * Instantiate LHConfig from properties file
   * @param file - path to properties file
   * @returns LHConfig instance
   */
  public static fromConfigFile(file: string): LHConfig {
    const config = getPropertiesFile(file)
    return new LHConfig(config)
  }

  public static from(args: Partial<ConfigArgs>): LHConfig {
    const config = getPropertiesArgs(args)
    return new LHConfig(config)
  }

  /**
   * Get gRPC client for littlehorse
   *
   * For more documentation about it's method please go to {@link https://littlehorse.io/docs/server}
   *
   * @param accessToken - optional bearer token added to every request
   * @returns a Promise-based gRPC client for littlehorse
   */
  public getClient(accessToken?: string): LHPublicClient {
    return this.createClientForHost(this.apiHost!, this.apiPort!, accessToken)
  }

  /**
   * Creates a transport pointing at the given host/port. Used internally by the
   * task worker to create per-host connections.
   */
  public createTransport(host: string, port: string | number): GrpcTransport {
    return new GrpcTransport({
      host: `${host}:${port}`,
      channelCredentials: this.channelCredentials,
    })
  }

  public createClientForHost(host: string, port: string | number, accessToken?: string): LHPublicClient {
    return this.createClientForTransport(this.createTransport(host, port), accessToken)
  }

  public createClientForTransport(transport: GrpcTransport, accessToken?: string): LHPublicClient {
    return promisifyClient(new LittleHorseClient(transport), {
      defaultOptions: { meta: this.getMetadata(accessToken) },
      resourceExhaustedRetryEnabled: this.resourceExhaustedRetryEnabled,
    })
  }

  getResourceExhaustedRetryEnabled(): boolean {
    return this.resourceExhaustedRetryEnabled
  }

  private getMetadata(accessToken?: string): RpcMetadata {
    const metadata: RpcMetadata = {}

    if (this.tenantId) {
      metadata['tenantId'] = this.tenantId
    }

    if (accessToken) {
      metadata['authorization'] = `Bearer ${accessToken}`
    }

    return metadata
  }

  /**
   * Returns the channel credentials for the configured protocol.
   */
  getChannelCredentials(): ChannelCredentials {
    return this.channelCredentials
  }

  /**
   * Returns the configured tenant ID.
   */
  getTenantId(): string | undefined {
    return this.tenantId
  }
}
