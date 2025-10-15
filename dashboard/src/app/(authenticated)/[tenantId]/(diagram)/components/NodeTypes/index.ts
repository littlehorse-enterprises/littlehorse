import { NodeRun } from 'littlehorse-client/proto'
import { ComponentType } from 'react'
import { NodeProps as NodeFlow } from 'reactflow'
import { NodeRunCase } from '../Modals/NodeRun/AccordionContent'
import { Entrypoint } from './Entrypoint'
import { Exit } from './Exit'
import { ExternalEvent } from './ExternalEvent/ExternalEvent'
import { NodeType } from './extractNodes'
import { Nop } from './Nop'
import { Sleep } from './Sleep'
import { StartMultipleThreads } from './StartMultipleThreads'
import { StartThread } from './StartThread'
import { Task } from './Task'
import { ThrowEvent } from './ThrowEvent'
import { UserTask } from './UserTask/UserTask'
import { WaitForCondition } from './WaitForCondition'
import { WaitForThreads } from './WaitForThreads'

const nodeTypes: Record<NodeType, ComponentType<any>> = {
  entrypoint: Entrypoint,
  exit: Exit,
  externalEvent: ExternalEvent,
  nop: Nop,
  sleep: Sleep,
  startMultipleThreads: StartMultipleThreads,
  startThread: StartThread,
  task: Task,
  throwEvent: ThrowEvent,
  userTask: UserTask,
  waitForCondition: WaitForCondition,
  waitForThreads: WaitForThreads,
  runChildWf: Nop,
  waitForChildWf: Nop,
}

export type NodeProps<C extends NonNullable<NodeRun['nodeType']>['$case'] = 'entrypoint', T = unknown> = NodeFlow<
  T & { nodeRun?: NodeRunCase<C>; fade?: boolean; nodeNeedsToBeHighlighted?: boolean; nodeRunsList: [NodeRunCase<C>] }
>
export default nodeTypes
