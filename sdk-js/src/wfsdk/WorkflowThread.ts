import { Edge, Node, ThreadSpec } from '../proto/wf_spec'
import { toVariableAssignment } from './builder'

export type ThreadFunc = (wf: WorkflowThread) => void

type NodeContent = Node['node'] & { oneofKind: string }

/** R1 suffix table (rules.md); entries beyond shipped cases are the forward recipe. */
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

/**
 * Records thread-function calls into a ThreadSpec — runs once at compile
 * time, never executes a workflow. Rules: conformance/areas/wfsdk/rules.md.
 */
export class WorkflowThread {
  readonly spec: ThreadSpec = ThreadSpec.create()
  private lastNodeName: string
  private isActive = false

  constructor(func: ThreadFunc) {
    // R2: every thread begins with the automatic entrypoint node.
    const entrypointNodeName = '0-entrypoint-ENTRYPOINT'
    this.spec.nodes[entrypointNodeName] = Node.create({ node: { oneofKind: 'entrypoint', entrypoint: {} } })
    this.lastNodeName = entrypointNodeName
    this.isActive = true

    func(this)

    // R4: append the automatic exit unless the thread already ends on one.
    if (this.spec.nodes[this.lastNodeName].node.oneofKind !== 'exit') {
      this.addNode('exit', { oneofKind: 'exit', exit: { result: { oneofKind: undefined } } })
    }
    this.isActive = false
  }

  buildSpec(): ThreadSpec {
    // R7: variableDefs/interruptDefs are present (empty) even when unused —
    // ThreadSpec.create() initializes them.
    return this.spec
  }

  /** R9: one sleep node whose sleepLength is rawSeconds of the literal (R8). */
  sleepSeconds(seconds: number): void {
    this.checkIfIsActive()
    this.addNode('sleep', {
      oneofKind: 'sleep',
      sleep: { sleepLength: { oneofKind: 'rawSeconds', rawSeconds: toVariableAssignment(seconds) } },
    })
  }

  /** R1 (naming) + R3 (edge wiring): appends a node and advances the chain. */
  private addNode(name: string, content: NodeContent): string {
    this.checkIfIsActive()
    const nextNodeName = `${Object.keys(this.spec.nodes).length}-${name}-${NODE_NAME_SUFFIX[content.oneofKind]}`

    const feederNode = this.spec.nodes[this.lastNodeName]
    if (feederNode.node.oneofKind !== 'exit') {
      feederNode.outgoingEdges.push(Edge.create({ sinkNodeName: nextNodeName }))
    }

    this.spec.nodes[nextNodeName] = Node.create({ node: content })
    this.lastNodeName = nextNodeName
    return nextNodeName
  }

  private checkIfIsActive(): void {
    if (!this.isActive) {
      throw new Error('Cannot mutate a WorkflowThread outside its thread function.')
    }
  }
}
