import { Node } from 'littlehorse-client/proto'
import { ChevronDown, ChevronUp, ScanIcon, SplitIcon } from 'lucide-react'
import { FC, useState } from 'react'
import { useDiagram } from '../../../hooks/useDiagram'
import { Mutations } from './Mutations'

export const OutgoingEdges: FC<Pick<Node, 'outgoingEdges'>> = ({ outgoingEdges }) => {
  const { setNodes, nodes } = useDiagram()
  const [open, setOpen] = useState(true)
  if (outgoingEdges.length === 0) return

  const IconSymbol = open ? ChevronDown : ChevronUp

  const outgoingEdgeClickHandler = (sinkNodeName: string) => {
    const updated = nodes.map(node => ({
      ...node,
      selected: node.id === sinkNodeName,
    }))
    setNodes(updated)
  }

  return (
    <div className={`mb-2 flex flex-col pb-2`}>
      <div className="flex cursor-pointer items-center gap-2" onClick={() => setOpen(!open)}>
        <IconSymbol className="h-4 w-4 flex-none text-gray-400" />
        <h3 className="grow text-gray-400">Outgoing Edges</h3>
      </div>
      {open &&
        outgoingEdges.map(edge => (
          <div key={JSON.stringify(edge)} className="ml-2 flex flex-col border-l pl-2">
            <div className="flex items-center gap-2 pl-2 pt-2">
              <SplitIcon className="h-4 w-4 flex-none" />
              <h4 className="grow text-sm">{edge.sinkNodeName}</h4>

              <ScanIcon
                className="h-4 w-4 flex-none cursor-pointer"
                onClick={() => outgoingEdgeClickHandler(edge.sinkNodeName)}
              />
            </div>
            <Mutations variableMutations={edge.variableMutations} />
          </div>
        ))}
    </div>
  )
}
