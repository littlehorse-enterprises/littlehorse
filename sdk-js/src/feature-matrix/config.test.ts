/**
 * Feature matrix: config and client.
 *
 * See sdk-js/PARITY_PLAN.md. Each test.todo is one capability of the Java
 * SDK's public API (referenced as `Java: Class#method`). sdk-js already
 * implements part of this surface (LHConfig.ts, client.ts, grpcRetry.ts, with
 * unit tests) — convert those todos to real tests first when auditing.
 */

describe('config', () => {
  describe('loading', () => {
    test.todo('load config from a properties file — Java: LHConfigBuilder#loadFromPropertiesFile')
    test.todo('load config from environment variables (LHC_*) — Java: LHConfigBuilder#loadFromEnvVariables')
    test.todo('load config from an in-memory map/object — Java: LHConfigBuilder#loadFromMap')
    test.todo('apply documented precedence when multiple sources are combined — Java: LHConfigBuilder source ordering')
    test.todo('list all recognized config option names — Java: LHConfig.configNames')
  })

  describe('client creation', () => {
    test.todo('create a client for the bootstrap host — Java: LHConfig#getBlockingStub')
    test.todo('create a client for a specific host/port (server topology) — Java: LHConfig#getBlockingStub(host, port)')
    test.todo('create a client bound to a specific tenant — Java: LHConfig#getBlockingStub(host, port, tenantId)')
    test.todo('expose bootstrap host, port, and protocol — Java: LHConfig#getApiBootstrapHost/Port/Protocol')
    test.todo('expose tenant id — Java: LHConfig#getTenantId')
    test.todo('fetch a TaskDef through the configured client — Java: LHConfig#getTaskDef')
  })

  describe('TLS and auth', () => {
    test.todo('connect over TLS with a custom CA certificate — Java: LHConfig TLS options')
    test.todo('connect with mutual TLS (client cert and key) — Java: LHConfig mTLS options')
    test.todo('authenticate via OAuth client-credentials and attach tokens to calls — Java: common/auth')
    test.todo('refresh OAuth tokens before expiry — Java: common/auth token refresh')
    test.todo('report whether OAuth is configured — Java: LHConfig#isOauth')
  })

  describe('channel behavior', () => {
    test.todo('configure gRPC keepalive time and timeout — Java: LHConfig#getKeepaliveTimeMs/getKeepaliveTimeoutMs')
    test.todo(
      'retry calls on RESOURCE_EXHAUSTED when enabled — Java: LHConfig#shouldRetryOnResourceExhausted, retryinterceptor'
    )
    test.todo(
      'configure worker concurrency (threads/inflight equivalents) — Java: LHConfig#getWorkerThreads/getInflightTasks'
    )
    test.todo('expose task worker id and version — Java: LHConfig#getTaskWorkerId/getTaskWorkerVersion')
  })

  describe('type adapters', () => {
    test.todo('register a custom type adapter for serde — Java: LHConfigBuilder#addTypeAdapter')
    test.todo('expose the type adapter registry to workflow and worker code — Java: LHConfig#getTypeAdapterRegistry')
  })
})
