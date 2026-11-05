import { cn } from '@/lib/utils'
import { LHStatus } from 'littlehorse-client/proto'
import { TriangleAlertIcon } from 'lucide-react'
import { useRouter } from 'next/navigation'
import { FC, memo } from 'react'
import { NodeProps } from 'reactflow'
import { useDiagram } from '../../hooks/useDiagram'
import { useReplaceQueryValue } from '../../hooks/useReplaceQueryValue'
import type { ExceptionHandlerData } from '../ExceptionHandlers/exceptionHandlers'

const RUN_STATE: Partial<Record<LHStatus, { text: string; className: string }>> = {
  [LHStatus.COMPLETED]: { text: 'handled', className: 'bg-emerald-100 text-emerald-800' },
  [LHStatus.RUNNING]: { text: 'handling', className: 'bg-blue-100 text-blue-800' },
  [LHStatus.ERROR]: { text: 'handler failed', className: 'bg-red-100 text-red-800' },
  [LHStatus.EXCEPTION]: { text: 'rethrown', className: 'bg-red-100 text-red-800' },
  [LHStatus.HALTED]: { text: 'halted', className: 'bg-gray-100 text-gray-800' },
}

const ExceptionHandlerNode: FC<NodeProps<ExceptionHandlerData>> = ({ data }) => {
  const { setThread } = useDiagram()
  const router = useRouter()
  const replaceQuery = useReplaceQueryValue()
  const { size, run, fade, catchLabel, handlerSpecName, rethrows } = data
  const runState = run ? RUN_STATE[run.status] : undefined

  const openThread = () => {
    if (run) {
      setThread({ name: handlerSpecName, number: run.number })
      router.replace(replaceQuery('threadRunNumber', run.number.toString()))
    } else {
      setThread({ name: handlerSpecName, number: 0 })
      router.replace(replaceQuery('thread', handlerSpecName))
    }
  }

  return (
    <div
      className={cn(
        'rounded-lg border border-dashed border-rose-400 bg-rose-50/70',
        fade ? 'opacity-40' : 'opacity-100'
      )}
      style={{ width: size?.w ?? 1, height: size?.h ?? 1 }}
    >
      {size && (
        <button
          type="button"
          onClick={openThread}
          title={`Open the ${handlerSpecName} thread`}
          className="pointer-events-auto flex max-w-full items-center gap-1 overflow-hidden whitespace-nowrap px-2 pt-1.5 text-left"
        >
          <TriangleAlertIcon className="h-2.5 w-2.5 shrink-0 stroke-rose-600" strokeWidth={2} />
          <span className="text-[9px] font-semibold uppercase tracking-wider text-rose-700/75">On</span>
          <span className="rounded bg-rose-100 px-1 font-mono text-[9px] text-rose-800">
            {run ? run.failureName : catchLabel}
          </span>
          <span className="truncate text-[9px] font-medium text-slate-600 underline-offset-2 hover:underline">
            {handlerSpecName}
          </span>
          {runState && (
            <span className={cn('rounded px-1 text-[8px] font-semibold uppercase', runState.className)}>
              {runState.text}
            </span>
          )}
          {!run && rethrows && (
            <span className="rounded bg-red-100 px-1 text-[8px] font-semibold uppercase text-red-800">rethrows</span>
          )}
        </button>
      )}
    </div>
  )
}

export const ExceptionHandler = memo(ExceptionHandlerNode)
