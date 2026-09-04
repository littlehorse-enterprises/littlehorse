import { afterAll, beforeAll, describe, expect, jest, test } from '@jest/globals'
import { LHStatus } from '../proto/common_enums'
import { Workflow } from '../wfsdk'
import { createTaskWorker, LHTaskWorker } from '../worker'
import { LHCluster } from './cluster'
import { awaitWfSpecReady, getVariable, sleep, uniqueName, unwrap, waitForWfRun } from './harness'

/**
 * Tests that need a real multi-node LittleHorse cluster.
 *
 * `lh-standalone` bundles its own Kafka, so two of them are two separate
 * clusters — rebalancing cannot be observed there at all. These start Kafka
 * plus real `lh-server` nodes (cluster.ts), which is slower but is the only
 * way to prove host discovery and rebalance against something that actually
 * rebalances.
 *
 * Measured while writing these, and worth knowing: a node *joining* shows up
 * in `yourHosts` within ~3s, but a node that *dies* keeps being advertised for
 * 54s+ (membership expires on a Kafka session timeout, not immediately). The
 * tests below are shaped around that reality rather than around what would be
 * convenient to assert.
 */

jest.setTimeout(900_000)

/** Registers a one-task workflow and returns everything needed to run it. */
async function setupWorkflow(cluster: LHCluster, label: string) {
  const config = cluster.config()
  const client = config.getClient()
  const taskDefName = uniqueName(label).replace(/[^a-z0-9-]/g, '')

  const wf = Workflow.newWorkflow(uniqueName(`${label}-wf`), thread => {
    const out = thread.declareStr('out')
    out.assign(thread.execute(taskDefName))
  })
  await client.putTaskDef({ name: taskDefName, inputVars: [] })
  await wf.registerWfSpec(config)
  await awaitWfSpecReady(client, wf.getName())

  return { config, client, taskDefName, wfSpecName: wf.getName() }
}

/** Runs the workflow once and asserts it completes with the task's output. */
async function expectWorkCompletes(
  client: ReturnType<ReturnType<LHCluster['config']>['getClient']>,
  wfSpecName: string
): Promise<void> {
  const wfRun = await client.runWf({ wfSpecName, variables: {} })
  const finished = await waitForWfRun(client, wfRun.id!, 120_000)
  expect(LHStatus[finished.status]).toBe('COMPLETED')
  expect(unwrap(await getVariable(client, wfRun.id!, 'out'))).toBe('ok')
}

/**
 * Runs the workflow until it completes, tolerating the RPC failures that
 * happen while the cluster is reassigning partitions after a node loss.
 */
async function expectWorkEventuallyCompletes(
  client: ReturnType<ReturnType<LHCluster['config']>['getClient']>,
  wfSpecName: string,
  timeoutMs = 180_000
): Promise<void> {
  const deadline = Date.now() + timeoutMs
  let lastError: unknown
  while (Date.now() < deadline) {
    try {
      await expectWorkCompletes(client, wfSpecName)
      return
    } catch (err) {
      lastError = err
      await sleep(2000)
    }
  }
  throw new Error(`work never completed after the node loss: ${(lastError as Error)?.message}`)
}

async function waitForHosts(worker: LHTaskWorker, count: number, timeoutMs = 90_000): Promise<void> {
  const deadline = Date.now() + timeoutMs
  while (worker.healthStatus().connectedHosts !== count && Date.now() < deadline) {
    await sleep(250)
  }
}

describe('multi-node cluster', () => {
  describe('scaling up', () => {
    let cluster: LHCluster

    beforeAll(async () => {
      cluster = new LHCluster({ nodes: 1 })
      await cluster.start()
      await cluster.awaitReady()
    }, 900_000)

    afterAll(async () => {
      await cluster?.stop()
    })

    test('a worker rebalances onto a node that joins the cluster', async () => {
      const { config, client, taskDefName, wfSpecName } = await setupWorkflow(cluster, 'cl-join')
      const worker = createTaskWorker(() => 'ok', taskDefName, config, {
        inputVars: {},
        heartbeatIntervalMs: 500,
      })

      try {
        await worker.start()
        await waitForHosts(worker, 1)
        expect(worker.healthStatus().connectedHosts).toBe(1)
        await expectWorkCompletes(client, wfSpecName)

        // A second server joins an already-running cluster. The bootstrap
        // node starts advertising it, and the worker must pick it up on its
        // own — this is the rebalance path, and it is fast (~3s observed).
        await cluster.addNode(2)
        await cluster.awaitReady()

        await waitForHosts(worker, 2)
        expect(worker.healthStatus().connectedHosts).toBe(2)
        expect(worker.healthStatus().healthy).toBe(true)

        // Still correct after rebalancing, now polling both nodes.
        await expectWorkCompletes(client, wfSpecName)
      } finally {
        await worker.close()
      }
    })
  })

  describe('losing a node', () => {
    let cluster: LHCluster

    beforeAll(async () => {
      cluster = new LHCluster({ nodes: 2 })
      await cluster.start()
      await cluster.awaitReady()
    }, 900_000)

    afterAll(async () => {
      await cluster?.stop()
    })

    test('a worker and client recover after a node dies', async () => {
      const { config, client, taskDefName, wfSpecName } = await setupWorkflow(cluster, 'cl-die')
      const worker = createTaskWorker(() => 'ok', taskDefName, config, {
        inputVars: {},
        heartbeatIntervalMs: 500,
      })

      try {
        await worker.start()
        await waitForHosts(worker, 2)
        expect(worker.healthStatus().connectedHosts).toBe(2)

        // Kill a non-bootstrap node out from under the worker.
        const doomed = cluster.nodes.find(n => n.port !== cluster.bootstrapPort)!
        await cluster.stopNode(doomed.instanceId)

        // Losing a node genuinely disrupts the cluster: partitions the dead
        // node owned are unserved until Kafka Streams reassigns them, so
        // requests in that window fail outright ("io exception"). The
        // property that matters is recovery, not uninterrupted service.
        await expectWorkEventuallyCompletes(client, wfSpecName)

        // Once recovered it keeps working, and the worker never gave up.
        await expectWorkEventuallyCompletes(client, wfSpecName)
        expect(worker.healthStatus().connectedHosts).toBeGreaterThanOrEqual(1)
        expect(worker.isRunning()).toBe(true)
      } finally {
        await worker.close()
      }
    })
  })
})
