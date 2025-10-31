import { WaitForThreadsRun } from 'littlehorse-client/proto'
import { FC } from 'react'
import { Accordion, AccordionContent, AccordionItem, AccordionTrigger } from '@/components/ui/accordion'
import { NodeStatus } from './NodeStatus'
import { LabelContent } from '../Components'

export const WaitForThreadsNodeRun: FC<{ node: WaitForThreadsRun }> = ({ node }) => {
  return (
    <div>
      {node.threads.map((thread, index) => {
        return (
          <div>
            <Accordion type="single" collapsible>
              <AccordionItem value={`action-${index}`}>
                <AccordionTrigger>
                  <div>{thread.threadRunNumber}</div>
                </AccordionTrigger>
                <AccordionContent>
                  <NodeStatus status={thread.threadStatus}></NodeStatus>
                  <LabelContent label="Node Type" content="Thread"></LabelContent>
                  <LabelContent label="Thread end time" content={thread.threadEndTime}></LabelContent>
                  <LabelContent label="Status node" content={thread.waitingStatus}></LabelContent>
                  <LabelContent
                    label="Failure id Handler"
                    content={`${thread.failureHandlerThreadRunId}`}
                  ></LabelContent>
                </AccordionContent>
              </AccordionItem>
            </Accordion>
          </div>
        )
      })}
    </div>
  )
}
