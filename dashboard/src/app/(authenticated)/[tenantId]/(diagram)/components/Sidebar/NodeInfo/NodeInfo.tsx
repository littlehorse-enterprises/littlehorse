import { FC } from 'react'
import { useDiagram } from '../../../hooks/useDiagram'
import { OutgoingEdges } from './OutgoingEdges'
import { LabelContent } from '../Components'
import { FailureHandler } from './FailureHandler'
import { NodeTypeDocumentation } from '../Components/NodeTypeDocumentation'

export const NodeInfo: FC = () => {
  const { selectedNode } = useDiagram()
  if (!selectedNode) {
    return null
  }

  const { type, id, data } = selectedNode
  const { failureHandlers } = data
  return (
    <div className="flex max-w-full flex-1 flex-col">
      <NodeTypeDocumentation
        nodeType={selectedNode.type}
        showNodeRun={false}
        className=" mt-1 text-lg font-medium"
      />

      <LabelContent label="Node Name" content={id} />
      <OutgoingEdges outgoingEdges={data.outgoingEdges} />
      {failureHandlers && failureHandlers.length > 0 && <FailureHandler failureHandlers={failureHandlers} />}
    </div>
  )
}
