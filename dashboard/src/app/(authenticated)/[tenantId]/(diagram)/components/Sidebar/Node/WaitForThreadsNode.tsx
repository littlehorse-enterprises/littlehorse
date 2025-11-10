import { WaitForThreadsNode as WaitForThreadsNodeProto } from 'littlehorse-client/proto'
import { FC } from 'react'
import { VariableAssignment } from '../Components'
import './node.css'

export const WaitForThreadsNode: FC<{ node: WaitForThreadsNodeProto }> = ({ node }) => {
  const { threadsToWaitFor, perThreadFailureHandlers } = node
  if (!threadsToWaitFor) return null

  return (
    <div className="flex max-w-full flex-1 flex-col gap-2">
      {threadsToWaitFor.$case === 'threads' && (
        <>
          <small className="node-title">Threads</small>
          <div className="mb-2 flex items-center">
            {threadsToWaitFor.value.threads.map((thread, key) => (
              <div key={JSON.stringify(thread)} className="flex">
                <span className="flex-1 truncate bg-gray-200 px-2 font-mono">{key}</span>
                <VariableAssignment variableAssigment={thread.threadRunNumber!} />
              </div>
            ))}
          </div>
        </>
      )}
      {threadsToWaitFor.$case === 'threadList' && (
        <>
          <small className="node-title">Threads List</small>
          <div className="mb-2 flex items-center">
            <VariableAssignment variableAssigment={threadsToWaitFor.value} />
          </div>
        </>
      )}
      {perThreadFailureHandlers.length > 0 && (
        <div>
          <small className="node-title">Failure Handlers List</small>
          {perThreadFailureHandlers.map((handler, index) => (
            <div key={index} className=" mt-2  bg-gray-200">
              <div className="truncate">{handler.handlerSpecName}</div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
