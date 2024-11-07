import { FC } from 'react'
import { Modal } from '../../context'
import { Edge } from './Edge'
import { ExternalEvent } from './ExternalEvent'
import { TaskRun } from './TaskRun'
import { UserTaskRun } from './UserTaskRun'
import { NodeRun } from './NodeRun/NodeRun'
export * from './Modals'

export type ModalComponent = FC<Modal>

export const ModalComponents = {
  taskRun: TaskRun,
  userTaskRun: UserTaskRun,
  edge: Edge,
  externalEvent: ExternalEvent,
  nodeRunList: NodeRun
} as const

export type ModalType = keyof typeof ModalComponents
