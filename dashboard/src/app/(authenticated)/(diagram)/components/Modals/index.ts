import { FC } from 'react'
import { Modal } from '../../context'
import { Edge } from './Edge'
import { ExternalEvent } from './ExternalEvent'
import { TaskRun } from './TaskRun'
import { UserTaskRun } from './UserTaskRun'
import { WorkflowEvent } from './WorkflowEvent'

export * from './Modals'

export type ModalComponent = FC<Modal>

export const ModalComponents = {
  taskRun: TaskRun,
  userTaskRun: UserTaskRun,
  edge: Edge,
  externalEvent: ExternalEvent,
  workflowEvent: WorkflowEvent
} as const

export type ModalType = keyof typeof ModalComponents
