import * as AccordionRedux from '@radix-ui/react-accordion'
import { NodeRun, UserTaskNode } from 'littlehorse-client/proto'
import { ChevronDownIcon } from 'lucide-react'
import { FC } from 'react'
import { statusColors } from '../../../wfRun/[...ids]/components/Details'
import { getNodeType } from '../../NodeTypes/extractAllNodes'
import { AccordionComponents } from './AccordionContent'

export const AccordionItem: FC<{ node: NodeRun; userTaskNode?: UserTaskNode }> = ({ node, userTaskNode }) => {
  const nodeType = getNodeType(node)
  const Component = AccordionComponents[nodeType]
  return (
    <AccordionRedux.Item value={`item-${node.id?.position}`} className="overflow-hidden rounded-lg border">
      <AccordionRedux.Header className="w-full">
        <AccordionRedux.Trigger className="flex w-full items-center justify-between bg-gray-100 px-4 py-2 text-left hover:bg-gray-200 focus:outline-none">
          <div className="mr-2 w-full font-medium">
            <div className="flex w-full justify-between">
              <div className="flex flex-col">
                <div className="flex">
                  NodeRun Position: &nbsp;
                  <span className="bold text-blue-500">{node.id?.position}</span>
                </div>
              </div>
              <div className="flex ">
                <span className={`ml-2 rounded px-2 ${statusColors[node.status]}`}>{`${node.status}`}</span>
              </div>
            </div>
          </div>
          <ChevronDownIcon className="group-radix-state-open:rotate-180 h-5 w-5 transform transition-transform duration-300 ease-in-out" />
        </AccordionRedux.Trigger>
      </AccordionRedux.Header>
      <AccordionRedux.Content className="bg-white px-4 py-2 text-gray-700">
        <Component nodeRun={node} userTaskNode={userTaskNode} />
      </AccordionRedux.Content>
    </AccordionRedux.Item>
  )
}
