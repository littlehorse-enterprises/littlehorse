import { Channel, ChannelCredentials, Client, createChannel, createClientFactory } from 'nice-grpc'
import { Metadata } from 'nice-grpc-common'
import { LittleHorseDefinition } from './proto/service'

type ServerConfig = {
  host: string
  oauth: boolean
  ssl?: SSLConfig
}

type SSLConfig = {
  enabled: boolean
  rootCa?: string
}

const DEFAULT_CONFIG: ServerConfig = {
  host: 'localhost:2023',
  oauth: false,
}

type ClientConfig = {
  accessToken?: string
  tenantId?: string
}

const DEFAULT_TENANT_ID = 'default'

/**
 * LHClient
 */
export default class LHClient {
  private config: ServerConfig
  private channel: Channel

  constructor(config: Partial<ServerConfig> = {}) {
    const mergedConfig = { ...DEFAULT_CONFIG, ...config }
    this.config = mergedConfig

    const ssl = this.config.ssl
    let channelCredentials
    if (ssl?.enabled) {
      const rootCa = ssl.rootCa ? Buffer.from(ssl.rootCa) : undefined
      channelCredentials = ChannelCredentials.createSsl(rootCa)
    }

    this.channel = createChannel(this.config.host, channelCredentials)
  }

  /**
   * Get gRPC client for littlehorse
   *
   * For more documentation about it's method please go to {@link https://littlehorse.dev}
   *
   * @param options - An object optionally containing `accessToken` and `tenantId`
   * @returns a gRPC client for littlehorse
   */
  public getClient(options: ClientConfig = {}): Client<typeof LittleHorseDefinition> {
    return createClientFactory()
      .use((call, callOptions) =>
        call.next(call.request, {
          ...callOptions,
          metadata: this.getMetadata(options, callOptions.metadata),
        })
      )
      .create(LittleHorseDefinition, this.channel)
  }

  private getMetadata({ accessToken, tenantId }: ClientConfig, metadata?: Metadata): Metadata {
    const newMetadata = Metadata(metadata).append('tenantId', tenantId || DEFAULT_TENANT_ID)
    if (!accessToken) return newMetadata
    return newMetadata.append('Authorization', `Bearer ${accessToken}`)
  }
}
type CreateClientConfig = {
  server?: Partial<ServerConfig>
  client?: Partial<ClientConfig>
}

export const createClient = ({ server, client }: CreateClientConfig = {}): Client<typeof LittleHorseDefinition> => {
  return new LHClient(server).getClient(client)
}
