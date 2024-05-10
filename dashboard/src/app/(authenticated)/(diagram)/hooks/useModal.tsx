import { useContext } from 'react'
import { ModalContext } from '../context'

export const useModal = () => {
  const { modal, setModal, showModal, setShowModal } = useContext(ModalContext)
  return { modal, setModal, showModal, setShowModal }
}
