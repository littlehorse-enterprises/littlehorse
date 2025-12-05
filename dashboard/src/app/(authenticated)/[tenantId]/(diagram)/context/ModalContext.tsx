import { createContext, Dispatch, FC, ProviderProps, SetStateAction } from 'react'
import { Modals, ModalType } from '../components/Modals'

export type Modal<T = any> = {
  type: ModalType
  data: T
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
