import type { VariableType } from '../../proto/common_enums'
import { VariableType as VT } from '../../proto/common_enums'
import type { TaskNode, VariableAssignment, VariableMutation } from '../../proto/common_wfspec'
import { VariableMutationType } from '../../proto/common_wfspec'
import type { TaskDefId } from '../../proto/object_id'
import type { VariableValue } from '../../proto/variable'
import type { Edge, ExitNode, Node } from '../../proto/wf_spec'
import { ThreadSpec } from '../../proto/wf_spec'
import { LHMisconfigurationException } from '../exceptions'
import { toVariableValue } from '../../utils/variableValueConvert'
import type { LHFormatString } from '../lhFormatString'
import type { TaskNodeOutput } from '../taskNodeOutput'
import type { WfRunVariable } from '../wfRunVariable'
import type { WorkflowRhs } from '../workflowRhs'
import type { WorkflowThread } from '../workflowThread'
import { assignVariable } from './builderUtil'
import { LHFormatStringImpl } from './lhFormatStringImpl'
import { TaskNodeOutputImpl } from './taskNodeOutputImpl'
import { WfRunVariableImpl, primitiveTypeDef, structTypeDef } from './wfRunVariableImpl'

const ENTRYPOINT_NODE_NAME = '0-entrypoint-ENTRYPOINT'

export class WorkflowThreadImpl implements WorkflowThread {
  private readonly nodes: Record<string, Node> = {}
  private lastNodeName: string
  private variableMutations: VariableMutation[] = []
  private readonly wfRunVariables: WfRunVariableImpl[] = []
  private readonly variableNames = new Set<string>()
  private active = true

  readonly threadSpec: import('../../proto/wf_spec').ThreadSpec

  constructor(_wfName: string, fn: (thread: WorkflowThread) => void) {
    void _wfName
    this.lastNodeName = ENTRYPOINT_NODE_NAME
    this.nodes[this.lastNodeName] = {
      outgoingEdges: [],
      failureHandlers: [],
      node: { $case: 'entrypoint', value: {} },
    }
    fn(this)
    if (this.nodes[this.lastNodeName].node?.$case !== 'exit') {
      this.addExitNodeBare()
    }
    this.active = false
    this.threadSpec = ThreadSpec.fromPartial({
      nodes: this.nodes,
      variableDefs: this.wfRunVariables.map(v => v.toThreadVarDef()),
      interruptDefs: [],
    })
  }

  assignVariable(rhs: WorkflowRhs): VariableAssignment {
    return assignVariable(rhs, r => this.assignVariable(r))
  }

  declareStr(name: string, defaultValue?: WorkflowRhs): WfRunVariable {
    return this.declarePrimitive(name, VT.STR, defaultValue)
  }

  declareInt(name: string, defaultValue?: WorkflowRhs): WfRunVariable {
    return this.declarePrimitive(name, VT.INT, defaultValue)
  }

  declareDouble(name: string, defaultValue?: WorkflowRhs): WfRunVariable {
    return this.declarePrimitive(name, VT.DOUBLE, defaultValue)
  }

  declareBool(name: string, defaultValue?: WorkflowRhs): WfRunVariable {
    return this.declarePrimitive(name, VT.BOOL, defaultValue)
  }

  declareStruct(name: string, structDefName: string): WfRunVariable {
    this.assertActive()
    this.registerVariableName(name)
    const v = new WfRunVariableImpl(name, structTypeDef(structDefName), undefined, this)
    this.wfRunVariables.push(v)
    return v
  }

  declareJsonObj(name: string, defaultValue?: WorkflowRhs): WfRunVariable {
    this.assertActive()
    this.registerVariableName(name)
    let def: VariableValue | undefined
    if (defaultValue !== undefined) {
      def = toVariableValue(defaultValue)
      if (def.value?.$case !== 'jsonObj') {
        throw new LHMisconfigurationException(
          `Variable "${name}": declareJsonObj default must be a plain object (not array); got ${def.value?.$case ?? 'empty'}`
        )
      }
    }
    const v = new WfRunVariableImpl(name, primitiveTypeDef(VT.JSON_OBJ), def, this)
    this.wfRunVariables.push(v)
    return v
  }

  execute(taskName: string | WfRunVariable | LHFormatString, ...args: WorkflowRhs[]): TaskNodeOutput {
    this.assertActive()
    this.assertCanAppend()
    let taskNode: TaskNode
    let label: string
    if (typeof taskName === 'string') {
      label = taskName
      taskNode = this.buildTaskNode({ $case: 'taskDefId', value: { name: taskName } satisfies TaskDefId }, args)
    } else if (taskName instanceof WfRunVariableImpl) {
      label = taskName.name
      taskNode = this.buildTaskNode({ $case: 'dynamicTask', value: taskName.toAssignment() }, args)
    } else if (taskName instanceof LHFormatStringImpl) {
      label = taskName.template
      taskNode = this.buildTaskNode(
        { $case: 'dynamicTask', value: taskName.toAssignment(r => this.assignVariable(r)) },
        args
      )
    } else {
      throw new LHMisconfigurationException(
        'execute: task name must be a string, WfRunVariable, or result of thread.format()'
      )
    }
    const nodeName = this.addGraphNode(label, 'TASK', { $case: 'task', value: taskNode })
    return new TaskNodeOutputImpl(nodeName, this)
  }

  mutate(lhs: WfRunVariable, operation: VariableMutationType, rhs: WorkflowRhs): void {
    this.assertActive()
    if (!(lhs instanceof WfRunVariableImpl)) {
      throw new LHMisconfigurationException('mutate: lhs must be a variable declared on this thread (declare*)')
    }
    const impl = lhs
    if (impl.lhSelectors.length > 0) {
      throw new LHMisconfigurationException(
        'Mutating struct/JSON sub-fields via get() is not supported yet in sdk-js workflow builder'
      )
    }
    const mutation: VariableMutation = {
      lhsName: impl.name,
      lhsJsonPath: impl.jsonPathValue,
      operation,
      rhsValue: { $case: 'rhsAssignment', value: this.assignVariable(rhs) },
    }
    this.variableMutations.push(mutation)
  }

  format(template: string, ...args: WorkflowRhs[]): LHFormatString {
    this.assertActive()
    return new LHFormatStringImpl(
      template,
      args.map(a => this.assignVariable(a))
    )
  }

  complete(result?: WorkflowRhs): void {
    this.assertActive()
    this.assertCanAppend()
    const exit: ExitNode =
      result === undefined
        ? { result: undefined }
        : { result: { $case: 'returnContent', value: this.assignVariable(result) } }
    this.addGraphNode('complete', 'EXIT', { $case: 'exit', value: exit })
  }

  patchTaskNode(nodeName: string, fn: (t: TaskNode) => void): void {
    const n = this.nodes[nodeName]
    if (n?.node?.$case !== 'task') {
      throw new LHMisconfigurationException('patchTaskNode: not a task node')
    }
    const task = { ...n.node.value }
    fn(task)
    this.nodes[nodeName] = { ...n, node: { $case: 'task', value: task } }
  }

  private registerVariableName(name: string): void {
    if (this.variableNames.has(name)) {
      throw new LHMisconfigurationException(`Duplicate variable name: "${name}"`)
    }
    this.variableNames.add(name)
  }

  private declarePrimitive(name: string, expected: VariableType, defaultValue?: WorkflowRhs): WfRunVariable {
    this.assertActive()
    this.registerVariableName(name)
    let def: VariableValue | undefined
    if (defaultValue !== undefined) {
      if (expected === VT.DOUBLE && typeof defaultValue === 'number') {
        def = { value: { $case: 'double', value: defaultValue } }
      } else if (expected === VT.INT && typeof defaultValue === 'number' && Number.isInteger(defaultValue)) {
        def = { value: { $case: 'int', value: defaultValue } }
      } else {
        def = toVariableValue(defaultValue)
        const inferred = this.varTypeFromDefault(defaultValue)
        if (inferred !== expected) {
          throw new LHMisconfigurationException(`Variable "${name}": default type does not match declared type`)
        }
      }
    }
    const v = new WfRunVariableImpl(name, primitiveTypeDef(expected), def, this)
    this.wfRunVariables.push(v)
    return v
  }

  private varTypeFromDefault(v: unknown): VariableType {
    const vv = toVariableValue(v)
    switch (vv.value?.$case) {
      case 'str':
        return VT.STR
      case 'int':
        return VT.INT
      case 'double':
        return VT.DOUBLE
      case 'bool':
        return VT.BOOL
      default:
        throw new LHMisconfigurationException('Unsupported default value type for variable declaration')
    }
  }

  private buildTaskNode(taskToExecute: TaskNode['taskToExecute'], args: WorkflowRhs[]): TaskNode {
    return {
      taskToExecute,
      timeoutSeconds: 0,
      retries: 0,
      exponentialBackoff: undefined,
      variables: args.map(a => this.assignVariable(a)),
    }
  }

  private addExitNodeBare(): void {
    this.addGraphNode('exit', 'EXIT', { $case: 'exit', value: { result: undefined } })
  }

  private addGraphNode(
    userLabel: string,
    kind: 'TASK' | 'EXIT',
    body: { $case: 'task'; value: TaskNode } | { $case: 'exit'; value: ExitNode }
  ): string {
    this.assertActive()
    const nextNodeName = this.getNodeName(userLabel, kind)
    const mutations = this.takeMutations()
    const feeder = this.nodes[this.lastNodeName]
    if (feeder.node?.$case !== 'exit') {
      const edge: Edge = { sinkNodeName: nextNodeName, variableMutations: mutations }
      this.nodes[this.lastNodeName] = { ...feeder, outgoingEdges: [...feeder.outgoingEdges, edge] }
    }
    const node: Node =
      body.$case === 'task'
        ? { outgoingEdges: [], failureHandlers: [], node: { $case: 'task', value: body.value } }
        : { outgoingEdges: [], failureHandlers: [], node: { $case: 'exit', value: body.value } }
    this.nodes[nextNodeName] = node
    this.lastNodeName = nextNodeName
    return nextNodeName
  }

  private takeMutations(): VariableMutation[] {
    const out = this.variableMutations
    this.variableMutations = []
    return out
  }

  private getNodeName(humanName: string, kind: 'TASK' | 'EXIT'): string {
    return `${Object.keys(this.nodes).length}-${humanName}-${kind}`
  }

  private assertActive(): void {
    if (!this.active) {
      throw new LHMisconfigurationException('Workflow thread is not active')
    }
  }

  private assertCanAppend(): void {
    if (this.nodes[this.lastNodeName].node?.$case === 'exit') {
      throw new LHMisconfigurationException('Cannot add steps after complete()')
    }
  }
}
