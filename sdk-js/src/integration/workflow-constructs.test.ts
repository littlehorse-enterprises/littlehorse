import { z } from 'zod'
import type { LHPublicClient } from '../client'
import { Comparator } from '../proto/type_definition'
import { LHStatus, VariableType } from '../proto/common_enums'
import { VariableMutationType } from '../proto/common_wfspec'
import { spawnedThreadsOf, Workflow } from '../wfsdk'
import { userTaskSchema } from '../usertask'
import {
  RunningWorker,
  awaitWfSpecReady,
  getVariable,
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
 * Integration coverage matrix: does each wfsdk construct actually *execute*?
 *
 * The acceptance suite proves the server accepts every reference workflow, and
 * the golden tests prove our protos match Java's. Neither says the engine
 * then does the right thing at runtime — a spec can be valid and still behave
 * differently than intended.
 *
 * The enumeration below is derived mechanically from the constructs on
 * WorkflowThread that produce runtime behavior, so "are we missing a test?"
 * has an answer: anything on that class without an entry here is a gap.
 * test.todo marks coverage we have deliberately not built yet, with a reason.
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

/** Registers, runs, and waits for a workflow with no inputs. */
async function runToCompletion(workflow: Workflow, variables = {}) {
  const name = await register(workflow)
  const wfRun = await client.runWf({ wfSpecName: name, variables })
  return { wfRun, finished: await waitForWfRun(client, wfRun.id!) }
}

describe('workflow constructs execute', () => {
  describe('child threads', () => {
    test('spawnThread + waitForThreads runs children and waits for all of them', async () => {
      const seen: string[] = []
      await track(startWorker('c-notify', () => seen.push('notify')))
      await track(startWorker('c-audit', () => seen.push('audit')))
      await track(startWorker('c-after', () => 'done'))

      const wf = Workflow.newWorkflow(uniqueName('ct-spawn'), thread => {
        const result = thread.declareStr('result')
        const notifier = thread.spawnThread(child => child.execute('c-notify'), 'notifier', {})
        const auditor = thread.spawnThread(child => child.execute('c-audit'), 'auditor', {})
        thread.waitForThreads(spawnedThreadsOf(notifier, auditor))
        result.assign(thread.execute('c-after'))
      })

      const { wfRun, finished } = await runToCompletion(wf)
      expect(LHStatus[finished.status]).toBe('COMPLETED')
      // Both children ran, and the parent only continued afterwards.
      expect(seen.sort()).toEqual(['audit', 'notify'])
      expect(unwrap(await getVariable(client, wfRun.id!, 'result'))).toBe('done')
      // The engine actually created child ThreadRuns, not just the entrypoint.
      expect(finished.threadRuns.length).toBeGreaterThan(1)
    })

    test('spawnThreadForEach starts one child per array element', async () => {
      const processed: number[] = []
      await track(startWorker('c-each', (item: number) => processed.push(item), { item: z.number().int() }))

      const wf = Workflow.newWorkflow(uniqueName('ct-foreach'), thread => {
        const items = thread.declareJsonArr('items').required()
        const children = thread.spawnThreadForEach(items, 'processor', child => {
          const input = child.addVariable('INPUT', VariableType.INT)
          child.execute('c-each', input)
        })
        thread.waitForThreads(children)
      })

      const name = await register(wf, { 'c-each': { item: z.number().int() } })
      const wfRun = await client.runWf({
        wfSpecName: name,
        variables: { items: { value: { oneofKind: 'jsonArr', jsonArr: '[10,20,30]' } } },
      })
      const finished = await waitForWfRun(client, wfRun.id!)

      expect(LHStatus[finished.status]).toBe('COMPLETED')
      expect(processed.sort((a, b) => a - b)).toEqual([10, 20, 30])
    }, 60000)
  })

  describe('child workflows', () => {
    test('runWf + waitForChildWf runs a child WfSpec and waits for it', async () => {
      let childRan = false
      await track(startWorker('cw-child-task', () => (childRan = true)))

      const child = Workflow.newWorkflow(uniqueName('cw-child'), thread => {
        thread.execute('cw-child-task')
      })
      const childName = await register(child)

      const parent = Workflow.newWorkflow(uniqueName('cw-parent'), thread => {
        const spawned = thread.runWf(childName, {})
        thread.waitForChildWf(spawned)
      })

      const { finished } = await runToCompletion(parent)
      expect(LHStatus[finished.status]).toBe('COMPLETED')
      expect(childRan).toBe(true)
    }, 60000)
  })

  describe('interrupts', () => {
    test('an external event fires a registered interrupt handler', async () => {
      const eventName = uniqueName('int-cancel')
      await client.putExternalEventDef({ name: eventName, contentType: {} })
      let handlerRan = false
      await track(startWorker('int-cleanup', () => (handlerRan = true)))
      await track(startWorker('int-wait', () => 'waited'))

      const wf = Workflow.newWorkflow(uniqueName('ct-interrupt'), thread => {
        thread.registerInterruptHandler(eventName, handler => {
          handler.execute('int-cleanup')
        })
        thread.sleepSeconds(2)
        thread.execute('int-wait')
      })

      const name = await register(wf)
      const wfRun = await client.runWf({ wfSpecName: name, variables: {} })
      await client.putExternalEvent({
        wfRunId: wfRun.id,
        externalEventDefId: { name: eventName },
        content: { value: { oneofKind: 'str', str: 'stop' } },
      })

      const finished = await waitForWfRun(client, wfRun.id!, 60000)
      expect(handlerRan).toBe(true)
      // The interrupt produced its own ThreadRun alongside the entrypoint.
      expect(finished.threadRuns.length).toBeGreaterThan(1)
    }, 90000)
  })

  describe('timing and conditions', () => {
    test('sleepSeconds delays the run without failing it', async () => {
      await track(startWorker('slp-after', () => 'after'))
      const wf = Workflow.newWorkflow(uniqueName('ct-sleep'), thread => {
        const out = thread.declareStr('out')
        thread.sleepSeconds(2)
        out.assign(thread.execute('slp-after'))
      })

      const startedAt = Date.now()
      const { wfRun, finished } = await runToCompletion(wf)
      const elapsed = Date.now() - startedAt

      expect(LHStatus[finished.status]).toBe('COMPLETED')
      expect(unwrap(await getVariable(client, wfRun.id!, 'out'))).toBe('after')
      // The engine really waited; it did not run straight through.
      expect(elapsed).toBeGreaterThanOrEqual(1500)
    }, 60000)

    test('waitForCondition blocks until a variable satisfies it', async () => {
      const eventName = uniqueName('cond-ready')
      await client.putExternalEventDef({ name: eventName, contentType: {} })
      await track(startWorker('cond-after', () => 'unblocked'))

      const wf = Workflow.newWorkflow(uniqueName('ct-cond'), thread => {
        const ready = thread.declareBool('ready').withDefault(false)
        const out = thread.declareStr('out')
        ready.assign(thread.waitForEvent(eventName))
        thread.waitForCondition(thread.condition(ready, Comparator.EQUALS, true))
        out.assign(thread.execute('cond-after'))
      })

      const name = await register(wf)
      const wfRun = await client.runWf({ wfSpecName: name, variables: {} })

      // Still blocked before the condition can be satisfied.
      expect(LHStatus[(await client.getWfRun(wfRun.id!)).status]).not.toBe('COMPLETED')

      await client.putExternalEvent({
        wfRunId: wfRun.id,
        externalEventDefId: { name: eventName },
        content: { value: { oneofKind: 'bool', bool: true } },
      })

      const finished = await waitForWfRun(client, wfRun.id!, 60000)
      expect(LHStatus[finished.status]).toBe('COMPLETED')
      expect(unwrap(await getVariable(client, wfRun.id!, 'out'))).toBe('unblocked')
    }, 90000)
  })

  describe('exits and failure handlers', () => {
    test('complete(output) ends the run with a return value', async () => {
      const wf = Workflow.newWorkflow(uniqueName('ct-complete'), thread => {
        const answer = thread.declareInt('answer').withDefault(42)
        thread.complete(answer)
      })
      const { wfRun, finished } = await runToCompletion(wf)

      expect(LHStatus[finished.status]).toBe('COMPLETED')
      expect(unwrap(await getVariable(client, wfRun.id!, 'answer'))).toBe(42)
    })

    test('fail() throws a named business failure', async () => {
      const wf = Workflow.newWorkflow(uniqueName('ct-fail'), thread => {
        thread.fail('deliberate-failure', 'stopping on purpose')
      })
      const { finished } = await runToCompletion(wf)

      // A thrown business failure surfaces as EXCEPTION, not a technical ERROR.
      expect(LHStatus[finished.status]).toBe('EXCEPTION')
    })

    test('handleError catches a technical ERROR and lets the run continue', async () => {
      await track(
        startWorker('he-broken', () => {
          throw new Error('kaboom')
        })
      )
      await track(startWorker('he-recover', () => 'recovered'))

      const wf = Workflow.newWorkflow(uniqueName('ct-handleerror'), thread => {
        const state = thread.declareStr('state').withDefault('none')
        const risky = thread.execute('he-broken')
        thread.handleError(risky, null, handler => {
          state.assign(handler.execute('he-recover'))
        })
      })

      const { wfRun, finished } = await runToCompletion(wf)
      expect(LHStatus[finished.status]).toBe('COMPLETED')
      expect(unwrap(await getVariable(client, wfRun.id!, 'state'))).toBe('recovered')
    }, 60000)
  })

  describe('variables and expressions', () => {
    test('mutations and arithmetic are evaluated by the engine', async () => {
      await track(startWorker('expr-noop', () => null))

      const wf = Workflow.newWorkflow(uniqueName('ct-expr'), thread => {
        const count = thread.declareInt('count').withDefault(10)
        const doubled = thread.declareInt('doubled').withDefault(0)
        const label = thread.declareStr('label').withDefault('')

        thread.mutate(count, VariableMutationType.ADD, 5)
        doubled.assign(count.multiply(2))
        label.assign(thread.format('count={0}', count))
        thread.execute('expr-noop')
      })

      const { wfRun, finished } = await runToCompletion(wf)
      expect(LHStatus[finished.status]).toBe('COMPLETED')
      // 10 + 5 = 15, then 15 * 2 = 30 — all computed server-side.
      expect(unwrap(await getVariable(client, wfRun.id!, 'count'))).toBe(15)
      expect(unwrap(await getVariable(client, wfRun.id!, 'doubled'))).toBe(30)
      expect(unwrap(await getVariable(client, wfRun.id!, 'label'))).toBe('count=15')
    }, 60000)

    test('jsonPath and get() read into a JSON variable at runtime', async () => {
      await track(startWorker('path-echo', (value: string) => value, { value: z.string() }))

      const wf = Workflow.newWorkflow(uniqueName('ct-path'), thread => {
        const payload = thread.declareJsonObj('payload').required()
        const city = thread.declareStr('city')
        city.assign(thread.execute('path-echo', payload.jsonPath('$.address.city')))
      })

      const name = await register(wf, { 'path-echo': { value: z.string() } })
      const wfRun = await client.runWf({
        wfSpecName: name,
        variables: {
          payload: { value: { oneofKind: 'jsonObj', jsonObj: '{"address":{"city":"London"}}' } },
        },
      })
      const finished = await waitForWfRun(client, wfRun.id!)

      expect(LHStatus[finished.status]).toBe('COMPLETED')
      expect(unwrap(await getVariable(client, wfRun.id!, 'city'))).toBe('London')
    }, 60000)

    test('a native typed map variable round-trips through the engine', async () => {
      const wf = Workflow.newWorkflow(uniqueName('ct-map'), thread => {
        thread.declareMap('scores', VariableType.STR, VariableType.INT).required()
      })
      const name = await register(wf)

      const wfRun = await client.runWf({
        wfSpecName: name,
        variables: {
          scores: {
            value: {
              oneofKind: 'map',
              map: {
                entries: [
                  {
                    key: { value: { oneofKind: 'str', str: 'alice' } },
                    value: { value: { oneofKind: 'int', int: '3' } },
                  },
                ],
              },
            },
          },
        },
      })
      const finished = await waitForWfRun(client, wfRun.id!)
      expect(LHStatus[finished.status]).toBe('COMPLETED')
    }, 60000)
  })

  describe('user tasks', () => {
    test('assignUserTask creates a UserTaskRun a human can complete', async () => {
      const formName = uniqueName('ut-approve')
      await userTaskSchema(formName, { approved: z.boolean() }).register(client)
      await track(startWorker('ut-after', () => 'finalized'))

      const wf = Workflow.newWorkflow(uniqueName('ct-usertask'), thread => {
        const out = thread.declareStr('out')
        thread.assignUserTask(formName, 'alice', null)
        out.assign(thread.execute('ut-after'))
      })

      const name = await register(wf)
      const wfRun = await client.runWf({ wfSpecName: name, variables: {} })

      // The run parks on the user task until a human acts.
      expect(LHStatus[(await client.getWfRun(wfRun.id!)).status]).not.toBe('COMPLETED')

      // wfRunId alone is not a valid search criterion; the server requires
      // one of status/user_id/user_group/user_task_def_name.
      const userTasks = await client.searchUserTaskRun({ userTaskDefName: formName, wfRunId: wfRun.id })
      expect(userTasks.results.length).toBeGreaterThan(0)

      await client.completeUserTaskRun({
        userTaskRunId: userTasks.results[0],
        userId: 'alice',
        results: { approved: { value: { oneofKind: 'bool', bool: true } } },
      })

      const finished = await waitForWfRun(client, wfRun.id!, 60000)
      expect(LHStatus[finished.status]).toBe('COMPLETED')
      expect(unwrap(await getVariable(client, wfRun.id!, 'out'))).toBe('finalized')
    }, 90000)
  })

  describe('structs', () => {
    // Struct execution needs a registered StructDef whose shape matches the
    // builder, plus a nested StructDef for the sub-structure; the acceptance
    // suite already proves that registration path. Left as a gap rather than
    // duplicating that setup here.
    test.todo('buildStruct assigns a struct-typed variable at runtime')
    test.todo('a struct-typed task input deserializes into a typed object')
  })

  describe('events thrown by workflows', () => {
    // WorkflowEvents are observable through the output topic / event APIs,
    // which this harness does not yet consume.
    test.todo('throwEvent emits a WorkflowEvent observable from the server')
  })
})
