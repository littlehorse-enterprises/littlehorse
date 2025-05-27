import { memo } from "react"
import { Handle, Position, type NodeProps } from "reactflow"
import { Square } from "lucide-react"

function EndNode({ data, selected }: NodeProps) {
  return (
    <div
      className={`flex items-center justify-center rounded-full border ${selected ? "border-blue-500 shadow-md" : "border-red-500"} bg-red-100 px-4 py-2 shadow-sm`}
    >
      <Square className="mr-1 h-4 w-4 fill-red-700 text-red-700" />
      <div className="font-medium text-red-700">{data.label}</div>
      <Handle type="target" position={Position.Top} className="!bg-red-500" />
    </div>
  )
}

export default memo(EndNode)
