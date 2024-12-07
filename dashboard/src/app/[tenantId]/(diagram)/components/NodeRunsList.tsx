import { NodeRun, TaskNode, UserTaskNode } from 'littlehorse-client/proto'
import { EyeIcon } from 'lucide-react'
import { FC, useCallback } from 'react'
import { useModal } from '../hooks/useModal'
import { NodeViewButton } from './NodeViewButton'

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
  }, [nodeRun, nodeRuns, setModal, setShowModal, taskNode, userTaskNode])

  if (!nodeRuns?.length) return

  return <NodeViewButton text="View NodeRuns" callback={showNodeRuns} />
}
