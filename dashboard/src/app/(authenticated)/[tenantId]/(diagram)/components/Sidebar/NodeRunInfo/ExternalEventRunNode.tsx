import { ExternalEventNodeRun } from 'littlehorse-client/proto'
import { FC } from 'react'
import { LabelContent } from '../Components'

export const ExternalEventRunNode: FC<{ node: ExternalEventNodeRun }> = ({ node }) => {
  console.log("testin", node)
  return (
    <div>
      <LabelContent label="Node Type" content="External event "></LabelContent>
      <LabelContent label="External event definition ID" content={`${node.externalEventDefId?.name}`}></LabelContent>
      <LabelContent label="Event  Time" content={`${node.eventTime}`}></LabelContent>
      <LabelContent label="External  event ID" content={`${node.externalEventId}`}></LabelContent>
      <LabelContent label="Time Out" content={`${node.timedOut}`}></LabelContent>
      <LabelContent label="Correlation Key" content={`${node.correlationKey}`}></LabelContent>
      {node.maskCorrelationKey && (
        <LabelContent label="Correlation key status" content={'it should be masked'}></LabelContent>
      )}
    </div>
  )
}
