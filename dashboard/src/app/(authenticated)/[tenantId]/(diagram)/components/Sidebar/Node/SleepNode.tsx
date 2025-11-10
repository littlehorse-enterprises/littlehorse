import { SleepNode as SleepNodeProp } from 'littlehorse-client/proto'
import { FC } from 'react'
import { VariableAssignment } from '../Components/VariableAssignment'
import './node.css'

export const SleepNode: FC<{ node: SleepNodeProp }> = ({ node }) => {
  const { sleepLength } = node

  if (!sleepLength) return null
  return (
    <div className="flex max-w-full flex-1 flex-col">
      <div className="mb-2 flex flex-col gap-2">
        <p className="node-title">Sleep</p>
        <div className="flex">
          <p className="flex-none truncate bg-blue-500 px-2 font-mono text-gray-200">{sleepLength.$case}</p>
          <VariableAssignment variableAssigment={sleepLength.value} />
        </div>
      </div>
    </div>
  )
}
