import { ChevronLeftIcon, ChevronRightIcon } from '@heroicons/react/24/solid'
import { WfRun } from 'littlehorse-client/dist/proto/wf_run'
import { WfSpec } from 'littlehorse-client/dist/proto/wf_spec'
import { FC, useEffect, useMemo } from 'react'
import { useScrollbar } from '../hooks/useScrollbar'
import { useThread } from '../hooks/useThread'

export const ThreadPanel: FC<{ spec: WfSpec; wfRun?: WfRun }> = ({ spec, wfRun }) => {
  const { thread, setThread } = useThread()
  const threads = useMemo(() => extractThreads(spec, wfRun), [spec, wfRun])
  const { scroll, itemsRef, containerRef, maxScroll, scrollLeft, scrollRight } = useScrollbar()

  return (
    <div className="relative mb-2 flex items-center">
      <div
        className={`absolute left-0 top-0 z-10 flex after:h-[38px] after:w-[50px] after:bg-gradient-to-r after:from-white after:to-transparent after:content-[''] ${scroll === 0 ? 'hidden' : ''}`}
      >
        <button className="bg-white" onClick={() => scrollLeft()}>
          <ChevronLeftIcon className="h-6 w-6" />
        </button>
      </div>
      <div className="flex touch-pan-y items-center overflow-hidden text-nowrap" ref={containerRef}>
        <div
          className="flex gap-2 duration-[15ms] ease-[cubic-bezier(.05,0,0,1)] will-change-transform	"
          style={{ transform: `translateX(${scroll}px)` }}
          ref={itemsRef}
        >
          {threads.map(({ name, number }) => (
            <button
              className={
                'border-[1px] p-2 text-sm shadow ' +
                (name === thread.name && number !== undefined && number === thread.number
                  ? 'bg-blue-500 text-white'
                  : 'bg-white text-black')
              }
              key={`${name}-${number}`}
              onClick={() => {
                setThread(prev => {
                  const current = number === undefined ? { name } : { name, number }
                  return {
                    ...prev,
                    ...current,
                  }
                })
              }}
            >
              {`${name}${number !== undefined ? `-${number}` : ''}`}
            </button>
          ))}
        </div>
      </div>
      <div
        className={`absolute right-0 top-0 flex before:h-[38px] before:w-[50px] before:bg-gradient-to-l before:from-white before:to-transparent before:content-[''] ${!maxScroll || -scroll >= maxScroll ? 'hidden' : ''}`}
      >
        <button className="bg-white" onClick={() => scrollRight()}>
          <ChevronRightIcon className="h-6 w-6" />
        </button>
      </div>
    </div>
  )
}

const extractThreads = (spec: WfSpec, wfRun?: WfRun): { name: string; number?: number }[] => {
  if (wfRun) {
    return wfRun.threadRuns.map(threadRun => ({
      name: threadRun.threadSpecName,
      number: threadRun.number,
    }))
  }
  return Object.keys(spec.threadSpecs).map(name => ({ name }))
}
