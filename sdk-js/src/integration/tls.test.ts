import { afterAll, beforeAll, describe, expect, jest, test } from '@jest/globals'
import { execFileSync } from 'child_process'
import { mkdtempSync } from 'fs'
import { tmpdir } from 'os'
import { join } from 'path'
import { LHConfig } from '../LHConfig'
import { LHStatus } from '../proto/common_enums'
import { Workflow } from '../wfsdk'
import { createTaskWorker } from '../worker'
import { LHCluster } from './cluster'
import { awaitWfSpecReady, getVariable, sleep, uniqueName, unwrap, waitForWfRun } from './harness'

/**
 * TLS against a real server.
 *
 * The config matrix proves we build the right channel credentials, but never
 * completes a handshake. This starts an `lh-server` with a TLS listener and a
 * certificate generated here, then runs a real workflow over that connection —
 * so the CA wiring, hostname matching, and gRPC-over-TLS path are all
 * exercised rather than assumed.
 */

jest.setTimeout(900_000)

/**
 * Self-signed cert for `localhost`. Generated per run rather than committed:
 * a private key in the repo is a liability, and generation is ~200ms.
 *
 * Throws rather than skipping if openssl is missing — a TLS suite that quietly
 * passes without ever completing a handshake is worse than no suite at all.
 */
function generateServerCert(dir: string): { cert: string; key: string } {
  const key = join(dir, 'server.key')
  const cert = join(dir, 'server.crt')
  try {
    execFileSync(
      'openssl',
      // prettier-ignore
      ['req', '-x509', '-newkey', 'rsa:2048', '-nodes', '-keyout', key, '-out', cert,
       '-days', '1', '-subj', '/CN=localhost',
       // The SAN is what gRPC actually checks; a bare CN is ignored.
       '-addext', 'subjectAltName=DNS:localhost,IP:127.0.0.1'],
      { stdio: 'ignore' }
    )
  } catch (err) {
    throw new Error(`could not generate a test certificate with openssl: ${(err as Error).message}`)
  }
  return { cert, key }
}

describe('TLS against a real server', () => {
  let cluster: LHCluster | undefined
  let caCertPath: string

  beforeAll(async () => {
    const pair = generateServerCert(mkdtempSync(join(tmpdir(), 'lh-tls-')))
    // Self-signed, so the server certificate is also its own CA.
    caCertPath = pair.cert

    cluster = new LHCluster({
      nodes: 0,
      listenerName: 'TLSLISTENER',
      files: [
        { source: pair.cert, target: '/certs/server.crt' },
        { source: pair.key, target: '/certs/server.key' },
      ],
      extraEnv: () => ({
        LHS_LISTENERS_PROTOCOL_MAP: 'TLSLISTENER:TLS',
        LHS_LISTENER_TLSLISTENER_CERT: '/certs/server.crt',
        LHS_LISTENER_TLSLISTENER_KEY: '/certs/server.key',
      }),
    })
    await cluster.startInfra()
    await cluster.addNode(1)
    await cluster.awaitReadyWith(async port => tlsConfig(port).getClient())
  }, 900_000)

  afterAll(async () => {
    await cluster?.stop()
  })

  function tlsConfig(port: number): LHConfig {
    return LHConfig.fromMap({
      LHC_API_HOST: 'localhost',
      LHC_API_PORT: String(port),
      LHC_API_PROTOCOL: 'TLS',
      LHC_CA_CERT: caCertPath,
    })
  }

  test('the client completes a TLS handshake and runs a workflow', async () => {
    const config = tlsConfig(cluster!.bootstrapPort)
    expect(config.getChannelCredentials()._isSecure()).toBe(true)

    const client = config.getClient()
    const taskDefName = uniqueName('tls-task').replace(/[^a-z0-9-]/g, '')
    await client.putTaskDef({ name: taskDefName, inputVars: [] })

    const wf = Workflow.newWorkflow(uniqueName('tls-wf'), thread => {
      const out = thread.declareStr('out')
      out.assign(thread.execute(taskDefName))
    })
    await wf.registerWfSpec(config)
    await awaitWfSpecReady(client, wf.getName())

    // The worker's poll and report streams ride the same TLS channel.
    const worker = createTaskWorker(() => 'over-tls', taskDefName, config, {
      inputVars: {},
      heartbeatIntervalMs: 500,
    })

    try {
      await worker.start()
      await sleep(500)
      const wfRun = await client.runWf({ wfSpecName: wf.getName(), variables: {} })
      const finished = await waitForWfRun(client, wfRun.id!, 120_000)

      expect(LHStatus[finished.status]).toBe('COMPLETED')
      expect(unwrap(await getVariable(client, wfRun.id!, 'out'))).toBe('over-tls')
    } finally {
      await worker.close()
    }
  })

  test('a plaintext client cannot talk to a TLS listener', async () => {
    // Guards the test above: if plaintext also worked, TLS would prove nothing.
    const plaintext = LHConfig.fromMap({
      LHC_API_HOST: 'localhost',
      LHC_API_PORT: String(cluster!.bootstrapPort),
    }).getClient()

    await expect(plaintext.putTaskDef({ name: 'no-tls', inputVars: [] })).rejects.toThrow()
  })
})
