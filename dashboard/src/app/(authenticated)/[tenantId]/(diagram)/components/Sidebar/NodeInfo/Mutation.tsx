import { VariableMutation } from 'littlehorse-client/proto'
import { Info } from 'lucide-react'
import { FC, useCallback } from 'react'
import { useModal } from '../../../hooks/useModal'

export const Mutation: FC<{ mutation: VariableMutation }> = ({ mutation }) => {
  const { setModal, setShowModal } = useModal()
  const onClick = useCallback(() => {
    if (!mutation) return
    setModal({ type: 'mutation', data: mutation })
    setShowModal(true)
  }, [mutation, setModal, setShowModal])

  return (
    <div className="relative mb-1 mb-2 flex flex-col gap-1">
      <div className="flex flex-row items-center">
        <p className="flex-1 truncate font-mono text-blue-500">{mutation.lhsName}</p>
        <Info className="h-4 w-4 cursor-pointer" onClick={onClick} />
      </div>
    </div>
  )
}
