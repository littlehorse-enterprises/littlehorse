import { TaskAttempt } from 'littlehorse-client/proto'
import { NodeStatus } from './NodeStatus'
import { NodeVariable } from './NodeVariable'
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from '@/components/ui/dropdown-menu'
import { Button } from '@/components/ui/button'
import { Dispatch, SetStateAction } from 'react'
import { ChevronDown } from 'lucide-react'
import { getAttemptOutput, getAttemptResult } from '@/app/utils/struct'
import { OutputModal } from './OutputModal'

export const Attempts = ({
  attempts,
  attemptIndex,
  setAttemptIndex,
}: {
  attempts: TaskAttempt[]
  attemptIndex: number
  setAttemptIndex: Dispatch<SetStateAction<number>>
}) => {
  const attempt = attempts[attemptIndex]
  const attemptLength = attempts.length
  return (
    <div className="ml-1 mt-1 ">
      <div className="flex items-center justify-between">
        <p className=" text-sm font-bold ">Attempts</p>
       {attemptLength > 1 && <DropdownMenu>
          <DropdownMenuTrigger className="my-1" asChild>
            <Button variant="outline" className="  my-0  px-2 drop-shadow-none">
              {`${attemptIndex + 1}`}
              <ChevronDown className="w-4" />
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent className="max-h-[300px] overflow-y-auto">
            {Array.from({ length: attemptLength }).map((_, nodeIndex) => (
              <DropdownMenuItem key={nodeIndex} className="cursor-pointer" onClick={() => setAttemptIndex(nodeIndex)}>
                {`${nodeIndex + 1}`}
              </DropdownMenuItem>
            ))}
          </DropdownMenuContent>
        </DropdownMenu>}
      </div>
      {attempt.status && <NodeStatus status={attempt.status} type="task" />}
      <NodeVariable label="scheduleTime:" text={`${attempt.scheduleTime}`} type="date" />
      <NodeVariable label="startTime:" text={`${attempt.startTime}`} type="date" />
      <NodeVariable label="endTime:" text={`${attempt.endTime}`} type="date" />
      <NodeVariable label="taskWorkerId:" text={`${attempt.taskWorkerId}`} />
      {attempt.taskWorkerVersion && <NodeVariable label="taskWorkerVersion:" text={`${attempt.taskWorkerVersion}`} />}
      {attempt.logOutput && <NodeVariable label="logOutput:" text={`${getAttemptOutput(attempt.logOutput)}`} />}
      <OutputModal label="result:" message={`${getAttemptResult(attempt.result)}`} />
    </div>
  )
}
