import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from '@/components/ui/dropdown-menu'
import { Button } from '@/components/ui/button'
import { ChevronDown } from 'lucide-react'
import { Dispatch, SetStateAction } from 'react'
import { useDiagram } from '../../../hooks/useDiagram'

export const SelectedNodeRun = ({
  nodeRunIndex,
  setNodeRunIndex,
}: {
  nodeRunIndex: number
  setNodeRunIndex: Dispatch<SetStateAction<number>>
}) => {
  const { selectedNode } = useDiagram()

  if (!selectedNode) {
    return null
  }
  if (!('nodeRunsList' in selectedNode.data)) {
    return null
  }

  const arrayRunNodeLength = selectedNode.data.nodeRunsList.length
  return (
    <div className="mb-2 ml-1 flex items-center justify-between ">
      <div className="truncate text-blue-500"> {selectedNode.id}</div>
      {arrayRunNodeLength > 1 && (
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <Button variant="outline" className="  px-2  drop-shadow-none">
              {`Node ${nodeRunIndex}`}
              <ChevronDown className="w-4" />
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent className="max-h-[300px] overflow-y-auto">
            {Array.from({ length: arrayRunNodeLength }).map((_, nodeIndex) => (
              <DropdownMenuItem key={nodeIndex} className="cursor-pointer" onClick={() => setNodeRunIndex(nodeIndex)}>
                {`Node ${nodeIndex}`}
              </DropdownMenuItem>
            ))}
          </DropdownMenuContent>
        </DropdownMenu>
      )}
    </div>
  )
}
