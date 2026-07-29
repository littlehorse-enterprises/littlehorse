import type { GrpcTransport } from '@protobuf-ts/grpc-transport'
import { ReportTaskRun, ScheduledTask, PutStructDefRequest, StructDefCompatibilityType } from '../proto/service'
import { TypeDefinition } from '../proto/type_definition'
import { Timestamp } from '../proto/google/protobuf/timestamp'
import { TaskDefId } from '../proto/object_id'
import { TaskStatus, LHErrorType } from '../proto/common_enums'
import { LHConfig } from '../LHConfig'
import type { LHPublicClient } from '../client'
import { WorkerContext } from './WorkerContext'
import { extractTaskArgs, toVariableValue } from './variableMapping'
import { toStructVariableValue, getStructName, zodToVariableDefs, buildPutStructDefRequest } from './zodSchema'
import { randomBytes } from 'crypto'
import { type ZodTypeAny } from 'zod'

/**
 * A task function that the LHTaskWorker will execute. It receives the input
 * variables as positional arguments and an optional WorkerContext as the last argument.
 */
export type TaskFunction = (...args: any[]) => any | Promise<any>

const HEARTBEAT_INTERVAL_MS = 15_000
const REPORT_TASK_MAX_RETRIES = 5
const REPORT_TASK_RETRY_DELAY_MS = 2_000
const CLOSE_DRAIN_TIMEOUT_MS = 30_000

/** Why a worker is (un)healthy — mirrors Java's LHTaskWorkerHealthReason. */
export enum LHTaskWorkerHealthReason {
  HEALTHY = 'HEALTHY',
  /** start() has not been called, or close() already ran. */
  NOT_RUNNING = 'NOT_RUNNING',
  /** Running, but not connected to any server host. */
  NO_CONNECTIONS = 'NO_CONNECTIONS',
  /** The server reported the cluster is not healthy (mid-rebalance). */
  SERVER_REBALANCING = 'SERVER_REBALANCING',
  /** The worker's own registration call is failing. */
  UNHEALTHY = 'UNHEALTHY',
}

/** Health snapshot of a task worker — mirrors Java's LHTaskWorkerHealth. */
export interface LHTaskWorkerHealth {
  healthy: boolean
  reason: LHTaskWorkerHealthReason
  /** Number of server hosts this worker is currently polling. */
  connectedHosts: number
}

/**
 * Represents a connection to a single LH Server host for polling tasks.
 * @internal
 */
class ServerConnection {
  private running = false
  private readonly host: string
  private readonly port: number
  private readonly transport: GrpcTransport
  private readonly client: LHPublicClient
  private pollPromise: Promise<void> | undefined
  private pollAbort: AbortController | undefined
  private inflight = 0
  /** True while we are deliberately not asking for work (at capacity). */
  private awaitingCapacity = false

  constructor(
    host: string,
    port: number,
    private readonly taskDefId: TaskDefId,
    private readonly clientId: string,
    private readonly taskFunction: TaskFunction,
    private readonly taskWorkerVersion: string | undefined,
    private readonly config: LHConfig,
    private readonly maxInflight: number,
    private readonly outputSchema?: ZodTypeAny
  ) {
    this.host = host
    this.port = port
    this.transport = config.createTransport(host, port)
    this.client = config.createClientForTransport(this.transport)
  }

  get hostKey(): string {
    return `${this.host}:${this.port}`
  }

  isRunning(): boolean {
    return this.running
  }

  /**
   * Starts the bidirectional PollTask stream. The async generator yields
   * PollTaskRequest messages; each response may contain a ScheduledTask.
   */
  start(): void {
    if (this.running) return
    this.running = true
    this.pollPromise = this.pollLoop()
  }

  /** Number of tasks currently executing on this connection. */
  getInflightCount(): number {
    return this.inflight
  }

  async close(drainTimeoutMs = CLOSE_DRAIN_TIMEOUT_MS): Promise<void> {
    this.running = false
    // Aborting the in-flight poll call is what actually unblocks the response
    // iterator: without it, `for await (call.responses)` waits forever for a
    // task that will never arrive and close() never returns.
    this.pollAbort?.abort()
    if (this.pollPromise) {
      await this.pollPromise.catch(() => {})
    }
    // Let tasks that were already handed to us finish and report before the
    // transport goes away, so results aren't lost on shutdown.
    const deadline = Date.now() + drainTimeoutMs
    while (this.inflight > 0 && Date.now() < deadline) {
      await sleep(10)
    }
    this.transport.close()
  }

  private async pollLoop(): Promise<void> {
    while (this.running) {
      try {
        await this.doPoll()
      } catch (err) {
        if (this.running) {
          console.error(`[LHTaskWorker] Poll stream error on ${this.hostKey}:`, err)
          // Wait a bit before reconnecting
          await sleep(2000)
        }
      }
    }
  }

  private async doPoll(): Promise<void> {
    this.pollAbort = new AbortController()
    const call = this.client.pollTask({ abort: this.pollAbort.signal })

    const sendRequest = () => {
      if (!this.running) return
      // Flow control: request the next task. We only ask for more work after
      // the previous response has been received and dispatched.
      call.requests
        .send({
          taskDefId: this.taskDefId,
          clientId: this.clientId,
          taskWorkerVersion: this.taskWorkerVersion,
        })
        .catch(() => {})
    }

    sendRequest()

    try {
      for await (const response of call.responses) {
        if (response.result) {
          // Execute task in the background (don't await—allow the next poll)
          this.inflight++
          this.executeAndReport(response.result)
            .catch(err => {
              console.error(`[LHTaskWorker] Unhandled error executing task:`, err)
            })
            .finally(() => {
              this.inflight--
              // A slot freed up; if we stopped asking for work, resume.
              if (this.awaitingCapacity) {
                this.awaitingCapacity = false
                sendRequest()
              }
            })
        }
        if (!this.running) break

        // Back-pressure: stop requesting work while at capacity, rather than
        // accepting tasks we cannot start. The `finally` above resumes.
        if (this.inflight >= this.maxInflight) {
          this.awaitingCapacity = true
          continue
        }
        sendRequest()
      }
    } finally {
      try {
        await call.requests.complete()
      } catch {
        // ignore errors while closing the request stream
      }
    }
  }

  private async executeAndReport(task: ScheduledTask): Promise<void> {
    const report = await this.executeTask(task)
    await this.reportTaskWithRetries(report, REPORT_TASK_MAX_RETRIES)
  }

  private async executeTask(task: ScheduledTask): Promise<ReportTaskRun> {
    // The client is passed so checkpointed operations can talk to the server.
    const context = new WorkerContext(task, this.client)
    const now = Timestamp.now()

    let args: unknown[]
    try {
      args = extractTaskArgs(task)
    } catch (err: any) {
      // Failing to map the server's input variables is distinct from the task
      // itself failing: the server must not retry this as a technical error.
      return {
        taskRunId: task.taskRunId,
        time: now,
        status: TaskStatus.TASK_INPUT_VAR_SUB_ERROR,
        attemptNumber: task.attemptNumber,
        logOutput: undefined,
        result: {
          oneofKind: 'error',
          error: {
            type: LHErrorType.VAR_SUB_ERROR,
            message: err?.message ?? String(err),
          },
        },
        totalCheckpoints: 0,
      }
    }

    try {
      // Append WorkerContext as the last argument
      args.push(context)

      const result = await Promise.resolve(this.taskFunction(...args))
      const output =
        this.outputSchema &&
        getStructName(this.outputSchema) &&
        result !== null &&
        result !== undefined &&
        typeof result === 'object'
          ? toStructVariableValue(result as Record<string, unknown>, this.outputSchema)
          : toVariableValue(result)

      return {
        taskRunId: task.taskRunId,
        time: now,
        status: TaskStatus.TASK_SUCCESS,
        attemptNumber: task.attemptNumber,
        logOutput: context.getLogOutput() ? { value: { oneofKind: 'str', str: context.getLogOutput()! } } : undefined,
        result: { oneofKind: 'output', output },
        totalCheckpoints: 0,
      }
    } catch (err: any) {
      // Check if it's an LHTaskException (user-defined business exception)
      if (err instanceof LHTaskException) {
        return {
          taskRunId: task.taskRunId,
          time: now,
          status: TaskStatus.TASK_EXCEPTION,
          attemptNumber: task.attemptNumber,
          logOutput: context.getLogOutput() ? { value: { oneofKind: 'str', str: context.getLogOutput()! } } : undefined,
          result: {
            oneofKind: 'exception',
            exception: {
              name: err.name,
              message: err.message,
              content: err.content ? toVariableValue(err.content) : { value: { oneofKind: undefined } },
            },
          },
          totalCheckpoints: 0,
        }
      }

      // Otherwise it's a TASK_FAILED
      return {
        taskRunId: task.taskRunId,
        time: now,
        status: TaskStatus.TASK_FAILED,
        attemptNumber: task.attemptNumber,
        logOutput: context.getLogOutput() ? { value: { oneofKind: 'str', str: context.getLogOutput()! } } : undefined,
        result: {
          oneofKind: 'error',
          error: {
            type: LHErrorType.TASK_FAILURE,
            message: err?.message ?? String(err),
          },
        },
        totalCheckpoints: 0,
      }
    }
  }

  private async reportTaskWithRetries(report: ReportTaskRun, retriesLeft: number): Promise<void> {
    try {
      await this.client.reportTask(report)
    } catch (err) {
      if (retriesLeft > 0) {
        console.warn(`[LHTaskWorker] Failed to report task on ${this.hostKey}, retrying (${retriesLeft} left)...`)
        await sleep(REPORT_TASK_RETRY_DELAY_MS)
        await this.reportTaskWithRetries(report, retriesLeft - 1)
      } else {
        console.error(`[LHTaskWorker] Failed to report task after all retries:`, err)
      }
    }
  }
}

/** Thrown when a worker's signature disagrees with the server's TaskDef. */
export class TaskSchemaMismatchError extends Error {
  constructor(message: string) {
    super(message)
    this.name = 'TaskSchemaMismatchError'
  }
}

/**
 * Exception class for user-defined business exceptions.
 * When thrown from a task function, the task will be marked as TASK_EXCEPTION
 * instead of TASK_FAILED.
 */
export class LHTaskException extends Error {
  public readonly content?: unknown

  constructor(name: string, message: string, content?: unknown) {
    super(message)
    this.name = name
    this.content = content
  }
}

/**
 * Options for configuring the task worker.
 */
export interface LHTaskWorkerOptions {
  /**
   * A record mapping parameter names to Zod schemas. The worker uses these
   * to derive typed `VariableDef[]` for TaskDef registration.
   *
   * ```ts
   * const worker = createTaskWorker(myFn, 'my-task', config, {
   *   inputVars: { name: z.string(), age: z.number().int() },
   * })
   * ```
   */
  inputVars: Record<string, ZodTypeAny>

  /**
   * When the task function returns a struct, provide the Zod schema
   * (created with `lhStruct()`) here so the worker can serialize the
   * return value as a Struct-typed VariableValue.
   *
   * ```ts
   * const worker = createTaskWorker(myFn, 'my-task', config, {
   *   inputVars: { report: ParkingTicketReport },
   *   outputSchema: PersonSchema,
   * })
   * ```
   */
  outputSchema?: ZodTypeAny

  /**
   * Optional version string for the task worker (recorded for debugging).
   */
  taskWorkerVersion?: string

  /**
   * How often to re-register with the server, which is also how quickly the
   * worker notices a rebalance. Defaults to 15s.
   */
  heartbeatIntervalMs?: number

  /**
   * Maximum tasks executing concurrently per server connection. The worker
   * stops requesting work while at capacity. Defaults to 10 (Java's
   * equivalent knobs are workerThreads / inflightTasks).
   */
  maxInflightTasks?: number
}

/**
 * The handle returned by `createTaskWorker`. Provides methods to register
 * metadata, start polling, and shut down.
 */
export interface LHTaskWorker {
  /** Returns the name of the TaskDef this worker polls for. */
  getTaskDefName(): string
  /** Returns the unique worker ID. */
  getTaskWorkerId(): string
  /** Checks whether the TaskDef exists on the server. */
  doesTaskDefExist(): Promise<boolean>
  /** Registers the TaskDef on the LH server. */
  registerTaskDef(): Promise<void>
  /** Registers a StructDef on the LH server. */
  registerStructDef(request: PutStructDefRequest): Promise<void>
  /**
   * Checks the worker's declared input vars against the server's TaskDef and
   * throws TaskSchemaMismatchError on a mismatch — Java: LHTaskWorker
   * start-time validation.
   */
  validateTaskDef(): Promise<void>
  /**
   * Asks the server whether these schemas are valid evolutions, without
   * registering them — Java: LHTaskWorker#validateStructDef(s).
   */
  validateStructDefs(schemas: ZodTypeAny[], compatibilityType?: StructDefCompatibilityType): Promise<void>
  /** Starts the task worker (heartbeat loop + poll streams). */
  start(): Promise<void>
  /** Cleanly shuts down the task worker. */
  close(): Promise<void>
  /** Returns whether the worker is currently running. */
  isRunning(): boolean
  /** Returns whether the worker has been shut down (inverse of isRunning). */
  isClosed(): boolean
  /** Returns a health snapshot — Java: LHTaskWorker#healthStatus. */
  healthStatus(): LHTaskWorkerHealth
  /** Number of tasks currently executing across all connections. */
  getInflightTaskCount(): number
}

/**
 * Creates a task worker that polls the LH Server(s) for tasks and executes
 * a user-provided task function whenever a task is scheduled.
 *
 * Usage:
 * ```ts
 * const config = LHConfig.from({ apiHost: 'localhost', apiPort: '2023' })
 * const worker = createTaskWorker(myTaskFunction, 'my-task', config, {
 *   inputVars: { name: z.string() },
 * })
 * await worker.start()
 * ```
 *
 * @param taskFunction - The function to execute when a task is scheduled.
 *   It receives the task's input variables as positional arguments, and
 *   optionally a `WorkerContext` as the last argument.
 * @param taskDefName - The name of the TaskDef to poll for.
 * @param config - An LHConfig instance for connecting to the LH Server.
 * @param options - Configuration including `inputVars` (a record of param name → Zod schema)
 *   for typed TaskDef registration.
 */
export function createTaskWorker(
  taskFunction: TaskFunction,
  taskDefName: string,
  config: LHConfig,
  options: LHTaskWorkerOptions
): LHTaskWorker {
  // Config-provided ids/versions win; the random suffix keeps two workers for
  // the same TaskDef on one host distinguishable.
  const taskWorkerId = `${config.getTaskWorkerId()}-${taskDefName}-${randomBytes(4).toString('hex')}`
  // Hold the bootstrap transport so close() can release it; config.getClient()
  // would create one we could never shut down.
  const bootstrapTransport = config.createTransport(config.getApiBootstrapHost()!, config.getApiBootstrapPort()!)
  const bootstrapClient = config.createClientForTransport(bootstrapTransport)
  const inputVars = zodToVariableDefs(options.inputVars)
  const outputSchema = options.outputSchema
  const taskWorkerVersion = options.taskWorkerVersion ?? config.getTaskWorkerVersion()
  const maxInflightTasks = options.maxInflightTasks ?? config.getNumWorkerThreads()
  const connections = new Map<string, ServerConnection>()
  let running = false
  let heartbeatTimer: ReturnType<typeof setInterval> | undefined
  let lastHeartbeatFailed = false
  let clusterHealthy = true

  async function heartbeat(): Promise<void> {
    try {
      const response = await bootstrapClient.registerTaskWorker({
        taskWorkerId,
        taskDefId: { name: taskDefName },
      })

      const newHosts = new Set(response.yourHosts.map(h => `${h.host}:${h.port}`))

      // Remove connections for hosts no longer in the list
      for (const [key, conn] of connections) {
        if (!newHosts.has(key)) {
          console.log(`[LHTaskWorker] Removing connection to ${key}`)
          await conn.close()
          connections.delete(key)
        }
      }

      // Add connections for new hosts
      for (const hostInfo of response.yourHosts) {
        const key = `${hostInfo.host}:${hostInfo.port}`
        const existing = connections.get(key)

        if (!existing || !existing.isRunning()) {
          if (existing) {
            await existing.close()
          }
          console.log(`[LHTaskWorker] Connecting to ${key}`)
          const conn = new ServerConnection(
            hostInfo.host,
            hostInfo.port,
            { name: taskDefName },
            taskWorkerId,
            taskFunction,
            taskWorkerVersion,
            config,
            maxInflightTasks,
            outputSchema
          )
          conn.start()
          connections.set(key, conn)
        }
      }
      lastHeartbeatFailed = false
      // The server tells us when the cluster itself is unhealthy (e.g. mid
      // rebalance); Java surfaces the same signal via LHLivenessController.
      clusterHealthy = response.isClusterHealthy ?? true
    } catch (err) {
      lastHeartbeatFailed = true
      console.error('[LHTaskWorker] Failed to register with server:', err)
    }
  }

  return {
    getTaskDefName(): string {
      return taskDefName
    },

    getTaskWorkerId(): string {
      return taskWorkerId
    },

    async doesTaskDefExist(): Promise<boolean> {
      try {
        await bootstrapClient.getTaskDef({ name: taskDefName })
        return true
      } catch (err: any) {
        if (err?.code === 'NOT_FOUND') {
          return false
        }
        throw err
      }
    },

    async registerTaskDef(): Promise<void> {
      try {
        const result = await bootstrapClient.putTaskDef({
          name: taskDefName,
          inputVars,
        })
        console.log(`[LHTaskWorker] Registered TaskDef: ${result.id?.name}`)
      } catch (err: any) {
        if (err?.code === 'ALREADY_EXISTS') {
          console.log(`[LHTaskWorker] TaskDef '${taskDefName}' already exists, skipping registration.`)
        } else {
          throw err
        }
      }
    },

    async registerStructDef(request: PutStructDefRequest): Promise<void> {
      try {
        const result = await bootstrapClient.putStructDef(request)
        console.log(`[LHTaskWorker] Registered StructDef: ${result.id?.name} v${result.id?.version}`)
      } catch (err: any) {
        if (err?.code === 'ALREADY_EXISTS') {
          console.log(`[LHTaskWorker] StructDef '${request.name}' already exists, skipping registration.`)
        } else {
          throw err
        }
      }
    },

    async validateTaskDef(): Promise<void> {
      // Java does this inside start(): fetch the server's TaskDef and check
      // the worker's declared inputs against it, so a signature mismatch
      // fails immediately instead of on the first scheduled task.
      let serverTaskDef
      try {
        serverTaskDef = await bootstrapClient.getTaskDef({ name: taskDefName })
      } catch (err: unknown) {
        const code = (err as { code?: unknown })?.code
        if (code === 'NOT_FOUND' || code === 5) {
          throw new TaskSchemaMismatchError(
            `TaskDef '${taskDefName}' does not exist on the server. Register it first (registerTaskDef()).`
          )
        }
        throw err
      }

      const expected = serverTaskDef.inputVars
      if (expected.length !== inputVars.length) {
        throw new TaskSchemaMismatchError(
          `TaskDef '${taskDefName}' expects ${expected.length} input var(s) ` +
            `(${expected.map(v => v.name).join(', ') || 'none'}), but this worker declares ` +
            `${inputVars.length} (${inputVars.map(v => v.name).join(', ') || 'none'}).`
        )
      }

      for (let i = 0; i < expected.length; i++) {
        const serverVar = expected[i]
        const workerVar = inputVars[i]
        if (serverVar.name !== workerVar.name) {
          throw new TaskSchemaMismatchError(
            `TaskDef '${taskDefName}' input var ${i} is named '${serverVar.name}', ` +
              `but this worker declares '${workerVar.name}'.`
          )
        }
        if (!TypeDefinition.equals(serverVar.typeDef, workerVar.typeDef)) {
          throw new TaskSchemaMismatchError(
            `TaskDef '${taskDefName}' input var '${serverVar.name}' has a different type on the server ` +
              `than this worker declares.`
          )
        }
      }
    },

    async validateStructDefs(
      schemas: ZodTypeAny[],
      compatibilityType: StructDefCompatibilityType = StructDefCompatibilityType.FULLY_COMPATIBLE_SCHEMA_UPDATES
    ): Promise<void> {
      // Asks the server whether the schema *could* be registered, without
      // registering it — Java: LHTaskWorker#validateStructDef(s).
      for (const schema of schemas) {
        const request = buildPutStructDefRequest(schema, compatibilityType)
        const response = await bootstrapClient.validateStructDefEvolution({
          structDefId: { name: request.name, version: 0 },
          structDef: request.structDef,
          compatibilityType,
        })
        if (!response.isValid) {
          throw new Error(
            `StructDef '${request.name}' is not a valid evolution under ${StructDefCompatibilityType[compatibilityType]}.`
          )
        }
      }
    },

    async start(): Promise<void> {
      if (running) return
      running = true

      console.log(`[LHTaskWorker] Starting worker for TaskDef '${taskDefName}' (id: ${taskWorkerId})`)

      // Run heartbeat immediately, then on an interval
      await heartbeat()
      heartbeatTimer = setInterval(() => {
        heartbeat().catch(err => {
          console.error('[LHTaskWorker] Heartbeat error:', err)
        })
      }, options.heartbeatIntervalMs ?? HEARTBEAT_INTERVAL_MS)
    },

    async close(): Promise<void> {
      if (!running) return
      running = false

      console.log(`[LHTaskWorker] Shutting down worker for TaskDef '${taskDefName}'`)

      if (heartbeatTimer) {
        clearInterval(heartbeatTimer)
        heartbeatTimer = undefined
      }

      const closePromises: Promise<void>[] = []
      for (const conn of connections.values()) {
        closePromises.push(conn.close())
      }
      await Promise.all(closePromises)
      connections.clear()
      bootstrapTransport.close()
    },

    isRunning(): boolean {
      return running
    },

    isClosed(): boolean {
      return !running
    },

    healthStatus(): LHTaskWorkerHealth {
      const connectedHosts = [...connections.values()].filter(conn => conn.isRunning()).length
      let reason = LHTaskWorkerHealthReason.HEALTHY
      if (!running) {
        reason = LHTaskWorkerHealthReason.NOT_RUNNING
      } else if (!clusterHealthy) {
        // Java checks cluster health before worker health, so a rebalancing
        // cluster is reported as such rather than as a worker fault.
        reason = LHTaskWorkerHealthReason.SERVER_REBALANCING
      } else if (lastHeartbeatFailed) {
        reason = LHTaskWorkerHealthReason.UNHEALTHY
      } else if (connectedHosts === 0) {
        reason = LHTaskWorkerHealthReason.NO_CONNECTIONS
      }
      return {
        healthy: reason === LHTaskWorkerHealthReason.HEALTHY,
        reason,
        connectedHosts,
      }
    },

    getInflightTaskCount(): number {
      let total = 0
      for (const conn of connections.values()) total += conn.getInflightCount()
      return total
    },
  }
}

function sleep(ms: number): Promise<void> {
  return new Promise(resolve => setTimeout(resolve, ms))
}
