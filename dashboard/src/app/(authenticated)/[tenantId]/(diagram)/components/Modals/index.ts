import { Edge } from './Edge'
import { ExecuteWorkflowRun } from './ExecuteWorkflowRun'
import { VariableAssignmentModal } from './VariableAssignmentModal'
import { MutationModal } from './MutationModal'
import { OutputModal } from './OutputModal'

export * from './Modals'

export const ModalComponents = {
  edge: Edge,
  workflowRun: ExecuteWorkflowRun,
  variableAssignment: VariableAssignmentModal,
  mutation: MutationModal,
  output: OutputModal,
} as const

export type ModalType = keyof typeof ModalComponents
