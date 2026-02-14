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
import { trySerializeAsStruct } from './decorators'
import { randomBytes } from 'crypto'

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
    channelCredentials?: ChannelCredentials
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
      const output = trySerializeAsStruct(result) ?? toVariableValue(result)

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
 * The LHTaskWorker polls the LH Server(s) for tasks and executes a user-provided
 * task function whenever a task is scheduled.
 *
 * Usage:
 * ```ts
 * const config = LHConfig.from({ apiHost: 'localhost', apiPort: '2023' })
 * const worker = new LHTaskWorker(myTaskFunction, 'my-task', config)
 * await worker.start()
 * ```
 */
/**
 * Options for configuring the LHTaskWorker.
 */
export interface LHTaskWorkerOptions {
  /**
   * Input variable definitions for the TaskDef. When provided, `registerTaskDef()`
   * will include these in the PutTaskDefRequest so the server knows the parameter
   * names, types, and (optionally) struct types.
   */
  inputVars?: VariableDef[]

  /**
   * Optional version string for the task worker (recorded for debugging).
   */
  taskWorkerVersion?: string
}

export class LHTaskWorker {
  private readonly config: LHConfig
  private readonly taskDefName: string
  private readonly taskFunction: TaskFunction
  private readonly taskWorkerId: string
  private readonly taskWorkerVersion?: string
  private readonly bootstrapClient: Client<typeof LittleHorseDefinition>
  private readonly connections: Map<string, ServerConnection> = new Map()
  private running = false
  private heartbeatTimer: ReturnType<typeof setInterval> | undefined
  private readonly channelCredentials?: ChannelCredentials
  private readonly tenantId?: string
  private readonly inputVars: VariableDef[]

  /**
   * Creates a new LHTaskWorker.
   *
   * @param taskFunction - The function to execute when a task is scheduled.
   *   It receives the task's input variables as positional arguments, and
   *   optionally a `WorkerContext` as the last argument.
   * @param taskDefName - The name of the TaskDef to poll for.
   * @param config - An LHConfig instance for connecting to the LH Server.
   * @param options - Optional configuration including inputVars for typed TaskDef registration.
   */
  constructor(taskFunction: TaskFunction, taskDefName: string, config: LHConfig, options?: LHTaskWorkerOptions) {
    this.taskFunction = taskFunction
    this.taskDefName = taskDefName
    this.config = config
    this.taskWorkerId = `worker-${taskDefName}-${randomBytes(8).toString('hex')}`
    this.bootstrapClient = config.getClient()
    this.channelCredentials = config.getChannelCredentials()
    this.tenantId = config.getTenantId()
    this.inputVars = options?.inputVars ?? []
    this.taskWorkerVersion = options?.taskWorkerVersion
  }

  /**
   * Returns the name of the TaskDef this worker polls for.
   */
  getTaskDefName(): string {
    return this.taskDefName
  }

  /**
   * Returns the unique worker ID.
   */
  getTaskWorkerId(): string {
    return this.taskWorkerId
  }

  /**
   * Checks whether the TaskDef exists on the server.
   */
  async doesTaskDefExist(): Promise<boolean> {
    try {
      await this.bootstrapClient.getTaskDef({ name: this.taskDefName })
      return true
    } catch (err: any) {
      if (err?.code === 5 /* NOT_FOUND */) {
        return false
      }
      throw err
    }
  }

  /**
   * Registers the TaskDef on the LH server. This is a convenience method
   * for development; in production you should register TaskDefs separately.
   */
  async registerTaskDef(): Promise<void> {
    try {
      const result = await this.bootstrapClient.putTaskDef({
        name: this.taskDefName,
        inputVars: this.inputVars,
      })
      console.log(`[LHTaskWorker] Registered TaskDef: ${result.id?.name}`)
    } catch (err: any) {
      if (err?.code === 6 /* ALREADY_EXISTS */) {
        console.log(`[LHTaskWorker] TaskDef '${this.taskDefName}' already exists, skipping registration.`)
      } else {
        throw err
      }
    }
  }

  /**
   * Registers a StructDef on the LH server. This is a convenience method
   * for development; in production you should register StructDefs separately.
   *
   * @param request - The PutStructDefRequest describing the struct schema.
   */
  async registerStructDef(request: PutStructDefRequest): Promise<void> {
    try {
      const result = await this.bootstrapClient.putStructDef(request)
      console.log(`[LHTaskWorker] Registered StructDef: ${result.id?.name} v${result.id?.version}`)
    } catch (err: any) {
      if (err?.code === 6 /* ALREADY_EXISTS */) {
        console.log(`[LHTaskWorker] StructDef '${request.name}' already exists, skipping registration.`)
      } else {
        throw err
      }
    }
  }

  /**
   * Starts the task worker. This begins the heartbeat loop which discovers
   * LH Server hosts and opens bidirectional poll streams to each.
   */
  async start(): Promise<void> {
    if (this.running) return
    this.running = true

    console.log(`[LHTaskWorker] Starting worker for TaskDef '${this.taskDefName}' (id: ${this.taskWorkerId})`)

    // Run heartbeat immediately, then on an interval
    await this.heartbeat()
    this.heartbeatTimer = setInterval(() => {
      this.heartbeat().catch((err) => {
        console.error('[LHTaskWorker] Heartbeat error:', err)
      })
    }, HEARTBEAT_INTERVAL_MS)
  }

  /**
   * Cleanly shuts down the task worker.
   */
  async close(): Promise<void> {
    if (!this.running) return
    this.running = false

    console.log(`[LHTaskWorker] Shutting down worker for TaskDef '${this.taskDefName}'`)

    if (this.heartbeatTimer) {
      clearInterval(this.heartbeatTimer)
      this.heartbeatTimer = undefined
    }

    const closePromises: Promise<void>[] = []
    for (const conn of this.connections.values()) {
      closePromises.push(conn.close())
    }
    await Promise.all(closePromises)
    this.connections.clear()
  }

  /**
   * Returns whether the worker is currently running.
   */
  isRunning(): boolean {
    return this.running
  }

  /**
   * Performs a single heartbeat: registers with the bootstrap server and
   * reconciles the set of per-host connections.
   */
  private async heartbeat(): Promise<void> {
    try {
      const response = await this.bootstrapClient.registerTaskWorker({
        taskWorkerId: this.taskWorkerId,
        taskDefId: { name: this.taskDefName },
      })

      const newHosts = new Set(response.yourHosts.map((h) => `${h.host}:${h.port}`))

      // Remove connections for hosts no longer in the list
      for (const [key, conn] of this.connections) {
        if (!newHosts.has(key)) {
          console.log(`[LHTaskWorker] Removing connection to ${key}`)
          await conn.close()
          this.connections.delete(key)
        }
      }

      // Add connections for new hosts
      for (const hostInfo of response.yourHosts) {
        const key = `${hostInfo.host}:${hostInfo.port}`
        const existing = this.connections.get(key)

        if (!existing || !existing.isRunning()) {
          if (existing) {
            await existing.close()
          }
          console.log(`[LHTaskWorker] Connecting to ${key}`)
          const conn = new ServerConnection(
            hostInfo.host,
            hostInfo.port,
            { name: this.taskDefName },
            this.taskWorkerId,
            this.taskFunction,
            this.taskWorkerVersion,
            this.tenantId,
            this.channelCredentials
          )
          conn.start()
          this.connections.set(key, conn)
        }
      }
    } catch (err) {
      console.error('[LHTaskWorker] Failed to register with server:', err)
    }
  }
}

function sleep(ms: number): Promise<void> {
  return new Promise((resolve) => setTimeout(resolve, ms))
}
