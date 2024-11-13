import { NodeRun } from 'littlehorse-client/proto'
import { NodeType } from '../../NodeTypes/extractNodes'
import {
  ExternalEventDefDetail,
  SleepDefDetail,
  StartThreadDefDetail,
  TaskDefDetail,
  UserTaskDefDetail,
  WaitForThreadDefDetail,
  WorkflowEventDefDetail,
} from './'
import { FC } from 'react'

export type AccordionNode = { nodeRun: NodeRun }
type AccordionNodes = {
  [key in NodeType]: FC<AccordionNode>
}

export const AccordionComponents: AccordionNodes = {
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
