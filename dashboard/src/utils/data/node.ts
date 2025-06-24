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

export function getNodeType(node: Node):
  | { type: Extract<NodeType, 'TASK'>; node: Node & { task: TaskNode } }
  | { type: Extract<NodeType, 'EXTERNAL_EVENT'>; node: Node & { externalEvent: ExternalEventNode } }
  | { type: Extract<NodeType, 'ENTRYPOINT'>; node: Node & { entrypoint: EntrypointNode } }
  | { type: Extract<NodeType, 'EXIT'>; node: Node & { exit: ExitNode } }
  | { type: Extract<NodeType, 'START_THREAD'>; node: Node & { startThread: StartThreadNode } }
  | { type: Extract<NodeType, 'WAIT_FOR_THREADS'>; node: Node & { waitForThreads: WaitForThreadsNode } }
  | { type: Extract<NodeType, 'SLEEP'>; node: Node & { sleep: SleepNode } }
  | { type: Extract<NodeType, 'USER_TASK'>; node: Node & { userTask: UserTaskNode } }
  | {
      type: Extract<NodeType, 'START_MULTIPLE_THREADS'>
      node: Node & { startMultipleThreads: StartMultipleThreadsNode }
    }
  | { type: Extract<NodeType, 'NOP'>; node: Node & { nop: NopNode } }
  | { type: Extract<NodeType, 'THROW_EVENT'>; node: Node & { throwEvent: ThrowEventNode } }
  | { type: Extract<NodeType, 'WAIT_FOR_CONDITION'>; node: Node & { waitForCondition: WaitForConditionNode } }
  | { type: Extract<NodeType, 'UNKNOWN_NODE_TYPE'>; node: Node } {
  if (node.task) return { type: 'TASK', node: node as Node & { task: TaskNode } }
  if (node.externalEvent) return { type: 'EXTERNAL_EVENT', node: node as Node & { externalEvent: ExternalEventNode } }
  if (node.entrypoint) return { type: 'ENTRYPOINT', node: node as Node & { entrypoint: EntrypointNode } }
  if (node.exit) return { type: 'EXIT', node: node as Node & { exit: ExitNode } }
  if (node.startThread) return { type: 'START_THREAD', node: node as Node & { startThread: StartThreadNode } }
  if (node.waitForThreads)
    return { type: 'WAIT_FOR_THREADS', node: node as Node & { waitForThreads: WaitForThreadsNode } }
  if (node.sleep) return { type: 'SLEEP', node: node as Node & { sleep: SleepNode } }
  if (node.userTask) return { type: 'USER_TASK', node: node as Node & { userTask: UserTaskNode } }
  if (node.startMultipleThreads)
    return { type: 'START_MULTIPLE_THREADS', node: node as Node & { startMultipleThreads: StartMultipleThreadsNode } }
  if (node.nop) return { type: 'NOP', node: node as Node & { nop: NopNode } }
  if (node.throwEvent) return { type: 'THROW_EVENT', node: node as Node & { throwEvent: ThrowEventNode } }
  if (node.waitForCondition)
    return { type: 'WAIT_FOR_CONDITION', node: node as Node & { waitForCondition: WaitForConditionNode } }
  return { type: 'UNKNOWN_NODE_TYPE', node }
}

type GetNodeTypeReturn = ReturnType<typeof getNodeType>
export type NodeForType<T extends NodeType> = Extract<GetNodeTypeReturn, { type: T }>['node']
