import { FC } from 'react'
import { Modal } from '../../context'
import { Edge } from './Edge'
import { NodeRun } from './NodeRun/NodeRun'
import { ExecuteWorkflowRun } from './ExecuteWorkflowRun'

export * from './Modals'

export type ModalComponent = FC<Modal>

export const ModalComponents = {
  edge: Edge,
  workflowRun: ExecuteWorkflowRun,
  nodeRunList: NodeRun,
} as const

export type ModalType = keyof typeof ModalComponents
