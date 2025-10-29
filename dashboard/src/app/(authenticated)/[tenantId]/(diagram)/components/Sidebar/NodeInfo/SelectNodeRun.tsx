import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from '@/components/ui/dropdown-menu'
import { Button } from '@/components/ui/button'
import { ChevronDown } from 'lucide-react'
import { Dispatch, SetStateAction } from 'react'

export const SelectedNodeRun = ({
  nodeRunIndex,
  setNodeRunIndex,
  arrayRunNodeLength,
  nodeName
}: {
  nodeRunIndex: number
  setNodeRunIndex: Dispatch<SetStateAction<number>>
  arrayRunNodeLength: number
  nodeName: string
}) => {
  return (
    <div className='flex items-center justify-between mb-2 ml-1'>
      <div className="text-blue-500"> {nodeName}</div>
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
    </div>
  )
}
