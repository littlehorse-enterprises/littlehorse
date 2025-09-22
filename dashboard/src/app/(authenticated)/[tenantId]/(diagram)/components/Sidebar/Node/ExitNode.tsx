import { ExitNode as ExitNodeProto } from 'littlehorse-client/proto'
import { FC } from 'react'
import { VariableAssignment } from '../Components'

export const ExitNode: FC<{ node: ExitNodeProto }> = ({ node }) => {
  const { result } = node

  if (!result) return null

  return (
    <div className="flex max-w-full flex-1 flex-col">
      <div className="mb-2 flex flex-col gap-2">
        {result.$case === 'failureDef' && <FailureDetails failureDef={result.value} />}
        {result.$case === 'returnContent' && <ReturnContent returnContent={result.value} />}
      </div>
    </div>
  )
}

const FailureDetails: FC<{
  failureDef: NonNullable<Extract<ExitNodeProto['result'], { $case: 'failureDef' }>>['value']
}> = ({ failureDef }) => {
  const { failureName, message, content } = failureDef
  return (
    <>
      <p className="text-[0.75em] text-slate-400">Failure</p>
      <p className="flex-grow truncate px-2 bg-black font-mono text-gray-200">{failureName}</p>
      <p className="text-[0.75em] text-slate-400 ">Message</p>
      <p className="line-clamp-2 truncate text-wrap border bg-black px-2 font-mono text-gray-200">{message}</p>
      {content && (
        <>
          <p className="text-[0.75em] text-slate-400">Content</p>
          <VariableAssignment variableAssigment={content} />
        </>
      )}
    </>
  )
}

const ReturnContent: FC<{
  returnContent: NonNullable<Extract<ExitNodeProto['result'], { $case: 'returnContent' }>>['value']
}> = ({ returnContent }) => {
  return (
    <>
      <small className="text-[0.75em] text-slate-400">Result</small>
      <VariableAssignment variableAssigment={returnContent} />
    </>
  )
}
