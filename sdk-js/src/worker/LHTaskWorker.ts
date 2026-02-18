import {
  Channel,
  ChannelCredentials,
  Client,
  Metadata,
  createChannel,
  createClientFactory,
} from 'nice-grpc'
import {
  LittleHorseDefinition,
  PollTaskRequest,
  PutTaskDefRequest,
  ReportTaskRun,
  ScheduledTask,
  StructDefCompatibilityType,
  PutStructDefRequest,
} from '../proto/service'
import { VariableDef } from '../proto/common_wfspec'
import { TaskDefId } from '../proto/object_id'
import { TaskStatus, LHErrorType } from '../proto/common_enums'
import { LHConfig } from '../LHConfig'
import { WorkerContext } from './WorkerContext'
import { extractTaskArgs, toVariableValue } from './variableMapping'
import { toStructVariableValue, getStructName, zodToVariableDefs } from './zodSchema'
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

/**
 * Represents a connection to a single LH Server host for polling tasks.
 * @internal
 */
class ServerConnection {
  private running = false
  private readonly host: string
  private readonly port: number
  private readonly channel: Channel
  private readonly client: Client<typeof LittleHorseDefinition>
  private pollPromise: Promise<void> | undefined

  constructor(
    host: string,
    port: number,
    private readonly taskDefId: TaskDefId,
    private readonly clientId: string,
    private readonly taskFunction: TaskFunction,
    private readonly taskWorkerVersion: string | undefined,
    private readonly tenantId: string | undefined,
    channelCredentials?: ChannelCredentials,
    private readonly outputSchema?: ZodTypeAny
  ) {
    this.host = host
    this.port = port
    this.channel = createChannel(`${host}:${port}`, channelCredentials)
    this.client = createClientFactory()
      .use((call, options) =>
        call.next(call.request, {
          ...options,
          metadata: this.withMetadata(options.metadata),
        })
      )
      .create(LittleHorseDefinition, this.channel)
  }

  private withMetadata(metadata?: Metadata): Metadata {
    let md = Metadata(metadata)
    if (this.tenantId) {
      md = md.append('tenantId', this.tenantId)
    }
    return md
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

  async close(): Promise<void> {
    this.running = false
    if (this.pollPromise) {
      await this.pollPromise.catch(() => {})
    }
    this.channel.close()
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
    const self = this
    let resolveReady: (() => void) | undefined
    let readyPromise: Promise<void> = Promise.resolve()

    async function* requestGenerator(): AsyncIterable<PollTaskRequest> {
      while (self.running) {
        // Wait until we're ready to ask for more work
        await readyPromise
        if (!self.running) break
        yield {
          taskDefId: self.taskDefId,
          clientId: self.clientId,
          taskWorkerVersion: self.taskWorkerVersion,
        }
        // Set up flow control: we won't yield the next request until the
        // current response is processed
        readyPromise = new Promise<void>((resolve) => {
          resolveReady = resolve
        })
      }
    }

    const responseStream = this.client.pollTask(requestGenerator())

    for await (const response of responseStream) {
      if (response.result) {
        // Execute task in the background (don't await—allow the next poll)
        this.executeAndReport(response.result).catch((err) => {
          console.error(`[LHTaskWorker] Unhandled error executing task:`, err)
        })
      }
      // Signal the generator to yield the next request
      resolveReady?.()
    }
  }

  private async executeAndReport(task: ScheduledTask): Promise<void> {
    const report = await this.executeTask(task)
    await this.reportTaskWithRetries(report, REPORT_TASK_MAX_RETRIES)
  }

  private async executeTask(task: ScheduledTask): Promise<ReportTaskRun> {
    const context = new WorkerContext(task)
    const now = new Date().toISOString()

    try {
      const args = extractTaskArgs(task)
      // Append WorkerContext as the last argument
      args.push(context)

      const result = await Promise.resolve(this.taskFunction(...args))
      const output =
        this.outputSchema && getStructName(this.outputSchema) && result !== null && result !== undefined && typeof result === 'object'
          ? toStructVariableValue(result as Record<string, unknown>, this.outputSchema)
          : toVariableValue(result)

      return {
        taskRunId: task.taskRunId,
        time: now,
        status: TaskStatus.TASK_SUCCESS,
        attemptNumber: task.attemptNumber,
        logOutput: context.getLogOutput()
          ? { value: { $case: 'str' as const, value: context.getLogOutput() } }
          : undefined,
        result: { $case: 'output', value: output },
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
          logOutput: context.getLogOutput()
            ? { value: { $case: 'str' as const, value: context.getLogOutput() } }
            : undefined,
          result: {
            $case: 'exception',
            value: {
              name: err.name,
              message: err.message,
              content: err.content ? toVariableValue(err.content) : { value: undefined },
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
        logOutput: context.getLogOutput()
          ? { value: { $case: 'str' as const, value: context.getLogOutput() } }
          : undefined,
        result: {
          $case: 'error',
          value: {
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
        console.warn(
          `[LHTaskWorker] Failed to report task on ${this.hostKey}, retrying (${retriesLeft} left)...`
        )
        await sleep(REPORT_TASK_RETRY_DELAY_MS)
        await this.reportTaskWithRetries(report, retriesLeft - 1)
      } else {
        console.error(`[LHTaskWorker] Failed to report task after all retries:`, err)
      }
    }
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
  /** Starts the task worker (heartbeat loop + poll streams). */
  start(): Promise<void>
  /** Cleanly shuts down the task worker. */
  close(): Promise<void>
  /** Returns whether the worker is currently running. */
  isRunning(): boolean
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
  const taskWorkerId = `worker-${taskDefName}-${randomBytes(8).toString('hex')}`
  const bootstrapClient = config.getClient()
  const channelCredentials = config.getChannelCredentials()
  const tenantId = config.getTenantId()
  const inputVars = zodToVariableDefs(options.inputVars)
  const outputSchema = options.outputSchema
  const taskWorkerVersion = options.taskWorkerVersion
  const connections = new Map<string, ServerConnection>()
  let running = false
  let heartbeatTimer: ReturnType<typeof setInterval> | undefined

  async function heartbeat(): Promise<void> {
    try {
      const response = await bootstrapClient.registerTaskWorker({
        taskWorkerId,
        taskDefId: { name: taskDefName },
      })

      const newHosts = new Set(response.yourHosts.map((h) => `${h.host}:${h.port}`))

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
            tenantId,
            channelCredentials,
            outputSchema
          )
          conn.start()
          connections.set(key, conn)
        }
      }
    } catch (err) {
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
        if (err?.code === 5 /* NOT_FOUND */) {
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
        if (err?.code === 6 /* ALREADY_EXISTS */) {
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
        if (err?.code === 6 /* ALREADY_EXISTS */) {
          console.log(`[LHTaskWorker] StructDef '${request.name}' already exists, skipping registration.`)
        } else {
          throw err
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
        heartbeat().catch((err) => {
          console.error('[LHTaskWorker] Heartbeat error:', err)
        })
      }, HEARTBEAT_INTERVAL_MS)
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
    },

    isRunning(): boolean {
      return running
    },
  }
}

function sleep(ms: number): Promise<void> {
  return new Promise((resolve) => setTimeout(resolve, ms))
}
