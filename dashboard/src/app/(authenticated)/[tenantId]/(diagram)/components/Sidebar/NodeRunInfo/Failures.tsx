import { FC } from 'react'
import { useDiagram } from '../../../hooks/useDiagram'
import { Accordion, AccordionContent, AccordionItem, AccordionTrigger } from '@/components/ui/accordion'
import { NodeStatus } from './NodeStatus'
import { LabelContent } from '../Components'
import { LHStatus } from 'littlehorse-client/proto'
import { Textarea } from '@/components/ui/textarea'
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
          <div>
            <Accordion type="single" collapsible>
              <AccordionItem value={`action-${index}`}>
                <AccordionTrigger>
                  <div>{failure.failureName}</div>
                </AccordionTrigger>
                <AccordionContent>
                  <NodeStatus status={(failure.wasProperlyHandled ? 'COMPLETED' : 'ERROR') as LHStatus}></NodeStatus>
                  <LabelContent label="content" content={failure.content}></LabelContent>
                  <LabelContent
                    label="Thread ID to Handle the Failure"
                    content={failure.failureHandlerThreadrunId}
                  ></LabelContent>
                  <div>{`${failure.message}`}</div>
                </AccordionContent>
              </AccordionItem>
            </Accordion>
          </div>
        )
      })}
    </div>
  )
}
