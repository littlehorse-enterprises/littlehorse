import { afterEach, describe, expect, test } from '@jest/globals'
import { z } from 'zod'
import { LHConfig } from '../LHConfig'
import { LHErrorType, TaskStatus, VariableType } from '../proto/common_enums'
import { ReportTaskRun, ScheduledTask } from '../proto/service'
import { VarNameAndVal } from '../proto/task_run'
import { Timestamp } from '../proto/google/protobuf/timestamp'
import { VariableValue } from '../proto/type_definition'
import {
  createTaskWorker,
  LHTaskException,
  LHTaskWorkerHealthReason,
  lhStruct,
  TaskFunction,
  TaskSchemaMismatchError,
  WorkerContext,
} from '../worker'
import { buildPutStructDefRequest } from '../worker'
import { TypeDefinition } from '../proto/type_definition'
import { extractVariableValue, toVariableValue } from '../worker/variableMapping'
import { FakeLHServer, waitFor } from './fakeServer'

/**
 * Feature matrix: task worker.
 *
 * See sdk-js/PARITY_PLAN.md. Each entry is one capability of the Java SDK's
 * public API (referenced as `Java: Class#method`). test.todo = not yet
 * implemented/proven. Do not delete entries; do not mark features done
 * anywhere else.
 *
 * Protocol behaviors are proven against `fakeServer.ts`, an in-process gRPC
 * server speaking the real wire protocol. That proves what the *client* does;
 * it is not a substitute for integration tests against `lh-standalone`
 * (plan tier 2), which are still todo.
 */

let servers: FakeLHServer[] = []
let workers: Array<{ close(): Promise<void> }> = []

afterEach(async () => {
  await Promise.all(workers.map(w => w.close().catch(() => {})))
  await Promise.all(servers.map(s => s.stop().catch(() => {})))
  workers = []
  servers = []
})

async function startServer(options?: ConstructorParameters<typeof FakeLHServer>[0]): Promise<FakeLHServer> {
  const server = new FakeLHServer(options)
  await server.start()
  servers.push(server)
  return server
}

function configFor(server: FakeLHServer): LHConfig {
  return LHConfig.fromMap({ LHC_API_HOST: '127.0.0.1', LHC_API_PORT: String(server.port) })
}

function scheduledTask(overrides: Partial<ScheduledTask> = {}): ScheduledTask {
  return ScheduledTask.create({
    taskRunId: { wfRunId: { id: 'wf-1' }, taskGuid: 'task-1' },
    taskDefId: { name: 'my-task' },
    attemptNumber: 0,
    ...overrides,
  })
}

function primitiveTypeDef(type: VariableType): TypeDefinition {
  return TypeDefinition.create({ definedType: { oneofKind: 'primitiveType', primitiveType: type } })
}

function varVal(value: VariableValue['value']): VarNameAndVal {
  return { varName: 'arg', value: { value }, masked: false }
}

/** Runs one task through a real worker and returns what the server received. */
async function runTask(
  taskFunction: TaskFunction,
  task: ScheduledTask,
  options: { inputVars?: Record<string, z.ZodTypeAny>; outputSchema?: z.ZodTypeAny } = {}
): Promise<ReportTaskRun> {
  const server = await startServer()
  const worker = createTaskWorker(taskFunction, 'my-task', configFor(server), {
    inputVars: options.inputVars ?? {},
    outputSchema: options.outputSchema,
  })
  workers.push(worker)
  server.enqueueTask(task)
  await worker.start()
  await waitFor(() => server.reportedTasks.length > 0, 10000, 'a reported task')
  return server.reportedTasks[0]
}

describe('worker', () => {
  describe('task registration', () => {
    test('register a TaskDef derived from the task function signature — Java: LHTaskWorker#registerTaskDef', async () => {
      const server = await startServer()
      const worker = createTaskWorker(() => null, 'my-task', configFor(server), {
        inputVars: { name: z.string(), age: z.number().int() },
      })
      await worker.registerTaskDef()

      expect(server.putTaskDefRequests).toHaveLength(1)
      const request = server.putTaskDefRequests[0]
      expect(request.name).toBe('my-task')
      expect(request.inputVars.map(v => v.name)).toEqual(['name', 'age'])
      expect(request.inputVars[0].typeDef?.definedType).toEqual({
        oneofKind: 'primitiveType',
        primitiveType: VariableType.STR,
      })
      expect(request.inputVars[1].typeDef?.definedType).toEqual({
        oneofKind: 'primitiveType',
        primitiveType: VariableType.INT,
      })
    })

    test('check whether the TaskDef exists on the server — Java: LHTaskWorker#doesTaskDefExist', async () => {
      const present = await startServer()
      const existing = createTaskWorker(() => null, 'my-task', configFor(present), { inputVars: {} })
      await expect(existing.doesTaskDefExist()).resolves.toBe(true)

      const absent = await startServer({ taskDefMissing: true })
      const missing = createTaskWorker(() => null, 'my-task', configFor(absent), { inputVars: {} })
      await expect(missing.doesTaskDefExist()).resolves.toBe(false)
    })

    test('registering an existing TaskDef is not an error — Java: LHTaskWorker#registerTaskDef', async () => {
      const server = await startServer({ taskDefAlreadyExists: true })
      const worker = createTaskWorker(() => null, 'my-task', configFor(server), { inputVars: {} })
      await expect(worker.registerTaskDef()).resolves.toBeUndefined()
    })

    test('register StructDefs used by the task (with compatibility type) — Java: LHTaskWorker#registerStructDef(s)', async () => {
      const server = await startServer()
      const worker = createTaskWorker(() => null, 'my-task', configFor(server), { inputVars: {} })
      const schema = lhStruct('person', z.object({ name: z.string() }))

      await worker.registerStructDef(buildPutStructDefRequest(schema))

      expect(server.putStructDefRequests.map(r => r.name)).toEqual(['person'])
    })

    test('validate the task function signature against the server TaskDef on start — Java: LHTaskWorker (start-time validation)', async () => {
      const strVar = { name: 'name', typeDef: primitiveTypeDef(VariableType.STR) }

      // Matching signature: accepted.
      const matching = await startServer({ taskDefInputVars: [strVar] })
      const good = createTaskWorker(() => null, 'my-task', configFor(matching), { inputVars: { name: z.string() } })
      await expect(good.validateTaskDef()).resolves.toBeUndefined()

      // Wrong arity, wrong name, and wrong type each fail with a clear reason
      // instead of surfacing on the first scheduled task.
      const wrongArity = createTaskWorker(() => null, 'my-task', configFor(matching), { inputVars: {} })
      await expect(wrongArity.validateTaskDef()).rejects.toThrow(/declares 0/)

      const wrongName = createTaskWorker(() => null, 'my-task', configFor(matching), {
        inputVars: { nombre: z.string() },
      })
      await expect(wrongName.validateTaskDef()).rejects.toThrow(/named 'name'/)

      const wrongType = createTaskWorker(() => null, 'my-task', configFor(matching), {
        inputVars: { name: z.number().int() },
      })
      await expect(wrongType.validateTaskDef()).rejects.toThrow(/different type/)

      // A TaskDef that does not exist is a mismatch too, not a crash.
      const absent = await startServer({ taskDefMissing: true })
      const orphan = createTaskWorker(() => null, 'my-task', configFor(absent), { inputVars: {} })
      await expect(orphan.validateTaskDef()).rejects.toThrow(TaskSchemaMismatchError)
    })

    test('validate StructDefs against the server without registering — Java: LHTaskWorker#validateStructDef(s)', async () => {
      const schema = lhStruct('person', z.object({ name: z.string() }))

      const ok = await startServer({ structDefEvolutionValid: true })
      const worker = createTaskWorker(() => null, 'my-task', configFor(ok), { inputVars: {} })
      await expect(worker.validateStructDefs([schema])).resolves.toBeUndefined()

      // Validation must not register anything — that is the whole point.
      expect(ok.putStructDefRequests).toHaveLength(0)
      expect(ok.validateStructDefRequests.map(r => r.structDefId?.name)).toEqual(['person'])

      const rejects = await startServer({ structDefEvolutionValid: false })
      const failing = createTaskWorker(() => null, 'my-task', configFor(rejects), { inputVars: {} })
      await expect(failing.validateStructDefs([schema])).rejects.toThrow(/not a valid evolution/)
    })
  })

  describe('task execution', () => {
    test('deserialize each input variable type into native values (INT, STR, DOUBLE, BOOL, BYTES, TIMESTAMP, JSON_OBJ, JSON_ARR) — Java: worker input mapping', () => {
      const when = new Date('2026-07-28T12:00:00.000Z')
      expect(extractVariableValue({ value: { oneofKind: 'str', str: 'hi' } })).toBe('hi')
      expect(extractVariableValue({ value: { oneofKind: 'int', int: '42' } })).toBe(42)
      expect(extractVariableValue({ value: { oneofKind: 'double', double: 1.5 } })).toBe(1.5)
      expect(extractVariableValue({ value: { oneofKind: 'bool', bool: true } })).toBe(true)
      expect(extractVariableValue({ value: { oneofKind: 'bytes', bytes: new Uint8Array([1, 2]) } })).toEqual(
        new Uint8Array([1, 2])
      )
      expect(
        extractVariableValue({ value: { oneofKind: 'utcTimestamp', utcTimestamp: Timestamp.fromDate(when) } })
      ).toEqual(when)
      expect(extractVariableValue({ value: { oneofKind: 'jsonObj', jsonObj: '{"a":1}' } })).toEqual({ a: 1 })
      expect(extractVariableValue({ value: { oneofKind: 'jsonArr', jsonArr: '[1,2]' } })).toEqual([1, 2])
    })

    test('deserialize struct inputs into typed objects — Java: worker struct mapping', async () => {
      let received: unknown
      const report = await runTask(
        (person: unknown) => {
          received = person
          return null
        },
        scheduledTask({
          variables: [
            varVal({
              oneofKind: 'struct',
              struct: {
                structDefId: { name: 'person', version: 0 },
                struct: {
                  fields: {
                    name: { value: { value: { oneofKind: 'str', str: 'Ada' } }, masked: false },
                    age: { value: { value: { oneofKind: 'int', int: '36' } }, masked: false },
                  },
                },
              },
            }),
          ],
        })
      )

      expect(report.status).toBe(TaskStatus.TASK_SUCCESS)
      expect(received).toEqual({ name: 'Ada', age: 36 })
    })

    test('inject WorkerContext as a task function parameter — Java: worker WorkerContext injection', async () => {
      let context: WorkerContext | undefined
      await runTask(
        (name: string, ctx: WorkerContext) => {
          context = ctx
          return name
        },
        scheduledTask({ variables: [varVal({ oneofKind: 'str', str: 'x' })] })
      )
      expect(context).toBeInstanceOf(WorkerContext)
      expect(context!.getWfRunId()?.id).toBe('wf-1')
    })

    test('serialize the task return value into a VariableValue output — Java: worker output mapping', async () => {
      expect(toVariableValue(7).value).toEqual({ oneofKind: 'int', int: '7' })
      expect(toVariableValue(1.5).value).toEqual({ oneofKind: 'double', double: 1.5 })
      expect(toVariableValue('s').value).toEqual({ oneofKind: 'str', str: 's' })
      expect(toVariableValue(true).value).toEqual({ oneofKind: 'bool', bool: true })
      expect(toVariableValue([1]).value).toEqual({ oneofKind: 'jsonArr', jsonArr: '[1]' })
      expect(toVariableValue({ a: 1 }).value).toEqual({ oneofKind: 'jsonObj', jsonObj: '{"a":1}' })
      expect(toVariableValue(null).value).toEqual({ oneofKind: undefined })

      const report = await runTask(() => ({ ok: true }), scheduledTask())
      expect(report.result).toEqual({
        oneofKind: 'output',
        output: { value: { oneofKind: 'jsonObj', jsonObj: '{"ok":true}' } },
      })
    })

    test('report a business EXCEPTION when the task throws LHTaskException (with name and content) — Java: LHTaskException', async () => {
      const report = await runTask(() => {
        throw new LHTaskException('out-of-stock', 'no widgets left', { sku: 'W-1' })
      }, scheduledTask())

      expect(report.status).toBe(TaskStatus.TASK_EXCEPTION)
      if (report.result.oneofKind !== 'exception') throw new Error('expected an exception result')
      expect(report.result.exception.name).toBe('out-of-stock')
      expect(report.result.exception.message).toBe('no widgets left')
      expect(report.result.exception.content?.value).toEqual({ oneofKind: 'jsonObj', jsonObj: '{"sku":"W-1"}' })
    })

    test('report a technical ERROR when the task throws any other exception — Java: TaskExecutionException semantics', async () => {
      const report = await runTask(() => {
        throw new Error('database is down')
      }, scheduledTask())

      expect(report.status).toBe(TaskStatus.TASK_FAILED)
      if (report.result.oneofKind !== 'error') throw new Error('expected an error result')
      expect(report.result.error.type).toBe(LHErrorType.TASK_FAILURE)
      expect(report.result.error.message).toBe('database is down')
    })

    test('report TASK_INPUT_VAR_SUB_ERROR when inputs cannot be mapped — Java: InputVarSubstitutionException', async () => {
      // Malformed JSON from the server cannot be turned into a task argument.
      const report = await runTask(
        () => 'never runs',
        scheduledTask({ variables: [varVal({ oneofKind: 'jsonObj', jsonObj: '{not json' })] })
      )

      expect(report.status).toBe(TaskStatus.TASK_INPUT_VAR_SUB_ERROR)
      if (report.result.oneofKind !== 'error') throw new Error('expected an error result')
      expect(report.result.error.type).toBe(LHErrorType.VAR_SUB_ERROR)
    })

    test('honor the reported attempt/retry semantics so the server can schedule retries — Java: ReportTaskRun', async () => {
      const report = await runTask(() => 'ok', scheduledTask({ attemptNumber: 3 }))
      expect(report.attemptNumber).toBe(3)
      expect(report.taskRunId).toEqual({ wfRunId: { id: 'wf-1' }, taskGuid: 'task-1' })
      expect(report.time).toBeDefined()
    })
  })

  describe('WorkerContext', () => {
    function contextFor(task: Partial<ScheduledTask> = {}): WorkerContext {
      return new WorkerContext(scheduledTask(task))
    }

    test('expose wfRunId — Java: WorkerContext#getWfRunId', () => {
      expect(contextFor().getWfRunId()?.id).toBe('wf-1')
    })

    test('expose nodeRunId — Java: WorkerContext#getNodeRunId', () => {
      const context = contextFor({
        source: {
          taskRunSource: {
            oneofKind: 'taskNode',
            taskNode: { nodeRunId: { wfRunId: { id: 'wf-1' }, threadRunNumber: 0, position: 2 } },
          },
        },
      })
      expect(context.getNodeRunId()?.position).toBe(2)
      expect(contextFor().getNodeRunId()).toBeUndefined()
    })

    test('expose taskRunId — Java: WorkerContext#getTaskRunId', () => {
      expect(contextFor().getTaskRunId()?.taskGuid).toBe('task-1')
    })

    test('expose attemptNumber — Java: WorkerContext#getAttemptNumber', () => {
      expect(contextFor({ attemptNumber: 2 }).getAttemptNumber()).toBe(2)
    })

    test('expose scheduledTime — Java: WorkerContext#getScheduledTime', () => {
      const when = new Date('2026-07-28T12:00:00.000Z')
      expect(contextFor({ createdAt: Timestamp.fromDate(when) }).getScheduledTime()).toBe(when.toISOString())
      expect(contextFor().getScheduledTime()).toBeUndefined()
    })

    test('expose an idempotency key — Java: WorkerContext#getIdempotencyKey', () => {
      // Stable across attempts of the same TaskRun, so retried tasks can dedupe.
      expect(contextFor().getIdempotencyKey()).toBe('wf-1/task-1')
      expect(contextFor({ attemptNumber: 5 }).getIdempotencyKey()).toBe('wf-1/task-1')
    })

    test('accumulate log output attached to the TaskRun result — Java: WorkerContext#log/getLogOutput', async () => {
      const report = await runTask((ctx: WorkerContext) => {
        ctx.log('first')
        ctx.log('second')
        return 'ok'
      }, scheduledTask())

      expect(report.logOutput?.value).toEqual({ oneofKind: 'str', str: 'first\nsecond' })
    })

    test('expose userId / userGroup for user-task-triggered tasks — Java: WorkerContext#getUserId/getUserGroup', () => {
      const reminder = contextFor({
        source: {
          taskRunSource: {
            oneofKind: 'userTaskTrigger',
            userTaskTrigger: {
              nodeRunId: { wfRunId: { id: 'wf-1' }, threadRunNumber: 0, position: 1 },
              userTaskEventNumber: 0,
              userId: 'alice',
              userGroup: 'approvers',
            },
          },
        },
      })
      expect(reminder.getUserId()).toBe('alice')
      expect(reminder.getUserGroup()).toBe('approvers')

      // Undefined for a plain task node, not an empty string.
      expect(contextFor().getUserId()).toBeUndefined()
      expect(contextFor().getUserGroup()).toBeUndefined()
    })

    test('checkpoint a sub-operation so retries can skip completed work — Java: WorkerContext#executeAndCheckpoint', async () => {
      const server = await startServer()
      const client = configFor(server).getClient()

      // First attempt: the operation runs and its result is checkpointed.
      let firstAttemptRuns = 0
      const first = new WorkerContext(scheduledTask({ totalObservedCheckpoints: 0 }), client)
      const charged = await first.executeAndCheckpoint(() => {
        firstAttemptRuns++
        return 'charged-card-abc'
      })
      expect(charged).toBe('charged-card-abc')
      expect(firstAttemptRuns).toBe(1)
      expect(server.checkpoints).toHaveLength(1)

      // Retry: the server reports one observed checkpoint, so the side effect
      // is replayed from storage instead of happening a second time.
      let retryRuns = 0
      const retry = new WorkerContext(scheduledTask({ attemptNumber: 1, totalObservedCheckpoints: 1 }), client)
      const replayed = await retry.executeAndCheckpoint(() => {
        retryRuns++
        return 'charged-card-SECOND-TIME'
      })
      expect(replayed).toBe('charged-card-abc')
      expect(retryRuns).toBe(0)
      expect(server.checkpoints).toHaveLength(1)

      // Work past the observed count still executes normally.
      const next = await retry.executeAndCheckpoint(() => 'second-step')
      expect(next).toBe('second-step')
      expect(server.checkpoints).toHaveLength(2)
    })
  })

  describe('lifecycle and protocol', () => {
    test('start the worker and receive/execute a scheduled task end-to-end — Java: LHTaskWorker#start', async () => {
      const report = await runTask(
        (name: string) => `hello ${name}`,
        scheduledTask({ variables: [varVal({ oneofKind: 'str', str: 'world' })] })
      )
      expect(report.status).toBe(TaskStatus.TASK_SUCCESS)
      expect(report.result).toEqual({
        oneofKind: 'output',
        output: { value: { oneofKind: 'str', str: 'hello world' } },
      })
    })

    test('long-poll the server for tasks over a bidirectional stream — Java: PollThread/PollTaskStub', async () => {
      const server = await startServer()
      const worker = createTaskWorker(() => 'ok', 'my-task', configFor(server), { inputVars: {} })
      workers.push(worker)
      await worker.start()

      // The worker asks for work up front and keeps asking after each task.
      await waitFor(() => server.pollRequests.length > 0, 5000, 'an initial poll request')
      const before = server.pollRequests.length
      server.enqueueTask(scheduledTask())
      await waitFor(() => server.pollRequests.length > before, 5000, 'a follow-up poll request')

      expect(server.pollRequests[0].clientId).toBe(worker.getTaskWorkerId())
      expect(server.pollRequests[0].taskDefId?.name).toBe('my-task')
    })

    test('discover cluster topology and poll every assigned server host — Java: LHServerConnectionManager', async () => {
      const hostA = await startServer()
      const hostB = await startServer()
      // hostA is the bootstrap and tells the worker to poll both hosts.
      hostA.setOptions({
        hosts: [
          { host: '127.0.0.1', port: hostA.port },
          { host: '127.0.0.1', port: hostB.port },
        ],
      })

      const worker = createTaskWorker(() => 'ok', 'my-task', configFor(hostA), { inputVars: {} })
      workers.push(worker)
      await worker.start()

      await waitFor(() => hostA.pollRequests.length > 0 && hostB.pollRequests.length > 0, 5000, 'polls on both hosts')
      expect(worker.healthStatus().connectedHosts).toBe(2)
    })

    test('handle server rebalance: pick up new hosts, drop unassigned ones — Java: RebalanceThread', async () => {
      const hostA = await startServer()
      const hostB = await startServer()
      hostA.setOptions({ hosts: [{ host: '127.0.0.1', port: hostA.port }] })

      const worker = createTaskWorker(() => 'ok', 'my-task', configFor(hostA), {
        inputVars: {},
        heartbeatIntervalMs: 100,
      })
      workers.push(worker)
      await worker.start()
      await waitFor(() => hostA.pollRequests.length > 0, 5000, 'initial poll on host A')
      expect(worker.healthStatus().connectedHosts).toBe(1)

      // Reassign the worker to host B only; the next heartbeat should move it.
      hostA.setOptions({ hosts: [{ host: '127.0.0.1', port: hostB.port }] })

      await waitFor(() => hostB.pollRequests.length > 0, 5000, 'poll on host B after rebalance')
      expect(worker.healthStatus().connectedHosts).toBe(1)
    }, 20000)

    test('report worker health — Java: LHTaskWorker#healthStatus', async () => {
      const server = await startServer()
      const worker = createTaskWorker(() => 'ok', 'my-task', configFor(server), { inputVars: {} })
      workers.push(worker)

      expect(worker.healthStatus()).toMatchObject({ healthy: false, reason: 'NOT_RUNNING', connectedHosts: 0 })

      await worker.start()
      await waitFor(() => worker.healthStatus().healthy, 5000, 'a healthy worker')
      expect(worker.healthStatus()).toMatchObject({ healthy: true, reason: 'HEALTHY', connectedHosts: 1 })

      await worker.close()
      expect(worker.healthStatus()).toMatchObject({ healthy: false, reason: 'NOT_RUNNING' })
    })

    test('reconnect with backoff after connection loss, without dropping tasks — Java: connection manager retry behavior', async () => {
      const server = await startServer()
      const worker = createTaskWorker(() => 'ok', 'my-task', configFor(server), { inputVars: {} })
      workers.push(worker)
      await worker.start()
      await waitFor(() => server.pollRequests.length > 0, 5000, 'an initial poll')

      const pollsBeforeDrop = server.pollRequests.length
      server.dropPollStreams()

      // The worker must re-establish the stream on its own...
      await waitFor(() => server.pollRequests.length > pollsBeforeDrop, 10000, 'a poll after reconnect')

      // ...and still deliver work queued after the disruption.
      server.enqueueTask(scheduledTask())
      await waitFor(() => server.reportedTasks.length > 0, 10000, 'a task reported after reconnect')
      expect(server.reportedTasks[0].status).toBe(TaskStatus.TASK_SUCCESS)
    }, 30000)

    test('never double-report a task result after reconnect — Java: report retry semantics', async () => {
      // The first two ReportTask calls fail; the worker retries until one lands
      // and must not leave duplicates behind.
      const server = await startServer({ failReportTaskTimes: 2 })
      const worker = createTaskWorker(() => 'ok', 'my-task', configFor(server), { inputVars: {} })
      workers.push(worker)
      server.enqueueTask(scheduledTask())
      await worker.start()

      await waitFor(() => server.reportedTasks.length > 0, 20000, 'a successfully reported task')
      // Give any stray retry a chance to arrive before asserting uniqueness.
      await new Promise(resolve => setTimeout(resolve, 500))

      expect(server.reportedTasks).toHaveLength(1)
      expect(server.reportedTasks[0].taskRunId?.taskGuid).toBe('task-1')
    }, 30000)

    test('close gracefully: stop polling, finish and report in-flight tasks — Java: LHTaskWorker#close', async () => {
      const server = await startServer()
      let release: () => void = () => {}
      const started = new Promise<void>(resolve => {
        release = resolve
      })
      let finished = false

      const worker = createTaskWorker(
        async () => {
          release()
          await new Promise(resolve => setTimeout(resolve, 300))
          finished = true
          return 'done'
        },
        'my-task',
        configFor(server),
        { inputVars: {} }
      )
      workers.push(worker)
      server.enqueueTask(scheduledTask())
      await worker.start()
      await started

      // close() must wait for the running task and let its result through.
      await worker.close()
      expect(finished).toBe(true)
      expect(server.reportedTasks).toHaveLength(1)
      expect(server.reportedTasks[0].status).toBe(TaskStatus.TASK_SUCCESS)
    }, 20000)

    test('closing promptly returns even with no work in flight — Java: LHTaskWorker#close', async () => {
      const server = await startServer()
      const worker = createTaskWorker(() => 'ok', 'my-task', configFor(server), { inputVars: {} })
      await worker.start()
      await waitFor(() => server.pollRequests.length > 0, 5000, 'an initial poll')

      // Regression guard: close() used to block forever on the idle poll
      // stream, because nothing aborted the in-flight PollTask call.
      const startedAt = Date.now()
      await worker.close()
      expect(Date.now() - startedAt).toBeLessThan(5000)
    }, 20000)

    test('expose closed state — Java: LHTaskWorker#isClosed', async () => {
      const server = await startServer()
      const worker = createTaskWorker(() => 'ok', 'my-task', configFor(server), { inputVars: {} })
      expect(worker.isClosed()).toBe(true)

      await worker.start()
      expect(worker.isClosed()).toBe(false)
      expect(worker.isRunning()).toBe(true)

      await worker.close()
      expect(worker.isClosed()).toBe(true)
      expect(worker.isRunning()).toBe(false)
    }, 20000)

    test('track in-flight task count — Java: config inflightTasks/workerThreads', async () => {
      const server = await startServer()
      let release: () => void = () => {}
      const started = new Promise<void>(resolve => {
        release = resolve
      })

      const worker = createTaskWorker(
        async () => {
          release()
          await new Promise(resolve => setTimeout(resolve, 200))
          return 'ok'
        },
        'my-task',
        configFor(server),
        { inputVars: {} }
      )
      workers.push(worker)
      expect(worker.getInflightTaskCount()).toBe(0)

      server.enqueueTask(scheduledTask())
      await worker.start()
      await started
      expect(worker.getInflightTaskCount()).toBe(1)

      await waitFor(() => worker.getInflightTaskCount() === 0, 5000, 'the task to finish')
    }, 20000)

    test('limit concurrent in-flight tasks to the configured inflight/threads setting — Java: config inflightTasks/workerThreads', async () => {
      const server = await startServer()
      let concurrent = 0
      let peakConcurrent = 0
      let release: () => void = () => {}
      const gate = new Promise<void>(resolve => {
        release = resolve
      })

      const worker = createTaskWorker(
        async () => {
          concurrent++
          peakConcurrent = Math.max(peakConcurrent, concurrent)
          await gate
          concurrent--
          return 'ok'
        },
        'my-task',
        configFor(server),
        { inputVars: {}, maxInflightTasks: 2 }
      )
      workers.push(worker)

      for (let i = 0; i < 8; i++) {
        server.enqueueTask(scheduledTask({ taskRunId: { wfRunId: { id: 'wf-1' }, taskGuid: `task-${i}` } }))
      }
      await worker.start()

      // Wait until the worker is saturated, then confirm it stops asking for
      // more rather than accepting work it cannot start.
      await waitFor(() => worker.getInflightTaskCount() >= 2, 5000, 'the worker to saturate')
      await new Promise(resolve => setTimeout(resolve, 300))
      expect(peakConcurrent).toBe(2)
      expect(worker.getInflightTaskCount()).toBe(2)

      // Releasing the gate lets the backlog drain through the same ceiling.
      release()
      await waitFor(() => server.reportedTasks.length === 8, 10000, 'all tasks to drain')
      expect(peakConcurrent).toBe(2)
    }, 30000)

    test('send liveness heartbeats and react to unhealthy status — Java: LHLivenessController', async () => {
      const server = await startServer({ isClusterHealthy: true })
      const worker = createTaskWorker(() => 'ok', 'my-task', configFor(server), {
        inputVars: {},
        heartbeatIntervalMs: 100,
      })
      workers.push(worker)
      await worker.start()

      // Heartbeats keep arriving, not just the one at startup.
      await waitFor(() => server.registerRequests.length >= 3, 5000, 'repeated heartbeats')
      expect(worker.healthStatus()).toMatchObject({ healthy: true, reason: 'HEALTHY' })

      // The server reporting an unhealthy cluster must surface as
      // SERVER_REBALANCING, distinct from the worker's own failure.
      server.setOptions({ isClusterHealthy: false })
      await waitFor(() => !worker.healthStatus().healthy, 5000, 'the worker to notice')
      expect(worker.healthStatus().reason).toBe(LHTaskWorkerHealthReason.SERVER_REBALANCING)

      // ...and it recovers once the cluster is healthy again.
      server.setOptions({ isClusterHealthy: true })
      await waitFor(() => worker.healthStatus().healthy, 5000, 'the worker to recover')
    }, 30000)

    test('survive a server restart mid-run (soak/chaos) — plan tier 3', async () => {
      const server = await startServer()
      const port = server.port
      const worker = createTaskWorker(() => 'ok', 'my-task', configFor(server), {
        inputVars: {},
        heartbeatIntervalMs: 200,
      })
      workers.push(worker)

      server.enqueueTask(scheduledTask({ taskRunId: { wfRunId: { id: 'wf-1' }, taskGuid: 'before' } }))
      await worker.start()
      await waitFor(() => server.reportedTasks.length === 1, 10000, 'the pre-restart task')

      // Take the server away entirely, then bring a new one up on the same
      // port — as a rolling restart would look to a worker.
      await server.stop()
      await new Promise(resolve => setTimeout(resolve, 500))
      const restarted = new FakeLHServer()
      await restarted.start(port)
      servers.push(restarted)

      restarted.enqueueTask(scheduledTask({ taskRunId: { wfRunId: { id: 'wf-1' }, taskGuid: 'after' } }))
      await waitFor(() => restarted.reportedTasks.length === 1, 20000, 'a task after the restart')
      expect(restarted.reportedTasks[0].taskRunId?.taskGuid).toBe('after')
      expect(restarted.reportedTasks[0].status).toBe(TaskStatus.TASK_SUCCESS)
    }, 60000)

    test('run under sustained load for an extended period without leaks or crashes (soak) — plan tier 3', async () => {
      const server = await startServer()
      const TASKS = 300
      let executed = 0

      const worker = createTaskWorker(
        () => {
          executed++
          return 'ok'
        },
        'my-task',
        configFor(server),
        { inputVars: {}, maxInflightTasks: 5 }
      )
      workers.push(worker)
      await worker.start()

      // Feed tasks continuously rather than all at once, so the poll/report
      // cycle runs repeatedly instead of draining one big backlog.
      const heapBefore = process.memoryUsage().heapUsed
      for (let i = 0; i < TASKS; i++) {
        server.enqueueTask(scheduledTask({ taskRunId: { wfRunId: { id: 'wf-1' }, taskGuid: `soak-${i}` } }))
        if (i % 25 === 0) await new Promise(resolve => setTimeout(resolve, 5))
      }
      await waitFor(() => server.reportedTasks.length === TASKS, 60000, `all ${TASKS} tasks to be reported`)

      // Every task ran exactly once, nothing was dropped or duplicated.
      expect(executed).toBe(TASKS)
      const guids = server.reportedTasks.map(r => r.taskRunId?.taskGuid)
      expect(new Set(guids).size).toBe(TASKS)
      expect(server.reportedTasks.every(r => r.status === TaskStatus.TASK_SUCCESS)).toBe(true)

      // In-flight work returns to zero, and the connection is still healthy.
      await waitFor(() => worker.getInflightTaskCount() === 0, 5000, 'in-flight work to drain')
      expect(worker.healthStatus().healthy).toBe(true)

      // Heap should not scale with tasks processed. Generous bound: this is a
      // leak check, not a memory benchmark.
      global.gc?.()
      const heapGrowthMb = (process.memoryUsage().heapUsed - heapBefore) / 1024 / 1024
      expect(heapGrowthMb).toBeLessThan(100)
    }, 120000)
  })

  describe('benchmarks (sanity, run last)', () => {
    // NOTE: these deliberately do NOT compare against a live Java worker —
    // that would mean building and running sdk-java inside this suite. They
    // are absolute floors chosen to catch order-of-magnitude regressions
    // (the stated purpose in PARITY_PLAN.md), measured against the in-process
    // fake server, so they bound SDK overhead rather than server throughput.
    test('task throughput stays within a sanity floor — plan: benchmarks', async () => {
      const server = await startServer()
      const TASKS = 200
      const worker = createTaskWorker(() => 'ok', 'my-task', configFor(server), {
        inputVars: {},
        maxInflightTasks: 10,
      })
      workers.push(worker)
      await worker.start()

      for (let i = 0; i < TASKS; i++) {
        server.enqueueTask(scheduledTask({ taskRunId: { wfRunId: { id: 'wf-1' }, taskGuid: `bench-${i}` } }))
      }
      const startedAt = Date.now()
      await waitFor(() => server.reportedTasks.length === TASKS, 60000, 'the benchmark batch')
      const elapsedSec = (Date.now() - startedAt) / 1000
      const perSecond = TASKS / elapsedSec

      console.log(`[bench] throughput ${perSecond.toFixed(0)} tasks/sec over ${elapsedSec.toFixed(2)}s`)
      expect(perSecond).toBeGreaterThan(20)
    }, 90000)

    test('task latency stays within a sanity ceiling — plan: benchmarks', async () => {
      const server = await startServer()
      const SAMPLES = 25
      const worker = createTaskWorker(() => 'ok', 'my-task', configFor(server), {
        inputVars: {},
        maxInflightTasks: 1,
      })
      workers.push(worker)
      await worker.start()
      await waitFor(() => server.pollRequests.length > 0, 5000, 'the worker to be polling')

      const latencies: number[] = []
      for (let i = 0; i < SAMPLES; i++) {
        const before = server.reportedTasks.length
        const sentAt = Date.now()
        server.enqueueTask(scheduledTask({ taskRunId: { wfRunId: { id: 'wf-1' }, taskGuid: `lat-${i}` } }))
        await waitFor(() => server.reportedTasks.length > before, 10000, `sample ${i}`)
        latencies.push(Date.now() - sentAt)
      }

      latencies.sort((a, b) => a - b)
      const median = latencies[Math.floor(latencies.length / 2)]
      const p95 = latencies[Math.floor(latencies.length * 0.95)]
      console.log(`[bench] dispatch→report latency median ${median}ms, p95 ${p95}ms`)
      expect(median).toBeLessThan(250)
    }, 90000)
  })
})
