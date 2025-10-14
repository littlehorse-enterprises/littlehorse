import { UTActionTrigger_UTATask, VariableMutationType } from 'littlehorse-client/proto'
import { FC } from 'react'
import { TaskNode as TaskNodeComponent } from '../TaskNode'
import { MutationModal } from '../../../Modals/MutationModal'
import { useModal } from '../../../../hooks/useModal'

export const ActionTask: FC<{ node: UTActionTrigger_UTATask }> = ({ node }) => {
  const { task, mutations } = node
  const { setShowModal } = useModal()
  const onClick = () => {
    setShowModal(true)
  }
  return (
    <div className="mt-2">
      {mutations && mutations.length > 0 && (
        <div>
          <small className="node-title">Mutations</small>
          <div className="my-2 ">
            {mutations.map((mutation, i) => (
              <div key={`mutation-content-${i}`}>
                <MutationModal data={mutation} type={'edge'}></MutationModal>
                <span className='bg-gray-200 px-2 font-mono rounded p-1' onClick={onClick}>{mutation.lhsName}</span>
              </div>
            ))}
          </div>
        </div>
      )}
      {task && <TaskNodeComponent node={task} />}
    </div>
  )
}
