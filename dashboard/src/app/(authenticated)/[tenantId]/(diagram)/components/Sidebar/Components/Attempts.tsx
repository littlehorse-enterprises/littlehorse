import { TaskAttempt } from 'littlehorse-client/proto'
import { NodeStatus } from './NodeStatus'
import { NodeVariable } from './NodeVariable'
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from '@/components/ui/dropdown-menu'
import { Accordion, AccordionContent, AccordionItem, AccordionTrigger } from '@/components/ui/accordion'
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
    <div className="mb-4 mt-4 rounded-lg border border-gray-200 bg-gray-50">
      <Accordion type="single" collapsible defaultValue="attempts">
        <AccordionItem value="attempts" className="border-0">
          <AccordionTrigger className="px-4 py-3 text-sm font-semibold text-gray-700 hover:no-underline">
            <div className="flex w-full items-center justify-between pr-2">
              <span>Attempts</span>
              <DropdownMenu>
                <DropdownMenuTrigger asChild onClick={e => e.stopPropagation()}>
                  <Button variant="outline" className="h-8 gap-1.5 px-3 text-sm" onClick={e => e.stopPropagation()}>
                    <span>Attempt {attemptIndex + 1}</span>
                    <span className="text-xs text-gray-500">of {attemptLength}</span>
                    <ChevronDown className="h-3.5 w-3.5" />
                  </Button>
                </DropdownMenuTrigger>
                <DropdownMenuContent className="max-h-[300px] overflow-y-auto">
                  {Array.from({ length: attemptLength }).map((_, nodeIndex) => (
                    <DropdownMenuItem
                      key={nodeIndex}
                      className="cursor-pointer"
                      onClick={() => setAttemptIndex(nodeIndex)}
                    >
                      Attempt {nodeIndex + 1}
                    </DropdownMenuItem>
                  ))}
                </DropdownMenuContent>
              </DropdownMenu>
            </div>
          </AccordionTrigger>
          <AccordionContent className="px-4 pb-4 pt-0">
            {attempt.status && <NodeStatus status={attempt.status} type="task" />}
            <div className="mt-3">
              <NodeVariable label="scheduleTime:" text={`${attempt.scheduleTime}`} type="date" />
              <NodeVariable label="startTime:" text={`${attempt.startTime}`} type="date" />
              <NodeVariable label="endTime:" text={`${attempt.endTime}`} type="date" />
              <NodeVariable label="taskWorkerId:" text={`${attempt.taskWorkerId}`} />
              {attempt.taskWorkerVersion && (
                <NodeVariable label="taskWorkerVersion:" text={`${attempt.taskWorkerVersion}`} />
              )}
              {attempt.logOutput && <NodeVariable label="logOutput:" text={`${getAttemptOutput(attempt.logOutput)}`} />}
              <OutputModal label="result:" message={`${getAttemptResult(attempt.result)}`} />
            </div>
          </AccordionContent>
        </AccordionItem>
      </Accordion>
    </div>
  )
}
