import { Channel, ChannelCredentials, Client, Metadata, createChannel, createClientFactory } from 'nice-grpc'
import { LittleHorseDefinition } from './proto/service'
import getPropertiesFile from './utils/getPropertiesFile'
import getPropertiesArgs, { ConfigArgs } from './utils/getPropertiesArgs'
import { readFileSync } from 'fs'

export const CONFIG_NAMES = [
  'LHC_API_HOST',
  'LHC_API_PORT',
  'LHC_API_PROTOCOL',
  'LHC_TENANT_ID',
  'LHC_CA_CERT',
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
  private channel: Channel

  private constructor(config: Config) {
    const mergedConfig = { ...DEFAULT_CONFIG, ...config } as Config
    this.apiHost = mergedConfig.LHC_API_HOST
    this.apiPort = mergedConfig.LHC_API_PORT
    this.protocol = mergedConfig.LHC_API_PROTOCOL
    this.tenantId = mergedConfig.LHC_TENANT_ID
    this.caCert = mergedConfig.LHC_CA_CERT

    let channelCredentials
    if (this.protocol === 'TLS') {
      const rootCa = this.caCert ? readFileSync(this.caCert) : undefined
      channelCredentials = ChannelCredentials.createSsl(rootCa)
    }

    this.channel = createChannel(`${this.apiHost}:${this.apiPort}`, channelCredentials)
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
    return createClientFactory()
      .use((call, options) =>
        call.next(call.request, {
          ...options,
          metadata: this.getMetadata(options.metadata, accessToken),
        })
      )
      .create(LittleHorseDefinition, this.channel)
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
}
