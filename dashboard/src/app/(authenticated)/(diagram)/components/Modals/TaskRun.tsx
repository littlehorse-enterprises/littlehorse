import { getVariableValue } from '@/app/utils'
import { Dialog } from '@headlessui/react'
import { ClipboardDocumentIcon } from '@heroicons/react/24/outline'
import { FC, useMemo, useState } from 'react'
import { Modal } from '../../context'
import { useModal } from '../../hooks/useModal'

export const TaskRun: FC<Modal> = ({ data }) => {
  const { showModal, setShowModal } = useModal()
  const [attemptIndex, setAttemptIndex] = useState(data.attempts.length - 1)
  const attempt = useMemo(() => data.attempts[attemptIndex], [attemptIndex, data.attempts])
  return (
    <Dialog open={showModal} className="relative z-50" onClose={() => setShowModal(false)}>
      <div className="fixed inset-0 bg-black/30" aria-hidden="true" />
      <div className="fixed inset-0 flex items-center justify-center p-4">
        <Dialog.Panel className="w-1/3 min-w-fit rounded bg-white p-2">
          <Dialog.Title className="mb-2 flex items-center justify-between">
            <h2 className="text-lg font-bold">TaskRun</h2>
            <div className="item-center flex gap-1 bg-gray-200 px-2 py-1">
              <span className="font-mono text-sm">{data.id?.taskGuid}</span>
              <ClipboardDocumentIcon className="h-4 w-4 fill-transparent stroke-blue-500" />
            </div>
          </Dialog.Title>
          <Dialog.Description>
            <div className="">
              <div className="flex items-center justify-between bg-green-200 p-2">
                <div className="flex items-center gap-1">
                  <div className="flex gap-1">
                    {data.attempts.reverse().map((_, i) => {
                      const index = data.attempts.length - i - 1
                      return (
                        <button
                          key={`attempt-${index}`}
                          className={`border-2 border-blue-500 px-2 ${attemptIndex === index ? 'text-blue-500' : 'bg-blue-500 text-white'}`}
                          onClick={() => setAttemptIndex(index)}
                        >
                          {index}
                        </button>
                      )
                    })}
                  </div>
                </div>
                <div className="">
                  <div className="">{attempt.status}</div>
                </div>
              </div>
              <div className="p-2">
                <div className="flex items-center gap-2">
                  <div className="font-bold">startTime:</div>
                  <div className="">{attempt.startTime}</div>
                </div>
                <div className="flex items-center gap-2">
                  <div className="font-bold">endTime:</div>
                  <div className="">{attempt.endTime}</div>
                </div>
              </div>
              {attempt.output && (
                <div className="mt-2 flex flex-col rounded bg-zinc-500 p-1 text-white">
                  <h3 className="font-bold">Output</h3>
                  <pre className="overflow-x-auto">{getVariableValue(attempt.output)}</pre>
                </div>
              )}
            </div>
          </Dialog.Description>
        </Dialog.Panel>
      </div>
    </Dialog>
  )
}
