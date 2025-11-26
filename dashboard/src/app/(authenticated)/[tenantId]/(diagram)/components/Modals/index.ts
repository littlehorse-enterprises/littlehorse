import { FC } from 'react'
import { Modal } from '../../context'
import { Edge } from './Edge'
import { ExecuteWorkflowRun } from './ExecuteWorkflowRun'
import { VariableAssignmentModal } from './VariableAssignmentModal'
import { MutationModal } from './MutationModal'
import { OutputModal } from './OutputModal'

export * from './Modals'

export type ModalComponent = FC<Modal>

export const ModalComponents = {
  edge: Edge,
  workflowRun: ExecuteWorkflowRun,
  variableAssignment: VariableAssignmentModal,
  mutation: MutationModal,
  output: OutputModal,
} as const

export type ModalType = keyof typeof ModalComponents
