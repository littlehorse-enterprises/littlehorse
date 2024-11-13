import { FC, ReactNode } from 'react'
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/components/ui/dialog'
import { Accordion } from '@/components/ui/accordion'
import { Modal, NodeRuns } from '../../../context'
import { useModal } from '../../../hooks/useModal'
import { TaskLink } from '../../NodeTypes/Task/TaskDetails'
import { NodeRun as Node } from 'littlehorse-client/proto'

export const getNodeType = (node: Node) => {
  if (node['task'] !== undefined) return 'TASK'
  if (node['externalEvent'] !== undefined) return 'EXTERNAL_EVENT'
  if (node['waitThreads'] !== undefined) return 'WAIT_FOR_THREADS'
  if (node['sleep'] !== undefined) return 'SLEEP'
  if (node['userTask'] !== undefined) return 'USER_TASK'
  if (node['startThread'] !== undefined) return 'START_THREAD'
  if (node['throwEvent'] !== undefined) return 'THROW_EVENT'
}

export const NodeRun: FC<Modal> = ({ data }) => {
  const { nodeRunsList, taskNode } = data as NodeRuns
  const node = nodeRunsList[0]
  const { showModal, setShowModal } = useModal()

  return (
    <Dialog open={showModal} onOpenChange={open => setShowModal(open)}>
      <DialogContent className="flex max-w-5xl flex-col">
        <DialogHeader>
          <DialogTitle className="mr-8 flex items-center justify-between">
            <h2 className="text-lg font-bold">NodeRuns</h2>
            {taskNode && <TaskLink taskName={taskNode.taskDefId?.name} />}
          </DialogTitle>
        </DialogHeader>
        <hr />
        <div className="flex justify-between">
          <div>
            <strong>WfRun</strong>: {node.id?.wfRunId?.id}
          </div>
          <div>
            <strong>Node Type</strong>: {getNodeType(node)}
          </div>
        </div>

        <div className=" h-full overflow-auto">
          <Accordion items={accordionNodeRuns} />
        </div>
      </DialogContent>
    </Dialog>
  )
}
