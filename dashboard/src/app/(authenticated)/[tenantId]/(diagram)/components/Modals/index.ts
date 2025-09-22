import { FC } from 'react'
import { Modal } from '../../context'
import { Edge } from './Edge'
import { NodeRun } from './NodeRun/NodeRun'
import { ExecuteWorkflowRun } from './ExecuteWorkflowRun'
import { VariableAssigment } from './VariableAssigment'

export * from './Modals'

export type ModalComponent = FC<Modal>

export const ModalComponents = {
  edge: Edge,
  workflowRun: ExecuteWorkflowRun,
  nodeRunList: NodeRun,
  variableAssignment: VariableAssigment,
} as const

export type ModalType = keyof typeof ModalComponents
