import { useContext } from 'react'
import { ModalContext } from '../context/ModalContext'

export const useModal = () => {
  const { modal, setModal, showModal, setShowModal } = useContext(ModalContext)
  return { modal, setModal, showModal, setShowModal }
}
