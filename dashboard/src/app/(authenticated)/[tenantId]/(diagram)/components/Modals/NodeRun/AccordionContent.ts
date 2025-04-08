import { NodeRun, UserTaskNode } from 'littlehorse-client/proto'
import { FC } from 'react'
import { NodeType } from '../../NodeTypes/extractAllNodes'
import {
  ExternalEventDefDetail,
  SleepDefDetail,
  StartThreadDefDetail,
  TaskDefDetail,
  UserTaskDefDetail,
  WaitForThreadDefDetail,
  WorkflowEventDefDetail,
} from './'
import { WaitForConditionDefDetail } from './WaitForConditionDefDetail'

export type AccordionNode = { nodeRun: NodeRun; userTaskNode?: UserTaskNode }
type AccordionNodes = {
  [key in NodeType]: FC<AccordionNode>
}

export const AccordionComponents: AccordionNodes = {
  WAIT_FOR_CONDITION: WaitForConditionDefDetail,
  ENTRYPOINT: TaskDefDetail,
  EXIT: TaskDefDetail,
  EXTERNAL_EVENT: ExternalEventDefDetail,
  NOP: TaskDefDetail,
  SLEEP: SleepDefDetail,
  START_MULTIPLE_THREADS: TaskDefDetail,
  START_THREAD: StartThreadDefDetail,
  TASK: TaskDefDetail,
  THROW_EVENT: WorkflowEventDefDetail,
  UNKNOWN_NODE_TYPE: TaskDefDetail,
  USER_TASK: UserTaskDefDetail,
  WAIT_FOR_THREADS: WaitForThreadDefDetail,
} as const

export type AccordionConentType = keyof typeof AccordionComponents
