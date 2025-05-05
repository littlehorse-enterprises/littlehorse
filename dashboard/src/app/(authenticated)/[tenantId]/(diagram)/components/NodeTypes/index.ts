import { Node, NodeRun } from 'littlehorse-client/proto'
import { ComponentType } from 'react'
import { NodeProps as NodeFlow } from 'reactflow'
import { Entrypoint } from './Entrypoint'
import { Exit } from './Exit'
import { ExternalEvent } from './ExternalEvent/ExternalEvent'
import { Nop } from './Nop'
import { Sleep } from './Sleep'
import { StartMultipleThreads } from './StartMultipleThreads'
import { StartThread } from './StartThread'
import { Task } from './Task'
import { UserTask } from './UserTask/UserTask'
import { WaitForThreads } from './WaitForThreads'
import { NodeType } from './extractNodes'
import { ThrowEvent } from './ThrowEvent'
import { WaitForCondition } from './WaitForCondition'

const nodeTypes: Record<NodeType, ComponentType<NodeProps>> = {
  ENTRYPOINT: Entrypoint,
  TASK: Task,
  NOP: Nop,
  EXIT: Exit,
  EXTERNAL_EVENT: ExternalEvent,
  START_THREAD: StartThread,
  WAIT_FOR_THREADS: WaitForThreads,
  WAIT_FOR_CONDITION: WaitForCondition,
  SLEEP: Sleep,
  USER_TASK: UserTask,
  START_MULTIPLE_THREADS: StartMultipleThreads,
  THROW_EVENT: ThrowEvent,
  UNKNOWN_NODE_TYPE: Nop,
}

export type NodeProps<T = Node> = NodeFlow<
  T & { nodeRun?: NodeRun; fade?: boolean; nodeNeedsToBeHighlighted?: boolean; nodeRunsList: [NodeRun] }
>
export default nodeTypes
