import { WfRun } from 'littlehorse-client/dist/proto/wf_run'
import { WfSpec } from 'littlehorse-client/dist/proto/wf_spec'
import { FC } from 'react'
import { useThread } from '../hooks/useThread'

export const ThreadPanel: FC<{ spec: WfSpec; wfRun?: WfRun }> = ({ spec, wfRun }) => {
  const { thread, setThread } = useThread()
  const threads = extractThreads(spec, wfRun)

  return (
    <div className="flex w-full items-center gap-2 overflow-x-auto text-nowrap pl-2">
      <>
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
      </>
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
