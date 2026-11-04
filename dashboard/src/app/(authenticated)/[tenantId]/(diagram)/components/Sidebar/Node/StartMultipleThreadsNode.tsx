import { StartMultipleThreadsNode as StartMultipleThreadsNodeProto } from 'littlehorse-client/proto'
import { ScanIcon } from 'lucide-react'
import { useRouter } from 'next/navigation'
import { FC } from 'react'
import { useDiagram } from '../../../hooks/useDiagram'
import { useReplaceQueryValue } from '../../../hooks/useReplaceQueryValue'
import { VariableAssignment } from '../Components'
import './node.css'

export const StartMultipleThreadsNode: FC<{ node: StartMultipleThreadsNodeProto }> = ({ node }) => {
  const { setThread, wfRun } = useDiagram()
  const router = useRouter()
  const replaceQuery = useReplaceQueryValue()
  const { threadSpecName, variables, iterable } = node
  const childThreadRun = wfRun?.threadRuns.find(threadRun => threadRun.threadSpecName === threadSpecName)
  const canGoToThread = !wfRun || childThreadRun !== undefined

  const goToThread = () => {
    if (childThreadRun) {
      setThread({ name: threadSpecName, number: childThreadRun.number })
      router.replace(replaceQuery('threadRunNumber', childThreadRun.number.toString()))
      return
    }
    setThread({ name: threadSpecName, number: 0 })
    router.replace(replaceQuery('thread', threadSpecName))
  }

  return (
    <div className="flex max-w-full flex-1 flex-col gap-2">
      <div>
        <small className="node-title">StartMultipleThreads</small>
        <div className="flex items-center">
          <p className="flex-grow truncate text-lg font-medium">{threadSpecName}</p>
          {canGoToThread && (
            <ScanIcon
              aria-label={`Go to thread ${threadSpecName}`}
              className="ml-1 h-4 w-4 cursor-pointer hover:text-slate-600"
              onClick={goToThread}
            />
          )}
        </div>
      </div>
      {iterable && (
        <div className="flex flex-col gap-2">
          <small className="node-title">Iterable</small>
          <div className="flex">
            <VariableAssignment variableAssigment={iterable} />
          </div>
          <small className="text-xs text-slate-400">
            One thread is spawned per item. Each thread receives its item as the <code>INPUT</code> variable.
          </small>
        </div>
      )}
      {Object.keys(variables).length > 0 && (
        <div className="flex flex-col gap-2">
          <small className="node-title">Inputs</small>
          {Object.entries(variables).map(([name, assignment]) => (
            <div key={name} className="flex">
              <span className="flex-1 truncate bg-gray-200 px-2 font-mono">{name}</span>
              <VariableAssignment variableAssigment={assignment} />
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
