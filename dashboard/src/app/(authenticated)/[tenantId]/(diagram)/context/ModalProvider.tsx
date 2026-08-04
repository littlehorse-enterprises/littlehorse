import { FC, ProviderProps } from 'react'
import { Modals } from '../components/Modals'
import { ModalContext, ModalContextType } from './ModalContext'

export const ModalProvider: FC<ProviderProps<ModalContextType>> = ({ value, children }) => {
  return (
    <ModalContext.Provider value={value}>
      {children}
      <Modals />
    </ModalContext.Provider>
  )
}
