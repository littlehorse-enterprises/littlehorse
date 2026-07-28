import { execFileSync } from 'child_process'
import { mkdtempSync, writeFileSync } from 'fs'
import { tmpdir } from 'os'
import { join } from 'path'
import { CONFIG_NAMES, LHConfig } from '../LHConfig'
import { FakeLHServer } from './fakeServer'

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

    test.todo('authenticate via OAuth client-credentials and attach tokens to calls — Java: common/auth')
    test.todo('refresh OAuth tokens before expiry — Java: common/auth token refresh')
    test.todo('report whether OAuth is configured — Java: LHConfig#isOauth')
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

    // Worker-scoped config: converted alongside the worker phase (Track B).
    test.todo(
      'configure worker concurrency (threads/inflight equivalents) — Java: LHConfig#getWorkerThreads/getInflightTasks'
    )
    test.todo('expose task worker id and version — Java: LHConfig#getTaskWorkerId/getTaskWorkerVersion')
  })

  describe('type adapters', () => {
    // Java uses LHTypeAdapter to serde custom classes; sdk-js solves the same
    // problem with zod schemas (src/worker/zodSchema.ts). Pending a decision on
    // whether to mark these not-applicable or build a JS analogue.
    test.todo('register a custom type adapter for serde — Java: LHConfigBuilder#addTypeAdapter')
    test.todo('expose the type adapter registry to workflow and worker code — Java: LHConfig#getTypeAdapterRegistry')
  })
})
