import { NodeRun } from 'littlehorse-client/dist/proto/node_run'
import { Node } from 'littlehorse-client/dist/proto/wf_spec'
import { ComponentType } from 'react'
import { NodeProps as NodeFlow } from 'reactflow'
import { Entrypoint } from './Entrypoint'
import { Exit } from './Exit'
import { ExternalEvent } from './ExternalEvent'
import { Nop } from './Nop'
import { Sleep } from './Sleep'
import { StartMultipleThreads } from './StartMultipleThreads'
import { StartThread } from './StartThread'
import { Task } from './Task'
import { UserTask } from './UserTask/UserTask'
import { WaitForThreads } from './WaitForThreads'
import { NodeType } from './extractNodes'

const nodeTypes: Record<NodeType, ComponentType<NodeProps>> = {
  ENTRYPOINT: Entrypoint,
  TASK: Task,
  NOP: Nop,
  EXIT: Exit,
  EXTERNAL_EVENT: ExternalEvent,
  START_THREAD: StartThread,
  WAIT_FOR_THREADS: WaitForThreads,
  SLEEP: Sleep,
  USER_TASK: UserTask,
  START_MULTIPLE_THREADS: StartMultipleThreads,
  THROW_EVENT: WaitForThreads,
  UNKNOWN_NODE_TYPE: WaitForThreads,
}

export type NodeProps<T = Node> = NodeFlow<T & { nodeRun?: NodeRun; fade?: boolean }>
export default nodeTypes
