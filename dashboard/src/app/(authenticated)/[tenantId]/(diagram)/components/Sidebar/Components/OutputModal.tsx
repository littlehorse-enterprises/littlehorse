import { useCallback, useState } from 'react'
import { useModal } from '../../../hooks/useModal'
import { Expand } from 'lucide-react'
export const OutputModal = ({
  label,
  message = '',
  buttonText,
  externalHoverState,
}: {
  label: string
  message?: string
  buttonText?: string
  externalHoverState?: boolean
}) => {
  const { setModal, setShowModal } = useModal()
  const [isHovered, setIsHovered] = useState(false)

  const onClick = useCallback(() => {
    if (!message) return
    setModal({ type: 'output', data: { message, label: 'Output' } })
    setShowModal(true)
  }, [message, setModal, setShowModal])

  const shouldShowButton = externalHoverState !== undefined ? externalHoverState : isHovered

  return (
    <div
      className="ml-1 mt-1 grid grid-cols-2 hover:bg-gray-100 "
      onMouseEnter={() => setIsHovered(true)}
      onMouseLeave={() => setIsHovered(false)}
    >
      <div className=" text-sm font-bold">{label}</div>
      <div className=" flex items-center justify-between">
        <div className="truncate  text-xs text-slate-400 ">{buttonText ? buttonText : message}</div>
        {shouldShowButton && (
          <div>
            <Expand className=" cursor-pointer text-gray-500 hover:text-gray-700" size={14} onClick={onClick} />
          </div>
        )}
      </div>
    </div>
  )
}
