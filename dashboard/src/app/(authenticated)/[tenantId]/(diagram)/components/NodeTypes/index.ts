import {
  ExternalEventNode,
  Node,
  NodeRun,
  UserTaskNode,
  EntrypointNode,
  ExitNode,
  TaskNode,
  StartThreadNode,
  WaitForThreadsNode,
  NopNode,
  SleepNode,
  StartMultipleThreadsNode,
  ThrowEventNode,
  WaitForConditionNode,
} from 'littlehorse-client/proto'
import { ComponentType } from 'react'
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
import { NodeTypes, NodeProps as XYNodeProps } from '@xyflow/react'
import { UserTaskNodeRun } from 'littlehorse-client/proto'

const nodeTypes: NodeTypes = {
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
  UNKNOWN_NODE_TYPE: WaitForThreads,
}

export type NodeProps = XYNodeProps & {
  data: {
    nodeRun?: NodeRun
    isFaded?: boolean
    nodeNeedsToBeHighlighted?: boolean
    nodeRunsList: [NodeRun]
    userTask?: UserTaskNode
    externalEvent?: ExternalEventNode
    entrypoint?: EntrypointNode
    exit?: ExitNode
    task?: TaskNode
    startThread?: StartThreadNode
    waitForThreads?: WaitForThreadsNode
    nop?: NopNode
    sleep?: SleepNode
    startMultipleThreads?: StartMultipleThreadsNode
    throwEvent?: ThrowEventNode
    waitForCondition?: WaitForConditionNode
  }
}
export default nodeTypes
