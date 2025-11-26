import { useCallback } from 'react'
import { useModal } from '../../../hooks/useModal'
import { Expand } from 'lucide-react'
export const OutputModal = ({ label, message = '' }: { label: string; message?: string }) => {
  const { setModal, setShowModal } = useModal()
  const onClick = useCallback(() => {
    if (!message) return
    setModal({ type: 'output', data: { message, label: 'Output' } })
    setShowModal(true)
  }, [message, setModal, setShowModal])
  return (
    <div className="ml-1 mt-1 grid grid-cols-2">
      <div className=" text-sm font-bold">{label}</div>
      <div className=" flex items-center justify-between">
        <div className="truncate  text-xs text-slate-400 ">{message}</div>
        <div>
          <Expand className=" cursor-pointer text-gray-500 hover:text-gray-700" size={14} onClick={onClick} />
        </div>
      </div>
    </div>
  )
}
