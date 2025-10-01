import { FC } from 'react'
import { Modal } from '../../context'
import { Edge } from './Edge'
import { ExecuteWorkflowRun } from './ExecuteWorkflowRun'
import { NodeRun } from './NodeRun/NodeRun'
import { VariableAssignmentModal } from './VariableAssignmentModal'

export * from './Modals'

export type ModalComponent = FC<Modal>

export const ModalComponents = {
  edge: Edge,
  workflowRun: ExecuteWorkflowRun,
  nodeRunList: NodeRun,
  variableAssignment: VariableAssignmentModal,
} as const

export type ModalType = keyof typeof ModalComponents
