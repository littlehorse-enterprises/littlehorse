import { NodeRun, UserTaskNode } from 'littlehorse-client/proto'
import { FC } from 'react'
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

export type NodeRunCase<C extends NonNullable<NodeRun['nodeType']>['$case']> = Omit<NodeRun, 'nodeType'> & {
  nodeType: Extract<NodeRun['nodeType'], { $case: C }>
}

type AccordionNodeTypes = NonNullable<NodeRun['nodeType']>['$case']

export type AccordionNode<C extends AccordionNodeTypes> = { nodeRun: NodeRunCase<C>; userTaskNode?: UserTaskNode }

type AccordionNodes = {
  [key in AccordionNodeTypes]: FC<AccordionNode<key>>
}

export const AccordionComponents: AccordionNodes = {
  externalEvent: ExternalEventDefDetail,
  sleep: SleepDefDetail,
  startThread: StartThreadDefDetail,
  task: TaskDefDetail,
  throwEvent: WorkflowEventDefDetail,
  userTask: UserTaskDefDetail,
  waitForCondition: WaitForConditionDefDetail,
  waitForThreads: WaitForThreadDefDetail,

  // Not supported but required for type safety
  entrypoint: () => null,
  exit: () => null,
  startMultipleThreads: () => null,
  waitForChildWf: () => null,
  runChildWf: () => null,
} as const

export type AccordionConentType = keyof typeof AccordionComponents
