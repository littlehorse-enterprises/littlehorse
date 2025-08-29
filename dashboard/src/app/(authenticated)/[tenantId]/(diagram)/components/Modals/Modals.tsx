import { FC } from 'react'
import { ModalComponents } from '.'
import { useModal } from '../../hooks/useModal'
import { NodeRun } from 'littlehorse-client/proto'

export const Modals: FC = () => {
  const { modal } = useModal()
  if (!modal) return null
  const Component = ModalComponents[modal.type]
  return <Component {...modal} />
}
