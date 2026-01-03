import { Accordion, AccordionContent, AccordionItem, AccordionTrigger } from '@/components/ui/accordion'
import { WaitForThreadsRun } from 'littlehorse-client/proto'
import { FC } from 'react'
import { NodeStatus } from '../Components/NodeStatus'
import { NodeVariable } from '../Components/NodeVariable'

export const WaitForThreadsNodeRun: FC<{ node: WaitForThreadsRun }> = ({ node }) => {
  return (
    <div>
      {node.threads.map((thread, index) => {
        return (
          <div key={index}>
            <Accordion type="single" collapsible>
              <AccordionItem value={`action-${index}`}>
                <AccordionTrigger>
                  <div>{thread.threadRunNumber}</div>
                </AccordionTrigger>
                <AccordionContent>
                  <NodeStatus status={thread.threadStatus}></NodeStatus>
                  <NodeVariable label="Node Type:" text="Thread"></NodeVariable>
                  <NodeVariable label="threadEndTime:" text={thread.threadEndTime}></NodeVariable>
                  <NodeVariable label="waitingStatus:" text={thread.waitingStatus}></NodeVariable>
                  <NodeVariable
                    label="failureHandlerThreadRunId:"
                    text={`${thread.failureHandlerThreadRunId}`}
                  ></NodeVariable>
                </AccordionContent>
              </AccordionItem>
            </Accordion>
          </div>
        )
      })}
    </div>
  )
}
