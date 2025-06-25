import {
  Node,
  EntrypointNode,
  ExitNode,
  ExternalEventNode,
  NopNode,
  SleepNode,
  StartMultipleThreadsNode,
  StartThreadNode,
  TaskNode,
  ThrowEventNode,
  UserTaskNode,
  WaitForConditionNode,
  WaitForThreadsNode,
} from 'littlehorse-client/proto'

export type NodeType =
  | 'ENTRYPOINT'
  | 'EXIT'
  | 'TASK'
  | 'EXTERNAL_EVENT'
  | 'START_THREAD'
  | 'WAIT_FOR_THREADS'
  | 'NOP'
  | 'SLEEP'
  | 'USER_TASK'
  | 'START_MULTIPLE_THREADS'
  | 'THROW_EVENT'
  | 'UNKNOWN_NODE_TYPE'
  | 'WAIT_FOR_CONDITION'

// Node type to specific node mapping
type NodeTypeMap = {
  ENTRYPOINT: EntrypointNode
  EXIT: ExitNode
  TASK: TaskNode
  EXTERNAL_EVENT: ExternalEventNode
  START_THREAD: StartThreadNode
  WAIT_FOR_THREADS: WaitForThreadsNode
  NOP: NopNode
  SLEEP: SleepNode
  USER_TASK: UserTaskNode
  START_MULTIPLE_THREADS: StartMultipleThreadsNode
  THROW_EVENT: ThrowEventNode
  WAIT_FOR_CONDITION: WaitForConditionNode
}

// Node property name mapping
type NodePropertyMap = {
  ENTRYPOINT: 'entrypoint'
  EXIT: 'exit'
  TASK: 'task'
  EXTERNAL_EVENT: 'externalEvent'
  START_THREAD: 'startThread'
  WAIT_FOR_THREADS: 'waitForThreads'
  NOP: 'nop'
  SLEEP: 'sleep'
  USER_TASK: 'userTask'
  START_MULTIPLE_THREADS: 'startMultipleThreads'
  THROW_EVENT: 'throwEvent'
  WAIT_FOR_CONDITION: 'waitForCondition'
}

// Create typed node type
export type NodeTypedOneOf<T extends NodeType> = T extends keyof NodeTypeMap
  ? Node & { [K in NodePropertyMap[T]]: NodeTypeMap[T] }
  : Node

// Node type detection mapping
const NODE_TYPE_DETECTORS: Record<keyof NodePropertyMap, (node: Node) => boolean> = {
  ENTRYPOINT: node => !!node.entrypoint,
  EXIT: node => !!node.exit,
  TASK: node => !!node.task,
  EXTERNAL_EVENT: node => !!node.externalEvent,
  START_THREAD: node => !!node.startThread,
  WAIT_FOR_THREADS: node => !!node.waitForThreads,
  NOP: node => !!node.nop,
  SLEEP: node => !!node.sleep,
  USER_TASK: node => !!node.userTask,
  START_MULTIPLE_THREADS: node => !!node.startMultipleThreads,
  THROW_EVENT: node => !!node.throwEvent,
  WAIT_FOR_CONDITION: node => !!node.waitForCondition,
}

export function getNodeType(
  node: Node
): { type: keyof NodeTypeMap; node: NodeTypedOneOf<keyof NodeTypeMap> } | { type: 'UNKNOWN_NODE_TYPE'; node: Node } {
  // Check each known node type
  for (const [type, detector] of Object.entries(NODE_TYPE_DETECTORS)) {
    if (detector(node)) {
      return {
        type: type as keyof NodeTypeMap,
        node: node as NodeTypedOneOf<keyof NodeTypeMap>,
      }
    }
  }

  return { type: 'UNKNOWN_NODE_TYPE', node }
}
