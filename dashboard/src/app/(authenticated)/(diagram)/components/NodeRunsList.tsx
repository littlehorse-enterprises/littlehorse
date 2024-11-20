import { NodeRun, TaskNode, UserTaskNode } from 'littlehorse-client/proto'
import { FC, useCallback } from 'react'
import { EyeIcon } from 'lucide-react'
import { useModal } from '@/app/(authenticated)/(diagram)/hooks/useModal'

type Prop = {
  nodeRuns: [NodeRun]
  taskNode?: TaskNode
  userTaskNode?: UserTaskNode
  nodeRun?: NodeRun
}
export const NodeRunsList: FC<Prop> = ({ nodeRuns, taskNode, userTaskNode, nodeRun }) => {
  const { setModal, setShowModal } = useModal()
  const showNodeRuns = useCallback(() => {
    setModal({ type: 'nodeRunList', data: { nodeRunsList: nodeRuns, taskNode, nodeRun, userTaskNode } })
    setShowModal(true)
  }, [nodeRuns, setModal, setShowModal, taskNode])

  if (!nodeRuns?.length) return

  return (
    <div className="mt-2 flex justify-center">
      <button className="flex items-center gap-1 p-1 text-blue-500 hover:bg-gray-200" onClick={showNodeRuns}>
        <EyeIcon className="h-4 w-4" />
        View NodeRuns
      </button>
    </div>
  )
}
