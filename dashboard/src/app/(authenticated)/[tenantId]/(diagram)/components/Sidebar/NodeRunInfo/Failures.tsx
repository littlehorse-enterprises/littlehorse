import { FC } from 'react'
import { useDiagram } from '../../../hooks/useDiagram'
import { Accordion, AccordionContent, AccordionItem, AccordionTrigger } from '@/components/ui/accordion'
import { NodeStatus } from './NodeStatus'
import { LabelContent } from '../Components'
import { LHStatus } from 'littlehorse-client/proto'

export const Failures: FC<{ nodeRunIndex: number }> = ({ nodeRunIndex }) => {
  const { selectedNode } = useDiagram()

  if (!selectedNode) {
    return null
  }

  if (!('nodeRunsList' in selectedNode.data)) {
    return null
  }
  const nodeRun = selectedNode.data.nodeRunsList[nodeRunIndex]
  const failures = nodeRun.failures

  return (
    <div>
      {failures.map((failure, index) => {
        return (
          <Accordion type="single" collapsible>
            <AccordionItem value={`action-${index}`}>
              <AccordionTrigger>
                <div className="ml-2">{failure.failureName}</div>
              </AccordionTrigger>
              <AccordionContent>
                <div className="mt-1">
                  <NodeStatus status={(failure.wasProperlyHandled ? 'COMPLETED' : 'ERROR') as LHStatus}></NodeStatus>
                </div>
                <div className="my-1 ml-2">
                  <div className="mb-2 border-b-2 pb-2">Message</div>
                  <div>{`${failure.message}`}</div>
                </div>
                {failure.content && <LabelContent label="content" content={`${failure.content}`}></LabelContent>}
                {failure.failureHandlerThreadrunId && (
                  <LabelContent
                    label="Thread ID to Handle the Failure"
                    content={`${failure.failureHandlerThreadrunId}`}
                  ></LabelContent>
                )}
              </AccordionContent>
            </AccordionItem>
          </Accordion>
        )
      })}
    </div>
  )
}
