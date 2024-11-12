import { FC } from 'react'
import { Modal } from '../../context'
import { Edge } from './Edge'
import { NodeRun } from './NodeRun/NodeRun'

export * from './Modals'

export type ModalComponent = FC<Modal>

export const ModalComponents = {
  edge: Edge,
  nodeRunList: NodeRun,
} as const

export type ModalType = keyof typeof ModalComponents
