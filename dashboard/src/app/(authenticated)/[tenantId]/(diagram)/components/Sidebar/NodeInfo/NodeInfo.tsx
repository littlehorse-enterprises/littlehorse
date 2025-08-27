import { FileArchive, Spline } from 'lucide-react'
import { FC, useCallback } from 'react'
import { useDiagram } from '../../../hooks/useDiagram'
import { SidebarSection } from '../SidebarSection'
import { OutgoingEdges } from './OutgoingEdges'

export const NodeInfo: FC = () => {
  const { selectedNode } = useDiagram()

  if (!selectedNode) {
    return null
  }



  return (
    <div className="flex flex-1 flex-col max-w-full">
      <h2 className="text-lg font-semibold">Node Details</h2>
      <p>
        <strong>Node Type:</strong> {selectedNode.type}
      </p>
      <p>
        <strong>Node Name:</strong> {selectedNode.id}
      </p>
      <OutgoingEdges outgoingEdges={selectedNode.data.outgoingEdges} />
    </div>
  )
}
