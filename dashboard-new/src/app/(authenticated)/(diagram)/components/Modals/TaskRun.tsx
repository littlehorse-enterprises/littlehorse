import { Dialog } from '@headlessui/react'
import { FC, useMemo, useState } from 'react'
import { Modal } from '../../context'
import { useModal } from '../../hooks/useModal'
import { ClipboardDocumentIcon } from '@heroicons/react/24/outline'

export const TaskRun: FC<Modal> = ({data}) => {
  const { showModal, setShowModal } = useModal()
  const [attemptIndex, setAttemptIndex] = useState(data.attempts.length - 1)
  const attempt = useMemo(() => data.attempts[attemptIndex], [attemptIndex, data.attempts])
  return (
    <Dialog open={showModal} className="relative z-50" onClose={() => setShowModal(false)}>
      <div className="fixed inset-0 bg-black/30" aria-hidden="true" />
      <div className="fixed inset-0 flex w-screen items-center justify-center p-4">
        <Dialog.Panel className="rounded bg-white p-2">
          <Dialog.Title className="">
            <h2 className="mb-2 text-lg font-bold">TaskRun</h2>
            <div className="flex items-center gap-2">
              <div className="font-bold">taskGuid:</div>
              <div className="flex gap-1  bg-gray-200 px-2 py-1">
                <span className="font-mono">{data.id?.taskGuid}</span>
                <ClipboardDocumentIcon className="h-6 w-6 fill-transparent stroke-blue-500" />
              </div>
            </div>
          </Dialog.Title>
          <Dialog.Description>
            <div className="mt-2">
              <div className="flex items-center gap-1">
                <div className="font-bold">Attempts</div>
                <div className="flex gap-1">
                  {data.attempts.map((_, i) => (
                    <button
                      key={`attempt-${i}`}
                      className={`px-2 border-blue-500 border-2 ${attemptIndex === i ? 'text-blue-500' : 'bg-blue-500 text-white'}`}
                      onClick={() => setAttemptIndex(i)}
                    >
                      {i}
                    </button>
                  ))}
                </div>
              </div>
              <div className="">
                <div className=""><span>Status</span> {attempt.status}</div>
              </div>
            </div>
          </Dialog.Description>
        </Dialog.Panel>
      </div>
    </Dialog>
  )
}
