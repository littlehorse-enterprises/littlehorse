import { cn } from '@/lib/utils'
import { ChevronDownIcon, ChevronRightIcon, TriangleAlertIcon } from 'lucide-react'
import { useRouter } from 'next/navigation'
import { FC, memo } from 'react'
import { Handle, NodeProps, Position } from 'reactflow'
import { useDiagram } from '../../hooks/useDiagram'
import { useReplaceQueryValue } from '../../hooks/useReplaceQueryValue'
import {
  CHIP_HEIGHT,
  HANDLER_LABEL_BAND,
  handlerBadge,
  type ExceptionHandlerData,
} from '../ExceptionHandlers/exceptionHandlers'

const useOpenHandlerThread = ({ run, handlerSpecName }: ExceptionHandlerData) => {
  const { setThread } = useDiagram()
  const router = useRouter()
  const replaceQuery = useReplaceQueryValue()
  return () => {
    if (run) {
      setThread({ name: handlerSpecName, number: run.number })
      router.replace(replaceQuery('threadRunNumber', run.number.toString()))
    } else {
      setThread({ name: handlerSpecName, number: 0 })
      router.replace(replaceQuery('thread', handlerSpecName))
    }
  }
}

const Badge: FC<{ data: ExceptionHandlerData }> = ({ data }) => {
  const badge = handlerBadge(data)
  if (!badge) return null
  return (
    <span className={cn('shrink-0 rounded px-1 text-[8px] font-semibold uppercase', badge.className)}>
      {badge.text}
    </span>
  )
}

/**
 * An expanded handler: a dashed box sized to the handler's own entry, nodes
 * and exits. Its "On <failure>" label sits just above the top-left corner and
 * its status badge just above the top-right one, in a row the layout keeps
 * clear (HANDLER_LABEL_BAND); neither one sizes the box.
 */
const ExceptionHandlerNode: FC<NodeProps<ExceptionHandlerData>> = ({ data }) => {
  const { toggleHandler } = useDiagram()
  const openThread = useOpenHandlerThread(data)
  const { size, run, fade, catchLabel, handlerSpecName, groupId } = data

  return (
    <div
      className={cn(
        'relative rounded-lg border border-dashed border-rose-400 bg-rose-50/70',
        fade ? 'opacity-40' : 'opacity-100'
      )}
      style={{ width: size?.w ?? 1, height: size?.h ?? 1 }}
    >
      {size && (
        <div
          className="absolute bottom-full left-0 flex min-w-full items-end justify-between gap-3 whitespace-nowrap pb-1"
          style={{ height: HANDLER_LABEL_BAND }}
        >
          <div className="flex items-center gap-1">
            <button
              type="button"
              onClick={() => toggleHandler?.(groupId)}
              title="Collapse handler"
              aria-label={`Collapse the ${handlerSpecName} handler`}
              className="pointer-events-auto -ml-0.5 shrink-0 rounded text-rose-600 hover:bg-rose-100"
            >
              <ChevronDownIcon className="h-3 w-3" strokeWidth={2.5} />
            </button>
            <button
              type="button"
              onClick={openThread}
              title={`Open the ${handlerSpecName} thread`}
              aria-label={`Open the ${handlerSpecName} thread`}
              className="pointer-events-auto flex items-center gap-1 rounded hover:bg-rose-100/60"
            >
              <TriangleAlertIcon className="h-2.5 w-2.5 shrink-0 stroke-rose-600" strokeWidth={2} />
              <span className="text-[9px] font-semibold uppercase tracking-wider text-rose-700/75">On</span>
              <span className="rounded bg-rose-100 px-1 font-mono text-[9px] text-rose-800">
                {run ? run.failureName : catchLabel}
              </span>
            </button>
          </div>
          <Badge data={data} />
        </div>
      )}
    </div>
  )
}

export const ExceptionHandler = memo(ExceptionHandlerNode)

/** A collapsed handler: a chip hanging off the throwing node's exception edge. */
const ExceptionHandlerChipNode: FC<NodeProps<ExceptionHandlerData>> = ({ data }) => {
  const { toggleHandler } = useDiagram()
  const { run, fade, catchLabel, handlerSpecName, groupId, footprint } = data

  return (
    <div className={cn(fade ? 'opacity-40' : 'opacity-100')}>
      <Handle type="target" id="target-0" position={Position.Left} className="bg-transparent" />
      <button
        type="button"
        onClick={() => toggleHandler?.(groupId)}
        title={`Expand the ${handlerSpecName} handler`}
        aria-label={`Expand the ${handlerSpecName} handler`}
        className="flex items-center gap-1 rounded-full border border-dashed border-rose-400 bg-rose-50 pl-1.5 pr-1 hover:bg-rose-100"
        style={{ width: footprint?.w, height: footprint?.h ?? CHIP_HEIGHT }}
      >
        <TriangleAlertIcon className="h-2.5 w-2.5 shrink-0 stroke-rose-600" strokeWidth={2} />
        <span className="shrink-0 rounded bg-rose-100 px-1 font-mono text-[9px] text-rose-800">
          {run ? run.failureName : catchLabel}
        </span>
        <Badge data={data} />
        <ChevronRightIcon className="ml-auto h-3 w-3 shrink-0 text-rose-600" strokeWidth={2.5} />
      </button>
    </div>
  )
}

export const ExceptionHandlerChip = memo(ExceptionHandlerChipNode)
