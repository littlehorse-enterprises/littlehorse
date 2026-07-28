import { z } from 'zod'
import { LHConfig } from '../LHConfig'
import { LHErrorType, TaskStatus, VariableType } from '../proto/common_enums'
import { ReportTaskRun, ScheduledTask } from '../proto/service'
import { Timestamp } from '../proto/google/protobuf/timestamp'
import { VariableValue } from '../proto/type_definition'
import { createTaskWorker, LHTaskException, lhStruct, TaskFunction, WorkerContext } from '../worker'
import { buildPutStructDefRequest } from '../worker'
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

function varVal(value: VariableValue['value']): { varName: string; value: VariableValue } {
  return { varName: 'arg', value: { value } }
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

    // Java validates the worker's signature against the server's TaskDef, and
    // can validate StructDefs without registering them. Neither exists in JS.
    test.todo(
      'validate the task function signature against the server TaskDef on start — Java: LHTaskWorker (start-time validation)'
    )
    test.todo('validate StructDefs against the server without registering — Java: LHTaskWorker#validateStructDef(s)')
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
                    name: { value: { value: { oneofKind: 'str', str: 'Ada' } } },
                    age: { value: { value: { oneofKind: 'int', int: '36' } } },
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

    // Java's WorkerContext exposes the user/group for user-task-triggered
    // tasks and can checkpoint sub-operations; neither exists in JS yet.
    test.todo('expose userId / userGroup for user-task-triggered tasks — Java: WorkerContext#getUserId/getUserGroup')
    test.todo(
      'checkpoint a sub-operation so retries can skip completed work — Java: WorkerContext#executeAndCheckpoint'
    )
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

    // Not yet implemented: JS has no configured concurrency ceiling (it tracks
    // in-flight work but never refuses to accept more), no liveness/heartbeat
    // health reporting to the server, and no soak coverage.
    test.todo(
      'limit concurrent in-flight tasks to the configured inflight/threads setting — Java: config inflightTasks/workerThreads'
    )
    test.todo('send liveness heartbeats and react to unhealthy status — Java: LHLivenessController')
    test.todo('survive a server restart mid-run (soak/chaos) — plan tier 3')
    test.todo('run under sustained load for an extended period without leaks or crashes (soak) — plan tier 3')
  })

  describe('benchmarks (sanity, run last)', () => {
    test.todo('task throughput within sanity range of the Java worker on the same server — plan: benchmarks')
    test.todo('task latency within sanity range of the Java worker on the same server — plan: benchmarks')
  })
})
