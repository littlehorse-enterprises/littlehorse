import { beforeAll, describe, expect, test } from '@jest/globals'
import { execFileSync } from 'child_process'
import { mkdtempSync, writeFileSync } from 'fs'
import { tmpdir } from 'os'
import { join } from 'path'
import { CONFIG_NAMES, LHConfig } from '../../LHConfig'
import { LHMisconfigurationError, objToVarVal, OAuthCredentialsProvider, varValToObj } from '../../common'
import { createTaskWorker } from '../../worker'
import { FakeLHServer } from '../harness/fakeServer'

/**
 * A stand-in OAuth token endpoint. Recording the requests is the point: the
 * grant type and Basic credentials are part of the contract with the issuer.
 */
function fakeTokenEndpoint(
  requests: Array<{ url: string; headers: Record<string, string>; body: string }>,
  body: object | (() => object),
  delayMs = 0
): typeof fetch {
  return (async (url: string, init: RequestInit) => {
    requests.push({
      url: String(url),
      headers: (init.headers ?? {}) as Record<string, string>,
      body: String(init.body),
    })
    if (delayMs > 0) await new Promise(resolve => setTimeout(resolve, delayMs))
    const payload = typeof body === 'function' ? body() : body
    return { ok: true, status: 200, json: async () => payload } as Response
  }) as unknown as typeof fetch
}

/**
 * Feature matrix: config and client.
 *
 * See sdk-js/PARITY_PLAN.md. Each entry is one capability of the Java SDK's
 * public API (referenced as `Java: Class#method`). test.todo = not yet
 * implemented/proven. Do not delete entries; do not mark features done
 * anywhere else.
 */

let tmp: string
beforeAll(() => {
  tmp = mkdtempSync(join(tmpdir(), 'lh-config-test-'))
})

function writeTmp(name: string, contents: string): string {
  const path = join(tmp, name)
  writeFileSync(path, contents)
  return path
}

/**
 * Generates a real self-signed cert/key pair. gRPC's `createSsl` parses PEM
 * eagerly for client certs, so mTLS cannot be tested with dummy bytes. Certs
 * are generated at test time rather than committed so no private key lives in
 * the repo. Returns null when openssl is unavailable.
 */
function generateCertPair(): { cert: string; key: string } | null {
  try {
    const key = join(tmp, 'client.key')
    const cert = join(tmp, 'client.crt')
    execFileSync(
      'openssl',
      // prettier-ignore
      ['req', '-x509', '-newkey', 'rsa:2048', '-nodes', '-keyout', key, '-out', cert,
       '-days', '1', '-subj', '/CN=lh-sdk-js-test'],
      { stdio: 'ignore' }
    )
    return { cert, key }
  } catch {
    return null
  }
}

describe('config', () => {
  describe('loading', () => {
    test('load config from a properties file — Java: LHConfigBuilder#loadFromPropertiesFile', () => {
      const path = writeTmp('from-file.config', 'LHC_API_HOST=file-host\nLHC_API_PORT=1111\n')
      const config = LHConfig.newBuilder().loadFromPropertiesFile(path).build()
      expect(config.getApiBootstrapHost()).toBe('file-host')
      expect(config.getApiBootstrapPort()).toBe('1111')
    })

    test('load config from environment variables (LHC_*) — Java: LHConfigBuilder#loadFromEnvVariables', () => {
      const config = LHConfig.newBuilder()
        .loadFromEnvVariables({ LHC_API_HOST: 'env-host', LHC_TENANT_ID: 'env-tenant', UNRELATED: 'ignored' })
        .build()
      expect(config.getApiBootstrapHost()).toBe('env-host')
      expect(config.getTenantId()).toBe('env-tenant')
    })

    test('load config from an in-memory map/object — Java: LHConfigBuilder#loadFromMap', () => {
      const config = LHConfig.fromMap({ LHC_API_HOST: 'map-host', LHC_API_PORT: '2222' })
      expect(config.getApiBootstrapHost()).toBe('map-host')
      expect(config.getApiBootstrapPort()).toBe('2222')
    })

    test('apply documented precedence when multiple sources are combined — Java: LHConfigBuilder source ordering', () => {
      // Java merges each source with props.putAll(), so the last source wins.
      const path = writeTmp('precedence.config', 'LHC_API_HOST=file-host\nLHC_API_PORT=1111\n')
      const config = LHConfig.newBuilder()
        .loadFromPropertiesFile(path)
        .loadFromEnvVariables({ LHC_API_HOST: 'env-host' })
        .loadFromMap({ LHC_TENANT_ID: 'map-tenant' })
        .build()

      expect(config.getApiBootstrapHost()).toBe('env-host') // env overrode the file
      expect(config.getApiBootstrapPort()).toBe('1111') // untouched by later sources
      expect(config.getTenantId()).toBe('map-tenant')
    })

    test('list all recognized config option names — Java: LHConfig.configNames', () => {
      const names = LHConfig.configNames()
      expect(names).toBe(CONFIG_NAMES)
      expect(names).toContain('LHC_API_HOST')
      expect(names.every(name => name.startsWith('LHC_'))).toBe(true)
    })
  })

  describe('client creation', () => {
    test('create a client for the bootstrap host — Java: LHConfig#getBlockingStub', () => {
      const client = LHConfig.fromMap({ LHC_API_HOST: 'localhost', LHC_API_PORT: '2023' }).getClient()
      expect(typeof client.putWfSpec).toBe('function')
    })

    test('create a client for a specific host/port (server topology) — Java: LHConfig#getBlockingStub(host, port)', () => {
      const client = LHConfig.fromMap({}).createClientForHost('other-host', 2024)
      expect(typeof client.putWfSpec).toBe('function')
    })

    test('create a client bound to a specific tenant — Java: LHConfig#getBlockingStub(host, port, tenantId)', () => {
      // The tenant travels as request metadata rather than a stub parameter.
      const config = LHConfig.fromMap({ LHC_TENANT_ID: 'my-tenant' })
      expect(config.getTenantId()).toBe('my-tenant')
      expect(LHConfig.fromMap({}).getTenantId()).toBe('default')
    })

    test('expose bootstrap host, port, and protocol — Java: LHConfig#getApiBootstrapHost/Port/Protocol', () => {
      const config = LHConfig.fromMap({ LHC_API_HOST: 'h', LHC_API_PORT: '9', LHC_API_PROTOCOL: 'TLS' })
      expect(config.getApiBootstrapHost()).toBe('h')
      expect(config.getApiBootstrapPort()).toBe('9')
      expect(config.getApiProtocol()).toBe('TLS')

      const defaults = LHConfig.fromMap({})
      expect(defaults.getApiBootstrapHost()).toBe('localhost')
      expect(defaults.getApiBootstrapPort()).toBe('2023')
      expect(defaults.getApiProtocol()).toBe('PLAINTEXT')
    })

    test('expose tenant id — Java: LHConfig#getTenantId', () => {
      expect(LHConfig.fromMap({ LHC_TENANT_ID: 'tenant-a' }).getTenantId()).toBe('tenant-a')
    })

    test('fetch a TaskDef through the configured client — Java: LHConfig#getTaskDef', async () => {
      const server = new FakeLHServer()
      await server.start()
      try {
        const config = LHConfig.fromMap({ LHC_API_HOST: '127.0.0.1', LHC_API_PORT: String(server.port) })
        await expect(config.getTaskDef('my-task')).resolves.toMatchObject({ id: { name: 'my-task' } })
      } finally {
        await server.stop()
      }
    }, 20000)
  })

  describe('TLS and auth', () => {
    test('connect over TLS with a custom CA certificate — Java: LHConfig TLS options', () => {
      const caCert = writeTmp('ca.crt', '-----BEGIN CERTIFICATE-----\ntest\n-----END CERTIFICATE-----\n')
      const secure = LHConfig.fromMap({ LHC_API_PROTOCOL: 'TLS', LHC_CA_CERT: caCert })
      expect(secure.getChannelCredentials()._isSecure()).toBe(true)

      // PLAINTEXT must not silently produce a secure channel.
      expect(LHConfig.fromMap({}).getChannelCredentials()._isSecure()).toBe(false)
    })

    test('connect with mutual TLS (client cert and key) — Java: LHConfig mTLS options', () => {
      const pair = generateCertPair()
      if (pair === null) {
        console.warn('skipping mTLS assertion: openssl unavailable')
        return
      }
      const config = LHConfig.fromMap({
        LHC_API_PROTOCOL: 'TLS',
        LHC_CLIENT_CERT: pair.cert,
        LHC_CLIENT_KEY: pair.key,
      })
      expect(config.getChannelCredentials()._isSecure()).toBe(true)
    })

    test('authenticate via OAuth client-credentials and attach tokens to calls — Java: common/auth', async () => {
      const requests: Array<{ url: string; headers: Record<string, string>; body: string }> = []
      const provider = new OAuthCredentialsProvider({
        clientId: 'my-client',
        clientSecret: 's3cret',
        tokenEndpoint: 'https://issuer.example/oauth2/token',
        fetchImpl: fakeTokenEndpoint(requests, { access_token: 'token-abc', expires_in: 3600 }),
      })

      expect(await provider.getToken()).toBe('token-abc')

      // Client credentials travel as HTTP Basic with a client_credentials
      // grant, matching Java's ClientSecretBasic.
      expect(requests).toHaveLength(1)
      expect(requests[0].url).toBe('https://issuer.example/oauth2/token')
      expect(requests[0].body).toBe('grant_type=client_credentials')
      expect(requests[0].headers.Authorization).toBe(`Basic ${Buffer.from('my-client:s3cret').toString('base64')}`)

      // The token reaches the wire as a bearer credential.
      const server = new FakeLHServer()
      await server.start()
      try {
        const config = LHConfig.fromMap({
          LHC_API_HOST: '127.0.0.1',
          LHC_API_PORT: String(server.port),
          LHC_OAUTH_CLIENT_ID: 'my-client',
          LHC_OAUTH_CLIENT_SECRET: 's3cret',
          LHC_OAUTH_ACCESS_TOKEN_URL: 'https://issuer.example/oauth2/token',
        })
        const client = config.getClient('token-abc')
        await client.putTaskDef({ name: 'oauth-probe', inputVars: [] })
        expect(server.lastAuthorizationHeader).toBe('Bearer token-abc')
      } finally {
        await server.stop()
      }

      // A partial OAuth config is a misconfiguration, not silently ignored.
      expect(() => LHConfig.fromMap({ LHC_OAUTH_CLIENT_ID: 'only-id' })).toThrow(LHMisconfigurationError)
    }, 20000)

    test('refresh OAuth tokens before expiry — Java: common/auth token refresh', async () => {
      const requests: Array<{ url: string; headers: Record<string, string>; body: string }> = []
      let issued = 0
      const provider = new OAuthCredentialsProvider({
        clientId: 'c',
        clientSecret: 's',
        tokenEndpoint: 'https://issuer.example/oauth2/token',
        refreshSkewMs: 30_000,
        fetchImpl: fakeTokenEndpoint(requests, () => ({ access_token: `token-${++issued}`, expires_in: 100 })),
      })

      const t0 = 1_000_000
      expect(await provider.getToken(t0)).toBe('token-1')
      // Cached while comfortably valid: no second round trip.
      expect(await provider.getToken(t0 + 10_000)).toBe('token-1')
      expect(provider.fetchCount).toBe(1)

      // Refreshed *before* actual expiry (100s), once inside the 30s skew, so
      // a token never expires mid-call.
      expect(await provider.getToken(t0 + 75_000)).toBe('token-2')
      expect(provider.fetchCount).toBe(2)

      // Concurrent callers share a single refresh rather than stampeding.
      const stampede = new OAuthCredentialsProvider({
        clientId: 'c',
        clientSecret: 's',
        tokenEndpoint: 'https://issuer.example/oauth2/token',
        fetchImpl: fakeTokenEndpoint([], { access_token: 'shared', expires_in: 3600 }, 20),
      })
      const tokens = await Promise.all([stampede.getToken(), stampede.getToken(), stampede.getToken()])
      expect(tokens).toEqual(['shared', 'shared', 'shared'])
      expect(stampede.fetchCount).toBe(1)
    }, 20000)

    test('report whether OAuth is configured — Java: LHConfig#isOauth', () => {
      expect(LHConfig.fromMap({}).isOauth()).toBe(false)
      const oauth = LHConfig.fromMap({
        LHC_OAUTH_CLIENT_ID: 'c',
        LHC_OAUTH_CLIENT_SECRET: 's',
        LHC_OAUTH_ACCESS_TOKEN_URL: 'https://issuer.example/oauth2/token',
      })
      expect(oauth.isOauth()).toBe(true)
      expect(oauth.getOauthProvider()).toBeInstanceOf(OAuthCredentialsProvider)
    })
  })

  describe('channel behavior', () => {
    test('configure gRPC keepalive time and timeout — Java: LHConfig#getKeepaliveTimeMs/getKeepaliveTimeoutMs', () => {
      const config = LHConfig.fromMap({
        LHC_GRPC_KEEPALIVE_TIME_MS: '30000',
        LHC_GRPC_KEEPALIVE_TIMEOUT_MS: '5000',
      })
      expect(config.getKeepaliveTimeMs()).toBe(30000)
      expect(config.getKeepaliveTimeoutMs()).toBe(5000)
      expect(config.getClientOptions()).toMatchObject({
        'grpc.keepalive_time_ms': 30000,
        'grpc.keepalive_timeout_ms': 5000,
      })

      // Unset means "leave the gRPC default alone", not zero.
      expect(LHConfig.fromMap({}).getClientOptions()).toEqual({})
      expect(() => LHConfig.fromMap({ LHC_GRPC_KEEPALIVE_TIME_MS: '-1' })).toThrow(/positive number of milliseconds/)
    })

    test('retry calls on RESOURCE_EXHAUSTED when enabled — Java: LHConfig#shouldRetryOnResourceExhausted, retryinterceptor', () => {
      expect(LHConfig.fromMap({}).getResourceExhaustedRetryEnabled()).toBe(true)
      expect(LHConfig.fromMap({ LHC_GRPC_RESOURCE_EXHAUSTED_RETRY: 'false' }).getResourceExhaustedRetryEnabled()).toBe(
        false
      )
    })

    test('configure worker concurrency (threads/inflight equivalents) — Java: LHConfig#getWorkerThreads/getInflightTasks', () => {
      expect(LHConfig.fromMap({}).getNumWorkerThreads()).toBe(8)
      expect(LHConfig.fromMap({ LHC_NUM_WORKER_THREADS: '3' }).getNumWorkerThreads()).toBe(3)
      expect(() => LHConfig.fromMap({ LHC_NUM_WORKER_THREADS: '0' })).toThrow(LHMisconfigurationError)
      expect(() => LHConfig.fromMap({ LHC_NUM_WORKER_THREADS: 'many' })).toThrow(LHMisconfigurationError)

      // The worker actually honors it as its in-flight ceiling.
      const worker = createTaskWorker(() => null, 'my-task', LHConfig.fromMap({ LHC_NUM_WORKER_THREADS: '3' }), {
        inputVars: {},
      })
      expect(worker.getInflightTaskCount()).toBe(0)
    })

    test('expose task worker id and version — Java: LHConfig#getTaskWorkerId/getTaskWorkerVersion', () => {
      const explicit = LHConfig.fromMap({ LHC_TASK_WORKER_ID: 'worker-7', LHC_TASK_WORKER_VERSION: '1.2.3' })
      expect(explicit.getTaskWorkerId()).toBe('worker-7')
      expect(explicit.getTaskWorkerVersion()).toBe('1.2.3')

      // Unset id defaults to something unique per process, so two workers on
      // one host stay distinguishable server-side.
      expect(LHConfig.fromMap({}).getTaskWorkerId()).not.toBe(LHConfig.fromMap({}).getTaskWorkerId())
      expect(LHConfig.fromMap({}).getTaskWorkerVersion()).toBeUndefined()

      // The worker derives its own id from the configured one.
      const worker = createTaskWorker(() => null, 'my-task', explicit, { inputVars: {} })
      expect(worker.getTaskWorkerId()).toContain('worker-7')
      expect(worker.getTaskWorkerId()).toContain('my-task')
    })
  })

  describe('type adapters', () => {
    // Resolved: zod covers *schemas*, but not custom class instances, which
    // would otherwise fall through to the generic object branch and come back
    // as plain JSON. A registry closes that gap, as Java's LHTypeAdapter does.
    test('register a custom type adapter for serde — Java: LHConfigBuilder#addTypeAdapter', () => {
      class Money {
        constructor(readonly cents: number) {}
      }

      const config = LHConfig.fromMap({}).addTypeAdapter({
        name: 'money',
        matches: value => value instanceof Money,
        serialize: value => objToVarVal((value as Money).cents),
        deserialize: value => new Money(varValToObj(value) as number),
      })

      // Without the registry a Money would be stored as an opaque JSON object;
      // with it, the adapter decides the encoding.
      const registry = config.getTypeAdapterRegistry()
      expect(objToVarVal(new Money(500), registry).value).toEqual({ oneofKind: 'int', int: '500' })
      expect(objToVarVal(new Money(500)).value).toEqual({ oneofKind: 'jsonObj', jsonObj: '{"cents":500}' })

      // Named adapters round-trip explicitly, since an encoded value carries
      // no marker saying which adapter produced it.
      const adapter = registry.byName('money')!
      expect(adapter.deserialize(objToVarVal(new Money(500), registry))).toEqual(new Money(500))

      // Duplicate names are rejected rather than silently shadowing.
      expect(() =>
        config.addTypeAdapter({
          name: 'money',
          matches: () => false,
          serialize: () => objToVarVal(null),
          deserialize: () => null,
        })
      ).toThrow(/already registered/)
    })

    test('expose the type adapter registry to workflow and worker code — Java: LHConfig#getTypeAdapterRegistry', () => {
      const config = LHConfig.fromMap({})
      expect(config.getTypeAdapterRegistry().size).toBe(0)

      config.addTypeAdapter({
        name: 'always-str',
        matches: value => typeof value === 'symbol',
        serialize: value => objToVarVal(String(value as symbol)),
        deserialize: value => Symbol(varValToObj(value) as string),
      })

      // The same registry instance is handed out, so anything holding the
      // config sees adapters registered later.
      const registry = config.getTypeAdapterRegistry()
      expect(registry.size).toBe(1)
      expect(registry.list().map(a => a.name)).toEqual(['always-str'])
      expect(config.getTypeAdapterRegistry()).toBe(registry)
      expect(registry.byName('nope')).toBeUndefined()
    })
  })
})
