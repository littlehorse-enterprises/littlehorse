import { ChannelCredentials } from '@grpc/grpc-js'
import { GrpcTransport } from '@protobuf-ts/grpc-transport'
import type { RpcMetadata } from '@protobuf-ts/runtime-rpc'
import { LittleHorseClient } from './proto/service.client'
import getPropertiesFile from './utils/getPropertiesFile'
import getPropertiesArgs, { ConfigArgs } from './utils/getPropertiesArgs'
import { readFileSync } from 'fs'
import type { LHPublicClient } from './client'
import { promisifyClient } from './client'
import { randomUUID } from 'crypto'
import { LHMisconfigurationError } from './common/errors'
import { OAuthCredentialsProvider } from './common/oauth'
import { LHTypeAdapterRegistry } from './common/typeAdapters'
import type { LHTypeAdapter } from './common/typeAdapters'

export const CONFIG_NAMES = [
  'LHC_API_HOST',
  'LHC_API_PORT',
  'LHC_API_PROTOCOL',
  'LHC_GRPC_RESOURCE_EXHAUSTED_RETRY',
  'LHC_GRPC_MAX_RECEIVE_MESSAGE_LENGTH',
  'LHC_TENANT_ID',
  'LHC_CA_CERT',
  'LHC_CLIENT_CERT',
  'LHC_CLIENT_KEY',
  'LHC_GRPC_KEEPALIVE_TIME_MS',
  'LHC_GRPC_KEEPALIVE_TIMEOUT_MS',
  'LHC_NUM_WORKER_THREADS',
  'LHC_TASK_WORKER_ID',
  'LHC_TASK_WORKER_VERSION',
  'LHC_OAUTH_CLIENT_ID',
  'LHC_OAUTH_CLIENT_SECRET',
  'LHC_OAUTH_ACCESS_TOKEN_URL',
] as const

export type Config = {
  [key in ConfigName]?: string
}

function isResourceExhaustedRetryEnabled(config?: string): boolean {
  return config?.toLowerCase() !== 'false'
}

function parseGrpcMaxReceiveMessageLength(config?: string): number | undefined {
  if (config === undefined || config === '') {
    return undefined
  }
  const value = Number(config)
  if (!Number.isInteger(value) || (value <= 0 && value !== -1)) {
    throw new Error(
      `Invalid LHC_GRPC_MAX_RECEIVE_MESSAGE_LENGTH "${config}": expected a positive number of bytes, or -1 for unlimited`
    )
  }
  return value
}
function parsePositiveInt(config: string | undefined, name: string): number | undefined {
  if (config === undefined || config === '') return undefined
  const value = Number(config)
  if (!Number.isInteger(value) || value <= 0) {
    throw new LHMisconfigurationError(`Invalid ${name} "${config}": expected a positive integer`)
  }
  return value
}

/**
 * OAuth is all-or-nothing: a partial config is a misconfiguration. Validated
 * lazily where OAuth is used, not at construction, mirroring Java's isOauth()
 * so ambient partial LHC_OAUTH_* env cannot break non-OAuth usage.
 */
function requireOauth(value: string | undefined, name: string): string {
  if (!value) {
    throw new LHMisconfigurationError(`${name} is required when configuring OAuth`)
  }
  return value
}

function parsePositiveMillis(config: string | undefined, name: string): number | undefined {
  if (config === undefined || config === '') {
    return undefined
  }
  const value = Number(config)
  if (!Number.isInteger(value) || value <= 0) {
    throw new Error(`Invalid ${name} "${config}": expected a positive number of milliseconds`)
  }
  return value
}

export type ConfigName = (typeof CONFIG_NAMES)[number]

const DEFAULT_CONFIG: Config = {
  LHC_API_HOST: 'localhost',
  LHC_API_PORT: '2023',
  LHC_TENANT_ID: 'default',
  LHC_API_PROTOCOL: 'PLAINTEXT',
}

// Java parity (LHConfig: 45s/5s, keepAliveWithoutCalls on): without these an
// idle poll stream behind NAT or a load balancer dies undetected.
const DEFAULT_KEEPALIVE_TIME_MS = 45000
const DEFAULT_KEEPALIVE_TIMEOUT_MS = 5000

/** Mints a fresh bearer token for a call; undefined sends the call anonymous. */
export type AccessTokenProvider = () => Promise<string | undefined>

export class LHConfig {
  private apiHost?: string = 'localhost'
  private apiPort?: string = '2023'
  private protocol?: string = 'PLAINTEXT'
  private tenantId?: string = 'default'
  private caCert?: string
  private clientCert?: string
  private clientKey?: string
  private resourceExhaustedRetryEnabled: boolean = true
  private grpcMaxReceiveMessageLength?: number
  private keepaliveTimeMs?: number
  private keepaliveTimeoutMs?: number
  private numWorkerThreads: number
  private taskWorkerId: string
  private taskWorkerVersion?: string
  private oauth?: OAuthCredentialsProvider
  private oauthClientId?: string
  private oauthClientSecret?: string
  private oauthTokenEndpoint?: string
  private readonly transports = new Map<string, GrpcTransport>()
  private readonly typeAdapters = new LHTypeAdapterRegistry()

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
    this.grpcMaxReceiveMessageLength = parseGrpcMaxReceiveMessageLength(
      mergedConfig.LHC_GRPC_MAX_RECEIVE_MESSAGE_LENGTH
    )
    this.keepaliveTimeMs = parsePositiveMillis(mergedConfig.LHC_GRPC_KEEPALIVE_TIME_MS, 'LHC_GRPC_KEEPALIVE_TIME_MS')
    this.keepaliveTimeoutMs = parsePositiveMillis(
      mergedConfig.LHC_GRPC_KEEPALIVE_TIMEOUT_MS,
      'LHC_GRPC_KEEPALIVE_TIMEOUT_MS'
    )
    this.numWorkerThreads = parsePositiveInt(mergedConfig.LHC_NUM_WORKER_THREADS, 'LHC_NUM_WORKER_THREADS') ?? 8
    // Java defaults the worker id to a random value so two workers on one host
    // stay distinguishable in server-side logs.
    this.taskWorkerId = mergedConfig.LHC_TASK_WORKER_ID || randomUUID()
    this.taskWorkerVersion = mergedConfig.LHC_TASK_WORKER_VERSION

    this.oauthClientId = mergedConfig.LHC_OAUTH_CLIENT_ID
    this.oauthClientSecret = mergedConfig.LHC_OAUTH_CLIENT_SECRET
    this.oauthTokenEndpoint = mergedConfig.LHC_OAUTH_ACCESS_TOKEN_URL

    if (this.protocol !== 'PLAINTEXT' && this.protocol !== 'TLS') {
      // Java throws here too; falling back silently would ship plaintext
      // traffic for a typo like 'tls' or 'SSL'.
      throw new LHMisconfigurationError(`Invalid LHC_API_PROTOCOL "${this.protocol}": expected PLAINTEXT or TLS`)
    }

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

  /**
   * Environment variables are read as the base layer and explicit args win —
   * mirroring the Java SDK, whose no-arg LHConfig reads LHC_* from the
   * environment.
   */
  public static from(args: Partial<ConfigArgs>): LHConfig {
    const envConfig = pickKnownConfig(process.env as Partial<Config>)
    const config = getPropertiesArgs(args)
    return new LHConfig({ ...envConfig, ...config })
  }

  /**
   * Instantiate LHConfig from an in-memory map of `LHC_*` properties.
   * Unrecognized keys are ignored.
   */
  public static fromMap(config: Partial<Config>): LHConfig {
    return new LHConfig(pickKnownConfig(config))
  }

  /**
   * Starts a builder that can combine several config sources. Sources are
   * merged in call order, so a later source overrides an earlier one (matches
   * Java's `LHConfigBuilder`, which does `props.putAll(...)` per source).
   */
  public static newBuilder(): LHConfigBuilder {
    return new LHConfigBuilder()
  }

  /** Returns every recognized config option name. */
  public static configNames(): readonly ConfigName[] {
    return CONFIG_NAMES
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
   * A client whose calls each carry a freshly-minted OAuth bearer token
   * (Java refreshes per RPC through CallCredentials; the provider caches the
   * token and refreshes it before expiry). Separate from getClient() because
   * acquiring a token is asynchronous, and making every client call async
   * would be a breaking change for non-OAuth users.
   */
  public async getAuthenticatedClient(): Promise<LHPublicClient> {
    const oauth = this.getOauthProvider()
    if (oauth === undefined) {
      return this.getClient()
    }
    return this.createClientForHost(this.apiHost!, this.apiPort!, () => oauth.getToken())
  }

  /**
   * Creates a transport pointing at the given host/port. Used internally by the
   * task worker to create per-host connections.
   */
  public createTransport(host: string, port: string | number): GrpcTransport {
    const clientOptions = this.getClientOptions()
    return new GrpcTransport({
      host: `${host}:${port}`,
      channelCredentials: this.channelCredentials,
      ...(Object.keys(clientOptions).length > 0 && { clientOptions }),
    })
  }

  /** gRPC channel options derived from the configured channel settings. */
  public getClientOptions(): Record<string, number> {
    const options: Record<string, number> = {
      'grpc.keepalive_time_ms': this.keepaliveTimeMs ?? DEFAULT_KEEPALIVE_TIME_MS,
      'grpc.keepalive_timeout_ms': this.keepaliveTimeoutMs ?? DEFAULT_KEEPALIVE_TIMEOUT_MS,
      'grpc.keepalive_permit_without_calls': 1,
    }
    if (this.grpcMaxReceiveMessageLength !== undefined) {
      options['grpc.max_receive_message_length'] = this.grpcMaxReceiveMessageLength
    }
    return options
  }

  public createClientForHost(
    host: string,
    port: string | number,
    accessToken?: string | AccessTokenProvider
  ): LHPublicClient {
    return this.createClientForTransport(this.sharedTransport(host, port), accessToken)
  }

  /**
   * One cached transport per host:port (Java caches channels the same way), so
   * repeated getClient() calls reuse a connection instead of leaking one each.
   * Released by close().
   */
  private sharedTransport(host: string, port: string | number): GrpcTransport {
    const key = `${host}:${port}`
    const existing = this.transports.get(key)
    if (existing) return existing
    const transport = this.createTransport(host, port)
    this.transports.set(key, transport)
    return transport
  }

  /** Closes every transport this config created through getClient()/createClientForHost(). */
  public close(): void {
    for (const transport of this.transports.values()) {
      transport.close()
    }
    this.transports.clear()
  }

  public createClientForTransport(
    transport: GrpcTransport,
    accessToken?: string | AccessTokenProvider
  ): LHPublicClient {
    const staticToken = typeof accessToken === 'string' ? accessToken : undefined
    return promisifyClient(new LittleHorseClient(transport), {
      defaultOptions: { meta: this.getMetadata(staticToken) },
      resourceExhaustedRetryEnabled: this.resourceExhaustedRetryEnabled,
      tokenProvider: typeof accessToken === 'function' ? accessToken : undefined,
    })
  }

  getResourceExhaustedRetryEnabled(): boolean {
    return this.resourceExhaustedRetryEnabled
  }

  /**
   * Returns the configured max gRPC receive message length in bytes, if any.
   */
  getGrpcMaxReceiveMessageLength(): number | undefined {
    return this.grpcMaxReceiveMessageLength
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

  /** Returns the configured bootstrap host. */
  getApiBootstrapHost(): string | undefined {
    return this.apiHost
  }

  /** Returns the configured bootstrap port. */
  getApiBootstrapPort(): string | undefined {
    return this.apiPort
  }

  /** Returns the configured API protocol (`PLAINTEXT` or `TLS`). */
  getApiProtocol(): string | undefined {
    return this.protocol
  }

  /** Fetches a TaskDef by name through the configured client. */
  async getTaskDef(name: string) {
    return (await this.getAuthenticatedClient()).getTaskDef({ name })
  }

  /** Worker concurrency (Java: LHConfig#getWorkerThreads/getInflightTasks). */
  getNumWorkerThreads(): number {
    return this.numWorkerThreads
  }

  /** Stable id for this worker process (Java: LHConfig#getTaskWorkerId). */
  getTaskWorkerId(): string {
    return this.taskWorkerId
  }

  /** Optional worker version tag (Java: LHConfig#getTaskWorkerVersion). */
  getTaskWorkerVersion(): string | undefined {
    return this.taskWorkerVersion
  }

  /**
   * Registers serde for a type the SDK does not know natively
   * (Java: LHConfigBuilder#addTypeAdapter).
   */
  addTypeAdapter(adapter: LHTypeAdapter): this {
    this.typeAdapters.add(adapter)
    return this
  }

  /**
   * The adapters the task worker consults when serializing values (task
   * outputs, exception content, checkpoints). Reads stay with the built-ins
   * unless an adapter is asked for by name, and compile-time adapter support
   * in the wfsdk is tracked in the conformance exemptions
   * (Java: LHConfig#getTypeAdapterRegistry).
   */
  getTypeAdapterRegistry(): LHTypeAdapterRegistry {
    return this.typeAdapters
  }

  /**
   * Whether OAuth credentials are configured (Java: LHConfig#isOauth).
   * Like Java, a partial LHC_OAUTH_* trio throws here, when OAuth is about to
   * be used, and never at construction.
   */
  isOauth(): boolean {
    // Absent means undefined, like Java's null check: an empty string counts
    // as present-but-invalid and throws below.
    if (
      this.oauthClientId === undefined &&
      this.oauthClientSecret === undefined &&
      this.oauthTokenEndpoint === undefined
    ) {
      return false
    }
    requireOauth(this.oauthClientId, 'LHC_OAUTH_CLIENT_ID')
    requireOauth(this.oauthClientSecret, 'LHC_OAUTH_CLIENT_SECRET')
    requireOauth(this.oauthTokenEndpoint, 'LHC_OAUTH_ACCESS_TOKEN_URL')
    return true
  }

  /**
   * The OAuth provider, when configured. Throws LHMisconfigurationError for a
   * partial LHC_OAUTH_* trio, same as isOauth().
   */
  getOauthProvider(): OAuthCredentialsProvider | undefined {
    if (!this.isOauth()) {
      return undefined
    }
    this.oauth ??= new OAuthCredentialsProvider({
      clientId: this.oauthClientId!,
      clientSecret: this.oauthClientSecret!,
      tokenEndpoint: this.oauthTokenEndpoint!,
    })
    return this.oauth
  }

  /** Returns the configured gRPC keepalive time in ms, if any. */
  getKeepaliveTimeMs(): number | undefined {
    return this.keepaliveTimeMs
  }

  /** Returns the configured gRPC keepalive timeout in ms, if any. */
  getKeepaliveTimeoutMs(): number | undefined {
    return this.keepaliveTimeoutMs
  }
}

/** Keeps only recognized `LHC_*` keys with a non-empty value. */
function pickKnownConfig(source: Record<string, string | undefined>): Config {
  return CONFIG_NAMES.reduce<Config>((config, name) => {
    const value = source[name]
    if (value !== undefined && value !== '') {
      config[name] = value
    }
    return config
  }, {})
}

/**
 * Combines multiple config sources. Each `loadFrom*` call merges into the
 * accumulated config, so later sources override earlier ones.
 */
export class LHConfigBuilder {
  private config: Config = {}

  /** Loads recognized `LHC_*` variables from the environment. */
  loadFromEnvVariables(env: Record<string, string | undefined> = process.env): this {
    Object.assign(this.config, pickKnownConfig(env))
    return this
  }

  /** Loads properties from a `littlehorse.config`-style file. */
  loadFromPropertiesFile(path: string): this {
    Object.assign(this.config, getPropertiesFile(path))
    return this
  }

  /** Loads properties from an in-memory map. Unrecognized keys are ignored. */
  loadFromMap(map: Partial<Config>): this {
    Object.assign(this.config, pickKnownConfig(map))
    return this
  }

  build(): LHConfig {
    return LHConfig.fromMap(this.config)
  }
}
