import { getVariableValue } from '@/app/utils'
import { cn } from '@/components/utils'
import { Description, Dialog, DialogPanel, DialogTitle } from '@headlessui/react'
import { TaskRun as LHTaskRun, TaskAttempt } from 'littlehorse-client/proto'
import { ClipboardIcon } from 'lucide-react'
import { FC, useMemo, useState } from 'react'
import { Modal } from '../../context'
import { useModal } from '../../hooks/useModal'

export const TaskRun: FC<Modal> = ({ data }) => {
  const lhTaskRun = data as LHTaskRun
  const { showModal, setShowModal } = useModal()
  const [attemptIndex, setAttemptIndex] = useState(lhTaskRun.attempts.length - 1)
  const attempt = useMemo(() => lhTaskRun.attempts[attemptIndex], [attemptIndex, lhTaskRun.attempts])
  return (
    <Dialog open={showModal} className="relative z-50" onClose={() => setShowModal(false)}>
      <div className="fixed inset-0 bg-black/30" aria-hidden="true" />
      <div className="fixed inset-0 flex items-center justify-center p-4">
        <DialogPanel className="w-1/3 min-w-fit rounded bg-white p-2">
          <DialogTitle className="mb-2 flex items-center justify-between">
            <h2 className="text-lg font-bold">TaskRun</h2>
            <div className="item-center flex gap-1 bg-gray-200 px-2 py-1">
              <span className="font-mono text-sm">{lhTaskRun.id?.taskGuid}</span>
              <ClipboardIcon className="h-4 w-4 fill-transparent stroke-blue-500" />
            </div>
          </DialogTitle>
          <Description>
            <div className="">
              <div
                className={cn(
                  'flex items-center justify-between p-2',
                  attempt.exception || attempt.error ? 'bg-red-200' : 'bg-green-200'
                )}
              >
                <div className="flex items-center gap-1">
                  <div className="flex gap-1">
                    {lhTaskRun.attempts.reverse().map((_, i) => {
                      const index = lhTaskRun.attempts.length - i - 1
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

              <AttemptErrorExceptionOutput attempt={attempt} />
            </div>
          </Description>
        </DialogPanel>
      </div>
    </Dialog>
  )
}

function AttemptErrorExceptionOutput({ attempt }: { attempt: TaskAttempt }) {
  if (!attempt.output && !attempt.error && !attempt.exception) return

  return (
    <div className={cn('mt-2 flex flex-col rounded p-1', attempt.output ? 'bg-zinc-500 text-white' : 'bg-red-200')}>
      <h3 className="font-bold">
        {attempt.error && 'Error'}
        {attempt.exception && 'Exception'}
        {attempt.output && 'Output'}
      </h3>
      <pre className="overflow-x-auto">
        {attempt.error && attempt.error.message}
        {attempt.exception && attempt.exception.message}
        {attempt.output && getVariableValue(attempt.output)}
      </pre>
    </div>
  )
}
