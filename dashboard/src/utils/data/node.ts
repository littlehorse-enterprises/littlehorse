import { Node } from 'littlehorse-client/proto'

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

export const getNodeType = (node: Node): NodeType => {
  if (node['exit'] !== undefined) return 'EXIT'
  if (node['task'] !== undefined) return 'TASK'
  if (node['externalEvent'] !== undefined) return 'EXTERNAL_EVENT'
  if (node['startThread'] !== undefined) return 'START_THREAD'
  if (node['waitForThreads'] !== undefined) return 'WAIT_FOR_THREADS'
  if (node['waitForCondition'] !== undefined) return 'WAIT_FOR_CONDITION'
  if (node['nop'] !== undefined) return 'NOP'
  if (node['sleep'] !== undefined) return 'SLEEP'
  if (node['userTask'] !== undefined) return 'USER_TASK'
  if (node['entrypoint'] !== undefined) return 'ENTRYPOINT'
  if (node['startMultipleThreads'] !== undefined) return 'START_MULTIPLE_THREADS'
  if (node['throwEvent'] !== undefined) return 'THROW_EVENT'
  return 'UNKNOWN_NODE_TYPE'
}
