import { ComponentType } from 'react'
import { NodeProps } from 'reactflow'
import { NodeType } from '../extractNodes'
import { Entrypoint } from './Entrypoint'
import { Exit } from './Exit'
import { Nop } from './Nop'
import { Task } from './Task'
import { ExternalEvent } from './ExternalEvent'
import { UserTask } from './UserTask'
import { WaitForThreads } from './WaitForThreads'
import { StartThread } from './StartThread'
import { Sleep } from './Sleep'

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
  START_MULTIPLE_THREADS: WaitForThreads,
  THROW_EVENT: WaitForThreads,
  UNKNOWN_NODE_TYPE: WaitForThreads,
}

export default nodeTypes
