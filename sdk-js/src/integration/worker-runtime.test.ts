import { z } from 'zod'
import type { LHPublicClient } from '../client'
import { LHStatus, TaskStatus } from '../proto/common_enums'
import { Workflow } from '../wfsdk'
import { LHTaskException, WorkerContext } from '../worker'
import {
  RunningWorker,
  awaitWfSpecReady,
  getVariable,
  registerRequiredTaskDefs,
  requireServer,
  sleep,
  startWorker,
  tenantClient,
  tenantConfig,
  uniqueName,
  unwrap,
  waitForWfRun,
} from './harness'

/**
 * Worker behavior that only a real server can drive.
 *
 * The fake server proves what the *client* does when handed a scenario. These
 * prove the things the server itself decides: when to retry, when a task has
 * timed out, which of several competing workers gets a task, and whether a
 * checkpoint actually survives an attempt. None of those can be faked without
 * assuming the answer.
 */

let client: LHPublicClient
const workers: RunningWorker[] = []

beforeAll(async () => {
  await requireServer()
  client = await tenantClient()
}, 120000)

afterAll(async () => {
  await Promise.all(workers.map(w => w.close().catch(() => {})))
})

async function register(
  workflow: Workflow,
  taskInputVars: Record<string, Record<string, z.ZodTypeAny>> = {}
): Promise<string> {
  await registerRequiredTaskDefs(client, workflow, taskInputVars)
  await workflow.registerWfSpec(await tenantConfig())
  await awaitWfSpecReady(client, workflow.getName())
  return workflow.getName()
}

async function track(worker: Promise<RunningWorker>): Promise<RunningWorker> {
  const running = await worker
  workers.push(running)
  return running
}

describe('worker behavior the server drives', () => {
  test('the server schedules retries after a technical failure', async () => {
    let attempts = 0
    await track(
      startWorker('rt-flaky', (ctx: WorkerContext) => {
        attempts++
        // Fail the first two attempts; the server decides whether to retry.
        if (ctx.getAttemptNumber() < 2) throw new Error(`attempt ${ctx.getAttemptNumber()} fails`)
        return 'succeeded-on-retry'
      })
    )

    const wf = Workflow.newWorkflow(uniqueName('rt-retry'), thread => {
      const out = thread.declareStr('out')
      out.assign(thread.execute('rt-flaky').withRetries(3))
    })
    const name = await register(wf)

    const wfRun = await client.runWf({ wfSpecName: name, variables: {} })
    const finished = await waitForWfRun(client, wfRun.id!, 60000)

    // The retries came from the server, not from any loop in the SDK.
    expect(LHStatus[finished.status]).toBe('COMPLETED')
    expect(attempts).toBeGreaterThanOrEqual(3)
    expect(unwrap(await getVariable(client, wfRun.id!, 'out'))).toBe('succeeded-on-retry')
  }, 90000)

  test('a business EXCEPTION is not retried, unlike a technical ERROR', async () => {
    let attempts = 0
    await track(
      startWorker('rt-exception', () => {
        attempts++
        throw new LHTaskException('not-retryable', 'business rule says no')
      })
    )

    const wf = Workflow.newWorkflow(uniqueName('rt-noretry'), thread => {
      thread.execute('rt-exception').withRetries(3)
    })
    const name = await register(wf)

    const wfRun = await client.runWf({ wfSpecName: name, variables: {} })
    const finished = await waitForWfRun(client, wfRun.id!, 60000)

    // Retries exist for transient technical faults; a business exception is a
    // decision, so the server must not repeat the work.
    expect(LHStatus[finished.status]).toBe('EXCEPTION')
    expect(attempts).toBe(1)
  }, 90000)

  test('the server enforces a task timeout when the worker is too slow', async () => {
    await track(
      startWorker('rt-slow', async () => {
        // Comfortably past the 1s timeout configured on the node.
        await sleep(6000)
        return 'too late'
      })
    )

    const wf = Workflow.newWorkflow(uniqueName('rt-timeout'), thread => {
      thread.execute('rt-slow').timeout(1)
    })
    const name = await register(wf)

    const wfRun = await client.runWf({ wfSpecName: name, variables: {} })
    const finished = await waitForWfRun(client, wfRun.id!, 60000)

    // Nothing in the SDK enforces this; the server timed the task out.
    expect(LHStatus[finished.status]).toBe('ERROR')
  }, 90000)

  test('several workers on one TaskDef share the work without duplicating it', async () => {
    const TASKS = 12
    const executedBy = new Map<string, number>()

    const makeWorker = async (label: string) => {
      const running = await startWorker('rt-shared', () => {
        executedBy.set(label, (executedBy.get(label) ?? 0) + 1)
        return label
      })
      workers.push(running)
      return running
    }
    await makeWorker('worker-a')
    await makeWorker('worker-b')

    const wf = Workflow.newWorkflow(uniqueName('rt-compete'), thread => {
      thread.execute('rt-shared')
    })
    const name = await register(wf)

    const runs = await Promise.all(
      Array.from({ length: TASKS }, () => client.runWf({ wfSpecName: name, variables: {} }))
    )
    const finished = await Promise.all(runs.map(run => waitForWfRun(client, run.id!, 60000)))

    // Every run completes exactly once, and the total executions match the
    // number of runs — no task was handed to two workers.
    expect(finished.every(f => LHStatus[f.status] === 'COMPLETED')).toBe(true)
    const totalExecutions = [...executedBy.values()].reduce((sum, n) => sum + n, 0)
    expect(totalExecutions).toBe(TASKS)
  }, 120000)

  test('a checkpointed sub-operation is not repeated when the task is retried', async () => {
    let sideEffects = 0
    let attempts = 0

    await track(
      startWorker('rt-checkpoint', async (ctx: WorkerContext) => {
        attempts++
        const charged = await ctx.executeAndCheckpoint(() => {
          sideEffects++
          return `charge-${sideEffects}`
        })
        // Fail after the checkpoint on the first attempt, so the retry has a
        // completed checkpoint to replay.
        if (ctx.getAttemptNumber() < 1) throw new Error('failing after the checkpoint')
        return charged
      })
    )

    const wf = Workflow.newWorkflow(uniqueName('rt-ckpt'), thread => {
      const out = thread.declareStr('out')
      out.assign(thread.execute('rt-checkpoint').withRetries(2))
    })
    const name = await register(wf)

    const wfRun = await client.runWf({ wfSpecName: name, variables: {} })
    const finished = await waitForWfRun(client, wfRun.id!, 60000)

    expect(LHStatus[finished.status]).toBe('COMPLETED')
    expect(attempts).toBeGreaterThanOrEqual(2)
    // The task ran twice but the side effect happened once — which is the
    // entire point of checkpointing.
    expect(sideEffects).toBe(1)
    expect(unwrap(await getVariable(client, wfRun.id!, 'out'))).toBe('charge-1')
  }, 90000)

  test('a worker started before its TaskDef exists still picks up work later', async () => {
    // Ordering hazard in real deployments: workers commonly boot before the
    // metadata they depend on has been registered.
    const taskDefName = uniqueName('rt-late').replace(/[^a-z0-9-]/g, '')
    await track(startWorker(taskDefName, () => 'late-binding-works'))

    const wf = Workflow.newWorkflow(uniqueName('rt-late-wf'), thread => {
      const out = thread.declareStr('out')
      out.assign(thread.execute(taskDefName))
    })
    const name = await register(wf)

    const wfRun = await client.runWf({ wfSpecName: name, variables: {} })
    const finished = await waitForWfRun(client, wfRun.id!, 60000)

    expect(LHStatus[finished.status]).toBe('COMPLETED')
    expect(unwrap(await getVariable(client, wfRun.id!, 'out'))).toBe('late-binding-works')
  }, 90000)

  test('reported task status reaches the server as the engine sees it', async () => {
    await track(startWorker('rt-status', () => 'ok'))

    const wf = Workflow.newWorkflow(uniqueName('rt-status-wf'), thread => {
      thread.execute('rt-status')
    })
    const name = await register(wf)
    const wfRun = await client.runWf({ wfSpecName: name, variables: {} })
    await waitForWfRun(client, wfRun.id!, 60000)

    // Inspect the server's own record of the TaskRun rather than trusting the
    // WfRun status alone.
    const nodeRuns = await client.listNodeRuns({ wfRunId: wfRun.id })
    const taskNode = nodeRuns.results.find(n => n.nodeType?.oneofKind === 'task')
    expect(taskNode).toBeDefined()
    if (taskNode?.nodeType.oneofKind !== 'task') throw new Error('expected a task node run')
    expect(LHStatus[taskNode.status]).toBe('COMPLETED')

    // The NodeRun only references the TaskRun; fetch it for the real status
    // and attempt record the server kept.
    const taskRun = await client.getTaskRun(taskNode.nodeType.task.taskRunId!)
    expect(TaskStatus[taskRun.status]).toBe('TASK_SUCCESS')
    expect(taskRun.attempts).toHaveLength(1)
    // The attempt carries the worker's own id and the value it returned.
    expect(taskRun.attempts[0].result).toEqual({
      oneofKind: 'output',
      output: { value: { oneofKind: 'str', str: 'ok' } },
    })
    expect(taskRun.attempts[0].taskWorkerId.length).toBeGreaterThan(0)
  }, 90000)

  // Needs a second server process to rebalance between; the fake-server suite
  // covers the client's reaction to a changed host list, and the container
  // harness runs a single standalone node.
  test.todo('a worker rebalances across a multi-node cluster')
  // Requires an lh-standalone configured for TLS and an OAuth issuer; the
  // credentials themselves are covered by the config matrix.
  test.todo('a worker connects over TLS to a real server')
  test.todo('a worker authenticates with OAuth against a real issuer')
})
