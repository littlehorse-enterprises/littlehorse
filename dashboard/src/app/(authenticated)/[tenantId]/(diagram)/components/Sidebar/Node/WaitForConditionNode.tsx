import { WaitForConditionNode } from 'littlehorse-client/proto'
import { FC } from 'react'
import { Info } from 'lucide-react';
import "./node.css"
import { VariableAssignment } from '../Components/VariableAssignment'

export const WaitForCondition: FC<{ node: WaitForConditionNode }> = ({ node }) => {
  const { condition } = node
  if (!condition) return null
  const { left, right } = condition
  return (
    <div className="flex max-w-full flex-1 flex-col">
      <div className="mb-2 flex flex-col gap-2">
        <div className='flex items-center'>
          <p className="node-title mr-1">Condition</p>
          <Info size="12"  color='#A2A2A2' />
        </div>
       
        <div>
          <VariableAssignment variableAssigment={left!} />
          <div className="my-2 flex w-full items-center">
            <div className="h-px flex-grow bg-gray-300"></div>
            <p className="node-title mx-4">{condition.comparator}</p>
            <div className="h-px flex-grow bg-gray-300"></div>
          </div>
          <VariableAssignment variableAssigment={right!} />
        </div>
      </div>
    </div>
  )
}