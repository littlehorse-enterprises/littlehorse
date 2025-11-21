import * as AccordionRedux from '@radix-ui/react-accordion'
import { NodeRun, UserTaskNode } from 'littlehorse-client/proto'
import { ChevronDownIcon } from 'lucide-react'
import { FC } from 'react'
import { AccordionComponents } from './AccordionContent'
import { WF_RUN_STATUS } from '../../Sidebar/Components/StatusColor'

export const AccordionItem: FC<{ nodeRun: NodeRun; userTaskNode?: UserTaskNode }> = ({ nodeRun, userTaskNode }) => {
  if (!nodeRun.nodeType) return null
  const Component = AccordionComponents[nodeRun.nodeType.$case]
  return (
    <AccordionRedux.Item value={`item-${nodeRun.id?.position}`} className="overflow-hidden rounded-lg border">
      <AccordionRedux.Header className="w-full">
        <AccordionRedux.Trigger className="flex w-full items-center justify-between bg-gray-100 px-4 py-2 text-left hover:bg-gray-200 focus:outline-none">
          <div className="mr-2 w-full font-medium">
            <div className="flex w-full justify-between">
              <div className="flex flex-col">
                <div className="flex">
                  NodeRun Position: &nbsp;
                  <span className="bold text-blue-500">{nodeRun.id?.position}</span>
                </div>
              </div>
              <div className="flex ">
                <span
                  className={`ml-2 rounded px-2 ${WF_RUN_STATUS[nodeRun.status].color}`}
                >{`${nodeRun.status}`}</span>
              </div>
            </div>
          </div>
          <ChevronDownIcon className="group-radix-state-open:rotate-180 h-5 w-5 transform transition-transform duration-300 ease-in-out" />
        </AccordionRedux.Trigger>
      </AccordionRedux.Header>
      <AccordionRedux.Content className="bg-white px-4 py-2 text-gray-700">
        <Component nodeRun={nodeRun as any} userTaskNode={userTaskNode} />
      </AccordionRedux.Content>
    </AccordionRedux.Item>
  )
}
