import { ExitNode as ExitNodeProto } from 'littlehorse-client/proto'
import { FC } from 'react'
import { VariableAssignment } from '../Components'
import './node.css'
export const ExitNode: FC<{ node: ExitNodeProto }> = ({ node }) => {
  const { result } = node

  if (!result) return null

  return (
    <div className="flex max-w-full flex-1 flex-col">
      <div className="mb-2 flex flex-col gap-2">
        {result.oneofKind === 'failureDef' && <FailureDetails failureDef={result.failureDef} />}
        {result.oneofKind === 'returnContent' && <ReturnContent returnContent={result.returnContent} />}
      </div>
    </div>
  )
}

const FailureDetails: FC<{
  failureDef: Extract<ExitNodeProto['result'], { oneofKind: 'failureDef' }>['failureDef']
}> = ({ failureDef }) => {
  const { failureName, message, content } = failureDef
  return (
    <>
      <p className="node-title">Failure</p>
      <p className="flex-grow truncate bg-black px-2 font-mono text-gray-200">{failureName}</p>
      <p className="node-title">Message</p>
      <p className="line-clamp-2 truncate text-wrap border bg-black px-2 font-mono text-gray-200">{message}</p>
      {content && (
        <>
          <p className="node-title">Content</p>
          <VariableAssignment variableAssigment={content} />
        </>
      )}
    </>
  )
}

const ReturnContent: FC<{
  returnContent: Extract<ExitNodeProto['result'], { oneofKind: 'returnContent' }>['returnContent']
}> = ({ returnContent }) => {
  return (
    <>
      <small className="node-title">Result</small>
      <VariableAssignment variableAssigment={returnContent} />
    </>
  )
}
