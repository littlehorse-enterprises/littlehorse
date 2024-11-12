import {
  ExternalEventDefDetail,
  TaskDefDetail,
  SleepDefDetail,
  UserTaskDefDetail,
  WaitForThreadDefDetail,
  StartThreadDefDetail,
  WorkflowEventDefDetail
} from './'

export const AccordionComponents = {
  TASK: TaskDefDetail,
  USER_TASK: UserTaskDefDetail,
  EXTERNAL_EVENT: ExternalEventDefDetail,
  SLEEP: SleepDefDetail,
  START_THREAD: StartThreadDefDetail,
  WAIT_FOR_THREADS: WaitForThreadDefDetail,
  THROW_EVENT:WorkflowEventDefDetail
} as const

export type AccordionConentType = keyof typeof AccordionComponents
