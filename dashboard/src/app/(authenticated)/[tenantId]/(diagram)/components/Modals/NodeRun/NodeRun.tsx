import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/components/ui/dialog'
import * as Accordion from '@radix-ui/react-accordion'
import { FC } from 'react'
import { Modal, NodeRuns } from '../../../context'
import { useModal } from '../../../hooks/useModal'
import { AccordionItem } from './AccordionItem'

export const NodeRun: FC<Modal<NodeRuns>> = ({ data, type }) => {
  const { nodeRunsList, taskNode, userTaskNode } = data
  const node = nodeRunsList[0]
  const { showModal, setShowModal } = useModal()

  return (
    <Dialog open={showModal} onOpenChange={open => setShowModal(open)}>
      <DialogContent className="flex max-w-5xl flex-col">
        <DialogHeader>
          <DialogTitle className="mr-8 flex items-center justify-between">
            <h2 className="text-lg font-bold">NodeRuns</h2>
            {/* {taskNode?.taskToExecute && <TaskLink taskToExecute={taskNode.taskToExecute} />} */}
          </DialogTitle>
        </DialogHeader>
        <hr />
        <div className="flex justify-between">
          <div>
            <strong>WfRun</strong>: {node.id?.wfRunId?.id}
          </div>
          <div>
            <strong>Node Type</strong>: {type}
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
              <AccordionItem key={`item-${node.id?.position}`} nodeRun={nodeRun} userTaskNode={userTaskNode} />
            ))}
          </Accordion.Root>
        </div>
      </DialogContent>
    </Dialog>
  )
}
