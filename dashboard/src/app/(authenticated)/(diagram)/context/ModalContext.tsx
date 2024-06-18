import { Edge as EdgeProto, NodeRun, TaskRun, UserTaskNode, UserTaskRun } from 'littlehorse-client/proto'
import { Dispatch, FC, ProviderProps, SetStateAction, createContext } from 'react'
import { ModalType, Modals } from '../components/Modals'

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

export const ModalProvider: FC<ProviderProps<ModalContextType>> = ({ value, children }) => {
  return (
    <ModalContext.Provider value={value}>
      {children}
      <Modals />
    </ModalContext.Provider>
  )
}
