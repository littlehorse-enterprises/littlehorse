import { memo } from "react"
import { Handle, Position, type NodeProps } from "reactflow"
import { Play } from "lucide-react"

function StartNode({ data, selected }: NodeProps) {
  return (
    <div
      className={`flex items-center justify-center rounded-full border ${selected ? "border-blue-500 shadow-md" : "border-green-500"} bg-green-100 px-4 py-2 shadow-sm`}
    >
      <Play className="mr-1 h-4 w-4 text-green-700" />
      <div className="font-medium text-green-700">{data.label}</div>
      <Handle type="source" position={Position.Bottom} className="!bg-green-500" />
    </div>
  )
}

export default memo(StartNode)
