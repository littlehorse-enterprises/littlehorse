import { TaskRun } from 'littlehorse-client/dist/proto/task_run'
import { Dispatch, SetStateAction, createContext } from 'react'
import { ModalType } from '../components/Modals'
import { UserTaskRun } from 'littlehorse-client/dist/proto/user_tasks'
import { NodeRun } from 'littlehorse-client/dist/proto/node_run'
import { UserTaskNode } from 'littlehorse-client/dist/proto/wf_spec'
import { Edge as EdgeProto } from 'littlehorse-client/dist/proto/wf_spec'

export type Modal = {
  type: ModalType
  data: TaskRun | UserTaskRun | EdgeProto
  nodeRun?: NodeRun
  userTaskNode?: UserTaskNode
}

type ModalContextType = {
  modal: Modal | null
  setModal: Dispatch<SetStateAction<Modal | null>>
  showModal: boolean
  setShowModal: Dispatch<SetStateAction<boolean>>
}
export const ModalContext = createContext<ModalContextType>({
  modal: null,
  setModal: () => {},
  showModal: false,
  setShowModal: () => {},
})

export const ModalProvider = ModalContext.Provider
