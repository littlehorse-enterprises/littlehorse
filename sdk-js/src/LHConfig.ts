import { Channel, ChannelCredentials, Client, Metadata, createChannel, createClientFactory } from 'nice-grpc'
import { LittleHorseDefinition } from './proto/service'
import { createResourceExhaustedRetryMiddleware } from './grpcRetry'
import getPropertiesFile from './utils/getPropertiesFile'
import getPropertiesArgs, { ConfigArgs } from './utils/getPropertiesArgs'
import { readFileSync } from 'fs'

export const CONFIG_NAMES = [
  'LHC_API_HOST',
  'LHC_API_PORT',
  'LHC_API_PROTOCOL',
  'LHC_TENANT_ID',
  'LHC_CA_CERT',
  'LHC_CLIENT_CERT',
  'LHC_CLIENT_KEY',
] as const

export type Config = {
  [key in ConfigName]?: string
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
  private channel: Channel

  private channelCredentials?: ChannelCredentials

  private constructor(config: Config) {
    const mergedConfig = { ...DEFAULT_CONFIG, ...config } as Config
    this.apiHost = mergedConfig.LHC_API_HOST
    this.apiPort = mergedConfig.LHC_API_PORT
    this.protocol = mergedConfig.LHC_API_PROTOCOL
    this.tenantId = mergedConfig.LHC_TENANT_ID
    this.caCert = mergedConfig.LHC_CA_CERT
    this.clientCert = mergedConfig.LHC_CLIENT_CERT
    this.clientKey = mergedConfig.LHC_CLIENT_KEY

    if (this.protocol === 'TLS') {
      const rootCa = this.caCert ? readFileSync(this.caCert) : undefined
      const clientCert = this.clientCert ? readFileSync(this.clientCert) : undefined
      const clientKey = this.clientKey ? readFileSync(this.clientKey) : undefined

      if (clientCert && clientKey) {
        this.channelCredentials = ChannelCredentials.createSsl(rootCa, clientKey, clientCert)
      } else {
        this.channelCredentials = ChannelCredentials.createSsl(rootCa)
      }
    }

    this.channel = this.openChannel(this.apiHost!, this.apiPort!)
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
   * @param options - An object optionally containing `accessToken` and `tenantId`
   * @returns a gRPC client for littlehorse
   */
  public getClient(accessToken?: string): Client<typeof LittleHorseDefinition> {
    return this.createClientForChannel(this.channel, accessToken)
  }

  public openChannel(host: string, port: string | number): Channel {
    return createChannel(`${host}:${port}`, this.channelCredentials)
  }

  public createClientForChannel(
    channel: Channel,
    accessToken?: string
  ): Client<typeof LittleHorseDefinition> {
    return createClientFactory()
      .use(createResourceExhaustedRetryMiddleware())
      .use((call, options) =>
        call.next(call.request, {
          ...options,
          metadata: this.getMetadata(options.metadata, accessToken),
        })
      )
      .create(LittleHorseDefinition, channel)
  }

  private getMetadata(metadata?: Metadata, accessToken?: string): Metadata {
    let newMetadata = Metadata(metadata)

    if (this.tenantId) {
      newMetadata = newMetadata.append('tenantId', this.tenantId)
    }

    if (accessToken) {
      newMetadata = newMetadata.append('Authorization', `Bearer ${accessToken}`)
    }

    return newMetadata
  }

  /**
   * Returns the channel credentials for TLS connections, or undefined for plaintext.
   * Used internally by the task worker to create per-host connections.
   */
  getChannelCredentials(): ChannelCredentials | undefined {
    return this.channelCredentials
  }

  /**
   * Returns the configured tenant ID.
   */
  getTenantId(): string | undefined {
    return this.tenantId
  }
}
