import { FC, ReactNode } from 'react'
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/components/ui/dialog'
import { Accordion } from '@/components/ui/accordion'

import { Modal, NodeRuns } from '../../../context'
import { useModal } from '../../../hooks/useModal'
import { statusColors } from '../../../wfRun/[...ids]/components/Details'
import { TaskDefDetail } from './TaskDefDetail'
import { TaskLink } from '../../NodeTypes/Task/TaskDetails'

export const NodeRun: FC<Modal> = ({ data }) => {
  const { nodeRunsList, taskNode } = data as NodeRuns
  const node = nodeRunsList?.[0]
  const { showModal, setShowModal } = useModal()
  const accordionNodeRuns: { title: string | ReactNode; content: string | ReactNode }[] = nodeRunsList
    ?.sort((a, b) => (a?.id?.position ?? 0) - (b?.id?.position ?? 0))
    ?.map(node => {
      return {
        title: (
          <div className="flex w-full justify-between">
            <div className="flex flex-col">
              <div className="flex">
                TaskGuid: &nbsp;
                <span className="text-blue-400 underline">{node?.task?.taskRunId?.taskGuid}</span>
              </div>
            </div>
            <div className="flex ">
              <span className={`ml-2 rounded px-2 ${statusColors[node.status]}`}>{`${node.status}`}</span>
            </div>
          </div>
        ),
        content: (
          <TaskDefDetail taskId={node?.task?.taskRunId?.taskGuid} wfRunId={node?.task?.taskRunId?.wfRunId?.id} />
        ),
      }
    })

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
            <strong>WfRun</strong>: {node?.task?.taskRunId?.wfRunId?.id}
          </div>
        </div>

        <div className=" h-full overflow-auto">
          <Accordion items={accordionNodeRuns} />
        </div>
      </DialogContent>
    </Dialog>
  )
}
