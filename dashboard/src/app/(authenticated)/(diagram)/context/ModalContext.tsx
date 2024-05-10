import { TaskRun } from 'littlehorse-client/dist/proto/task_run'
import { Dispatch, SetStateAction, createContext } from 'react'
import { ModalType } from '../components/Modals'
import { UserTaskRun } from 'littlehorse-client/dist/proto/user_tasks'

export type Modal = {
  type: ModalType
  data: TaskRun | UserTaskRun
}

export interface UserTaskModal extends Modal {
  data: UserTaskRun
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
