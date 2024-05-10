import { TaskRun } from 'littlehorse-client/dist/proto/task_run'
import { Dispatch, SetStateAction, createContext } from 'react'
import { ModalType } from '../components/Modals'

export type Modal = {
  type: ModalType
  data: TaskRun
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
