import { UTActionTrigger_UTATask, VariableMutation } from 'littlehorse-client/proto'
import { FC } from 'react'
import { useModal } from '../../../../hooks/useModal'
import { TaskNode as TaskNodeComponent } from '../TaskNode'

export const ActionTask: FC<UTActionTrigger_UTATask> = ({ task, mutations }) => {
  const { setShowModal, setModal } = useModal()

  const onClick = (data: VariableMutation) => {
    setModal({ type: 'mutation', data })
    setShowModal(true)
  }

  return (
    <div className="mt-2">
      {mutations.length > 0 && (
        <div>
          <small className="node-title">Mutations</small>
          <div className="my-2 ">
            {mutations.map((mutation, i) => (
              <div key={`mutation-content-${i}`}>
                <span className="py2 cursor-pointer rounded font-mono  text-blue-500" onClick={() => onClick(mutation)}>
                  {mutation.lhsName}
                </span>
              </div>
            ))}
          </div>
        </div>
      )}
      {task && <TaskNodeComponent node={task} />}
    </div>
  )
}
