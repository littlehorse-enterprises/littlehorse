import { FC } from 'react'
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/components/ui/dialog'
import { Modal, NodeRuns } from '../../../context'
import { useModal } from '../../../hooks/useModal'
import { TaskLink } from '../../NodeTypes/Task/TaskDetails'
import { getNodeType } from '../../NodeTypes/extractNodes'
import * as Accordion from '@radix-ui/react-accordion'
import { AccordionItem } from './AccordionItem'

export const NodeRun: FC<Modal> = ({ data }) => {
  const { nodeRunsList, taskNode, userTaskNode } = data as NodeRuns
  const node = nodeRunsList[0]
  const { showModal, setShowModal } = useModal()

  return (
    <Dialog open={showModal} onOpenChange={open => setShowModal(open)}>
      <DialogContent className="flex max-w-5xl flex-col">
        <DialogHeader>
          <DialogTitle className="mr-8 flex items-center justify-between">
            <h2 className="text-lg font-bold">TaskAttempts</h2>
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

        <div className="h-full overflow-auto">
          <Accordion.Root
            className="bg-mauve6 wfull rounded-md shadow-[0_2px_10px] shadow-black/5"
            type="single"
            defaultValue="item-1"
            collapsible
          >
            {nodeRunsList?.map(nodeRun => (
              <AccordionItem key={`item-${node.id?.position}`} node={nodeRun} userTaskNode={userTaskNode} />
            ))}
          </Accordion.Root>
        </div>
      </DialogContent>
    </Dialog>
  )
}
