import { afterAll, beforeAll, describe, expect, jest, test } from '@jest/globals'
import { GenericContainer, StartedTestContainer, Wait } from 'testcontainers'
import { LHConfig } from '../LHConfig'
import { LHStatus } from '../proto/common_enums'
import { OAuthCredentialsProvider } from '../common'
import { Workflow } from '../wfsdk'
import { createTaskWorker } from '../worker'
import { LHCluster } from './cluster'
import { allocatePort } from './container'
import { awaitWfSpecReady, getVariable, sleep, uniqueName, unwrap, waitForWfRun } from './harness'

/**
 * OAuth end to end against a real issuer and a real server.
 *
 * The config matrix proves our OAuth client speaks the protocol correctly, but
 * against a stand-in inside the test process. This proves the whole chain: a
 * real OAuth2 issuer mints the token, the LH server validates it by calling
 * that issuer's introspection endpoint, and an unauthenticated call is
 * actually refused.
 *
 * The issuer runs on the cluster's Docker network so the *server* can reach it
 * by alias, and is published on a host port so the *client* can reach it too —
 * they resolve the same issuer by different addresses.
 */

jest.setTimeout(900_000)

const OAUTH_IMAGE = process.env.LH_IT_OAUTH_IMAGE ?? 'quay.io/keycloak/keycloak:26.0'
const REALM = 'lhtest'
const CLIENT_ID = 'lh-sdk-js'
const CLIENT_SECRET = 'sdk-js-secret'
/**
 * Keycloak is pinned to one canonical issuer URL. Without this it derives the
 * issuer from each request's Host header, so a token minted by the client via
 * localhost is rejected when the *server* introspects it via the network
 * alias — the two would disagree about who issued it. (The lighter
 * mock-oauth2-server has the same behavior with no way to pin it, which is
 * why this uses a real IdP.)
 */
const CANONICAL_ISSUER = 'http://keycloak:8080'

describe('OAuth against a real issuer', () => {
  let cluster: LHCluster
  let issuer: StartedTestContainer
  let tokenEndpoint: string

  beforeAll(async () => {
    cluster = new LHCluster({
      nodes: 0,
      listenerName: 'OAUTHLISTENER',
      extraEnv: () => ({
        // The listener authenticates with OAuth; the server introspects each
        // presented token against the issuer, reachable by network alias.
        LHS_LISTENERS_AUTHENTICATION_MAP: 'OAUTHLISTENER:OAUTH',
        LHS_OAUTH_CLIENT_ID: CLIENT_ID,
        LHS_OAUTH_CLIENT_SECRET: CLIENT_SECRET,
        LHS_OAUTH_INTROSPECT_URL: `${CANONICAL_ISSUER}/realms/${REALM}/protocol/openid-connect/token/introspect`,
      }),
    })
    await cluster.startInfra()

    const issuerPort = await allocatePort()
    issuer = await new GenericContainer(OAUTH_IMAGE)
      .withNetwork(cluster.dockerNetwork)
      .withNetworkAliases('keycloak')
      .withExposedPorts({ container: 8080, host: issuerPort })
      .withEnvironment({
        KC_BOOTSTRAP_ADMIN_USERNAME: 'admin',
        KC_BOOTSTRAP_ADMIN_PASSWORD: 'admin',
        KC_HOSTNAME: CANONICAL_ISSUER,
        KC_HOSTNAME_STRICT: 'false',
        KC_HOSTNAME_BACKCHANNEL_DYNAMIC: 'false',
      })
      .withCopyFilesToContainer([
        {
          source: require('path').resolve(__dirname, 'fixtures/keycloak-realm.json'),
          target: '/opt/keycloak/data/import/realm.json',
        },
      ])
      .withCommand(['start-dev', '--import-realm'])
      .withWaitStrategy(Wait.forHttp(`/realms/${REALM}/.well-known/openid-configuration`, 8080))
      .withStartupTimeout(180_000)
      .start()

    // The client reaches the same issuer on the host port; the token it mints
    // still carries the canonical issuer, so the server's introspection agrees.
    tokenEndpoint = `http://localhost:${issuerPort}/realms/${REALM}/protocol/openid-connect/token`

    await cluster.addNode(1)
    await cluster.awaitReadyWith(async port => {
      const token = await mintToken()
      return LHConfig.fromMap({ LHC_API_HOST: 'localhost', LHC_API_PORT: String(port) }).getClient(token)
    })
  }, 900_000)

  afterAll(async () => {
    await cluster?.stop()
    await issuer?.stop({ remove: true, removeVolumes: true }).catch(() => {})
  })

  /** Fetches a token the way the SDK does, from the real issuer. */
  async function mintToken(): Promise<string> {
    const provider = new OAuthCredentialsProvider({
      clientId: CLIENT_ID,
      clientSecret: CLIENT_SECRET,
      tokenEndpoint,
    })
    return provider.getToken()
  }

  test('the SDK obtains a token from a real OAuth2 issuer', async () => {
    const provider = new OAuthCredentialsProvider({
      clientId: CLIENT_ID,
      clientSecret: CLIENT_SECRET,
      tokenEndpoint,
    })

    const token = await provider.getToken()
    // A real issuer returns a JWT, not an opaque stub value.
    expect(token.split('.')).toHaveLength(3)

    const cached = provider.getCachedToken()
    expect(cached?.clientId).toBe(CLIENT_ID)
    // The issuer told us a real lifetime, so refresh has something to work with.
    expect(cached!.expiresAt).toBeGreaterThan(Date.now())
    expect(provider.fetchCount).toBe(1)

    // Cached, not re-fetched, on the next call.
    await provider.getToken()
    expect(provider.fetchCount).toBe(1)
  })

  test('the server rejects a call with no token on an OAuth listener', async () => {
    const anonymous = LHConfig.fromMap({
      LHC_API_HOST: 'localhost',
      LHC_API_PORT: String(cluster.bootstrapPort),
    }).getClient()

    // If this ever succeeds, every other assertion here is meaningless.
    await expect(anonymous.putTaskDef({ name: 'should-not-work', inputVars: [] })).rejects.toThrow()
  })

  test('a worker authenticates with OAuth and executes a real task', async () => {
    const token = await mintToken()
    const config = LHConfig.fromMap({
      LHC_API_HOST: 'localhost',
      LHC_API_PORT: String(cluster.bootstrapPort),
      LHC_OAUTH_CLIENT_ID: CLIENT_ID,
      LHC_OAUTH_CLIENT_SECRET: CLIENT_SECRET,
      LHC_OAUTH_ACCESS_TOKEN_URL: tokenEndpoint,
    })
    expect(config.isOauth()).toBe(true)

    // getAuthenticatedClient mints its own token through the same provider.
    const client = await config.getAuthenticatedClient()
    const taskDefName = uniqueName('oauth-task').replace(/[^a-z0-9-]/g, '')
    await client.putTaskDef({ name: taskDefName, inputVars: [] })

    const wf = Workflow.newWorkflow(uniqueName('oauth-wf'), thread => {
      const out = thread.declareStr('out')
      out.assign(thread.execute(taskDefName))
    })
    await client.putWfSpec(wf.compileWorkflow())
    await awaitWfSpecReady(client, wf.getName())

    // The worker needs credentials too: its poll and report streams are
    // authenticated calls on the same listener.
    const worker = createTaskWorker(() => 'authenticated', taskDefName, config, {
      inputVars: {},
      heartbeatIntervalMs: 500,
      accessToken: token,
    })

    try {
      await worker.start()
      await sleep(500)

      const wfRun = await client.runWf({ wfSpecName: wf.getName(), variables: {} })
      const finished = await waitForWfRun(client, wfRun.id!, 120_000)

      expect(LHStatus[finished.status]).toBe('COMPLETED')
      expect(unwrap(await getVariable(client, wfRun.id!, 'out'))).toBe('authenticated')
    } finally {
      await worker.close()
    }
  })
})
