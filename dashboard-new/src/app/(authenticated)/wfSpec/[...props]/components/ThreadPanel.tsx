import { WfRun } from 'littlehorse-client/dist/proto/wf_run'
import { WfSpec } from 'littlehorse-client/dist/proto/wf_spec'
import { FC } from 'react'
import { Panel } from 'reactflow'
import { useThread } from '../hooks/useThread'

export const ThreadPanel: FC<{ spec: WfSpec; wfRun?: WfRun }> = ({ spec, wfRun }) => {
  const { thread, setThread } = useThread()
  return (
    <Panel position="top-left">
      <div className="flex w-full items-center justify-between gap-2">
        {wfRun ? (
          <>
            {wfRun.threadRuns.map(threadRun => (
              <button
                className={
                  'border-[1px] p-2 text-sm shadow ' +
                  (threadRun.threadSpecName === thread.name && threadRun.number === thread.number
                    ? 'bg-blue-500 text-white'
                    : 'bg-white text-black')
                }
                key={`${threadRun.threadSpecName}-${threadRun.number}`}
                onClick={() => {
                  setThread({ name: threadRun.threadSpecName, number: threadRun.number })
                }}
              >
                {threadRun.threadSpecName}-{threadRun.number}
              </button>
            ))}
          </>
        ) : (
          <>
            {Object.keys(spec.threadSpecs)
              .reverse()
              .map(threadName => (
                <button
                  className={
                    'border-[1px] p-2 text-sm shadow ' +
                    (threadName === thread.name ? 'bg-blue-500 text-white' : 'bg-white text-black')
                  }
                  key={threadName}
                  onClick={() => setThread(prev => ({ ...prev, name: threadName }))}
                >
                  {threadName}
                </button>
              ))}
          </>
        )}
      </div>
    </Panel>
  )
}
