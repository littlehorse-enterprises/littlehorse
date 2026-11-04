import type { ZodTypeAny } from 'zod'
import {
  Edge,
  ExternalEventNode,
  FailureHandlerDef,
  FailureHandlerDef_LHFailureType,
  InterruptDef,
  Node,
  RunChildWfNode,
  StartMultipleThreadsNode,
  StartThreadNode,
  ThreadRetentionPolicy,
  ThreadSpec,
  UserTaskNode,
  WaitForThreadsNode,
  WaitForThreadsStrategy,
  WfRunVariableAccessLevel,
} from '../proto/wf_spec'
import { CorrelatedEventConfig } from '../proto/external_event'
import {
  ExponentialBackoffRetryPolicy,
  TaskNode,
  UTActionTrigger,
  UTActionTrigger_UTHook,
  VariableAssignment,
  VariableMutation,
  VariableMutationType,
} from '../proto/common_wfspec'
import { Comparator } from '../proto/type_definition'
import { LHErrorType, VariableType } from '../proto/common_enums'
import { isDoubleContext, toVariableAssignment } from './builder'
import { LHExpression, LHExpressionImpl, LHFormatString, LHValue } from './expressions'
import {
  ExternalEventNodeOutput,
  InterruptHandler,
  FixedSpawnedThreads,
  NodeOutput,
  SpawnedChildWf,
  SpawnedThread,
  SpawnedThreads,
  SpawnedThreadsIterator,
  TaskNodeOutput,
  ThrowEventNodeOutput,
  UserTaskOutput,
  WaitForConditionNodeOutput,
  WaitForThreadsNodeOutput,
} from './nodeOutputs'
import { InlineLHStructBuilder, LHStructBuilder } from './structBuilders'
import { arrayOf, LHType, mapOf, structOf, toTypeDefinition } from './types'
import { WfRunVariable } from './variables'
import type { Workflow } from './Workflow'

export type ThreadFunc = (wf: WorkflowThread) => void

export type IfElseBody = (body: WorkflowThread) => void

type NodeContent = Node['node'] & { oneofKind: string }

/** Maps a Node oneof kind to the suffix Java uses in generated node names. */
const NODE_NAME_SUFFIX: Record<string, string> = {
  entrypoint: 'ENTRYPOINT',
  exit: 'EXIT',
  task: 'TASK',
  externalEvent: 'EXTERNAL_EVENT',
  startThread: 'START_THREAD',
  waitForThreads: 'WAIT_FOR_THREADS',
  nop: 'NOP',
  sleep: 'SLEEP',
  userTask: 'USER_TASK',
  startMultipleThreads: 'START_MULTIPLE_THREADS',
  throwEvent: 'THROW_EVENT',
  waitForCondition: 'WAIT_FOR_CONDITION',
  runChildWf: 'RUN_CHILD_WF',
  waitForChildWf: 'WAIT_FOR_CHILD_WF',
}

/** Result of doIf(); allows chaining doElseIf()/doElse(). */
export class WorkflowIfStatement {
  private wasElseExecuted = false

  constructor(
    private readonly thread: WorkflowThread,
    readonly firstNopNodeName: string,
    readonly lastNopNodeName: string
  ) {}

  doElseIf(condition: LHExpression, body: IfElseBody): WorkflowIfStatement {
    return this.thread.doElseIfInternal(this, condition, body)
  }

  doElse(body: IfElseBody): void {
    if (this.wasElseExecuted) {
      throw new Error('doElse() can only be called once per WorkflowIfStatement.')
    }
    this.wasElseExecuted = true
    this.thread.doElseIfInternal(this, null, body)
  }
}

/** Optional settings for a variable declaration; see each declare* method. */
export interface WfRunVariableOptions<TDefault = unknown> {
  defaultValue?: TDefault
  required?: boolean
  searchable?: boolean
  searchableOn?: { fieldPath: string; fieldType: VariableType }[]
  masked?: boolean
  accessLevel?: WfRunVariableAccessLevel
}

/** Optional per-node settings for execute(); each key mirrors a chain method. */
export interface TaskNodeOptions {
  timeoutSeconds?: number
  retries?: number
  exponentialBackoff?: ExponentialBackoffRetryPolicy
}

/** Optional settings for waitForEvent(); each key mirrors a chain method. */
export interface ExternalEventNodeOptions {
  timeoutSeconds?: number
  correlationId?: LHValue
  maskCorrelationId?: boolean
  payloadSchema?: ZodTypeAny
  correlatedEventConfig?: CorrelatedEventConfig
}

/** Optional settings for assignUserTask(); notes and the exception name mirror their chain methods. */
export interface AssignUserTaskOptions {
  userId?: LHValue
  userGroup?: LHValue
  notes?: LHValue
  onCancellationException?: LHValue
}

const TASK_NODE_OPTION_KEYS = new Set(['timeoutSeconds', 'retries', 'exponentialBackoff'])
const ASSIGN_USER_TASK_OPTION_KEYS = new Set(['userId', 'userGroup', 'notes', 'onCancellationException'])

/**
 * Variadic calls can legally take a JSON-object argument, so an options bag
 * is only recognized as a plain object holding nothing but known keys.
 */
function isOptionsBag(value: unknown, knownKeys: Set<string>): boolean {
  if (typeof value !== 'object' || value === null) return false
  if (Object.getPrototypeOf(value) !== Object.prototype) return false
  const keys = Object.keys(value)
  return keys.length > 0 && keys.every(key => knownKeys.has(key))
}

/**
 * Records the calls made in a thread function and compiles them into a
 * ThreadSpec (mirrors Java WorkflowThreadImpl). This code never executes a
 * workflow: it runs once, at registration time, to build the graph.
 */
export class WorkflowThread {
  readonly spec: ThreadSpec = ThreadSpec.create()
  readonly wfRunVariables: WfRunVariable[] = []
  lastNodeName: string
  isActive = false
  private lastNodeCondition?: VariableAssignment
  private variableMutations: VariableMutation[] = []
  private retentionPolicy?: ThreadRetentionPolicy

  constructor(
    readonly name: string,
    readonly workflow: Workflow,
    func: ThreadFunc
  ) {
    const entrypointNodeName = '0-entrypoint-ENTRYPOINT'
    this.spec.nodes[entrypointNodeName] = Node.create({ node: { oneofKind: 'entrypoint', entrypoint: {} } })
    this.lastNodeName = entrypointNodeName
    this.isActive = true
    workflow.threadsStack.push(this)

    func(this)

    if (this.spec.nodes[this.lastNodeName].node.oneofKind !== 'exit') {
      this.addNode('exit', { oneofKind: 'exit', exit: { result: { oneofKind: undefined } } })
    }
    this.isActive = false

    const retention = this.retentionPolicy ?? workflow.defaultThreadRetentionPolicy
    if (retention !== undefined) {
      this.spec.retentionPolicy = retention
    }
  }

  buildSpec(): ThreadSpec {
    this.spec.variableDefs = this.wfRunVariables.map(v => v.buildThreadVarDef())
    return this.spec
  }

  withRetentionPolicy(policy: ThreadRetentionPolicy): void {
    this.retentionPolicy = policy
  }

  // ---------------------------------------------------------------- variables

  /**
   * Each option delegates to the matching fluent method, so both styles run
   * the same validation and compile to the same proto.
   */
  private applyVariableOptions(variable: WfRunVariable, options?: WfRunVariableOptions): WfRunVariable {
    if (options === undefined) return variable
    if (options.required) variable.required()
    if (options.searchable) variable.searchable()
    for (const index of options.searchableOn ?? []) {
      variable.searchableOn(index.fieldPath, index.fieldType)
    }
    if (options.masked) variable.masked()
    if (options.accessLevel !== undefined) variable.withAccessLevel(options.accessLevel)
    if (options.defaultValue !== undefined) variable.withDefault(options.defaultValue)
    return variable
  }

  /**
   * Kept private in JS (Java's is public): inferring a type from a default
   * value reads badly and misroutes small integers that collide with
   * VariableType ordinals. The declare* methods are the public path.
   */
  private addVariable(name: string, typeOrDefaultVal: VariableType | unknown): WfRunVariable {
    this.checkIfIsActive()
    const wfRunVariable = WfRunVariable.createPrimitive(name, typeOrDefaultVal, this)
    this.wfRunVariables.push(wfRunVariable)
    return wfRunVariable
  }

  declareStr(name: string, options?: WfRunVariableOptions<string>): WfRunVariable {
    return this.applyVariableOptions(this.addVariable(name, VariableType.STR), options)
  }

  declareInt(name: string, options?: WfRunVariableOptions<number | bigint>): WfRunVariable {
    return this.applyVariableOptions(this.addVariable(name, VariableType.INT), options)
  }

  declareDouble(name: string, options?: WfRunVariableOptions<number>): WfRunVariable {
    return this.applyVariableOptions(this.addVariable(name, VariableType.DOUBLE), options)
  }

  declareBool(name: string, options?: WfRunVariableOptions<boolean>): WfRunVariable {
    return this.applyVariableOptions(this.addVariable(name, VariableType.BOOL), options)
  }

  declareBytes(name: string, options?: WfRunVariableOptions<Uint8Array>): WfRunVariable {
    return this.applyVariableOptions(this.addVariable(name, VariableType.BYTES), options)
  }

  declareTimestamp(name: string, options?: WfRunVariableOptions<Date>): WfRunVariable {
    return this.applyVariableOptions(this.addVariable(name, VariableType.TIMESTAMP), options)
  }

  declareJsonObj(name: string, options?: WfRunVariableOptions<Record<string, unknown>>): WfRunVariable {
    return this.applyVariableOptions(this.addVariable(name, VariableType.JSON_OBJ), options)
  }

  declareJsonArr(name: string, options?: WfRunVariableOptions<unknown[]>): WfRunVariable {
    return this.applyVariableOptions(this.addVariable(name, VariableType.JSON_ARR), options)
  }

  declareWfRunId(name: string, options?: WfRunVariableOptions): WfRunVariable {
    return this.applyVariableOptions(this.addVariable(name, VariableType.WF_RUN_ID), options)
  }

  /**
   * Declares a native, element-typed LH array (an `InlineArrayDef`) — distinct
   * from `declareJsonArr`, which is schemaless JSON.
   */
  declareArray(name: string, elementType: LHType, options?: Omit<WfRunVariableOptions, 'defaultValue'>): WfRunVariable {
    return this.applyVariableOptions(this.addTypedVariable(name, arrayOf(elementType)), options)
  }

  /** Declares a native LH map with typed keys and values (an `InlineMapDef`). */
  declareMap(
    name: string,
    keyType: LHType,
    valueType: LHType,
    options?: Omit<WfRunVariableOptions, 'defaultValue'>
  ): WfRunVariable {
    return this.applyVariableOptions(this.addTypedVariable(name, mapOf(keyType, valueType)), options)
  }

  /**
   * Declares a variable typed by a registered StructDef, referenced by name
   * (optionally pinned to a version) or by an `lhStruct(...)` zod schema.
   */
  declareStruct(
    name: string,
    structDef: string | ZodTypeAny,
    versionOrOptions?: number | (Omit<WfRunVariableOptions, 'defaultValue'> & { version?: number })
  ): WfRunVariable {
    const version = typeof versionOrOptions === 'number' ? versionOrOptions : versionOrOptions?.version
    const options = typeof versionOrOptions === 'number' ? undefined : versionOrOptions
    const type = typeof structDef === 'string' ? structOf(structDef, version) : structDef
    return this.applyVariableOptions(this.addTypedVariable(name, type), options)
  }

  private addTypedVariable(name: string, type: LHType): WfRunVariable {
    this.checkIfIsActive()
    const wfRunVariable = new WfRunVariable(name, toTypeDefinition(type), this)
    this.wfRunVariables.push(wfRunVariable)
    return wfRunVariable
  }

  // ------------------------------------------------------------------ structs

  /** Builds a value conforming to a registered StructDef. */
  buildStruct(structDefName: string, version?: number): LHStructBuilder {
    return new LHStructBuilder(structDefName, version)
  }

  /** Builds a schemaless (inline) struct value. */
  buildInlineStruct(): InlineLHStructBuilder {
    return new InlineLHStructBuilder()
  }

  // ------------------------------------------------------------------- tasks

  execute(taskName: string | WfRunVariable | LHFormatString, ...args: LHValue[]): TaskNodeOutput
  execute(taskName: string | WfRunVariable | LHFormatString, args: LHValue[], options: TaskNodeOptions): TaskNodeOutput
  execute(taskName: string | WfRunVariable | LHFormatString, ...argsOrOptions: unknown[]): TaskNodeOutput {
    this.checkIfIsActive()
    let args = argsOrOptions as LHValue[]
    let options: TaskNodeOptions | undefined
    if (
      argsOrOptions.length === 2 &&
      Array.isArray(argsOrOptions[0]) &&
      isOptionsBag(argsOrOptions[1], TASK_NODE_OPTION_KEYS)
    ) {
      args = argsOrOptions[0] as LHValue[]
      options = argsOrOptions[1] as TaskNodeOptions
    }
    let taskNode: TaskNode
    let nodeBaseName: string
    if (typeof taskName === 'string') {
      this.workflow.addTaskDefName(taskName)
      taskNode = this.createTaskNode({ oneofKind: 'taskDefId', taskDefId: { name: taskName } }, args)
      nodeBaseName = taskName
    } else if (taskName instanceof WfRunVariable) {
      taskNode = this.createTaskNode({ oneofKind: 'dynamicTask', dynamicTask: toVariableAssignment(taskName) }, args)
      nodeBaseName = taskName.name
    } else {
      taskNode = this.createTaskNode({ oneofKind: 'dynamicTask', dynamicTask: toVariableAssignment(taskName) }, args)
      nodeBaseName = taskName.format
    }
    const nodeName = this.addNode(nodeBaseName, { oneofKind: 'task', task: taskNode })
    let output = new TaskNodeOutput(nodeName, this)
    if (options?.retries !== undefined) output = output.withRetries(options.retries)
    if (options?.exponentialBackoff !== undefined) output = output.withExponentialBackoff(options.exponentialBackoff)
    if (options?.timeoutSeconds !== undefined) output = output.timeout(options.timeoutSeconds)
    return output
  }

  private createTaskNode(taskToExecute: TaskNode['taskToExecute'], args: LHValue[]): TaskNode {
    const taskNode = TaskNode.create({ taskToExecute })
    taskNode.variables = args.map(arg => toVariableAssignment(arg))
    if (this.workflow.defaultTaskTimeout !== undefined) {
      taskNode.timeoutSeconds = this.workflow.defaultTaskTimeout
    }
    taskNode.retries = this.workflow.defaultSimpleRetries
    if (this.workflow.defaultExponentialBackoff !== undefined) {
      taskNode.exponentialBackoff = this.workflow.defaultExponentialBackoff
    }
    return taskNode
  }

  overrideTaskRetries(node: TaskNodeOutput, retries: number): void {
    this.checkIfIsActive()
    const task = this.getTaskNodeOrThrow(node.nodeName)
    task.retries = retries
  }

  overrideTaskExponentialBackoffPolicy(node: TaskNodeOutput, policy: ExponentialBackoffRetryPolicy): void {
    this.checkIfIsActive()
    const task = this.getTaskNodeOrThrow(node.nodeName)
    task.exponentialBackoff = policy
  }

  addTimeoutToTaskNode(node: TaskNodeOutput, timeoutSeconds: number): void {
    this.checkIfIsActive()
    const task = this.getTaskNodeOrThrow(node.nodeName)
    task.timeoutSeconds = timeoutSeconds
  }

  private getTaskNodeOrThrow(nodeName: string): TaskNode {
    const node = this.getNodeOrThrow(nodeName)
    if (node.node.oneofKind !== 'task') {
      throw new Error('Impossible to not have task node here')
    }
    return node.node.task
  }

  // ------------------------------------------------------------- expressions

  condition(lhs: LHValue, comparator: Comparator, rhs: LHValue): LHExpression {
    return new LHExpressionImpl(lhs, { comparator }, rhs)
  }

  add(lhs: LHValue, rhs: LHValue): LHExpression {
    return new LHExpressionImpl(lhs, { mutation: VariableMutationType.ADD }, rhs)
  }

  subtract(lhs: LHValue, rhs: LHValue): LHExpression {
    return new LHExpressionImpl(lhs, { mutation: VariableMutationType.SUBTRACT }, rhs)
  }

  multiply(lhs: LHValue, rhs: LHValue): LHExpression {
    return new LHExpressionImpl(lhs, { mutation: VariableMutationType.MULTIPLY }, rhs)
  }

  divide(lhs: LHValue, rhs: LHValue): LHExpression {
    return new LHExpressionImpl(lhs, { mutation: VariableMutationType.DIVIDE }, rhs)
  }

  pow(base: LHValue, exponent: LHValue): LHExpression {
    return new LHExpressionImpl(base, { mutation: VariableMutationType.POW }, exponent)
  }

  extend(lhs: LHValue, rhs: LHValue): LHExpression {
    return new LHExpressionImpl(lhs, { mutation: VariableMutationType.EXTEND }, rhs)
  }

  removeIfPresent(lhs: LHValue, rhs: LHValue): LHExpression {
    return new LHExpressionImpl(lhs, { mutation: VariableMutationType.REMOVE_IF_PRESENT }, rhs)
  }

  removeIndex(lhs: LHValue, index: LHValue): LHExpression {
    return new LHExpressionImpl(lhs, { mutation: VariableMutationType.REMOVE_INDEX }, index)
  }

  removeKey(lhs: LHValue, key: LHValue): LHExpression {
    return new LHExpressionImpl(lhs, { mutation: VariableMutationType.REMOVE_KEY }, key)
  }

  format(format: string, ...args: LHValue[]): LHFormatString {
    return new LHFormatString(format, args)
  }

  mutate(lhsVar: WfRunVariable, type: VariableMutationType, rhs: LHValue): void {
    this.checkIfIsActive()
    const mutation = VariableMutation.create({
      lhsName: lhsVar.name,
      operation: type,
      rhsValue: {
        oneofKind: 'rhsAssignment',
        rhsAssignment: toVariableAssignment(rhs, isDoubleContext(lhsVar) ? VariableType.DOUBLE : undefined),
      },
    })
    if (lhsVar.jsonPathStr !== undefined) {
      mutation.lhsJsonPath = lhsVar.jsonPathStr
    }
    this.variableMutations.push(mutation)
  }

  // ------------------------------------------------------------ control flow

  doIf(condition: LHExpression | WfRunVariable, ifBody: IfElseBody): WorkflowIfStatement {
    this.checkIfIsActive()
    this.addNopNode()
    const firstNopNodeName = this.lastNodeName
    this.lastNodeCondition = toVariableAssignment(condition)

    ifBody(this)

    this.addNopNode()
    const lastNopNodeName = this.lastNodeName
    this.addOutgoingEdgeToNode(firstNopNodeName, Edge.create({ sinkNodeName: lastNopNodeName }))
    return new WorkflowIfStatement(this, firstNopNodeName, lastNopNodeName)
  }

  doIfElse(condition: LHExpression | WfRunVariable, ifBody: IfElseBody, elseBody: IfElseBody): void {
    this.checkIfIsActive()
    this.doIf(condition, ifBody).doElse(elseBody)
  }

  doElseIfInternal(
    ifStatement: WorkflowIfStatement,
    condition: LHExpression | null,
    body: IfElseBody
  ): WorkflowIfStatement {
    // Remove the unconditioned "else" edge from the first NOP node.
    const elseEdge = this.removeLastOutgoingEdgeFromNode(ifStatement.firstNopNodeName)

    const lastNodeOfParentThreadName = this.lastNodeName

    body(this)

    const lastNodeOfBodyName = this.lastNodeName

    if (lastNodeOfParentThreadName === lastNodeOfBodyName) {
      // No nodes were added by the body.
      const edgeToNopNode = Edge.create({
        sinkNodeName: ifStatement.lastNopNodeName,
        variableMutations: this.collectVariableMutations(),
      })
      if (condition !== null) {
        edgeToNopNode.edgeCondition = { oneofKind: 'condition', condition: toVariableAssignment(condition) }
      }
      this.addOutgoingEdgeToNode(ifStatement.firstNopNodeName, edgeToNopNode)
    } else {
      // Reroute the edge into the body to come from the first NOP node.
      const lastOutgoingEdge = this.removeLastOutgoingEdgeFromNode(lastNodeOfParentThreadName)
      const firstNodeOfBodyName = lastOutgoingEdge.sinkNodeName

      const edgeToBody = Edge.create({
        sinkNodeName: firstNodeOfBodyName,
        variableMutations: lastOutgoingEdge.variableMutations,
      })
      if (condition !== null) {
        edgeToBody.edgeCondition = { oneofKind: 'condition', condition: toVariableAssignment(condition) }
      }
      this.addOutgoingEdgeToNode(ifStatement.firstNopNodeName, edgeToBody)

      this.addOutgoingEdgeToNode(
        lastNodeOfBodyName,
        Edge.create({
          sinkNodeName: ifStatement.lastNopNodeName,
          variableMutations: this.collectVariableMutations(),
        })
      )
    }

    // Restore the "else" edge we removed, unless this call IS the else.
    if (condition !== null) {
      this.addOutgoingEdgeToNode(ifStatement.firstNopNodeName, elseEdge)
    }

    this.lastNodeName = lastNodeOfParentThreadName
    return ifStatement
  }

  doWhile(condition: LHExpression, whileBody: ThreadFunc): void {
    this.checkIfIsActive()
    if (!(condition instanceof LHExpressionImpl) || condition.comparator === undefined) {
      throw new Error('doWhile() requires a comparator condition (e.g. from wf.condition())')
    }

    this.addNopNode()
    const treeRootNodeName = this.lastNodeName
    this.lastNodeCondition = toVariableAssignment(condition)

    whileBody(this)

    this.addNopNode()
    const treeLastNodeName = this.lastNodeName

    // Sideways path from root directly to last (loop never entered).
    this.addOutgoingEdgeToNode(
      treeRootNodeName,
      Edge.create({
        sinkNodeName: treeLastNodeName,
        edgeCondition: { oneofKind: 'condition', condition: toVariableAssignment(condition.reverse()) },
      })
    )

    // Path from last back to root (loop repeats).
    this.addOutgoingEdgeToNode(
      treeLastNodeName,
      Edge.create({
        sinkNodeName: treeRootNodeName,
        edgeCondition: { oneofKind: 'condition', condition: toVariableAssignment(condition) },
      })
    )
  }

  sleepSeconds(seconds: LHValue): void {
    this.checkIfIsActive()
    this.addNode('sleep', {
      oneofKind: 'sleep',
      sleep: { sleepLength: { oneofKind: 'rawSeconds', rawSeconds: toVariableAssignment(seconds) } },
    })
  }

  sleepUntil(timestamp: WfRunVariable): void {
    this.checkIfIsActive()
    this.addNode('sleep', {
      oneofKind: 'sleep',
      sleep: { sleepLength: { oneofKind: 'timestamp', timestamp: toVariableAssignment(timestamp) } },
    })
  }

  complete(output?: LHValue): void {
    this.checkIfIsActive()
    const result =
      output === undefined
        ? ({ oneofKind: undefined } as const)
        : ({ oneofKind: 'returnContent', returnContent: toVariableAssignment(output) } as const)
    this.addNode('complete', { oneofKind: 'exit', exit: { result } })
  }

  fail(failureName: string, message?: string, output?: LHValue): void {
    this.checkIfIsActive()
    this.addNode(failureName, {
      oneofKind: 'exit',
      exit: {
        result: {
          oneofKind: 'failureDef',
          failureDef: {
            failureName,
            message: message ?? '',
            content: output === undefined ? undefined : toVariableAssignment(output),
          },
        },
      },
    })
  }

  waitForCondition(condition: LHExpression): WaitForConditionNodeOutput {
    this.checkIfIsActive()
    const nodeName = this.addNode('wait-for-condition', {
      oneofKind: 'waitForCondition',
      waitForCondition: {
        nodeCondition: { oneofKind: 'condition', condition: toVariableAssignment(condition) },
      },
    })
    return new WaitForConditionNodeOutput(nodeName, this)
  }

  // ---------------------------------------------------------- external events

  waitForEvent(externalEventDefName: string, options?: ExternalEventNodeOptions): ExternalEventNodeOutput {
    this.checkIfIsActive()
    this.workflow.addExternalEventDefName(externalEventDefName)
    const waitNode = ExternalEventNode.create({ externalEventDefId: { name: externalEventDefName } })
    const nodeName = this.addNode(externalEventDefName, { oneofKind: 'externalEvent', externalEvent: waitNode })
    let output = new ExternalEventNodeOutput(nodeName, externalEventDefName, this)
    if (options === undefined) return output
    if (options.maskCorrelationId !== undefined && options.correlationId === undefined) {
      throw new Error('maskCorrelationId requires correlationId')
    }
    if (options.timeoutSeconds !== undefined) output = output.timeout(options.timeoutSeconds)
    if (options.correlationId !== undefined)
      output = output.withCorrelationId(options.correlationId, options.maskCorrelationId)
    if (options.payloadSchema !== undefined) output = output.registeredAs(options.payloadSchema)
    if (options.correlatedEventConfig !== undefined)
      output = output.withCorrelatedEventConfig(options.correlatedEventConfig)
    return output
  }

  addTimeoutToExtEvtNode(node: ExternalEventNodeOutput, timeoutSeconds: number): void {
    this.checkIfIsActive()
    const evt = this.getExternalEventNodeOrThrow(node.nodeName)
    evt.timeoutSeconds = VariableAssignment.create({
      source: { oneofKind: 'literalValue', literalValue: { value: { oneofKind: 'int', int: String(timeoutSeconds) } } },
    })
  }

  addCorrelationIdToExtEvtNode(node: ExternalEventNodeOutput, correlationId: LHValue, mask?: boolean): void {
    const evt = this.getExternalEventNodeOrThrow(node.nodeName)
    const shouldMask =
      mask !== undefined ? mask : correlationId instanceof WfRunVariable && correlationId.typeDef.masked
    evt.correlationKey = toVariableAssignment(correlationId)
    evt.maskCorrelationKey = shouldMask
  }

  private getExternalEventNodeOrThrow(nodeName: string): ExternalEventNode {
    const node = this.getNodeOrThrow(nodeName)
    if (node.node.oneofKind !== 'externalEvent') {
      throw new Error('Expected an EXTERNAL_EVENT node here')
    }
    return node.node.externalEvent
  }

  // ---------------------------------------------------------- workflow events

  throwEvent(
    workflowEventDefName: string,
    content: LHValue,
    options?: { payloadSchema?: ZodTypeAny }
  ): ThrowEventNodeOutput {
    this.checkIfIsActive()
    this.workflow.addWorkflowEventDefName(workflowEventDefName)
    this.addNode(`throw-${workflowEventDefName}`, {
      oneofKind: 'throwEvent',
      throwEvent: {
        eventDefId: { name: workflowEventDefName },
        content: toVariableAssignment(content),
      },
    })
    const output = new ThrowEventNodeOutput(workflowEventDefName, this)
    if (options?.payloadSchema !== undefined) output.registeredAs(options.payloadSchema)
    return output
  }

  // ------------------------------------------------------------ child threads

  spawnThread(threadFunc: ThreadFunc, threadName: string, inputVars?: Record<string, LHValue>): SpawnedThread {
    this.checkIfIsActive()
    const finalThreadName = this.workflow.addSubThread(threadName, threadFunc)

    const variables: Record<string, VariableAssignment> = {}
    for (const [key, value] of Object.entries(inputVars ?? {})) {
      variables[key] = toVariableAssignment(value)
    }
    const startThread = StartThreadNode.create({ threadSpecName: finalThreadName, variables })

    const nodeName = this.addNode(threadName, { oneofKind: 'startThread', startThread })
    const internalStartedThreadVar = this.addVariable(nodeName, VariableType.INT)
    // The output of a StartThreadNode is the number of the spawned ThreadRun.
    this.mutate(internalStartedThreadVar, VariableMutationType.ASSIGN, new NodeOutput(nodeName, this))
    return new SpawnedThread(this, finalThreadName, internalStartedThreadVar)
  }

  spawnThreadForEach(
    arrVar: WfRunVariable,
    threadName: string,
    threadFunc: ThreadFunc,
    inputVars?: Record<string, LHValue>
  ): SpawnedThreads {
    this.checkIfIsActive()
    const finalThreadName = this.workflow.addSubThread(threadName, threadFunc)

    const variables: Record<string, VariableAssignment> = {}
    for (const [key, value] of Object.entries(inputVars ?? {})) {
      variables[key] = toVariableAssignment(value)
    }
    const startMultipleThreads = StartMultipleThreadsNode.create({
      threadSpecName: finalThreadName,
      iterable: toVariableAssignment(arrVar),
      variables,
    })

    const nodeName = this.addNode(threadName, { oneofKind: 'startMultipleThreads', startMultipleThreads })
    const internalStartedThreadVar = this.addVariable(nodeName, VariableType.JSON_ARR)
    this.mutate(internalStartedThreadVar, VariableMutationType.ASSIGN, new NodeOutput(nodeName, this))
    return new SpawnedThreadsIterator(internalStartedThreadVar)
  }

  waitForThreads(threads: SpawnedThreads): WaitForThreadsNodeOutput {
    return this.addWaitForThreadsNode(threads, WaitForThreadsStrategy.WAIT_FOR_ALL)
  }

  waitForAnyOf(threads: SpawnedThreads): WaitForThreadsNodeOutput {
    return this.addWaitForThreadsNode(threads, WaitForThreadsStrategy.WAIT_FOR_ANY)
  }

  waitForFirstOf(threads: SpawnedThreads): WaitForThreadsNodeOutput {
    return this.addWaitForThreadsNode(threads, WaitForThreadsStrategy.WAIT_FOR_FIRST)
  }

  private addWaitForThreadsNode(threads: SpawnedThreads, strategy: WaitForThreadsStrategy): WaitForThreadsNodeOutput {
    this.checkIfIsActive()
    const waitNode = WaitForThreadsNode.create({ strategy })
    if (threads instanceof FixedSpawnedThreads) {
      waitNode.threadsToWaitFor = {
        oneofKind: 'threads',
        threads: {
          threads: threads.threads.map(t => ({
            threadRunNumber: toVariableAssignment(t.internalThreadVar),
          })),
        },
      }
    } else {
      waitNode.threadsToWaitFor = {
        oneofKind: 'threadList',
        threadList: toVariableAssignment(threads.internalStartedThreadVar),
      }
    }
    const nodeName = this.addNode('threads', { oneofKind: 'waitForThreads', waitForThreads: waitNode })
    return new WaitForThreadsNodeOutput(nodeName, this)
  }

  addPerThreadFailureHandler(
    node: WaitForThreadsNodeOutput,
    prefix: string,
    suffix: string,
    opts: { exceptionName?: string; anyErrors?: boolean; anyExceptions?: boolean; handler: ThreadFunc }
  ): void {
    this.checkIfIsActive()
    const threadName = this.workflow.addSubThread(`${prefix}-${node.nodeName}-${suffix}`, opts.handler)
    const handlerDef = FailureHandlerDef.create({ handlerSpecName: threadName })
    if (opts.exceptionName !== undefined) {
      handlerDef.failureToCatch = { oneofKind: 'specificFailure', specificFailure: opts.exceptionName }
    } else if (opts.anyErrors) {
      handlerDef.failureToCatch = {
        oneofKind: 'anyFailureOfType',
        anyFailureOfType: FailureHandlerDef_LHFailureType.FAILURE_TYPE_ERROR,
      }
    } else if (opts.anyExceptions) {
      handlerDef.failureToCatch = {
        oneofKind: 'anyFailureOfType',
        anyFailureOfType: FailureHandlerDef_LHFailureType.FAILURE_TYPE_EXCEPTION,
      }
    }
    const wfNode = this.getNodeOrThrow(node.nodeName)
    if (wfNode.node.oneofKind !== 'waitForThreads') {
      throw new Error('Per-thread failure handlers only apply to WAIT_FOR_THREADS nodes')
    }
    wfNode.node.waitForThreads.perThreadFailureHandlers.push(handlerDef)
  }

  // ---------------------------------------------------------- child workflows

  runWf(wfSpecName: string | LHValue, inputs?: Record<string, LHValue>): SpawnedChildWf {
    this.checkIfIsActive()
    const node = RunChildWfNode.create({ majorVersion: -1 })
    for (const [key, value] of Object.entries(inputs ?? {})) {
      node.inputs[key] = toVariableAssignment(value)
    }

    let nodeName: string
    if (typeof wfSpecName === 'string') {
      node.wfSpec = { oneofKind: 'wfSpecName', wfSpecName }
      this.workflow.addChildWfSpecName(wfSpecName)
      nodeName = this.addNode(`run-${wfSpecName}`, { oneofKind: 'runChildWf', runChildWf: node })
    } else {
      node.wfSpec = { oneofKind: 'wfSpecVar', wfSpecVar: toVariableAssignment(wfSpecName) }
      nodeName = this.addNode('run-child-wf', { oneofKind: 'runChildWf', runChildWf: node })
    }
    return new SpawnedChildWf(nodeName, this)
  }

  waitForChildWf(childWf: SpawnedChildWf): NodeOutput {
    this.checkIfIsActive()
    if (childWf.thread !== this) {
      throw new Error('Currently cannot wait for WfRun started in other thread')
    }
    const nodeName = this.addNode('wait', {
      oneofKind: 'waitForChildWf',
      waitForChildWf: {
        childWfRunId: VariableAssignment.create({
          source: { oneofKind: 'nodeOutput', nodeOutput: { nodeName: childWf.sourceNodeName } },
        }),
        childWfRunSourceNode: childWf.sourceNodeName,
      },
    })
    return new NodeOutput(nodeName, this)
  }

  // -------------------------------------------------------------- interrupts

  registerInterruptHandler(
    interruptName: string,
    handler: ThreadFunc,
    options?: { payloadSchema?: ZodTypeAny | null }
  ): InterruptHandler {
    this.checkIfIsActive()
    const threadName = this.workflow.addSubThread(`interrupt-${interruptName}`, handler)
    this.workflow.addExternalEventDefName(interruptName)
    this.spec.interruptDefs.push(
      InterruptDef.create({
        externalEventDefId: { name: interruptName },
        handlerSpecName: threadName,
      })
    )
    const interruptHandler = new InterruptHandler(interruptName, this)
    if (options !== undefined && 'payloadSchema' in options) {
      interruptHandler.withEventType(options.payloadSchema ?? null)
    }
    return interruptHandler
  }

  // -------------------------------------------------------- failure handling

  handleException(node: NodeOutput, exceptionName: string | null, handler: ThreadFunc): void {
    this.checkIfIsActive()
    // Java produces the literal string "null" when no exception name is given
    // ("exn-handler-" + nodeName + "-" + null) — keep wire compatibility.
    const threadName = this.workflow.addSubThread(`exn-handler-${node.nodeName}-${exceptionName}`, handler)
    const handlerDef = FailureHandlerDef.create({ handlerSpecName: threadName })
    if (exceptionName !== null) {
      handlerDef.failureToCatch = { oneofKind: 'specificFailure', specificFailure: exceptionName }
    } else {
      handlerDef.failureToCatch = {
        oneofKind: 'anyFailureOfType',
        anyFailureOfType: FailureHandlerDef_LHFailureType.FAILURE_TYPE_EXCEPTION,
      }
    }
    this.addFailureHandlerDef(handlerDef, node)
  }

  handleError(node: NodeOutput, error: LHErrorType | null, handler: ThreadFunc): void {
    this.checkIfIsActive()
    const errorName = error === null ? undefined : LHErrorType[error]
    const threadName = this.workflow.addSubThread(
      `exn-handler-${node.nodeName}-${errorName ?? 'FAILURE_TYPE_ERROR'}`,
      handler
    )
    const handlerDef = FailureHandlerDef.create({ handlerSpecName: threadName })
    if (errorName !== undefined) {
      handlerDef.failureToCatch = { oneofKind: 'specificFailure', specificFailure: errorName }
    } else {
      handlerDef.failureToCatch = {
        oneofKind: 'anyFailureOfType',
        anyFailureOfType: FailureHandlerDef_LHFailureType.FAILURE_TYPE_ERROR,
      }
    }
    this.addFailureHandlerDef(handlerDef, node)
  }

  handleAnyFailure(node: NodeOutput, handler: ThreadFunc): void {
    this.checkIfIsActive()
    const threadName = this.workflow.addSubThread(`exn-handler-${node.nodeName}-any-failure`, handler)
    this.addFailureHandlerDef(FailureHandlerDef.create({ handlerSpecName: threadName }), node)
  }

  private addFailureHandlerDef(handlerDef: FailureHandlerDef, node: NodeOutput): void {
    this.getNodeOrThrow(node.nodeName).failureHandlers.push(handlerDef)
  }

  // -------------------------------------------------------------- user tasks

  assignUserTask(userTaskDefName: string, options?: AssignUserTaskOptions): UserTaskOutput
  assignUserTask(userTaskDefName: string, userId: LHValue | null, userGroup: LHValue | null): UserTaskOutput
  assignUserTask(
    userTaskDefName: string,
    userIdOrOptions?: AssignUserTaskOptions | LHValue | null,
    maybeUserGroup?: LHValue | null
  ): UserTaskOutput {
    this.checkIfIsActive()
    let options: AssignUserTaskOptions | undefined
    let userId: LHValue | null
    let userGroup: LHValue | null
    if (
      maybeUserGroup === undefined &&
      (userIdOrOptions === undefined || isOptionsBag(userIdOrOptions, ASSIGN_USER_TASK_OPTION_KEYS))
    ) {
      options = userIdOrOptions as AssignUserTaskOptions | undefined
      userId = options?.userId ?? null
      userGroup = options?.userGroup ?? null
    } else {
      userId = (userIdOrOptions ?? null) as LHValue | null
      userGroup = maybeUserGroup ?? null
    }
    if (typeof userId === 'string' && userId.trim() === '') {
      throw new Error("UserId can't be empty")
    }
    if (typeof userGroup === 'string' && userGroup.trim() === '') {
      throw new Error("UserGroup can't be empty")
    }
    const utNode = UserTaskNode.create({ userTaskDefName })
    if (userId !== null && userId !== undefined) {
      utNode.userId = toVariableAssignment(userId)
    }
    if (userGroup !== null && userGroup !== undefined) {
      utNode.userGroup = toVariableAssignment(userGroup)
    }
    const nodeName = this.addNode(userTaskDefName, { oneofKind: 'userTask', userTask: utNode })
    const output = new UserTaskOutput(nodeName, this)
    if (options?.notes !== undefined) output.withNotes(options.notes)
    if (options?.onCancellationException !== undefined)
      output.withOnCancellationException(options.onCancellationException)
    return output
  }

  setUserTaskNotes(userTask: UserTaskOutput, notes: LHValue): void {
    this.getUserTaskNodeOrThrow(userTask.nodeName).notes = toVariableAssignment(notes)
  }

  setUserTaskOnCancellationException(userTask: UserTaskOutput, exceptionName: LHValue): void {
    this.getUserTaskNodeOrThrow(userTask.nodeName).onCancellationExceptionName = toVariableAssignment(exceptionName)
  }

  reassignUserTask(
    userTask: UserTaskOutput,
    userId: LHValue | null,
    userGroup: LHValue | null,
    deadlineSeconds: LHValue
  ): void {
    this.checkIfIsActive()
    this.checkUserTaskNotStale(userTask)
    const reassign: UTActionTrigger['action'] = {
      oneofKind: 'reassign',
      reassign: {
        userId: userId === null || userId === undefined ? undefined : toVariableAssignment(userId),
        userGroup: userGroup === null || userGroup === undefined ? undefined : toVariableAssignment(userGroup),
      },
    }
    this.addUserTaskAction(userTask, {
      action: reassign,
      hook: UTActionTrigger_UTHook.ON_TASK_ASSIGNED,
      delaySeconds: toVariableAssignment(deadlineSeconds),
    })
  }

  releaseToGroupOnDeadline(userTask: UserTaskOutput, deadlineSeconds: LHValue): void {
    this.checkIfIsActive()
    this.checkUserTaskNotStale(userTask)
    const utNode = this.getUserTaskNodeOrThrow(userTask.nodeName)
    if (utNode.userId === undefined) {
      throw new Error('The User Task is not assigned to any user')
    }
    if (utNode.userGroup === undefined) {
      throw new Error('The User Task is assigned to a user without a group.')
    }
    this.addUserTaskAction(userTask, {
      action: { oneofKind: 'reassign', reassign: { userGroup: utNode.userGroup } },
      hook: UTActionTrigger_UTHook.ON_TASK_ASSIGNED,
      delaySeconds: toVariableAssignment(deadlineSeconds),
    })
  }

  scheduleReminderTask(userTask: UserTaskOutput, delaySeconds: LHValue, taskDefName: string, ...args: LHValue[]): void {
    this.scheduleTaskAfterHelper(userTask, delaySeconds, taskDefName, UTActionTrigger_UTHook.ON_ARRIVAL, args)
  }

  scheduleReminderTaskOnAssignment(
    userTask: UserTaskOutput,
    delaySeconds: LHValue,
    taskDefName: string,
    ...args: LHValue[]
  ): void {
    this.scheduleTaskAfterHelper(userTask, delaySeconds, taskDefName, UTActionTrigger_UTHook.ON_TASK_ASSIGNED, args)
  }

  private scheduleTaskAfterHelper(
    userTask: UserTaskOutput,
    delaySeconds: LHValue,
    taskDefName: string,
    hook: UTActionTrigger_UTHook,
    args: LHValue[]
  ): void {
    this.checkIfIsActive()
    this.checkUserTaskNotStale(userTask)
    this.workflow.addTaskDefName(taskDefName)
    const taskNode = this.createTaskNode({ oneofKind: 'taskDefId', taskDefId: { name: taskDefName } }, args)
    this.addUserTaskAction(userTask, {
      action: { oneofKind: 'task', task: { task: taskNode, mutations: [] } },
      hook,
      delaySeconds: toVariableAssignment(delaySeconds),
    })
  }

  cancelUserTaskRunAfter(userTask: UserTaskOutput, delaySeconds: LHValue): void {
    this.checkIfIsActive()
    this.scheduleUserTaskCancellation(userTask, delaySeconds, UTActionTrigger_UTHook.ON_ARRIVAL)
  }

  cancelUserTaskRunAfterAssignment(userTask: UserTaskOutput, delaySeconds: LHValue): void {
    this.checkIfIsActive()
    this.scheduleUserTaskCancellation(userTask, delaySeconds, UTActionTrigger_UTHook.ON_TASK_ASSIGNED)
  }

  private scheduleUserTaskCancellation(
    userTask: UserTaskOutput,
    delaySeconds: LHValue,
    hook: UTActionTrigger_UTHook
  ): void {
    this.checkUserTaskNotStale(userTask)
    this.addUserTaskAction(userTask, {
      action: { oneofKind: 'cancel', cancel: {} },
      hook,
      delaySeconds: toVariableAssignment(delaySeconds),
    })
  }

  private addUserTaskAction(userTask: UserTaskOutput, trigger: Omit<UTActionTrigger, never>): void {
    this.getUserTaskNodeOrThrow(userTask.nodeName).actions.push(UTActionTrigger.create(trigger))
  }

  private checkUserTaskNotStale(userTask: UserTaskOutput): void {
    if (this.lastNodeName !== userTask.nodeName) {
      throw new Error('Tried to edit a stale User Task node!')
    }
  }

  private getUserTaskNodeOrThrow(nodeName: string): UserTaskNode {
    const node = this.getNodeOrThrow(nodeName)
    if (node.node.oneofKind !== 'userTask') {
      throw new Error('Expected a USER_TASK node here')
    }
    return node.node.userTask
  }

  // -------------------------------------------------------------- graph plumbing

  private addNopNode(): void {
    this.checkIfIsActive()
    this.addNode('nop', { oneofKind: 'nop', nop: {} })
  }

  private addNode(name: string, content: NodeContent): string {
    this.checkIfIsActive()
    const nextNodeName = `${Object.keys(this.spec.nodes).length}-${name}-${NODE_NAME_SUFFIX[content.oneofKind]}`

    const feederNode = this.getNodeOrThrow(this.lastNodeName)
    const edge = Edge.create({
      sinkNodeName: nextNodeName,
      variableMutations: this.collectVariableMutations(),
    })
    if (this.lastNodeCondition !== undefined) {
      edge.edgeCondition = { oneofKind: 'condition', condition: this.lastNodeCondition }
      this.lastNodeCondition = undefined
    }
    if (feederNode.node.oneofKind !== 'exit') {
      feederNode.outgoingEdges.push(edge)
    }

    this.spec.nodes[nextNodeName] = Node.create({ node: content })
    this.lastNodeName = nextNodeName
    return nextNodeName
  }

  private getNodeOrThrow(nodeName: string): Node {
    const node = this.spec.nodes[nodeName]
    if (node === undefined) {
      throw new Error(`Node ${nodeName} not found in thread spec`)
    }
    return node
  }

  private removeLastOutgoingEdgeFromNode(nodeName: string): Edge {
    const node = this.getNodeOrThrow(nodeName)
    const lastEdge = node.outgoingEdges.pop()
    if (lastEdge === undefined) {
      throw new Error(`Node ${nodeName} has no outgoing edges to remove`)
    }
    return lastEdge
  }

  private addOutgoingEdgeToNode(nodeName: string, edge: Edge): void {
    this.getNodeOrThrow(nodeName).outgoingEdges.push(edge)
  }

  private collectVariableMutations(): VariableMutation[] {
    const out = this.variableMutations
    this.variableMutations = []
    return out
  }

  private checkIfIsActive(): void {
    if (!this.isActive) {
      throw new Error('Using an inactive thread')
    }
  }
}
