import { FC } from 'react'
import { Modal } from '../../context'
import { Edge } from './Edge'
import { ExecuteWorkflowRun } from './ExecuteWorkflowRun'
import { VariableAssignmentModal } from './VariableAssignmentModal'

export * from './Modals'

export type ModalComponent = FC<Modal>

export const ModalComponents = {
  edge: Edge,
  workflowRun: ExecuteWorkflowRun,
  variableAssignment: VariableAssignmentModal,
} as const

export type ModalType = keyof typeof ModalComponents
