import { UTActionTrigger_UTATask } from 'littlehorse-client/proto'
import { FC } from 'react'
import { TaskNode as TaskNodeComponent } from '../TaskNode'
// import { Edge } from '../../../Modals/Edge'

export const ActionTask: FC<{ node: UTActionTrigger_UTATask }> = ({ node }) => {
  const { task, mutations } = node
  console.log(mutations)
  return (
    <div className="mt-1 ">
      {mutations && mutations.length > 0 && (
        <div>
          <small className="node-title">Mutations</small>
          <div className="mt-1">
            {mutations.map((mutation, i) => (
              <div key={i} className="flex">
                {/* <Edge data={{ variableMutations: [mutation], sinkNodeName: '' }} type={'edge'}></Edge> */}
                <span className="bg-gray-200 px-2 font-mono">{mutation.lhsName}</span>
              </div>
            ))}
          </div>
        </div>
      )}
      {task && <TaskNodeComponent node={task} />}
    </div>
  )
}
