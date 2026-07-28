import * as grpc from '@grpc/grpc-js'
import { Empty } from '../proto/google/protobuf/empty'
import { StructDef } from '../proto/struct_def'
import { TaskDef } from '../proto/task_def'
import { WfSpec } from '../proto/wf_spec'
import { ExternalEventDef } from '../proto/external_event'
import { WorkflowEventDef } from '../proto/workflow_event'
import {
  GetLatestWfSpecRequest,
  PollTaskRequest,
  PollTaskResponse,
  PutExternalEventDefRequest,
  PutStructDefRequest,
  PutTaskDefRequest,
  PutWfSpecRequest,
  PutWorkflowEventDefRequest,
  RegisterTaskWorkerRequest,
  RegisterTaskWorkerResponse,
  ReportTaskRun,
  ScheduledTask,
} from '../proto/service'
import { TaskDefId, WfSpecId } from '../proto/object_id'

/**
 * An in-process LittleHorse server for worker tests.
 *
 * The worker's hardest behaviors are protocol behaviors — polling, rebalance,
 * reconnect, not double-reporting — and they can only be observed from the
 * other end of a real connection. This is a scriptable stand-in that speaks
 * the actual gRPC wire protocol on an ephemeral port, so those behaviors are
 * testable deterministically and without Docker.
 *
 * It is NOT a substitute for integration tests against `lh-standalone`
 * (PARITY_PLAN.md tier 2): it proves what the *client* does, not what the
 * real server does.
 */

type MessageType<T extends object> = {
  toBinary(message: T): Uint8Array
  fromBinary(bytes: Uint8Array): T
  create(): T
}

/** Builds a grpc-js method definition backed by protobuf-ts serializers. */
function method<I extends object, O extends object>(
  name: string,
  request: MessageType<I>,
  response: MessageType<O>,
  streaming: { requestStream: boolean; responseStream: boolean } = { requestStream: false, responseStream: false }
) {
  return {
    path: `/littlehorse.LittleHorse/${name}`,
    requestStream: streaming.requestStream,
    responseStream: streaming.responseStream,
    requestSerialize: (value: I) => Buffer.from(request.toBinary(value)),
    requestDeserialize: (bytes: Buffer) => request.fromBinary(bytes),
    responseSerialize: (value: O) => Buffer.from(response.toBinary(value)),
    responseDeserialize: (bytes: Buffer) => response.fromBinary(bytes),
  }
}

const SERVICE_DEFINITION = {
  registerTaskWorker: method('RegisterTaskWorker', RegisterTaskWorkerRequest, RegisterTaskWorkerResponse),
  pollTask: method('PollTask', PollTaskRequest, PollTaskResponse, { requestStream: true, responseStream: true }),
  reportTask: method('ReportTask', ReportTaskRun, Empty),
  getTaskDef: method('GetTaskDef', TaskDefId, TaskDef),
  putTaskDef: method('PutTaskDef', PutTaskDefRequest, TaskDef),
  putStructDef: method('PutStructDef', PutStructDefRequest, StructDef),
  putWfSpec: method('PutWfSpec', PutWfSpecRequest, WfSpec),
  getLatestWfSpec: method('GetLatestWfSpec', GetLatestWfSpecRequest, WfSpec),
  getWfSpec: method('GetWfSpec', WfSpecId, WfSpec),
  putExternalEventDef: method('PutExternalEventDef', PutExternalEventDefRequest, ExternalEventDef),
  putWorkflowEventDef: method('PutWorkflowEventDef', PutWorkflowEventDefRequest, WorkflowEventDef),
} as unknown as grpc.ServiceDefinition

export interface FakeServerOptions {
  /**
   * Hosts returned from RegisterTaskWorker. Defaults to this server itself,
   * so a worker connects straight back. Change it mid-test to drive a
   * rebalance.
   */
  hosts?: Array<{ host: string; port: number }>
  /** When set, GetTaskDef fails with NOT_FOUND. */
  taskDefMissing?: boolean
  /** When set, PutTaskDef fails with ALREADY_EXISTS. */
  taskDefAlreadyExists?: boolean
  /** Fails the first N ReportTask calls, to exercise report retries. */
  failReportTaskTimes?: number
  /** When set, GetLatestWfSpec / GetWfSpec fail with NOT_FOUND. */
  wfSpecMissing?: boolean
}

export class FakeLHServer {
  private server?: grpc.Server
  private boundPort = 0

  /** Every ReportTaskRun the server received, in arrival order. */
  readonly reportedTasks: ReportTaskRun[] = []
  /** Every PollTaskRequest received, in arrival order. */
  readonly pollRequests: PollTaskRequest[] = []
  /** Every RegisterTaskWorker (heartbeat) request received. */
  readonly registerRequests: RegisterTaskWorkerRequest[] = []
  readonly putTaskDefRequests: PutTaskDefRequest[] = []
  readonly putStructDefRequests: PutStructDefRequest[] = []
  readonly putWfSpecRequests: PutWfSpecRequest[] = []
  readonly putExternalEventDefRequests: PutExternalEventDefRequest[] = []
  readonly putWorkflowEventDefRequests: PutWorkflowEventDefRequest[] = []
  readonly getLatestWfSpecRequests: GetLatestWfSpecRequest[] = []
  readonly getWfSpecRequests: WfSpecId[] = []

  /** Tasks handed to the next poller(s). */
  private readonly pending: ScheduledTask[] = []
  private reportFailuresLeft: number
  private openPollStreams = new Set<grpc.ServerDuplexStream<PollTaskRequest, PollTaskResponse>>()
  private waitingPollers: Array<grpc.ServerDuplexStream<PollTaskRequest, PollTaskResponse>> = []

  constructor(private options: FakeServerOptions = {}) {
    this.reportFailuresLeft = options.failReportTaskTimes ?? 0
  }

  get port(): number {
    return this.boundPort
  }

  async start(): Promise<number> {
    this.server = new grpc.Server()
    this.server.addService(SERVICE_DEFINITION, {
      registerTaskWorker: this.handleRegisterTaskWorker,
      pollTask: this.handlePollTask,
      reportTask: this.handleReportTask,
      getTaskDef: this.handleGetTaskDef,
      putTaskDef: this.handlePutTaskDef,
      putStructDef: this.handlePutStructDef,
      putWfSpec: this.handlePutWfSpec,
      getLatestWfSpec: this.handleGetLatestWfSpec,
      getWfSpec: this.handleGetWfSpec,
      putExternalEventDef: this.handlePutExternalEventDef,
      putWorkflowEventDef: this.handlePutWorkflowEventDef,
    })

    this.boundPort = await new Promise<number>((resolve, reject) => {
      this.server!.bindAsync('127.0.0.1:0', grpc.ServerCredentials.createInsecure(), (err, port) =>
        err ? reject(err) : resolve(port)
      )
    })
    return this.boundPort
  }

  async stop(): Promise<void> {
    const server = this.server
    if (!server) return
    this.server = undefined
    for (const stream of this.openPollStreams) stream.end()
    this.openPollStreams.clear()
    this.waitingPollers = []
    await new Promise<void>(resolve => server.tryShutdown(() => resolve()))
  }

  /**
   * Breaks all open poll streams with an UNAVAILABLE status, as a server
   * going away mid-poll would. Emitting the error is what actually reaches
   * the client — `destroy()` alone is silent on the wire.
   */
  dropPollStreams(): void {
    for (const stream of this.openPollStreams) {
      stream.emit('error', {
        code: grpc.status.UNAVAILABLE,
        details: 'simulated connection loss',
      })
    }
    this.openPollStreams.clear()
    this.waitingPollers = []
  }

  setOptions(options: Partial<FakeServerOptions>): void {
    this.options = { ...this.options, ...options }
  }

  /**
   * Queues a task. If a poller is already waiting, it is delivered
   * immediately — PollTask is a long poll, so a request stays outstanding
   * until work exists.
   */
  enqueueTask(task: ScheduledTask): void {
    this.pending.push(task)
    this.deliverPending()
  }

  /** Hands queued tasks to waiting pollers, one task per outstanding request. */
  private deliverPending(): void {
    while (this.pending.length > 0 && this.waitingPollers.length > 0) {
      const stream = this.waitingPollers.shift()!
      if (stream.destroyed || !this.openPollStreams.has(stream)) continue
      stream.write(PollTaskResponse.create({ result: this.pending.shift()! }))
    }
  }

  private hosts(): Array<{ host: string; port: number }> {
    return this.options.hosts ?? [{ host: '127.0.0.1', port: this.boundPort }]
  }

  private handleRegisterTaskWorker = (
    call: grpc.ServerUnaryCall<RegisterTaskWorkerRequest, RegisterTaskWorkerResponse>,
    callback: grpc.sendUnaryData<RegisterTaskWorkerResponse>
  ): void => {
    this.registerRequests.push(call.request)
    callback(
      null,
      RegisterTaskWorkerResponse.create({
        yourHosts: this.hosts().map(h => ({ host: h.host, port: h.port })),
      })
    )
  }

  private handlePollTask = (call: grpc.ServerDuplexStream<PollTaskRequest, PollTaskResponse>): void => {
    this.openPollStreams.add(call)
    call.on('data', (request: PollTaskRequest) => {
      this.pollRequests.push(request)
      // Each request is an outstanding ask for one task; park it until work
      // is available rather than replying empty.
      this.waitingPollers.push(call)
      this.deliverPending()
    })
    const forget = () => {
      this.openPollStreams.delete(call)
      this.waitingPollers = this.waitingPollers.filter(stream => stream !== call)
    }
    call.on('end', () => {
      forget()
      call.end()
    })
    call.on('error', forget)
    call.on('close', forget)
  }

  private handleReportTask = (
    call: grpc.ServerUnaryCall<ReportTaskRun, Empty>,
    callback: grpc.sendUnaryData<Empty>
  ): void => {
    if (this.reportFailuresLeft > 0) {
      this.reportFailuresLeft--
      callback({ code: grpc.status.UNAVAILABLE, details: 'simulated report failure' })
      return
    }
    this.reportedTasks.push(call.request)
    callback(null, Empty.create())
  }

  private handleGetTaskDef = (
    call: grpc.ServerUnaryCall<TaskDefId, TaskDef>,
    callback: grpc.sendUnaryData<TaskDef>
  ): void => {
    if (this.options.taskDefMissing) {
      callback({ code: grpc.status.NOT_FOUND, details: 'TaskDef not found' })
      return
    }
    callback(null, TaskDef.create({ id: { name: call.request.name } }))
  }

  private handlePutTaskDef = (
    call: grpc.ServerUnaryCall<PutTaskDefRequest, TaskDef>,
    callback: grpc.sendUnaryData<TaskDef>
  ): void => {
    this.putTaskDefRequests.push(call.request)
    if (this.options.taskDefAlreadyExists) {
      callback({ code: grpc.status.ALREADY_EXISTS, details: 'TaskDef already exists' })
      return
    }
    callback(null, TaskDef.create({ id: { name: call.request.name }, inputVars: call.request.inputVars }))
  }

  private handlePutStructDef = (
    call: grpc.ServerUnaryCall<PutStructDefRequest, StructDef>,
    callback: grpc.sendUnaryData<StructDef>
  ): void => {
    this.putStructDefRequests.push(call.request)
    callback(null, StructDef.create({ id: { name: call.request.name, version: 0 } }))
  }

  private handlePutWfSpec = (
    call: grpc.ServerUnaryCall<PutWfSpecRequest, WfSpec>,
    callback: grpc.sendUnaryData<WfSpec>
  ): void => {
    this.putWfSpecRequests.push(call.request)
    callback(null, WfSpec.create({ id: { name: call.request.name, majorVersion: 0, revision: 0 } }))
  }

  private handleGetLatestWfSpec = (
    call: grpc.ServerUnaryCall<GetLatestWfSpecRequest, WfSpec>,
    callback: grpc.sendUnaryData<WfSpec>
  ): void => {
    this.getLatestWfSpecRequests.push(call.request)
    if (this.options.wfSpecMissing) {
      callback({ code: grpc.status.NOT_FOUND, details: 'WfSpec not found' })
      return
    }
    callback(null, WfSpec.create({ id: { name: call.request.name, majorVersion: 0, revision: 0 } }))
  }

  private handleGetWfSpec = (
    call: grpc.ServerUnaryCall<WfSpecId, WfSpec>,
    callback: grpc.sendUnaryData<WfSpec>
  ): void => {
    this.getWfSpecRequests.push(call.request)
    if (this.options.wfSpecMissing) {
      callback({ code: grpc.status.NOT_FOUND, details: 'WfSpec not found' })
      return
    }
    callback(null, WfSpec.create({ id: call.request }))
  }

  private handlePutExternalEventDef = (
    call: grpc.ServerUnaryCall<PutExternalEventDefRequest, ExternalEventDef>,
    callback: grpc.sendUnaryData<ExternalEventDef>
  ): void => {
    this.putExternalEventDefRequests.push(call.request)
    callback(null, ExternalEventDef.create({ id: { name: call.request.name } }))
  }

  private handlePutWorkflowEventDef = (
    call: grpc.ServerUnaryCall<PutWorkflowEventDefRequest, WorkflowEventDef>,
    callback: grpc.sendUnaryData<WorkflowEventDef>
  ): void => {
    this.putWorkflowEventDefRequests.push(call.request)
    callback(null, WorkflowEventDef.create({ id: { name: call.request.name } }))
  }
}

/** Waits until `predicate` holds, polling briefly. Throws on timeout. */
export async function waitFor(predicate: () => boolean, timeoutMs = 5000, label = 'condition'): Promise<void> {
  const deadline = Date.now() + timeoutMs
  while (Date.now() < deadline) {
    if (predicate()) return
    await new Promise(resolve => setTimeout(resolve, 10))
  }
  throw new Error(`Timed out after ${timeoutMs}ms waiting for ${label}`)
}
