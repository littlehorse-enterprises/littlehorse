import { FC } from 'react'
import { useDiagram } from '../../../hooks/useDiagram'
import { OutgoingEdges } from './OutgoingEdges'
import { LabelContent } from '../Components'

export const NodeInfo: FC = () => {
  const { selectedNode } = useDiagram()

  if (!selectedNode) {
    return null
  }

  const { type, id, data } = selectedNode

  return (
    <div className="flex max-w-full flex-1 flex-col">
      <LabelContent label="Node Type" content={type} />
      <LabelContent label="Node Name" content={id} />
      <OutgoingEdges outgoingEdges={data.outgoingEdges} />
    </div>
  )
}
