import { createContext, Dispatch, SetStateAction } from 'react'
import type { ModalType } from '../components/Modals'

export type Modal<T = any> = {
  type: ModalType
  data: T
}

export type ModalContextType = {
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
