import { getVariableValue } from '@/app/utils'
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/components/ui/dialog'
import { cn } from '@/components/utils'
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
    <Dialog open={showModal} onOpenChange={open => setShowModal(open)}>
      <DialogContent className="flex flex-col">
        <DialogHeader>
          <DialogTitle className="flex items-center justify-between">
            <h2 className="text-lg font-bold">TaskRun</h2>
            <div className="item-center flex gap-1 bg-gray-200 px-2 py-1">
              <span className="font-mono text-sm">{lhTaskRun.id?.taskGuid}</span>
              <ClipboardIcon
                className="h-4 w-4 cursor-pointer fill-transparent stroke-blue-500"
                onClick={() => {
                  navigator.clipboard.writeText(lhTaskRun.id?.taskGuid ?? '')
                }}
              />
            </div>
          </DialogTitle>
        </DialogHeader>

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
        <div>
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
      </DialogContent>
    </Dialog>
  )
}

function AttemptErrorExceptionOutput({ attempt }: { attempt: TaskAttempt }) {
  if (!attempt.output && !attempt.error && !attempt.exception) return

  return (
    <div
      className={cn(
        'flex w-full flex-col overflow-auto rounded p-1',
        attempt.output ? 'bg-zinc-500 text-white' : 'bg-red-200'
      )}
    >
      <h3 className="font-bold">
        {attempt.error && 'Error'}
        {attempt.exception && 'Exception'}
        {attempt.output && 'Output'}
      </h3>
      <pre className="overflow-auto">
        {attempt.error && attempt.error.message}
        {attempt.exception && attempt.exception.message}
        {attempt.output && String(getVariableValue(attempt.output))}
      </pre>
    </div>
  )
}
