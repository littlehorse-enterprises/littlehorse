import { FC } from 'react'
import { Modal } from '../../context'
import { TaskRun } from './TaskRun'

export * from './Modals'

export type ModalComponent = FC<Modal>

export const ModalComponents = {
  taskRun: TaskRun,
} as const

export type ModalType = keyof typeof ModalComponents
