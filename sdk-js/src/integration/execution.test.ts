import { z } from 'zod'
import type { LHPublicClient } from '../client'
import { Comparator } from '../proto/type_definition'
import { LHStatus, VariableType } from '../proto/common_enums'
import { VariableMutationType } from '../proto/common_wfspec'
import { Workflow } from '../wfsdk'
import { LHTaskException, WorkerContext } from '../worker'
import {
  RunningWorker,
  awaitWfSpecReady,
  getVariable,
  isTerminal,
  registerRequiredTaskDefs,
  requireServer,
  startWorker,
  tenantClient,
  tenantConfig,
  uniqueName,
  unwrap,
  waitForWfRun,
} from './harness'

/**
 * End-to-end execution against a real LittleHorse server.
 *
 * The unit tests prove the worker handles a ScheduledTask *we fabricated*.
 * These prove the server actually schedules the task, our worker executes it,
 * the result travels back, and the engine advances the workflow accordingly.
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

/** Registers a workflow (and its TaskDefs) and returns its name. */
async function register(
  workflow: Workflow,
  taskInputVars: Record<string, Record<string, z.ZodTypeAny>> = {}
): Promise<string> {
  await registerRequiredTaskDefs(client, workflow, taskInputVars)
  await workflow.registerWfSpec(await tenantConfig())
  // Registration is async server-side; runWf can outrun it on a cold server.
  await awaitWfSpecReady(client, workflow.getName())
  return workflow.getName()
}

async function track(worker: Promise<RunningWorker>): Promise<RunningWorker> {
  const running = await worker
  workers.push(running)
  return running
}

describe('real execution', () => {
  test('a task worker executes a real scheduled task and its output lands in a variable', async () => {
    const name = uniqueName('e2e-greet')
    const wf = Workflow.newWorkflow(name, thread => {
      const who = thread.declareStr('who').required()
      const greeting = thread.declareStr('greeting')
      greeting.assign(thread.execute('e2e-greet-task', who))
    })
    await register(wf, { 'e2e-greet-task': { who: z.string() } })
    await track(startWorker('e2e-greet-task', (who: string) => `hello, ${who}`, { who: z.string() }))

    const wfRun = await client.runWf({
      wfSpecName: name,
      variables: { who: { value: { oneofKind: 'str', str: 'world' } } },
    })
    const finished = await waitForWfRun(client, wfRun.id!)

    expect(LHStatus[finished.status]).toBe('COMPLETED')
    expect(unwrap(await getVariable(client, wfRun.id!, 'greeting'))).toBe('hello, world')
  })

  test('the engine takes the true branch of a conditional based on a real variable', async () => {
    const name = uniqueName('e2e-cond')
    const wf = Workflow.newWorkflow(name, thread => {
      const amount = thread.declareInt('amount').required()
      const chosen = thread.declareStr('chosen')
      thread.doIfElse(
        thread.condition(amount, Comparator.GREATER_THAN, 100),
        body => chosen.assign(body.execute('e2e-large')),
        body => chosen.assign(body.execute('e2e-small'))
      )
    })
    await register(wf)
    await track(startWorker('e2e-large', () => 'large'))
    await track(startWorker('e2e-small', () => 'small'))

    const big = await client.runWf({
      wfSpecName: name,
      variables: { amount: { value: { oneofKind: 'int', int: '500' } } },
    })
    const small = await client.runWf({
      wfSpecName: name,
      variables: { amount: { value: { oneofKind: 'int', int: '5' } } },
    })

    expect(LHStatus[(await waitForWfRun(client, big.id!)).status]).toBe('COMPLETED')
    expect(LHStatus[(await waitForWfRun(client, small.id!)).status]).toBe('COMPLETED')
    expect(unwrap(await getVariable(client, big.id!, 'chosen'))).toBe('large')
    expect(unwrap(await getVariable(client, small.id!, 'chosen'))).toBe('small')
  })

  test('a doWhile loop runs the body the expected number of times', async () => {
    const name = uniqueName('e2e-loop')
    const wf = Workflow.newWorkflow(name, thread => {
      const remaining = thread.declareInt('remaining').required()
      const runs = thread.declareInt('runs').withDefault(0)
      thread.doWhile(thread.condition(remaining, Comparator.GREATER_THAN, 0), body => {
        body.execute('e2e-tick')
        body.mutate(runs, VariableMutationType.ADD, 1)
        body.mutate(remaining, VariableMutationType.SUBTRACT, 1)
      })
    })
    await register(wf)
    await track(startWorker('e2e-tick', () => null))

    const wfRun = await client.runWf({
      wfSpecName: name,
      variables: { remaining: { value: { oneofKind: 'int', int: '3' } } },
    })
    const finished = await waitForWfRun(client, wfRun.id!)

    expect(LHStatus[finished.status]).toBe('COMPLETED')
    // The engine evaluated the loop condition and the mutations, not us.
    expect(unwrap(await getVariable(client, wfRun.id!, 'runs'))).toBe(3)
    expect(unwrap(await getVariable(client, wfRun.id!, 'remaining'))).toBe(0)
  })

  test('an LHTaskException from a task triggers the matching failure handler', async () => {
    const name = uniqueName('e2e-exn')
    const wf = Workflow.newWorkflow(name, thread => {
      const handled = thread.declareStr('handled').withDefault('no')
      const risky = thread.execute('e2e-risky')
      thread.handleException(risky, 'out-of-stock', handler => {
        handled.assign(handler.execute('e2e-compensate'))
      })
    })
    await register(wf)
    await track(
      startWorker('e2e-risky', () => {
        throw new LHTaskException('out-of-stock', 'no widgets left')
      })
    )
    await track(startWorker('e2e-compensate', () => 'yes'))

    const wfRun = await client.runWf({ wfSpecName: name, variables: {} })
    const finished = await waitForWfRun(client, wfRun.id!)

    // The workflow recovers: the handler ran, so the run completes.
    expect(LHStatus[finished.status]).toBe('COMPLETED')
    expect(unwrap(await getVariable(client, wfRun.id!, 'handled'))).toBe('yes')
  })

  test('an unhandled technical error fails the WfRun', async () => {
    const name = uniqueName('e2e-fail')
    const wf = Workflow.newWorkflow(name, thread => {
      thread.execute('e2e-broken')
    })
    await register(wf)
    await track(
      startWorker('e2e-broken', () => {
        throw new Error('database is down')
      })
    )

    const wfRun = await client.runWf({ wfSpecName: name, variables: {} })
    const finished = await waitForWfRun(client, wfRun.id!)

    // Distinguishes a technical ERROR from a business EXCEPTION end to end.
    expect(LHStatus[finished.status]).toBe('ERROR')
  })

  test('an external event unblocks a waiting workflow', async () => {
    const name = uniqueName('e2e-evt')
    const eventName = uniqueName('e2e-approval')
    await client.putExternalEventDef({ name: eventName, contentType: {} })

    const wf = Workflow.newWorkflow(name, thread => {
      const approval = thread.declareStr('approval')
      approval.assign(thread.waitForEvent(eventName))
    })
    await register(wf)

    const wfRun = await client.runWf({ wfSpecName: name, variables: {} })

    // The run must still be in flight: nothing has satisfied the wait yet.
    // Asserting "not terminal" rather than exactly RUNNING, because a run
    // that was only just created is briefly STARTING.
    const midFlight = await client.getWfRun(wfRun.id!)
    expect(isTerminal(midFlight.status)).toBe(false)

    await client.putExternalEvent({
      wfRunId: wfRun.id,
      externalEventDefId: { name: eventName },
      content: { value: { oneofKind: 'str', str: 'approved' } },
    })

    const finished = await waitForWfRun(client, wfRun.id!)
    expect(LHStatus[finished.status]).toBe('COMPLETED')
    expect(unwrap(await getVariable(client, wfRun.id!, 'approval'))).toBe('approved')
  })

  test('typed input variables survive a round trip through the engine and worker', async () => {
    const name = uniqueName('e2e-types')
    const wf = Workflow.newWorkflow(name, thread => {
      const count = thread.declareInt('count').required()
      const ratio = thread.declareDouble('ratio').required()
      const flag = thread.declareBool('flag').required()
      const payload = thread.declareJsonObj('payload').required()
      const echoed = thread.declareJsonObj('echoed')
      echoed.assign(thread.execute('e2e-echo', count, ratio, flag, payload))
    })
    await register(wf, {
      'e2e-echo': {
        count: z.number().int(),
        ratio: z.number(),
        flag: z.boolean(),
        payload: z.object({}),
      },
    })
    await track(
      startWorker(
        'e2e-echo',
        (count: number, ratio: number, flag: boolean, payload: Record<string, unknown>, ctx: WorkerContext) => {
          ctx.log(`count=${count} ratio=${ratio} flag=${flag}`)
          return { count, ratio, flag, nested: payload }
        },
        { count: z.number().int(), ratio: z.number(), flag: z.boolean(), payload: z.object({}) }
      )
    )

    const wfRun = await client.runWf({
      wfSpecName: name,
      variables: {
        count: { value: { oneofKind: 'int', int: '7' } },
        ratio: { value: { oneofKind: 'double', double: 2.5 } },
        flag: { value: { oneofKind: 'bool', bool: true } },
        payload: { value: { oneofKind: 'jsonObj', jsonObj: '{"a":[1,2]}' } },
      },
    })
    const finished = await waitForWfRun(client, wfRun.id!)

    expect(LHStatus[finished.status]).toBe('COMPLETED')
    // Every type survived: JS → proto → engine → worker → proto → engine.
    expect(unwrap(await getVariable(client, wfRun.id!, 'echoed'))).toEqual({
      count: 7,
      ratio: 2.5,
      flag: true,
      nested: { a: [1, 2] },
    })
  })

  test('a native typed array variable round-trips through the engine', async () => {
    const name = uniqueName('e2e-array')
    const wf = Workflow.newWorkflow(name, thread => {
      const names = thread.declareArray('names', VariableType.STR).required()
      const size = thread.declareInt('size')
      size.assign(names.size())
    })
    await register(wf)

    const wfRun = await client.runWf({
      wfSpecName: name,
      variables: {
        names: {
          value: {
            oneofKind: 'array',
            array: {
              items: [
                { value: { oneofKind: 'str', str: 'a' } },
                { value: { oneofKind: 'str', str: 'b' } },
                { value: { oneofKind: 'str', str: 'c' } },
              ],
            },
          },
        },
      },
    })
    const finished = await waitForWfRun(client, wfRun.id!)

    expect(LHStatus[finished.status]).toBe('COMPLETED')
    // Proves declareArray produces a type the engine understands, and that
    // size() evaluates server-side over a native LH array.
    expect(unwrap(await getVariable(client, wfRun.id!, 'size'))).toBe(3)
  })

  test('worker health reflects a real server connection', async () => {
    const running = await track(startWorker('e2e-health', () => null))
    await registerRequiredTaskDefs(
      client,
      Workflow.newWorkflow(uniqueName('e2e-health-wf'), t => t.execute('e2e-health'))
    )

    const health = running.worker.healthStatus()
    expect(health.healthy).toBe(true)
    expect(health.connectedHosts).toBeGreaterThanOrEqual(1)
  })
})
